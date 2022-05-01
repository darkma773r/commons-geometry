/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.geometry.euclidean.threed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.geometry.core.partitioning.HyperplaneLocation;
import org.apache.commons.geometry.euclidean.ConvexHull;
import org.apache.commons.geometry.euclidean.EuclideanCollections;
import org.apache.commons.geometry.euclidean.threed.line.Line3D;
import org.apache.commons.geometry.euclidean.threed.line.Lines3D;
import org.apache.commons.geometry.euclidean.threed.line.Segment3D;
import org.apache.commons.numbers.core.Precision;

public final class ConvexHull3D implements ConvexHull<Vector3D> {

    /** List of vertices on the convex hull. */
    private final List<Vector3D> vertices;

    /** Region representing the convex hull; may be null */
    private final ConvexVolume region;

    private ConvexHull3D(final List<Vector3D> vertices, final ConvexVolume region) {
        this.vertices = vertices;
        this.region = region;
    }

    /** {@inheritDoc} */
    @Override
    public List<Vector3D> getVertices() {
        return vertices;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasSize() {
        return region != null;
    }

    /** {@inheritDoc} */
    @Override
    public ConvexVolume getRegion() {
        return region;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName())
            .append("[vertices= ")
            .append(getVertices())
            .append(", region= ")
            .append(getRegion())
            .append(']');

        return sb.toString();
    }

    public static Builder builder(final Precision.DoubleEquivalence precision) {
        return new Builder(precision);
    }

    public static final class Builder {

        /** Precision used to determine floating point equality. */
        private final Precision.DoubleEquivalence precision;

        /** List of points added to the builder. */
        private final List<Vector3D> pointList = new ArrayList<>();

        private Builder(final Precision.DoubleEquivalence precision) {
            this.precision = precision;
        }

        public Builder add(final Vector3D pt) {
            pointList.add(pt);
            return this;
        }

        public Builder add(final Collection<? extends Vector3D> pts) {
            pointList.addAll(pts);
            return this;
        }

        public Builder add(final PlaneConvexSubset subset) {
            if (!subset.isFinite()) {
                throw new IllegalArgumentException("Cannot construct convex hull with non-finite plane subset: " +
                        subset);
            }
            return add(subset.getVertices());
        }

        public Builder add(final BoundarySource3D boundarySource) {
            boundarySource.boundaryStream().forEach(this::add);
            return this;
        }

        public ConvexHull3D build() {
            if (pointList.isEmpty()) {
                throw new IllegalStateException("Cannot construct convex hull: no points provided");
            }

            final List<Vector3D> minMaxVertices = getMinMaxVertices(pointList);

            // TODO: remove
            System.out.println("MinMax vertices: " + minMaxVertices);

            final Segment3D segment = getMaxLineSegment(minMaxVertices);
            if (segment == null) {
                // no non-zero line segment, meaning that all points are equivalent;
                // return a representative point
                return new ConvexHull3D(Collections.singletonList(minMaxVertices.get(0)), null);
            }

            // TODO: remove
            System.out.println("Segment: " + segment);

            final Triangle3D triangle = getMaxTriangle(segment, minMaxVertices);
            if (triangle == null) {
                // TODO: return linear hull
                throw new UnsupportedOperationException("TODO: linear hull");
            }

            System.out.println("Triangle: " + triangle);

            final List<Triangle3D> tetrahedron = getMaxTetrahedron(triangle, minMaxVertices);
            if (tetrahedron == null) {
                // TODO: return planar hull
                throw new UnsupportedOperationException("TODO: planar hull");
            }

            System.out.println("Tetrahedron:");
            tetrahedron.forEach(t -> System.out.println("    " + t));

            final Quickhull3D quickhull = new Quickhull3D(precision);
            for (final Triangle3D facet : tetrahedron) {
                quickhull.add(facet);
            }

            for (final Vector3D pt : pointList) {
                quickhull.addOutsidePoint(pt);
            }

            return quickhull.build();
        }

        private List<Vector3D> getMinMaxVertices(final Collection<? extends Vector3D> points) {
            Vector3D minX = null;
            Vector3D maxX = null;

            Vector3D minY = null;
            Vector3D maxY = null;

            Vector3D minZ = null;
            Vector3D maxZ = null;

            for (final Vector3D pt : points) {
                if (minX == null || pt.getX() < minX.getX()) {
                    minX = pt;
                }
                if (maxX == null || pt.getX() > maxX.getX()) {
                    maxX = pt;
                }

                if (minY == null || pt.getY() < minY.getY()) {
                    minY = pt;
                }
                if (maxY == null || pt.getY() > maxY.getY()) {
                    maxY = pt;
                }

                if (minZ == null || pt.getZ() < minZ.getZ()) {
                    minZ = pt;
                }
                if (maxZ == null || pt.getZ() > maxZ.getZ()) {
                    maxZ = pt;
                }
            }

            return Arrays.asList(minX, maxX, minY, maxY, minZ, maxZ).stream()
                    .distinct()
                    .collect(Collectors.toList());
        }

        private Segment3D getMaxLineSegment(final List<Vector3D> pts) {
            final int size = pts.size();

            Vector3D p1 = null;
            Vector3D p2 = null;
            double maxDist = 0d;

            // find the pair of points that are most distant from
            // each other and construct a line between them
            for (int i = 0; i < size - 1; ++i) {
                for (int j = i + 1; j < size; ++j) {
                    final Vector3D pi = pts.get(i);
                    final Vector3D pj = pts.get(j);

                    final double dist = pi.distance(pj);
                    if (dist > maxDist) {
                        maxDist = dist;

                        p1 = pi;
                        p2 = pj;
                    }
                }
            }

            if (p1 != null && !p1.eq(p2, precision)) {
                pts.remove(p1);
                pts.remove(p2);

                return Lines3D.segmentFromPoints(p1, p2, precision);
            }

            return null;
        }

        private Triangle3D getMaxTriangle(final Segment3D segment, final List<Vector3D> pts) {
            Vector3D p3 = null;
            double maxDist = 0d;

            final Line3D line = segment.getLine();
            for (final Vector3D pt : pts) {
                final double dist = line.distance(pt);
                if (dist > maxDist) {
                    maxDist = dist;

                    p3 = pt;
                }
            }

            if (p3 != null && !line.contains(p3)) {
                pts.remove(p3);

                return Planes.triangleFromVertices(
                        segment.getStartPoint(),
                        segment.getEndPoint(),
                        p3,
                        precision);
            }

            return null;
        }

        private List<Triangle3D> getMaxTetrahedron(final Triangle3D triangle, final List<Vector3D> pts) {
            Vector3D p4 = null;

            double maxPlaneDist = 0d;
            double maxAbsPlaneDist = 0d;

            for (final Vector3D pt : pts) {
                final double dist = triangle.getPlane().offset(pt);
                final double absDist = Math.abs(dist);
                if (absDist > maxAbsPlaneDist) {
                    maxPlaneDist = dist;
                    maxAbsPlaneDist = absDist;

                    p4 = pt;
                }
            }

            final Triangle3D base = maxPlaneDist < 0d ?
                    triangle.reverse() :
                    triangle;

            if (p4 != null && !base.getPlane().contains(p4)) {
                return Arrays.asList(
                        base,
                        Planes.triangleFromVertices(base.getPoint1(), base.getPoint2(), p4, precision),
                        Planes.triangleFromVertices(base.getPoint2(), base.getPoint3(), p4, precision),
                        Planes.triangleFromVertices(base.getPoint3(), base.getPoint1(), p4, precision));
            }

            return null;
        }
    }

    private static final class Quickhull3D {

        private final Precision.DoubleEquivalence precision;

        private final Map<Vector3D, List<QuickhullFacet3D>> facetsByEdgeStart;

        private final Set<QuickhullFacet3D> facets = new HashSet<>();

        private final List<QuickhullFacet3D> added = new ArrayList<>();

        private final List<QuickhullFacet3D> removed = new ArrayList<>();

        private final Set<QuickhullFacet3D> visited = new HashSet<>();

        Quickhull3D(final Precision.DoubleEquivalence precision) {
            this.precision = precision;
            this.facetsByEdgeStart = EuclideanCollections.pointMap3D(precision);
        }

        public void add(final Triangle3D triangle) {
            add(new QuickhullFacet3D(triangle));
        }

        public void addOutsidePoint(final Vector3D pt) {
            for (final QuickhullFacet3D facet : facets) {
                if (facet.addOutside(pt)) {
                    break;
                }
            }
        }

        /** Build and return the convex hull instance.
         * @return convex hull
         */
        public ConvexHull3D build() {
            while (process()) {
                // process until done
            }

            final List<Plane> planes = new ArrayList<>();
            final Set<Vector3D> vertices = EuclideanCollections.pointSet3D(precision);

            for (final QuickhullFacet3D facet : facets) {
                planes.add(facet.triangle.getPlane());
                vertices.addAll(facet.triangle.getVertices());
            }

            return new ConvexHull3D(new ArrayList<>(vertices), ConvexVolume.fromBounds(planes));
        }

        /** Process the set of quick hull facets, returning {@code true} if an unprocessed
         * facet was processed.
         * @return {@code true} if a facet was processed, otherwise {@code false}
         */
        public boolean process() {
            final QuickhullFacet3D toProcess = nextFacetForProcessing();
            if (toProcess != null) {
                process(toProcess);
                return true;
            }
            return false;
        }

        private QuickhullFacet3D nextFacetForProcessing() {
            final Iterator<QuickhullFacet3D> it = facets.iterator();
            while (it.hasNext()) {
                final QuickhullFacet3D facet = it.next();
                if (facet.requiresProcessing()) {
                    it.remove();
                    return facet;
                }
            }
            return null;
        }

        private void process(final QuickhullFacet3D facet) {
            final Vector3D newVertex = facet.farthestOutsidePoint;

            removed.clear();
            added.clear();
            visited.clear();

            // TODO: actually process the facet

        }

        private void expandRecursive(final Vector3D vertex, final QuickhullFacet3D facet) {
            if (facet != null && visited.add(facet)) {
                if (facet.triangle.getPlane().classify(vertex) == HyperplaneLocation.PLUS) {
                    // this facet is hidden
                    removed.add(facet);

                    // visit the edges in order to find an edge
                    expandAlongEdgeRecursive(vertex, facet, facet.edge1.reverse());
                    expandAlongEdgeRecursive(vertex, facet, facet.edge2.reverse());
                    expandAlongEdgeRecursive(vertex, facet, facet.edge3.reverse());
                } else {

                }
            }
        }

        private void expandAlongEdgeRecursive(
                final Vector3D vertex,
                final QuickhullFacet3D neighborFacet,
                final Edge edge) {
            // TODO: see if the facet is visible and if not, add the edge as a new facet
        }

        private void add(final QuickhullFacet3D facet) {
            facets.add(facet);

            addEdge(facet, facet.triangle.getPoint1());
            addEdge(facet, facet.triangle.getPoint2());
            addEdge(facet, facet.triangle.getPoint3());
        }

        private void remove(final QuickhullFacet3D facet) {
            facets.remove(facet);

            removeEdge(facet, facet.triangle.getPoint1());
            removeEdge(facet, facet.triangle.getPoint2());
            removeEdge(facet, facet.triangle.getPoint3());
        }

        private void addEdge(final QuickhullFacet3D facet, final Vector3D edgeStart) {
            facetsByEdgeStart.computeIfAbsent(edgeStart, k -> new ArrayList<>())
                .add(facet);
        }

        private void removeEdge(final QuickhullFacet3D facet, final Vector3D edgeStart) {
            final List<QuickhullFacet3D> edgeFacets = facetsByEdgeStart.get(edgeStart);
            if (edgeFacets != null) {
                edgeFacets.remove(facet);

                if (edgeFacets.isEmpty()) {
                    facetsByEdgeStart.remove(edgeStart);
                }
            }
        }
    }

    private static final class QuickhullFacet3D {

        private final Triangle3D triangle;

        private final Edge edge1;

        private final Edge edge2;

        private final Edge edge3;

        private final List<Vector3D> outsidePoints = new ArrayList<>();

        private Vector3D farthestOutsidePoint;

        private double farthestOutsideDistance;

        QuickhullFacet3D(final Triangle3D triangle) {
            this.triangle = triangle;

            this.edge1 = new Edge(triangle.getPoint1(), triangle.getPoint2());
            this.edge2 = new Edge(triangle.getPoint2(), triangle.getPoint3());
            this.edge3 = new Edge(triangle.getPoint3(), triangle.getPoint1());
        }

        public boolean requiresProcessing() {
            return farthestOutsidePoint != null;
        }

        public boolean addOutside(final Vector3D pt) {
            final Plane plane = triangle.getPlane();

            final double dist = plane.offset(pt);
            if (plane.getPrecision().gt(dist, 0d)) {
                outsidePoints.add(pt);

                if (dist > farthestOutsideDistance) {
                    farthestOutsideDistance = dist;
                    farthestOutsidePoint = pt;
                }

                return true;
            }
            return false;
        }
    }

    private static final class Edge {

        final Vector3D start;

        final Vector3D end;

        Edge(final Vector3D start, final Vector3D end) {
            this.start = start;
            this.end = end;
        }

        public Edge reverse() {
            return new Edge(end, start);
        }

        /** {@inheritDoc} */
        @Override
        public int hashCode() {
            return Objects.hash(start, end);
        }

        /** {@inheritDoc} */
        @Override
        public boolean equals(final Object obj) {
            if (!(obj instanceof Edge)) {
                return false;
            }

            final Edge other = (Edge) obj;
            return start.equals(other.start) &&
                    end.equals(other.end);
        }
    }
}

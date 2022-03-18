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
package org.apache.commons.geometry.hull.euclidean.threed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.geometry.core.partitioning.HyperplaneLocation;
import org.apache.commons.geometry.euclidean.threed.ConvexVolume;
import org.apache.commons.geometry.euclidean.threed.Plane;
import org.apache.commons.geometry.euclidean.threed.Planes;
import org.apache.commons.geometry.euclidean.threed.Triangle3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.line.Lines3D;
import org.apache.commons.geometry.euclidean.threed.line.Segment3D;
import org.apache.commons.numbers.core.Precision;

public class Quickhull3D implements ConvexHullGenerator3D {

    /** Object used to determine floating point equality. */
    private final Precision.DoubleEquivalence precision;

    public Quickhull3D(final Precision.DoubleEquivalence precision) {
        this.precision = precision;
    }

    /** {@inheritDoc} */
    @Override
    public ConvexHull3D generate(final Collection<? extends Vector3D> points) {
        if (points.isEmpty()) {
            return new ConvexHull3D(Collections.emptyList(), null);
        }

        final List<Vector3D> minMaxVertices = getMinMaxVertices(points);
        final Segment3D segment = getMaxLine(minMaxVertices);
        if (segment == null) {
            // no non-zero line segment, meaning that all points are equivalent;
            // return a representative point
            return new ConvexHull3D(Collections.singletonList(minMaxVertices.get(0)), null);
        }

        final Triangle3D triangle = getMaxTriangle(segment, minMaxVertices);
        if (triangle != null) {
            // TODO: return linear hull
            return null;
        }

        final List<Triangle3D> tetrahedron = getMaxTetrahedron(triangle, minMaxVertices);
        if (tetrahedron == null) {
            // TODO: return planar hull
            return null;
        }

        final QuickhullBuilder builder = new QuickhullBuilder();
        for (final Triangle3D facet : tetrahedron) {
            builder.add(facet);
        }

        for (final Vector3D pt : points) {
            builder.addOutsidePoint(pt);
        }

        return null;
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

    private Segment3D getMaxLine(final List<Vector3D> pts) {
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

        for (final Vector3D pt : pts) {
            final double dist = segment.getLine().distance(pt);
            if (dist > maxDist) {
                maxDist = dist;

                p3 = pt;
            }
        }

        if (p3 != null && !segment.contains(p3)) {
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

    private boolean pointsEq(final Vector3D a, final Vector3D b) {
        return a.eq(b, precision);
    }

    private final class QuickhullBuilder {

        private final Map<Edge, QuickhullFacet3D> facetsByEdge = new HashMap<>();

        private final Set<QuickhullFacet3D> facets = new HashSet<>();

        private final List<QuickhullFacet3D> added = new ArrayList<>();

        private final List<QuickhullFacet3D> removed = new ArrayList<>();

        private final Set<QuickhullFacet3D> visited = new HashSet<>();

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

        public ConvexHull3D build() {
            while (process()) {
                // process until done
            }

            // TODO: take the vertices from the convex volume
            final List<Vector3D> vertices = null;

            final List<Plane> planes = facets.stream()
                    .map(f -> f.triangle.getPlane())
                    .collect(Collectors.toList());

            return new ConvexHull3D(vertices, ConvexVolume.fromBounds(planes));
        }

        public boolean process() {
            for (final QuickhullFacet3D facet : facets) {
                if (facet.requiresProcessing()) {
                    process(facet);
                }
                return true;
            }
            return false;
        }

        private void process(final QuickhullFacet3D facet) {
            final Vector3D newVertex = facet.farthestOutsidePoint;

            added.clear();
            removed.clear();
            visited.clear();
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

    private final class QuickhullFacet3D {

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
            final double dist = triangle.getPlane().offset(pt);
            if (dist > 0.0) {
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

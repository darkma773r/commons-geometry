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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.ToDoubleFunction;

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.euclidean.AbstractBounds;
import org.apache.commons.geometry.euclidean.threed.line.Line3D;
import org.apache.commons.geometry.euclidean.threed.line.LineConvexSubset3D;
import org.apache.commons.geometry.euclidean.threed.line.LinecastPoint3D;
import org.apache.commons.geometry.euclidean.threed.line.Linecastable3D;
import org.apache.commons.geometry.euclidean.threed.line.Segment3D;
import org.apache.commons.geometry.euclidean.threed.shape.Parallelepiped;
import org.apache.commons.numbers.core.Precision;

/** Class containing minimum and maximum points defining a 3D axis-aligned bounding box. Unless otherwise
 * noted, floating point comparisons used in this class are strict, meaning that values are considered equal
 * if and only if they match exactly.
 *
 * <p>Instances of this class are guaranteed to be immutable.</p>
 */
public final class Bounds3D extends AbstractBounds<Vector3D, Bounds3D>
    implements Linecastable3D {

    /** Simple constructor. Callers are responsible for ensuring the min is not greater than max.
     * @param min minimum point
     * @param max maximum point
     */
    private Bounds3D(final Vector3D min, final Vector3D max) {
        super(min, max);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasSize(final Precision.DoubleEquivalence precision) {
        final Vector3D diag = getDiagonal();

        return !precision.eqZero(diag.getX()) &&
                !precision.eqZero(diag.getY()) &&
                !precision.eqZero(diag.getZ());
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(final Vector3D pt) {
        final double x = pt.getX();
        final double y = pt.getY();
        final double z = pt.getZ();

        final Vector3D min = getMin();
        final Vector3D max = getMax();

        return x >= min.getX() && x <= max.getX() &&
                y >= min.getY() && y <= max.getY() &&
                z >= min.getZ() && z <= max.getZ();
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(final Vector3D pt, final Precision.DoubleEquivalence precision) {
        final double x = pt.getX();
        final double y = pt.getY();
        final double z = pt.getZ();

        final Vector3D min = getMin();
        final Vector3D max = getMax();

        return precision.gte(x, min.getX()) && precision.lte(x, max.getX()) &&
                precision.gte(y, min.getY()) && precision.lte(y, max.getY()) &&
                precision.gte(z, min.getZ()) && precision.lte(z, max.getZ());
    }

    /** {@inheritDoc} */
    @Override
    public boolean intersects(final Bounds3D other) {
        final Vector3D aMin = getMin();
        final Vector3D aMax = getMax();

        final Vector3D bMin = other.getMin();
        final Vector3D bMax = other.getMax();

        return aMin.getX() <= bMax.getX() && aMax.getX() >= bMin.getX() &&
                aMin.getY() <= bMax.getY() && aMax.getY() >= bMin.getY() &&
                aMin.getZ() <= bMax.getZ() && aMax.getZ() >= bMin.getZ();
    }

    /** {@inheritDoc} */
    @Override
    public Bounds3D intersection(final Bounds3D other) {
        if (intersects(other)) {
            final Vector3D aMin = getMin();
            final Vector3D aMax = getMax();

            final Vector3D bMin = other.getMin();
            final Vector3D bMax = other.getMax();

            // get the max of the mins and the mins of the maxes
            final double minX = Math.max(aMin.getX(), bMin.getX());
            final double minY = Math.max(aMin.getY(), bMin.getY());
            final double minZ = Math.max(aMin.getZ(), bMin.getZ());

            final double maxX = Math.min(aMax.getX(), bMax.getX());
            final double maxY = Math.min(aMax.getY(), bMax.getY());
            final double maxZ = Math.min(aMax.getZ(), bMax.getZ());

            return new Bounds3D(
                    Vector3D.of(minX, minY, minZ),
                    Vector3D.of(maxX, maxY, maxZ));
        }

        return null; // no intersection
    }

    /** Return {@code true} if the region represented by this instance shares any points with
     * the given line. Floating point comparisons are made using the
     * {@link Line3D#getPrecision() precision} of the line.
     * @param line line to determine intersection with
     * @return {@code true} if the region represented by this instance intersects
     *      the given line
     */
    public boolean intersects(final Line3D line) {
        return intersects(line.span());
    }

    /** Return {@code true} if the region represented by this instance shares any points with
     * the given line convex subset. Floating point comparisons are made using the
     * {@link Line3D#getPrecision() precision} of the subset's line.
     * @param subset line convex subset to determine intersection with
     * @return {@code true} if the region represented by this instance intersects
     *      the given line convex subset
     */
    public boolean intersects(final LineConvexSubset3D subset) {
        return new Linecaster(subset).intersectsRegion();
    }

    /** Return a {@link Segment3D} representing the intersection of the region
     * represented by this instance with the given line or {@code null} if no such
     * intersection exists. Floating point comparisons are made using the
     * {@link Line3D#getPrecision() precision} of the line.
     * @param line line to intersect with
     * @return {@link Segment3D} representing the intersection of the region
     *      represented by this instance with the given line or {@code null}
     *      if no such intersection exists
     */
    public Segment3D intersection(final Line3D line) {
        return intersection(line.span());
    }

    /** Return a {@link Segment3D} representing the intersection of the region
     * represented by this instance with the given line convex subset or {@code null}
     * if no such intersection exists. Floating point comparisons are made using the
     * {@link Line3D#getPrecision() precision} of the subset's line.
     * @param subset line convex subset to intersect with
     * @return {@link Segment3D} representing the intersection of the region
     *      represented by this instance with the given line convex subset or {@code null}
     *      if no such intersection exists
     */
    public Segment3D intersection(final LineConvexSubset3D subset) {
        return new Linecaster(subset).getRegionIntersection();
    }

    /** {@inheritDoc} */
    @Override
    public List<LinecastPoint3D> linecast(final LineConvexSubset3D subset) {
        return new Linecaster(subset).getBoundaryIntersections();
    }

    /** {@inheritDoc} */
    @Override
    public LinecastPoint3D linecastFirst(final LineConvexSubset3D subset) {
        return new Linecaster(subset).getFirstBoundaryIntersection();
    }

    /** {@inheritDoc}
     *
     * @throws IllegalArgumentException if any dimension of the bounding box is zero
     *      as evaluated by the given precision context
     */
    @Override
    public Parallelepiped toRegion(final Precision.DoubleEquivalence precision) {
        return Parallelepiped.axisAligned(getMin(), getMax(), precision);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(getMin(), getMax());
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof Bounds3D)) {
            return false;
        }

        final Bounds3D other = (Bounds3D) obj;

        return getMin().equals(other.getMin()) &&
                getMax().equals(other.getMax());
    }

    /** Construct a new instance from the given points.
     * @param first first point
     * @param more additional points
     * @return a new instance containing the min and max coordinates values from the input points
     */
    public static Bounds3D from(final Vector3D first, final Vector3D... more) {
        final Builder builder = builder();

        builder.add(first);
        builder.addAll(Arrays.asList(more));

        return builder.build();
    }

    /** Construct a new instance from the given points.
     * @param points input points
     * @return a new instance containing the min and max coordinates values from the input points
     */
    public static Bounds3D from(final Iterable<Vector3D> points) {
        final Builder builder = builder();

        builder.addAll(points);

        return builder.build();
    }

    /** Construct a new {@link Builder} instance for creating bounds.
     * @return a new builder instance for creating bounds
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Class used to construct {@link Bounds3D} instances.
     */
    public static final class Builder {

        /** Minimum x coordinate. */
        private double minX = Double.POSITIVE_INFINITY;

        /** Minimum y coordinate. */
        private double minY = Double.POSITIVE_INFINITY;

        /** Minimum z coordinate. */
        private double minZ = Double.POSITIVE_INFINITY;

        /** Maximum x coordinate. */
        private double maxX = Double.NEGATIVE_INFINITY;

        /** Maximum y coordinate. */
        private double maxY = Double.NEGATIVE_INFINITY;

        /** Maximum z coordinate. */
        private double maxZ = Double.NEGATIVE_INFINITY;

        /** Private constructor; instantiate through factory method. */
        private Builder() { }

        /** Add a point to this instance.
         * @param pt point to add
         * @return this instance
         */
        public Builder add(final Vector3D pt) {
            final double x = pt.getX();
            final double y = pt.getY();
            final double z = pt.getZ();

            minX = Math.min(x, minX);
            minY = Math.min(y, minY);
            minZ = Math.min(z, minZ);

            maxX = Math.max(x, maxX);
            maxY = Math.max(y, maxY);
            maxZ = Math.max(z, maxZ);

            return this;
        }

        /** Add a collection of points to this instance.
         * @param pts points to add
         * @return this instance
         */
        public Builder addAll(final Iterable<? extends Vector3D> pts) {
            for (final Vector3D pt : pts) {
                add(pt);
            }

            return this;
        }

        /** Add the min and max points from the given bounds to this instance.
         * @param bounds bounds containing the min and max points to add
         * @return this instance
         */
        public Builder add(final Bounds3D bounds) {
            add(bounds.getMin());
            add(bounds.getMax());

            return this;
        }

        /** Return true if this builder contains valid min and max coordinate values.
         * @return true if this builder contains valid min and max coordinate values
         */
        public boolean hasBounds() {
            return Double.isFinite(minX) &&
                    Double.isFinite(minY) &&
                    Double.isFinite(minZ) &&
                    Double.isFinite(maxX) &&
                    Double.isFinite(maxY) &&
                    Double.isFinite(maxZ);
        }

        /** Create a new {@link Bounds3D} instance from the values in this builder.
         * The builder can continue to be used to create other instances.
         * @return a new bounds instance
         * @throws IllegalStateException if no points were given to the builder or any of the computed
         *      min and max coordinate values are NaN or infinite
         * @see #hasBounds()
         */
        public Bounds3D build() {
            final Vector3D min = Vector3D.of(minX, minY, minZ);
            final Vector3D max = Vector3D.of(maxX, maxY, maxZ);

            if (!hasBounds()) {
                if (Double.isInfinite(minX) && minX > 0 &&
                        Double.isInfinite(maxX) && maxX < 0) {
                    throw new IllegalStateException("Cannot construct bounds: no points given");
                }

                throw new IllegalStateException("Invalid bounds: min= " + min + ", max= " + max);
            }

            return new Bounds3D(min, max);
        }
    }

    /** Internal enum containing general dimension information.
     */
    private enum Dimension {
        /** X axis dimension. */
        X(Vector3D::getX, Vector3D.Unit.MINUS_X, Vector3D.Unit.PLUS_X),

        /** Y axis dimension. */
        Y(Vector3D::getY, Vector3D.Unit.MINUS_Y, Vector3D.Unit.PLUS_Y),

        /** Z axis dimension. */
        Z(Vector3D::getZ, Vector3D.Unit.MINUS_Z, Vector3D.Unit.PLUS_Z);

        /** Function used to extract the coordinate value from a vector instance. */
        private final ToDoubleFunction<Vector3D> coordinateFn;

        /** Minus direction. */
        private final Vector3D.Unit minus;

        /** Plus direction. */
        private final Vector3D.Unit plus;

        Dimension(
                final ToDoubleFunction<Vector3D> coordinateFn,
                final Vector3D.Unit minus,
                final Vector3D.Unit plus) {
            this.coordinateFn = coordinateFn;
            this.minus = minus;
            this.plus = plus;
        }

        /** Get the minus direction for the dimension.
         * @return minus direction for the dimension
         */
        public Vector3D.Unit getMinus() {
            return minus;
        }

        /** Get the plus direction for the dimension.
         * @return plus direction for the dimension
         */
        public Vector3D.Unit getPlus() {
            return plus;
        }

        /** Get the dimension coordinate value from the argument.
         * @param pt point to get the coordinate from
         * @return dimension coordinate value
         */
        public double get(final Vector3D pt) {
            return coordinateFn.applyAsDouble(pt);
        }
    }

    /** Internal class used to perform linecast and line intersection operations using the "slabs" algorithm
     * (https://education.siggraph.org/static/HyperGraph/raytrace/rtinter3.htm). Floating
     * point comparisons use the precision of the intersecting line.
     */
    private final class Linecaster {

        /** Maximum number of line intersections possible. */
        private static final int MAX_LINECAST_INTERSECTIONS = 6;

        /** Line convex subset to be tested against the bounds. */
        private final LineConvexSubset3D subset;

        /** Line instance for the subset being tested. */
        private final Line3D line;

        /** Precision context for the intersection operation. */
        private final Precision.DoubleEquivalence precision;

        /** Near slab intersection abscissa value. */
        private double near = Double.NEGATIVE_INFINITY;

        /** Far slab intersection abscissa value. */
        private double far = Double.POSITIVE_INFINITY;

         /** Construct a new instance for computing bounds intersection information with
          * the given line convex subset.
          * @param subset line convex subset to compute intersection information for
          */
        Linecaster(final LineConvexSubset3D subset) {
            this.subset = subset;
            this.line = subset.getLine();
            this.precision = line.getPrecision();
        }

        /** Return {@code true} if the line convex subset shares any points with the
         * bounding box.
         * @return {@code true} if the line convex subset shares any points with the
         *      bounding box
         */
        public boolean intersectsRegion() {
            return computeNearFar() &&
                    precision.gte(subset.getSubspaceEnd(), near) &&
                    precision.lte(subset.getSubspaceStart(), far);
        }

        /** Get the {@link Segment3D} containing all points shared by the line convex
         * subset and the bounding box, or {@code null} if no points are shared.
         * @return segment containing all points shared by the line convex
         *      subset and the bounding box, or {@code null} if no points are shared.
         */
        public Segment3D getRegionIntersection() {
            if (intersectsRegion()) {
                final double start = Math.max(near, subset.getSubspaceStart());
                final double end = Math.min(far, subset.getSubspaceEnd());

                return line.segment(start, end);
            }
            return null;
        }

        /** Get a list of {@link LinecastPoint3D} instances representing the intersections of
         * the line convex subset with the faces of the bounding box. An empty list is returned
         * if no such intersections exist.
         * @return list of {@link LinecastPoint3D} instances representing the intersections of
         *      the line convex subset with the faces of the bounding box
         */
        public List<LinecastPoint3D> getBoundaryIntersections() {
            if (computeNearFar()) {
                final List<LinecastPoint3D> results = new ArrayList<>(MAX_LINECAST_INTERSECTIONS);

                addIntersections(near, results);
                if (!precision.eq(near, far)) {
                    addIntersections(far, results);
                }

                results.sort(LinecastPoint3D.ABSCISSA_ORDER);

                return results;
            }

            return Collections.emptyList();
        }

        /** Get a {@link LinecastPoint3D} representing the <em>first</em> intersection of the
         * line convex subset with the faces of the bounding box, where points are placed in
         * ordered of increasing abscissa value. Null is returned if no such point exists.
         * @return {@link LinecastPoint3D} representing the first intersection of the
         *      line convex subset with the faces of the bounding box, or {@code null} if no
         *      such point exists
         */
        public LinecastPoint3D getFirstBoundaryIntersection() {
            final List<LinecastPoint3D> results = getBoundaryIntersections();
            return results.isEmpty() ?
                    null :
                    results.get(0);
        }

        /** Add {@link LinecastPoint3D} instances to {@code results} for any bounding box faces
         * that contain the point on the line at {@code abscissa}.
         * @param abscissa line abscissa
         * @param results list containing linecast results
         */
        private void addIntersections(final double abscissa, final List<LinecastPoint3D> results) {
            if (containsAbscissa(abscissa)) {
                final Vector3D pt = line.toSpace(abscissa);

                addIntersectionIfPresent(pt, Dimension.X, results);
                addIntersectionIfPresent(pt, Dimension.Y, results);
                addIntersectionIfPresent(pt, Dimension.Z, results);
            }
        }

        /** Add a {@link LinecastPoint3D} instance to {@code results} if the given point lies on
         * one of the bounding box faces orthogonal to {@code dim}.
         * @param pt potential face intersection point
         * @param dim dimension to test
         * @param results list containing linecast results
         */
        private void addIntersectionIfPresent(
                final Vector3D pt,
                final Dimension dim,
                final List<LinecastPoint3D> results) {

            // only include linecast results for dimensions that are not considered
            // parallel to the line, according to the line precision
            if (!precision.eqZero(line.getDirection().dot(dim.getPlus()))) {
                final double coordinate = dim.get(pt);
                final double dimMin = dim.get(getMin());
                final double dimMax = dim.get(getMax());

                if (precision.eq(coordinate, dimMin)) {
                    results.add(new LinecastPoint3D(pt, dim.getMinus(), line));
                }

                if (precision.eq(coordinate, dimMax)) {
                    results.add(new LinecastPoint3D(pt, dim.getPlus(), line));
                }
            }
        }

        /** Compute the {@code near} and {@code far} slab intersection values for the
         * line under test, returning {@code true} if the line intersects the bounding
         * box.
         * @return {@code true} if the line intersects the bounding box
         */
        private boolean computeNearFar() {
            return updateNearFar(Dimension.X) &&
                    updateNearFar(Dimension.Y) &&
                    updateNearFar(Dimension.Z);
        }

        /** Update the {@code near} and {@code far} slab intersection points with the
         * intersection values for the planes orthogonal to {@code dim}, returning
         * {@code false} if the line is determined to not intersect the bounding box.
         * @param dim dimension to compute
         * @return {@code false} if the line is determined to not intersect the bounding
         *      box
         */
        private boolean updateNearFar(final Dimension dim) {
            final double dir = dim.get(line.getDirection());
            final double origin = dim.get(line.getOrigin());

            final double min = dim.get(getMin());
            final double max = dim.get(getMax());

            double t1 = (min - origin) / dir;
            double t2 = (max - origin) / dir;

            if (!Double.isFinite(t1) || !Double.isFinite(t2)) {
                // the line is parallel to this dimension; only continue if the
                // line origin lies between the min and max for this dimension
                return precision.gte(origin, min) && precision.lte(origin, max);
            }

            if (t1 > t2) {
                final double temp = t1;
                t1 = t2;
                t2 = temp;
            }

            if (t1 > near) {
                near = t1;
            }

            if (t2 < far) {
                far = t2;
            }

            return precision.lte(near, far);
        }

        /** Return {@code true} if the line convex subset contains the given abscissa value.
         * @param abscissa abscissa to test
         * @return {@code true} if the line convex subset contains the given abscissa value
         */
        private boolean containsAbscissa(final double abscissa) {
            return subset.classifyAbscissa(abscissa) != RegionLocation.OUTSIDE;
        }
    }
}

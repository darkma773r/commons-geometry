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

    /** Return {@code true} if the region represented by this instance intersects
     * the given line.
     * @param line line to determine intersection with
     * @return {@code true} if the region represented by this instance intersects
     *      the given line
     */
    public boolean intersects(final Line3D line) {
        return intersects(line.span());
    }

    /** Return {@code true} if the region represented by this instance intersects
     * the given line convex subset.
     * @param subset line convex subset to determine intersection with
     * @return {@code true} if the region represented by this instance intersects
     *      the given line convex subset
     */
    public boolean intersects(final LineConvexSubset3D subset) {
        return new Linecaster(subset).intersectsRegion();
    }

    /** Return a {@link Segment3D} representing the intersection of the region
     * represented by this instance with the given line or {@code null} if no such
     * intersection exists.
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
     * if no such intersection exists.
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

    private enum Dimension {
        X(1, Vector3D::getX, Vector3D.Unit.MINUS_X, Vector3D.Unit.PLUS_X),
        Y(1 << 1, Vector3D::getY, Vector3D.Unit.MINUS_Y, Vector3D.Unit.PLUS_Y),
        Z(1 << 2, Vector3D::getZ, Vector3D.Unit.MINUS_Z, Vector3D.Unit.PLUS_Z);

        private final int id;

        private final ToDoubleFunction<Vector3D> coordinateFn;

        private final Vector3D minus;

        private final Vector3D plus;

        Dimension(
                final int id,
                final ToDoubleFunction<Vector3D> coordinateFn,
                final Vector3D minus,
                final Vector3D plus) {
            this.id = id;
            this.coordinateFn = coordinateFn;
            this.minus = minus;
            this.plus = plus;
        }

        public int getId() {
            return id;
        }

        public Vector3D getMinus() {
            return minus;
        }

        public Vector3D getPlus() {
            return plus;
        }

        public double get(final Vector3D pt) {
            return coordinateFn.applyAsDouble(pt);
        }

        public boolean matches(final int val) {
            return (id & val) > 0;
        }
    }

    private final class Linecaster {

        private static final int MAX_INTERSECTIONS = 6;

        private final LineConvexSubset3D subset;

        private final Line3D line;

        private final Precision.DoubleEquivalence precision;

        private double near = Double.NEGATIVE_INFINITY;

        private double far = Double.POSITIVE_INFINITY;

        private int parallelDimensions;

        Linecaster(final LineConvexSubset3D subset) {
            this.subset = subset;
            this.line = subset.getLine();
            this.precision = line.getPrecision();
        }

        public boolean intersectsRegion() {
            return computeNearFar() &&
                    precision.gte(subset.getSubspaceEnd(), near) &&
                    precision.lte(subset.getSubspaceStart(), far);
        }

        public Segment3D getRegionIntersection() {
            if (intersectsRegion()) {
                final double start = Math.max(near, subset.getSubspaceStart());
                final double end = Math.min(far, subset.getSubspaceEnd());

                return line.segment(start, end);
            }
            return null;
        }

        public List<LinecastPoint3D> getBoundaryIntersections() {
            if (computeNearFar()) {
                final List<LinecastPoint3D> results = new ArrayList<>(MAX_INTERSECTIONS);

                addIntersections(near, results);
                if (!precision.eq(near, far)) {
                    addIntersections(far, results);
                }

                results.sort(LinecastPoint3D.ABSCISSA_ORDER);

                return results;
            }

            return Collections.emptyList();
        }

        public LinecastPoint3D getFirstBoundaryIntersection() {
            final List<LinecastPoint3D> results = getBoundaryIntersections();
            return results.isEmpty() ?
                    null :
                    results.get(0);
        }

        private void addIntersections(final double abscissa, final List<LinecastPoint3D> results) {
            if (containsAbscissa(abscissa)) {
                final Vector3D pt = line.toSpace(abscissa);

                addIntersectionIfPresent(pt, Dimension.X, results);
                addIntersectionIfPresent(pt, Dimension.Y, results);
                addIntersectionIfPresent(pt, Dimension.Z, results);
            }
        }

        private void addIntersectionIfPresent(
                final Vector3D pt,
                final Dimension dim,
                final List<LinecastPoint3D> results) {

            if (!dim.matches(parallelDimensions)) {
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

        private boolean computeNearFar() {
            return computeNearFar(Dimension.X) &&
                    computeNearFar(Dimension.Y) &&
                    computeNearFar(Dimension.Z);
        }

        private boolean computeNearFar(final Dimension dim) {
            final double dir = dim.get(line.getDirection());
            final double origin = dim.get(line.getOrigin());

            final double min = dim.get(getMin());
            final double max = dim.get(getMax());

            if (precision.eqZero(dir)) {
                // the line is parallel to this dimension; store this fact for
                // use when creating the line cast points
                parallelDimensions |= dim.getId();

                return precision.gte(origin, min) && precision.lte(origin, max);
            }

            double t1 = (min - origin) / dir;
            double t2 = (max - origin) / dir;

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

            if (precision.gt(near, far)) {
                return false;
            }

            return true;
        }

        private boolean containsAbscissa(final double abscissa) {
            return subset.classifyAbscissa(abscissa) != RegionLocation.OUTSIDE;
        }
    }
}

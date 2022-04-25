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
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.ToDoubleFunction;
import java.util.regex.Pattern;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.line.Line3D;
import org.apache.commons.geometry.euclidean.threed.line.LineConvexSubset3D;
import org.apache.commons.geometry.euclidean.threed.line.LinecastPoint3D;
import org.apache.commons.geometry.euclidean.threed.line.Lines3D;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.apache.commons.geometry.euclidean.threed.shape.Parallelepiped;
import org.apache.commons.numbers.angle.Angle;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class Bounds3DTest {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    private static final String NO_POINTS_MESSAGE = "Cannot construct bounds: no points given";

    private static final Pattern INVALID_BOUNDS_PATTERN =
            Pattern.compile("^Invalid bounds: min= \\([^\\)]+\\), max= \\([^\\)]+\\)");

    @Test
    void testFrom_varargs_singlePoint() {
        // arrange
        final Vector3D p1 = Vector3D.of(-1, 2, -3);

        // act
        final Bounds3D b = Bounds3D.from(p1);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(p1, b.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p1, b.getMax(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, b.getDiagonal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p1, b.getCentroid(), TEST_EPS);
    }

    @Test
    void testFrom_varargs_multiplePoints() {
        // arrange
        final Vector3D p1 = Vector3D.of(1, 6, 7);
        final Vector3D p2 = Vector3D.of(0, 5, 11);
        final Vector3D p3 = Vector3D.of(3, 6, 8);

        // act
        final Bounds3D b = Bounds3D.from(p1, p2, p3);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 5, 7), b.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(3, 6, 11), b.getMax(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(3, 1, 4), b.getDiagonal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1.5, 5.5, 9), b.getCentroid(), TEST_EPS);
    }

    @Test
    void testFrom_iterable_singlePoint() {
        // arrange
        final Vector3D p1 = Vector3D.of(-1, 2, -3);

        // act
        final Bounds3D b = Bounds3D.from(Collections.singletonList(p1));

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(p1, b.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p1, b.getMax(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, b.getDiagonal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p1, b.getCentroid(), TEST_EPS);
    }

    @Test
    void testFrom_iterable_multiplePoints() {
        // arrange
        final Vector3D p1 = Vector3D.of(1, 6, 7);
        final Vector3D p2 = Vector3D.of(2, 5, 9);
        final Vector3D p3 = Vector3D.of(3, 4, 8);

        // act
        final Bounds3D b = Bounds3D.from(Arrays.asList(p1, p2, p3));

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 4, 7), b.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(3, 6, 9), b.getMax(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(2, 2, 2), b.getDiagonal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(2, 5, 8), b.getCentroid(), TEST_EPS);
    }

    @Test
    void testFrom_iterable_noPoints() {
        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Bounds3D.from(new ArrayList<>());
        }, IllegalStateException.class, NO_POINTS_MESSAGE);
    }

    @Test
    void testFrom_invalidBounds() {
        // arrange
        final Vector3D good = Vector3D.of(1, 1, 1);

        final Vector3D nan = Vector3D.of(Double.NaN, 1, 1);
        final Vector3D posInf = Vector3D.of(1, Double.POSITIVE_INFINITY, 1);
        final Vector3D negInf = Vector3D.of(1, 1, Double.NEGATIVE_INFINITY);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Bounds3D.from(Vector3D.NaN);
        }, IllegalStateException.class, INVALID_BOUNDS_PATTERN);

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Bounds3D.from(Vector3D.POSITIVE_INFINITY);
        }, IllegalStateException.class, INVALID_BOUNDS_PATTERN);

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Bounds3D.from(Vector3D.NEGATIVE_INFINITY);
        }, IllegalStateException.class, INVALID_BOUNDS_PATTERN);

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Bounds3D.from(good, nan);
        }, IllegalStateException.class, INVALID_BOUNDS_PATTERN);

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Bounds3D.from(posInf, good);
        }, IllegalStateException.class, INVALID_BOUNDS_PATTERN);

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Bounds3D.from(good, negInf, good);
        }, IllegalStateException.class, INVALID_BOUNDS_PATTERN);
    }

    @Test
    void testHasSize() {
        // arrange
        final Precision.DoubleEquivalence low = Precision.doubleEquivalenceOfEpsilon(1e-2);
        final Precision.DoubleEquivalence high = Precision.doubleEquivalenceOfEpsilon(1e-10);

        final Vector3D p1 = Vector3D.ZERO;

        final Vector3D p2 = Vector3D.of(1e-5, 1, 1);
        final Vector3D p3 = Vector3D.of(1, 1e-5, 1);
        final Vector3D p4 = Vector3D.of(1, 1, 1e-5);

        final Vector3D p5 = Vector3D.of(1, 1, 1);

        // act/assert
        Assertions.assertFalse(Bounds3D.from(p1).hasSize(high));
        Assertions.assertFalse(Bounds3D.from(p1).hasSize(low));

        Assertions.assertTrue(Bounds3D.from(p1, p2).hasSize(high));
        Assertions.assertFalse(Bounds3D.from(p1, p2).hasSize(low));

        Assertions.assertTrue(Bounds3D.from(p1, p3).hasSize(high));
        Assertions.assertFalse(Bounds3D.from(p1, p3).hasSize(low));

        Assertions.assertTrue(Bounds3D.from(p1, p4).hasSize(high));
        Assertions.assertFalse(Bounds3D.from(p1, p4).hasSize(low));

        Assertions.assertTrue(Bounds3D.from(p1, p5).hasSize(high));
        Assertions.assertTrue(Bounds3D.from(p1, p5).hasSize(low));
    }

    @Test
    void testContains_strict() {
        // arrange
        final Bounds3D b = Bounds3D.from(
                Vector3D.of(0, 4, 8),
                Vector3D.of(2, 6, 10));

        // act/assert
        assertContainsStrict(b, true,
                b.getCentroid(),
                Vector3D.of(0, 4, 8), Vector3D.of(2, 6, 10),
                Vector3D.of(1, 5, 9),
                Vector3D.of(0, 5, 9), Vector3D.of(2, 5, 9),
                Vector3D.of(1, 4, 9), Vector3D.of(1, 6, 9),
                Vector3D.of(1, 5, 8), Vector3D.of(1, 5, 10));

        assertContainsStrict(b, false,
                Vector3D.ZERO,
                Vector3D.of(-1, 5, 9), Vector3D.of(3, 5, 9),
                Vector3D.of(1, 3, 9), Vector3D.of(1, 7, 9),
                Vector3D.of(1, 5, 7), Vector3D.of(1, 5, 11),
                Vector3D.of(-1e-15, 4, 8), Vector3D.of(2, 6 + 1e-15, 10), Vector3D.of(0, 4, 10 + 1e-15));
    }

    @Test
    void testContains_precision() {
        // arrange
        final Bounds3D b = Bounds3D.from(
                Vector3D.of(0, 4, 8),
                Vector3D.of(2, 6, 10));

        // act/assert
        assertContainsWithPrecision(b, true,
                b.getCentroid(),
                Vector3D.of(0, 4, 8), Vector3D.of(2, 6, 10),
                Vector3D.of(1, 5, 9),
                Vector3D.of(0, 5, 9), Vector3D.of(2, 5, 9),
                Vector3D.of(1, 4, 9), Vector3D.of(1, 6, 9),
                Vector3D.of(1, 5, 8), Vector3D.of(1, 5, 10),
                Vector3D.of(-1e-15, 4, 8), Vector3D.of(2, 6 + 1e-15, 10), Vector3D.of(0, 4, 10 + 1e-15));

        assertContainsWithPrecision(b, false,
                Vector3D.ZERO,
                Vector3D.of(-1, 5, 9), Vector3D.of(3, 5, 9),
                Vector3D.of(1, 3, 9), Vector3D.of(1, 7, 9),
                Vector3D.of(1, 5, 7), Vector3D.of(1, 5, 11));
    }

    @Test
    void testIntersects() {
        // arrange
        final Bounds3D b = Bounds3D.from(Vector3D.ZERO, Vector3D.of(1, 1, 1));

        // act/assert
        checkIntersects(b, Vector3D::getX, (v, x) -> Vector3D.of(x, v.getY(), v.getZ()));
        checkIntersects(b, Vector3D::getY, (v, y) -> Vector3D.of(v.getX(), y, v.getZ()));
        checkIntersects(b, Vector3D::getZ, (v, z) -> Vector3D.of(v.getX(), v.getY(), z));
    }

    private void checkIntersects(final Bounds3D b, final ToDoubleFunction<? super Vector3D> getter,
                                 final BiFunction<? super Vector3D, Double, ? extends Vector3D> setter) {

        final Vector3D min = b.getMin();
        final Vector3D max = b.getMax();

        final double minValue = getter.applyAsDouble(min);
        final double maxValue = getter.applyAsDouble(max);
        final double midValue = (0.5 * (maxValue - minValue)) + minValue;

        // check all possible interval relationships

        // start below minValue
        Assertions.assertFalse(b.intersects(Bounds3D.from(
                setter.apply(min, minValue - 2), setter.apply(max, minValue - 1))));

        Assertions.assertTrue(b.intersects(Bounds3D.from(
                setter.apply(min, minValue - 2), setter.apply(max, minValue))));
        Assertions.assertTrue(b.intersects(Bounds3D.from(
                setter.apply(min, minValue - 2), setter.apply(max, midValue))));
        Assertions.assertTrue(b.intersects(Bounds3D.from(
                setter.apply(min, minValue - 2), setter.apply(max, maxValue))));
        Assertions.assertTrue(b.intersects(Bounds3D.from(
                setter.apply(min, minValue - 2), setter.apply(max, maxValue + 1))));

        // start on minValue
        Assertions.assertTrue(b.intersects(Bounds3D.from(
                setter.apply(min, minValue), setter.apply(max, minValue))));
        Assertions.assertTrue(b.intersects(Bounds3D.from(
                setter.apply(min, minValue), setter.apply(max, midValue))));
        Assertions.assertTrue(b.intersects(Bounds3D.from(
                setter.apply(min, minValue), setter.apply(max, maxValue))));
        Assertions.assertTrue(b.intersects(Bounds3D.from(
                setter.apply(min, minValue), setter.apply(max, maxValue + 1))));

        // start on midValue
        Assertions.assertTrue(b.intersects(Bounds3D.from(
                setter.apply(min, midValue), setter.apply(max, midValue))));
        Assertions.assertTrue(b.intersects(Bounds3D.from(
                setter.apply(min, midValue), setter.apply(max, maxValue))));
        Assertions.assertTrue(b.intersects(Bounds3D.from(
                setter.apply(min, midValue), setter.apply(max, maxValue + 1))));

        // start on maxValue
        Assertions.assertTrue(b.intersects(Bounds3D.from(
                setter.apply(min, maxValue), setter.apply(max, maxValue))));
        Assertions.assertTrue(b.intersects(Bounds3D.from(
                setter.apply(min, maxValue), setter.apply(max, maxValue + 1))));

        // start above maxValue
        Assertions.assertFalse(b.intersects(Bounds3D.from(
                setter.apply(min, maxValue + 1), setter.apply(max, maxValue + 2))));
    }

    @Test
    void testIntersection() {
        // -- arrange
        final Bounds3D b = Bounds3D.from(Vector3D.ZERO, Vector3D.of(1, 1, 1));

        // -- act/assert

        // move along x-axis
        Assertions.assertNull(b.intersection(Bounds3D.from(Vector3D.of(-2, 0, 0), Vector3D.of(-1, 1, 1))));
        checkIntersection(b, Vector3D.of(-1, 0, 0), Vector3D.of(0, 1, 1),
                Vector3D.of(0, 0, 0), Vector3D.of(0, 1, 1));
        checkIntersection(b, Vector3D.of(-1, 0, 0), Vector3D.of(0.5, 1, 1),
                Vector3D.of(0, 0, 0), Vector3D.of(0.5, 1, 1));
        checkIntersection(b, Vector3D.of(-1, 0, 0), Vector3D.of(1, 1, 1),
                Vector3D.of(0, 0, 0), Vector3D.of(1, 1, 1));
        checkIntersection(b, Vector3D.of(-1, 0, 0), Vector3D.of(2, 1, 1),
                Vector3D.of(0, 0, 0), Vector3D.of(1, 1, 1));
        checkIntersection(b, Vector3D.of(0, 0, 0), Vector3D.of(2, 1, 1),
                Vector3D.of(0, 0, 0), Vector3D.of(1, 1, 1));
        checkIntersection(b, Vector3D.of(0.5, 0, 0), Vector3D.of(2, 1, 1),
                Vector3D.of(0.5, 0, 0), Vector3D.of(1, 1, 1));
        checkIntersection(b, Vector3D.of(1, 0, 0), Vector3D.of(2, 1, 1),
                Vector3D.of(1, 0, 0), Vector3D.of(1, 1, 1));
        Assertions.assertNull(b.intersection(Bounds3D.from(Vector3D.of(2, 0, 0), Vector3D.of(3, 1, 1))));

        // move along y-axis
        Assertions.assertNull(b.intersection(Bounds3D.from(Vector3D.of(0, -2, 0), Vector3D.of(1, -1, 1))));
        checkIntersection(b, Vector3D.of(0, -1, 0), Vector3D.of(1, 0, 1),
                Vector3D.of(0, 0, 0), Vector3D.of(1, 0, 1));
        checkIntersection(b, Vector3D.of(0, -1, 0), Vector3D.of(1, 0.5, 1),
                Vector3D.of(0, 0, 0), Vector3D.of(1, 0.5, 1));
        checkIntersection(b, Vector3D.of(0, -1, 0), Vector3D.of(1, 1, 1),
                Vector3D.of(0, 0, 0), Vector3D.of(1, 1, 1));
        checkIntersection(b, Vector3D.of(0, -1, 0), Vector3D.of(1, 2, 1),
                Vector3D.of(0, 0, 0), Vector3D.of(1, 1, 1));
        checkIntersection(b, Vector3D.of(0, 0, 0), Vector3D.of(1, 2, 1),
                Vector3D.of(0, 0, 0), Vector3D.of(1, 1, 1));
        checkIntersection(b, Vector3D.of(0, 0.5, 0), Vector3D.of(1, 2, 1),
                Vector3D.of(0, 0.5, 0), Vector3D.of(1, 1, 1));
        checkIntersection(b, Vector3D.of(0, 1, 0), Vector3D.of(1, 2, 1),
                Vector3D.of(0, 1, 0), Vector3D.of(1, 1, 1));
        Assertions.assertNull(b.intersection(Bounds3D.from(Vector3D.of(0, 2, 0), Vector3D.of(1, 3, 1))));

        // move along z-axis
        Assertions.assertNull(b.intersection(Bounds3D.from(Vector3D.of(0, 0, -2), Vector3D.of(1, 1, -1))));
        checkIntersection(b, Vector3D.of(0, 0, -1), Vector3D.of(1, 1, 0),
                Vector3D.of(0, 0, 0), Vector3D.of(1, 1, 0));
        checkIntersection(b, Vector3D.of(0, 0, -1), Vector3D.of(1, 1, 0.5),
                Vector3D.of(0, 0, 0), Vector3D.of(1, 1, 0.5));
        checkIntersection(b, Vector3D.of(0, 0, -1), Vector3D.of(1, 1, 1),
                Vector3D.of(0, 0, 0), Vector3D.of(1, 1, 1));
        checkIntersection(b, Vector3D.of(0, 0, -1), Vector3D.of(1, 1, 2),
                Vector3D.of(0, 0, 0), Vector3D.of(1, 1, 1));
        checkIntersection(b, Vector3D.of(0, 0, 0), Vector3D.of(1, 1, 2),
                Vector3D.of(0, 0, 0), Vector3D.of(1, 1, 1));
        checkIntersection(b, Vector3D.of(0, 0, 0.5), Vector3D.of(1, 1, 2),
                Vector3D.of(0, 0, 0.5), Vector3D.of(1, 1, 1));
        checkIntersection(b, Vector3D.of(0, 0, 1), Vector3D.of(1, 1, 2),
                Vector3D.of(0, 0, 1), Vector3D.of(1, 1, 1));
        Assertions.assertNull(b.intersection(Bounds3D.from(Vector3D.of(0, 0, 2), Vector3D.of(1, 1, 3))));
    }

    private void checkIntersection(final Bounds3D b, final Vector3D a1, final Vector3D a2, final Vector3D r1, final Vector3D r2) {
        final Bounds3D a = Bounds3D.from(a1, a2);
        final Bounds3D result = b.intersection(a);

        checkBounds(result, r1, r2);
    }

    @Test
    void toRegion() {
        // arrange
        final Bounds3D b = Bounds3D.from(
                Vector3D.of(0, 4, 8),
                Vector3D.of(2, 6, 10));

        // act
        final Parallelepiped p = b.toRegion(TEST_PRECISION);

        // assert
        Assertions.assertEquals(8, p.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 5, 9), p.getCentroid(), TEST_EPS);
    }

    @Test
    void toRegion_boundingBoxTooSmall() {
        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> Bounds3D.from(Vector3D.ZERO, Vector3D.of(1e-12, 1e-12, 1e-12))
                .toRegion(TEST_PRECISION));
    }

    @Test
    void testEq() {
        // arrange
        final Precision.DoubleEquivalence low = Precision.doubleEquivalenceOfEpsilon(1e-2);
        final Precision.DoubleEquivalence high = Precision.doubleEquivalenceOfEpsilon(1e-10);

        final Bounds3D b1 = Bounds3D.from(Vector3D.of(1, 1, 1), Vector3D.of(2, 2, 2));

        final Bounds3D b2 = Bounds3D.from(Vector3D.of(1.1, 1, 1), Vector3D.of(2, 2, 2));
        final Bounds3D b3 = Bounds3D.from(Vector3D.of(1, 1, 1), Vector3D.of(1.9, 2, 2));

        final Bounds3D b4 = Bounds3D.from(Vector3D.of(1.001, 1.001, 1.001), Vector3D.of(2.001, 2.001, 2.001));

        // act/assert
        Assertions.assertTrue(b1.eq(b1, low));

        Assertions.assertFalse(b1.eq(b2, low));
        Assertions.assertFalse(b1.eq(b3, low));

        Assertions.assertTrue(b1.eq(b4, low));
        Assertions.assertTrue(b4.eq(b1, low));

        Assertions.assertFalse(b1.eq(b4, high));
        Assertions.assertFalse(b4.eq(b1, high));
    }

    @Test
    void testLinecast_intersectsFace() {
        // -- arrange
        // use unequal face sizes so that our test lines do not end up passing through
        // a vertex on the opposite side
        final Bounds3D bounds = Bounds3D.from(Vector3D.of(-0.9, -2, -3), Vector3D.of(0.9, 2, 3));

        // -- act/assert
        checkLinecastAgainstFace(bounds, Vector3D.of(0.9, 0, 0), Vector3D.Unit.PLUS_X);
        checkLinecastAgainstFace(bounds, Vector3D.of(-0.9, 0, 0), Vector3D.Unit.MINUS_X);

        checkLinecastAgainstFace(bounds, Vector3D.of(0, 2, 0), Vector3D.Unit.PLUS_Y);
        checkLinecastAgainstFace(bounds, Vector3D.of(0, -2, 0), Vector3D.Unit.MINUS_Y);

        checkLinecastAgainstFace(bounds, Vector3D.of(0, 0, 3), Vector3D.Unit.PLUS_Z);
        checkLinecastAgainstFace(bounds, Vector3D.of(0, 0, -3), Vector3D.Unit.MINUS_Z);
    }

    private void checkLinecastAgainstFace(
            final Bounds3D bounds,
            final Vector3D facePt,
            final Vector3D normal) {

        // -- arrange
        final Vector3D offset = normal.multiply(1.2);
        final Parallelepiped region = bounds.toRegion(TEST_PRECISION);

        EuclideanTestUtils.permute(-1, 1, 0.5, (x, y, z) -> {
            final Vector3D otherPt = facePt
                    .add(Vector3D.of(x, y, z))
                    .add(offset);
            final Line3D line = Lines3D.fromPoints(otherPt, facePt, TEST_PRECISION);

            final LinecastPoint3D reversePt = region.linecastFirst(line.reverse());

            // -- act/assert
            linecastCheckerFor(bounds)
                .expect(facePt, normal)
                .and(reversePt.getPoint(), reversePt.getNormal())
                .whenGiven(line);

            linecastCheckerFor(bounds)
                .and(reversePt.getPoint(), reversePt.getNormal())
                .expect(facePt, normal)
                .whenGiven(line.reverse());
        });
    }

    @Test
    void testLinecast_intersectsSingleVertex() {
        // -- arrange
        final Bounds3D bounds = Bounds3D.from(Vector3D.ZERO, Vector3D.of(1, 1, 1));

        // -- act/assert
        checkLinecastAgainstSingleVertex(bounds, Vector3D.ZERO);
        checkLinecastAgainstSingleVertex(bounds, Vector3D.of(0, 0, 1));
        checkLinecastAgainstSingleVertex(bounds, Vector3D.of(0, 1, 0));
        checkLinecastAgainstSingleVertex(bounds, Vector3D.of(0, 1, 1));
        checkLinecastAgainstSingleVertex(bounds, Vector3D.of(1, 0, 0));
        checkLinecastAgainstSingleVertex(bounds, Vector3D.of(1, 0, 1));
        checkLinecastAgainstSingleVertex(bounds, Vector3D.of(1, 1, 0));
        checkLinecastAgainstSingleVertex(bounds, Vector3D.of(1, 1, 1));
    }

    private void checkLinecastAgainstSingleVertex(
            final Bounds3D bounds,
            final Vector3D vertex) {

        // -- arrange
        final Vector3D centerToVertex = vertex.subtract(bounds.getCentroid()).normalize();
        final Vector3D baseLineDir = centerToVertex.orthogonal();

        final int runCnt = 10;
        for (double a = 0; a < Angle.TWO_PI; a += Angle.TWO_PI / runCnt) {

            // construct a line orthogonal to the vector from the bounds center to the vertex and passing
            // through the vertex
            final Vector3D lineDir = QuaternionRotation.fromAxisAngle(centerToVertex, a).apply(baseLineDir);
            final Line3D line = Lines3D.fromPointAndDirection(vertex, lineDir, TEST_PRECISION);

            // construct possible normals for this vertex
            final List<Vector3D> normals = new ArrayList<>();
            normals.add(centerToVertex.project(Vector3D.Unit.PLUS_X).normalize());
            normals.add(centerToVertex.project(Vector3D.Unit.PLUS_Y).normalize());
            normals.add(centerToVertex.project(Vector3D.Unit.PLUS_Z).normalize());

            normals.sort(Vector3D.COORDINATE_ASCENDING_ORDER);

            // create the checker and populate it with the normals of faces that are not parallel to
            // the line
            final BoundsLinecastChecker3D checker = linecastCheckerFor(bounds);
            for (final Vector3D normal : normals) {
                if (!TEST_PRECISION.eqZero(normal.dot(lineDir))) {
                    checker.expect(vertex, normal);
                }
            }

            // -- act/assert
            checker.whenGiven(line)
                .whenGiven(line.reverse());
        }
    }

    @Test
    void testLinecast_vertexToVertex() {
        // -- arrange
        final Vector3D min = Vector3D.ZERO;
        final Vector3D max = Vector3D.of(1, 1, 1);

        final Bounds3D bounds = Bounds3D.from(min, max);
        final Line3D line = Lines3D.fromPoints(min, max, TEST_PRECISION);

        // -- act/assert
        linecastCheckerFor(bounds)
            .expect(min, Vector3D.Unit.MINUS_X)
            .and(min, Vector3D.Unit.MINUS_Y)
            .and(min, Vector3D.Unit.MINUS_Z)
            .and(max, Vector3D.Unit.PLUS_Z)
            .and(max, Vector3D.Unit.PLUS_Y)
            .and(max, Vector3D.Unit.PLUS_X)
            .whenGiven(line);
    }

    @Test
    void testLinecast_edgeToEdge() {
        // -- arrange
        final Vector3D min = Vector3D.of(0, 0, 0);
        final Vector3D max = Vector3D.of(1, 1, 1);

        final Bounds3D bounds = Bounds3D.from(min, max);

        final Vector3D start = Vector3D.of(0, 0, 0.5);
        final Vector3D end = Vector3D.of(1, 1, 0.5);

        final Line3D line = Lines3D.fromPoints(start, end, TEST_PRECISION);

        // -- act/assert
        linecastCheckerFor(bounds)
            .expect(start, Vector3D.Unit.MINUS_X)
            .and(start, Vector3D.Unit.MINUS_Y)
            .and(end, Vector3D.Unit.PLUS_Y)
            .and(end, Vector3D.Unit.PLUS_X)
            .whenGiven(line);
    }

    @Test
    void testLinecast_alongFace() {
        // -- arrange
        final Vector3D min = Vector3D.ZERO;
        final Vector3D max = Vector3D.of(1, 1, 1);

        final Bounds3D bounds = Bounds3D.from(min, max);

        final int cnt = 10;
        for (double x = min.getX();
                x <= max.getX();
                x += bounds.getDiagonal().getX() / cnt) {

            final Vector3D start = Vector3D.of(x, min.getY(), max.getZ());
            final Vector3D end = Vector3D.of(x, max.getY(), max.getZ());

            final Line3D line = Lines3D.fromPoints(start, end, TEST_PRECISION);

            // -- act/assert
            linecastCheckerFor(bounds)
                .expect(start, Vector3D.Unit.MINUS_Y)
                .and(end, Vector3D.Unit.PLUS_Y)
                .whenGiven(line);
        }
    }

    @Test
    void testLinecast_noIntersection() {
        // -- arrange
        final Bounds3D bounds = Bounds3D.from(Vector3D.ZERO, Vector3D.of(1, 1, 1));

        // -- act/assert
        checkLinecastNoIntersection(bounds, Vector3D.ZERO);
        checkLinecastNoIntersection(bounds, Vector3D.of(0, 0, 1));
        checkLinecastNoIntersection(bounds, Vector3D.of(0, 1, 0));
        checkLinecastNoIntersection(bounds, Vector3D.of(0, 1, 1));
        checkLinecastNoIntersection(bounds, Vector3D.of(1, 0, 0));
        checkLinecastNoIntersection(bounds, Vector3D.of(1, 0, 1));
        checkLinecastNoIntersection(bounds, Vector3D.of(1, 1, 0));
        checkLinecastNoIntersection(bounds, Vector3D.of(1, 1, 1));
    }

    private void checkLinecastNoIntersection(
            final Bounds3D bounds,
            final Vector3D vertex) {

        // -- arrange
        final Vector3D toVertex = bounds.getCentroid().directionTo(vertex);
        final Vector3D baseLineDir = toVertex.orthogonal();

        final Vector3D offsetVertex = vertex.add(toVertex);

        final Line3D plusXLine = Lines3D.fromPointAndDirection(offsetVertex, Vector3D.Unit.PLUS_X, TEST_PRECISION);
        final Line3D plusYLine = Lines3D.fromPointAndDirection(offsetVertex, Vector3D.Unit.PLUS_Y, TEST_PRECISION);
        final Line3D plusZLine = Lines3D.fromPointAndDirection(offsetVertex, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        final BoundsLinecastChecker3D emptyChecker = linecastCheckerFor(bounds)
                .expectNothing();

        // -- act/assert
        // check axis-aligned lines
        emptyChecker
            .whenGiven(plusXLine)
            .whenGiven(plusYLine)
            .whenGiven(plusZLine);

        // check lines orthogonal to the axis
        final int runCnt = 10;
        for (double a = 0; a < Angle.TWO_PI; a += Angle.TWO_PI / runCnt) {
            final Vector3D lineDir = QuaternionRotation.fromAxisAngle(toVertex, a).apply(baseLineDir);
            final Line3D line = Lines3D.fromPointAndDirection(offsetVertex, lineDir, TEST_PRECISION);

            emptyChecker.whenGiven(line);
        }
    }

    @Test
    void testLinecast_nonSpan() {
        // -- arrange
        final Vector3D min = Vector3D.ZERO;
        final Vector3D max = Vector3D.of(1, 1, 1);

        final Bounds3D bounds = Bounds3D.from(min, max);

        final Vector3D centroid = bounds.getCentroid();

        final Vector3D start = Vector3D.of(max.getX(), centroid.getY(), centroid.getZ());
        final Vector3D end = Vector3D.of(min.getX(), centroid.getY(), centroid.getZ());

        final Line3D line = Lines3D.fromPoints(start, end, TEST_PRECISION);

        // -- act/assert
        linecastCheckerFor(bounds)
            .expect(end, Vector3D.Unit.MINUS_X)
            .whenGiven(line.rayFrom(-0.5));

        linecastCheckerFor(bounds)
            .expect(start, Vector3D.Unit.PLUS_X)
            .whenGiven(line.reverseRayTo(-0.5));

        linecastCheckerFor(bounds)
            .expectNothing()
            .whenGiven(line.segment(-0.9, -0.1));

        linecastCheckerFor(bounds)
            .expect(end, Vector3D.Unit.MINUS_X)
            .whenGiven(line.segment(-0.9, 0.1));

        linecastCheckerFor(bounds)
            .expect(start, Vector3D.Unit.PLUS_X)
            .whenGiven(line.segment(-1.1, -0.1));

        linecastCheckerFor(bounds)
            .expect(start, Vector3D.Unit.PLUS_X)
            .expect(end, Vector3D.Unit.MINUS_X)
            .whenGiven(line.segment(-1.1, 0.1));
    }

    @Test
    void testHashCode() {
        // arrange
        final Bounds3D b1 = Bounds3D.from(Vector3D.of(1, 1, 1), Vector3D.of(2, 2, 2));

        final Bounds3D b2 = Bounds3D.from(Vector3D.of(-2, 1, 1), Vector3D.of(2, 2, 2));
        final Bounds3D b3 = Bounds3D.from(Vector3D.of(1, 1, 1), Vector3D.of(3, 2, 2));
        final Bounds3D b4 = Bounds3D.from(Vector3D.of(1 + 1e-15, 1, 1), Vector3D.of(2, 2, 2));
        final Bounds3D b5 = Bounds3D.from(Vector3D.of(1, 1, 1), Vector3D.of(2 + 1e-15, 2, 2));

        final Bounds3D b6 = Bounds3D.from(Vector3D.of(1, 1, 1), Vector3D.of(2, 2, 2));

        // act
        final int hash = b1.hashCode();

        // assert
        Assertions.assertEquals(hash, b1.hashCode());

        Assertions.assertNotEquals(hash, b2.hashCode());
        Assertions.assertNotEquals(hash, b3.hashCode());
        Assertions.assertNotEquals(hash, b4.hashCode());
        Assertions.assertNotEquals(hash, b5.hashCode());

        Assertions.assertEquals(hash, b6.hashCode());
    }

    @Test
    void testEquals() {
        // arrange
        final Bounds3D b1 = Bounds3D.from(Vector3D.of(1, 1, 1), Vector3D.of(2, 2, 2));

        final Bounds3D b2 = Bounds3D.from(Vector3D.of(-1, 1, 1), Vector3D.of(2, 2, 2));
        final Bounds3D b3 = Bounds3D.from(Vector3D.of(1, 1, 1), Vector3D.of(3, 2, 2));
        final Bounds3D b4 = Bounds3D.from(Vector3D.of(1 + 1e-15, 1, 1), Vector3D.of(2, 2, 2));
        final Bounds3D b5 = Bounds3D.from(Vector3D.of(1, 1, 1), Vector3D.of(2 + 1e-15, 2, 2));

        final Bounds3D b6 = Bounds3D.from(Vector3D.of(1, 1, 1), Vector3D.of(2, 2, 2));

        // act/assert
        GeometryTestUtils.assertSimpleEqualsCases(b1);

        Assertions.assertNotEquals(b1, b2);
        Assertions.assertNotEquals(b1, b3);
        Assertions.assertNotEquals(b1, b4);
        Assertions.assertNotEquals(b1, b5);

        Assertions.assertEquals(b1, b6);
    }

    @Test
    void testToString() {
        // arrange
        final Bounds3D b = Bounds3D.from(Vector3D.of(1, 1, 1), Vector3D.of(2, 2, 2));

        // act
        final String str = b.toString();

        // assert
        GeometryTestUtils.assertContains("Bounds3D[min= (1", str);
        GeometryTestUtils.assertContains(", max= (2", str);
    }

    @Test
    void testBuilder_addMethods() {
        // arrange
        final Vector3D p1 = Vector3D.of(1, 10, 11);
        final Vector3D p2 = Vector3D.of(2, 9, 12);
        final Vector3D p3 = Vector3D.of(3, 8, 13);
        final Vector3D p4 = Vector3D.of(4, 7, 14);
        final Vector3D p5 = Vector3D.of(5, 6, 15);

        // act
        final Bounds3D b = Bounds3D.builder()
                .add(p1)
                .addAll(Arrays.asList(p2, p3))
                .add(Bounds3D.from(p4, p5))
                .build();

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 6, 11), b.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(5, 10, 15), b.getMax(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(3, 8, 13), b.getCentroid(), TEST_EPS);
    }

    @Test
    void testBuilder_hasBounds() {
        // act/assert
        Assertions.assertFalse(Bounds3D.builder().hasBounds());

        Assertions.assertFalse(Bounds3D.builder().add(Vector3D.of(Double.NaN, 1, 1)).hasBounds());
        Assertions.assertFalse(Bounds3D.builder().add(Vector3D.of(1, Double.NaN, 1)).hasBounds());
        Assertions.assertFalse(Bounds3D.builder().add(Vector3D.of(1, 1, Double.NaN)).hasBounds());

        Assertions.assertFalse(Bounds3D.builder().add(Vector3D.of(Double.POSITIVE_INFINITY, 1, 1)).hasBounds());
        Assertions.assertFalse(Bounds3D.builder().add(Vector3D.of(1, Double.POSITIVE_INFINITY, 1)).hasBounds());
        Assertions.assertFalse(Bounds3D.builder().add(Vector3D.of(1, 1, Double.POSITIVE_INFINITY)).hasBounds());

        Assertions.assertFalse(Bounds3D.builder().add(Vector3D.of(Double.NEGATIVE_INFINITY, 1, 1)).hasBounds());
        Assertions.assertFalse(Bounds3D.builder().add(Vector3D.of(1, Double.NEGATIVE_INFINITY, 1)).hasBounds());
        Assertions.assertFalse(Bounds3D.builder().add(Vector3D.of(1, 1, Double.NEGATIVE_INFINITY)).hasBounds());

        Assertions.assertTrue(Bounds3D.builder().add(Vector3D.ZERO).hasBounds());
    }

    private static void checkBounds(final Bounds3D b, final Vector3D min, final Vector3D max) {
        EuclideanTestUtils.assertCoordinatesEqual(min, b.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(max, b.getMax(), TEST_EPS);
    }

    private static void assertContainsStrict(
            final Bounds3D bounds,
            final boolean contains,
            final Vector3D... pts) {
        for (final Vector3D pt : pts) {
            Assertions.assertEquals(contains, bounds.contains(pt), "Unexpected location for point " + pt);
        }
    }

    private static void assertContainsWithPrecision(
            final Bounds3D bounds,
            final boolean contains,
            final Vector3D... pts) {
        for (final Vector3D pt : pts) {
            Assertions.assertEquals(contains, bounds.contains(pt, TEST_PRECISION), "Unexpected location for point " + pt);
        }
    }

    private static BoundsLinecastChecker3D linecastCheckerFor(final Bounds3D bounds) {
        return new BoundsLinecastChecker3D(bounds);
    }

    private static final class BoundsLinecastChecker3D {

        private final Bounds3D bounds;

        private final Parallelepiped region;

        private final LinecastChecker3D checker;

        BoundsLinecastChecker3D(final Bounds3D bounds) {
            this.bounds = bounds;
            this.region = bounds.toRegion(TEST_PRECISION);
            this.checker = LinecastChecker3D.with(bounds);
        }

        public BoundsLinecastChecker3D expectNothing() {
            checker.expectNothing();
            return this;
        }

        public BoundsLinecastChecker3D expect(final Vector3D pt, final Vector3D normal) {
            checker.expect(pt, normal);
            return this;
        }

        public BoundsLinecastChecker3D and(final Vector3D pt, final Vector3D normal) {
            return expect(pt, normal);
        }

        public BoundsLinecastChecker3D whenGiven(final Line3D line) {
            // perform the standard checks
            checker.whenGiven(line);

            // check that the returned points are equivalent to the region points
            assertEquivalentRegionLinecastResult(region.linecast(line), bounds.linecast(line));

            return this;
        }

        public BoundsLinecastChecker3D whenGiven(final LineConvexSubset3D subset) {
            // perform the standard checks
            checker.whenGiven(subset);

            // check that the returned points are equivalent to the region points
            assertEquivalentRegionLinecastResult(region.linecast(subset), bounds.linecast(subset));

            return this;
        }

        private void assertEquivalentRegionLinecastResult(
                final List<LinecastPoint3D> regionList,
                final List<LinecastPoint3D> boundsList) {
            assertLinecastElements(regionList, boundsList);

            // ensure consistency with the intersects method
            if (!boundsList.isEmpty()) {
                final Line3D line = boundsList.get(0).getLine();

                Assertions.assertTrue(bounds.intersects(line), "Expected line to intersect bounds");
            }
        }

        /** Assert that the two collections contain the same linecast points and that the elements
         * of {@code actual} are arranged in ascending abscissa order.
         * @param expected expected collection
         * @param actual actual collection
         */
        private void assertLinecastElements(
                final Collection<LinecastPoint3D> expected,
                final Collection<LinecastPoint3D> actual) {
            Assertions.assertEquals(expected.size(), actual.size(), "Unexpected list size");

            // create a sorted copy
            final List<LinecastPoint3D> sortedList = new ArrayList<>(actual);
            sortedList.sort(LinecastPoint3D.ABSCISSA_ORDER);

            // check element membership
            for (final LinecastPoint3D expectedPt : expected) {
                final Iterator<LinecastPoint3D> sortedIt = sortedList.iterator();

                boolean found = false;
                while (sortedIt.hasNext()) {
                    if (expectedPt.eq(sortedIt.next(), TEST_PRECISION)) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    Assertions.fail("Missing expected linecast point " + expectedPt);
                }
            }

            // check the order
            Assertions.assertEquals(sortedList, actual);
        }
    }
}

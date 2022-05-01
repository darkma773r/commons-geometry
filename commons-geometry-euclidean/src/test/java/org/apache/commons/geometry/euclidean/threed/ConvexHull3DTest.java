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
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConvexHull3DTest {

    private static final int RANDOMIZATION_COUNT = 5;

    private static final long DEFAULT_RANDOM_SEED = 0L;

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    @Test
    void testBuild_noPoints() {
        // arrange
        final ConvexHull3D.Builder builder = ConvexHull3D.builder(TEST_PRECISION);

        // act/assert
        Assertions.assertThrows(IllegalStateException.class, () -> builder.build());
    }

    @Test
    void testBuild_singlePoint() {
        // arrange
        final Vector3D pt = Vector3D.of(1, 2, 3);

        // act
        final ConvexHull3D hull = ConvexHull3D.builder(TEST_PRECISION)
                .add(pt)
                .build();

        // assert
        Assertions.assertEquals(Arrays.asList(pt), hull.getVertices());
        Assertions.assertFalse(hull.hasSize());
        Assertions.assertNull(hull.getRegion());
    }

    @Test
    void testBuild_singleRepeatedPoint() {
        // arrange
        final Vector3D pt = Vector3D.of(1, 2, 3);

        // act
        final ConvexHull3D hull = ConvexHull3D.builder(TEST_PRECISION)
                .add(pt)
                .add(Arrays.asList(pt, pt, pt, pt, pt, pt))
                .build();

        // assert
        Assertions.assertEquals(Arrays.asList(pt), hull.getVertices());
        Assertions.assertFalse(hull.hasSize());
        Assertions.assertNull(hull.getRegion());
    }

    @Test
    void testBuild_tetrahedron_verticesEqual() {
        // arrange
        final List<Vector3D> pts = Arrays.asList(
                Vector3D.ZERO,
                Vector3D.Unit.PLUS_X,
                Vector3D.Unit.PLUS_Y,
                Vector3D.Unit.PLUS_Z);

        // act
        checkConvexHull(pts, hull -> {
            // assert
            assertPointSetsEqual(pts, hull.getVertices());
            Assertions.assertTrue(hull.hasSize());

            final ConvexVolume vol = hull.getRegion();
            EuclideanTestUtils.assertRegionLocation(vol, RegionLocation.INSIDE, Vector3D.of(0.1, 0.1, 0.1));
            EuclideanTestUtils.assertRegionLocation(vol, RegionLocation.BOUNDARY,
                    pts.toArray(new Vector3D[0]));

            EuclideanTestUtils.assertRegionLocation(vol, RegionLocation.OUTSIDE,
                    Vector3D.of(-0.1, -0.1, -0.1), Vector3D.of(1, 1, 1),
                    Vector3D.of(-2, 0, 0), Vector3D.of(2, 0, 0),
                    Vector3D.of(0, -2, 0), Vector3D.of(0, 2, 0),
                    Vector3D.of(0, 0, -2), Vector3D.of(0, 0, 2));
        });
    }

    /** Perform a number of convex hull computations and pass each result
     * to {@code hullConsumer}. The input point list is shuffled after each
     * run to verify that the computation is consistent regardless of the order of
     * the input points.
     * @param pts input points
     * @param hullConsumer consumer passed the result of each convex hull computation
     */
    private static void checkConvexHull(
            final Collection<? extends Vector3D> pts,
            final Consumer<ConvexHull3D> hullConsumer) {
        checkConvexHull(
                pts,
                new Random(DEFAULT_RANDOM_SEED),
                RANDOMIZATION_COUNT,
                hullConsumer);
    }

    /** Perform {@code runCount} number of convex hull computations and pass each
     * result to {@code hullConsumer}. The input point list is shuffled after each
     * run to verify that the computation is consistent regardless of the order of
     * the input points.
     * @param pts input points
     * @param rnd random instance
     * @param runCount number of runs to perform
     * @param hullConsumer consumer passed the result of each convex hull computation
     */
    private static void checkConvexHull(
            final Collection<? extends Vector3D> pts,
            final Random rnd,
            final int runCount,
            final Consumer<ConvexHull3D> hullConsumer) {

        final List<Vector3D> shuffledPoints = new ArrayList<>(pts);

        for (int i = 0; i < runCount; ++i) {

            final ConvexHull3D hull = ConvexHull3D.builder(TEST_PRECISION)
                    .add(shuffledPoints)
                    .build();

            hullConsumer.accept(hull);

            Collections.shuffle(shuffledPoints, rnd);
        }
    }

    /** Assert that two point collection contain the same elements.
     * @param expected expected points
     * @param actual actual points
     */
    private static void assertPointSetsEqual(
            final Collection<? extends Vector3D> expected,
            final Collection<? extends Vector3D> actual) {

        Assertions.assertEquals(expected.size(), actual.size());

        final Set<Vector3D> actualSet = new HashSet<>(actual);

        for (final Vector3D expectedPt : expected) {
            Assertions.assertTrue(actualSet.contains(expectedPt),
                    () -> "Expected vertex list to contain " + expectedPt);
        }
    }
}

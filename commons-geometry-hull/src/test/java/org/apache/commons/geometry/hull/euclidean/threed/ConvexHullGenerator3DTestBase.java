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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.euclidean.threed.ConvexVolume;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Abstract base test class for Euclidean 3D convex hull generators.
 */
public abstract class ConvexHullGenerator3DTestBase {

    protected static final double TEST_EPS = 1e-10;

    protected static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    protected abstract ConvexHullGenerator3D createGenerator(Precision.DoubleEquivalence precision);

    @Test
    void testEmpty() {
        // arrange
        final ConvexHullGenerator3D gen = createGenerator(TEST_PRECISION);

        // act
        final ConvexHull3D hull = gen.generate(Collections.emptyList());

        // assert
        assertDegenerate(hull, 0);
    }

    @Test
    void testDegenerate_singlePoint() {
        // arrange
        final ConvexHullGenerator3D gen = createGenerator(TEST_PRECISION);
        final List<Vector3D.Unit> pts = Arrays.asList(Vector3D.Unit.PLUS_X);

        // act
        final ConvexHull3D hull = gen.generate(pts);

        // assert
        assertDegenerate(hull, 1);
        Assertions.assertEquals(pts, hull.getVertices());
    }

    @Test
    void testDegenerate_twoPoints() {
        // arrange
        final ConvexHullGenerator3D gen = createGenerator(TEST_PRECISION);
        final List<Vector3D.Unit> pts = Arrays.asList(Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y);

        // act
        final ConvexHull3D hull = gen.generate(pts);

        // assert
        assertDegenerate(hull, 2);
        Assertions.assertEquals(pts, hull.getVertices());
    }

    @Test
    void testDegenerate_threePoints() {
        // arrange
        final ConvexHullGenerator3D gen = createGenerator(TEST_PRECISION);
        final List<Vector3D.Unit> pts = Arrays.asList(
                Vector3D.Unit.PLUS_X,
                Vector3D.Unit.PLUS_Y,
                Vector3D.Unit.PLUS_Z);

        // act
        final ConvexHull3D hull = gen.generate(pts);

        // assert
        assertDegenerate(hull, 3);
        Assertions.assertEquals(pts, hull.getVertices());
    }

    @Test
    void testDegenerate_equivalentPoints() {
        // arrange
        final ConvexHullGenerator3D gen = createGenerator(TEST_PRECISION);
        final List<Vector3D> pts = Arrays.asList(
                Vector3D.ZERO,
                Vector3D.of(1e-11, 0, 0),
                Vector3D.of(0, 1e-11, 0),
                Vector3D.of(0, 0, 1e-11));

        // act
        final ConvexHull3D hull = gen.generate(pts);

        // assert
        assertDegenerate(hull, 1);
        Assertions.assertEquals(Arrays.asList(Vector3D.ZERO), hull.getVertices());
    }

    @Test
    void testTetrahedron() {
        // arrange
        final ConvexHullGenerator3D gen = createGenerator(TEST_PRECISION);
        final List<Vector3D> pts = Arrays.asList(
                Vector3D.of(1, 2, 3),
                Vector3D.of(2, 2, 4),
                Vector3D.of(2, 3, 3),
                Vector3D.of(1, 3, 4));

        // act
        final ConvexHull3D hull = gen.generate(pts);

        // assert
        assertNonDegenerate(hull, pts);
    }

    @Test
    void testTetrahedron_additionalInteriorPoints() {
        // arrange
        final ConvexHullGenerator3D gen = createGenerator(TEST_PRECISION);
        final List<Vector3D> pts = new ArrayList<>();
        pts.add(Vector3D.of(1, 2, 3));
        pts.add(Vector3D.of(2, 2, 4));
        pts.add(Vector3D.of(2, 3, 3));
        pts.add(Vector3D.of(1, 3, 4));

        pts.add(Vector3D.of(1.3, 2.3, 3.3));
        pts.add(Vector3D.of(1.5, 2.5, 3.5));
        pts.add(Vector3D.of(1.7, 2.7, 3.7));

        // act
        final ConvexHull3D hull = gen.generate(pts);

        // assert
        assertNonDegenerate(hull, pts);
    }

    protected static void assertDegenerate(final ConvexHull3D hull, final int vertexCount) {
        Assertions.assertEquals(vertexCount, hull.getMesh().getVertexCount());
        Assertions.assertEquals(vertexCount, hull.getVertices().size());

        Assertions.assertFalse(hull.hasSize());

        Assertions.assertNull(hull.getRegion());
    }

    protected static void assertNonDegenerate(final ConvexHull3D hull, final int vertexCount) {
        Assertions.assertEquals(vertexCount, hull.getMesh().getVertexCount());
        Assertions.assertEquals(vertexCount, hull.getVertices().size());

        Assertions.assertTrue(hull.hasSize());

        final ConvexVolume region = hull.getRegion();
        Assertions.assertNotNull(region);
        Assertions.assertFalse(region.isEmpty());
        Assertions.assertTrue(region.isFinite());

        for (final Vector3D vertex : hull.getVertices()) {
            Assertions.assertEquals(RegionLocation.BOUNDARY, region.classify(vertex),
                    () -> "Expected vertex " + vertex + " to be on region boundary");
        }
    }

    protected static void assertNonDegenerate(final ConvexHull3D hull,
            final Collection<? extends Vector3D> vertices) {
        assertNonDegenerate(hull, vertices);
        assertSamePoints(vertices, hull.getVertices());
    }

    protected static void assertSamePoints(final Collection<? extends Vector3D> aList,
            final Collection<? extends Vector3D> bList) {

        Assertions.assertEquals(aList.size(), bList.size());

        final Set<Vector3D> bSet = new HashSet<>(bList);
        for (final Vector3D aPt : aList) {
            Assertions.assertTrue(bSet.remove(aPt), () -> "Expected point list to contain " + aPt);
        }
    }
}

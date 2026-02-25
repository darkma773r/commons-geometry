/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.geometry.euclidean.threed.hull;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.geometry.euclidean.EuclideanCollections;
import org.apache.commons.geometry.euclidean.threed.ConvexPolygon3D;
import org.apache.commons.geometry.euclidean.threed.ConvexVolume;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.numbers.core.Precision;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConvexHull3DTest {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    private ConvexHull3D.Builder builder;

    private UniformRandomProvider random;

    @BeforeEach
    void setUp() {
        builder = new ConvexHull3D.Builder(TEST_PRECISION);
        random = RandomSource.XO_SHI_RO_256_PP.create(10);
    }

    /**
     * A hull with less than four points is degenerate.
     */
    @Test
    void lessThanFourPoints() {
        // arrange
        final List<Vector3D> vertices = Arrays.asList(
                Vector3D.of(0, 0, 0),
                Vector3D.of(1, 0, 0),
                Vector3D.of(0, 1, 0));
        builder.append(vertices);

        // act
        final ConvexHull3D hull = builder.build();

        // assert
        checkDegenerateHull(hull, vertices);
    }

    /**
     * A Hull with less than four points is degenerate.
     */
    @Test
    void samePoints() {
        // arrange
        final List<Vector3D> vertices = Arrays.asList(
                Vector3D.ZERO,
                Vector3D.ZERO,
                Vector3D.ZERO,
                Vector3D.ZERO);
        builder.append(vertices);

        // act
        final ConvexHull3D hull = builder.build();

        // assert
        checkDegenerateHull(hull, vertices);
    }

    @Test
    void collinearPoints() {
        // arrange
        final List<Vector3D> vertices = Arrays.asList(
                Vector3D.ZERO,
                Vector3D.of(1, 0, 0),
                Vector3D.of(2, 0, 0),
                Vector3D.of(3, 0, 0));
        builder.append(vertices);

        // act
        final ConvexHull3D hull = builder.build();

        // assert
        checkDegenerateHull(hull, vertices);
    }

    @Test
    void coplanarPoints() {
        // arrange
        final List<Vector3D> vertices = Arrays.asList(
                Vector3D.ZERO,
                Vector3D.of(1, 0, 0),
                Vector3D.of(0, 1, 0),
                Vector3D.of(3, 0, 0));
        builder.append(vertices);

        // act
        final ConvexHull3D hull = builder.build();

        // assert
        checkDegenerateHull(hull, vertices);
    }

    @Test
    void simplex() {
        // arrange
        final List<Vector3D> vertices = Arrays.asList(
                Vector3D.ZERO,
                Vector3D.of(1, 0, 0),
                Vector3D.of(0, 1, 0),
                Vector3D.of(0, 0, 1));
        builder.append(vertices);

        // act
        final ConvexHull3D hull = builder.build();

        // assert
        checkHull(hull, vertices);

        Assertions.assertEquals(1.0 / 6.0, hull.getRegion().getSize(), TEST_EPS);
        Assertions.assertEquals(4, hull.getFacets().size());
    }

    @Test
    void simplexPlusPoint() {
        // arrange
        final List<Vector3D> vertices = Arrays.asList(
                Vector3D.ZERO,
                Vector3D.of(1, 0, 0),
                Vector3D.of(0, 1, 0),
                Vector3D.of(0, 0, 1),
                Vector3D.of(1, 1, 1));
        builder.append(vertices);

        // act
        final ConvexHull3D hull = builder.build();

        // assert
        checkHull(hull, vertices);

        Assertions.assertEquals(1.0 / 2.0, hull.getRegion().getSize(), TEST_EPS);
        assertEquals(6, hull.getFacets().size());
    }

    @Test
    void unitCube() {
        // arrange
        final List<Vector3D> vertices = Arrays.asList(
                Vector3D.ZERO,
                Vector3D.of(1, 0, 0),
                Vector3D.of(0, 1, 0),
                Vector3D.of(0, 0, 1),
                Vector3D.of(1, 1, 0),
                Vector3D.of(1, 0, 1),
                Vector3D.of(0, 1, 1),
                Vector3D.of(1, 1, 1));
        builder.append(vertices);

        // act
        final ConvexHull3D hull = builder.build();

        // assert
        checkHull(hull, vertices);

        Assertions.assertEquals(1.0, hull.getRegion().getSize(), TEST_EPS);
        Assertions.assertEquals(12, hull.getFacets().size());
    }

    @Test
    void unitCubeSequentially() {
        // arrange
        final List<Vector3D> vertices = Arrays.asList(
                Vector3D.ZERO,
                Vector3D.of(1, 0, 0),
                Vector3D.of(0, 1, 0),
                Vector3D.of(0, 0, 1),
                Vector3D.of(1, 1, 0),
                Vector3D.of(1, 0, 1),
                Vector3D.of(0, 1, 1),
                Vector3D.of(1, 1, 1));
        vertices.forEach(builder::append);

        // act
        final ConvexHull3D hull = builder.build();

        // assert
        checkHull(hull, vertices);

        Assertions.assertEquals(1.0, hull.getRegion().getSize(), TEST_EPS);
        Assertions.assertEquals(12, hull.getFacets().size());
    }

    @Test
    void multiplePoints() {
        // arrange
        final List<Vector3D> vertices = Arrays.asList(
                Vector3D.ZERO,
                Vector3D.of(1, 0, 0),
                Vector3D.of(0, 1, 0),
                Vector3D.of(0, 0, 1),
                Vector3D.of(1, 1, 0),
                Vector3D.of(1, 0, 1),
                Vector3D.of(0, 1, 1),
                Vector3D.of(1, 1, 1),
                Vector3D.of(10, 20, 30),
                Vector3D.of(-0.5, 0, 5));
        builder.append(vertices);

        // act
        final ConvexHull3D hull = builder.build();

        // assert
        checkHull(hull, vertices);

        Assertions.assertEquals(42.58333333333329, hull.getRegion().getSize(), TEST_EPS);
    }

    /**
     * Create 1000 points on a unit sphere. Then every point of the set must be a
     * vertex of the hull.
     */
    @Test
    void randomUnitPoints() {
        // arrange
        // All points in the set must be on the hull. This is a worst case scenario.
        final Set<Vector3D> set = createRandomPoints(1000, true);
        builder.append(set);

        // act
        final ConvexHull3D hull = builder.build();

        // assert
        final ConvexVolume region = hull.getRegion();
        Assertions.assertNotNull(region);
        final List<Vector3D> vertices = hull.getVertices();
        for (final Vector3D p : set) {
            Assertions.assertTrue(vertices.contains(p));
        }
        checkHull(hull, vertices);
        Assertions.assertEquals(1000, hull.getVertices().size());
    }

    @Test
    void randomPoints() {
        // arrange
        final Set<Vector3D> set = createRandomPoints(100000, false);
        builder.append(set);

        // act
        final ConvexHull3D hull = builder.build();

        // assert
        checkHull(hull, set);
    }

    @Test
    void randomPointsInTwoSets() {
        // arrange
        final Set<Vector3D> set1 = createRandomPoints(50000, false);
        final Set<Vector3D> set2 = createRandomPoints(50000, false);
        builder.append(set1);
        builder.append(set2);

        // act
        final ConvexHull3D hull = builder.build();

        // assert
        checkHull(hull, set1);
        checkHull(hull, set2);
    }

    @Test
    void randomPointsSequentially() {
        // arrange
        final List<Vector3D> list = new ArrayList<>(createRandomPoints(100, false));
        list.forEach(builder::append);

        // act
        final ConvexHull3D hull = builder.build();

        // assert
        checkHull(hull, list);
    }

    /**
     * Create a specified number of random points on the unit sphere.
     *
     * @param number    the given number.
     * @param normalize normalize the output points.
     * @return a specified number of random points on the unit sphere.
     */
    private Set<Vector3D> createRandomPoints(int number, boolean normalize) {
        final Set<Vector3D> set = EuclideanCollections.pointSet3D(TEST_PRECISION);
        for (int i = 0; i < number; i++) {
            if (normalize) {
                set.add(Vector3D.Unit.from(random.nextDouble(), random.nextDouble(), random.nextDouble()));
            } else {
                set.add(Vector3D.of(random.nextDouble(), random.nextDouble(), random.nextDouble()));
            }
        }
        return set;
    }

    /**
     * Check if the hull contains all the points in the given collection and checks if the volume is finite and
     * non-zero.
     */
    private void checkHull(ConvexHull3D hull, Collection<Vector3D> points) {
        final ConvexVolume region = hull.getRegion();
        Assertions.assertNotNull(region);
        Assertions.assertTrue(region.isFinite());
        Assertions.assertFalse(region.isEmpty());
        Assertions.assertFalse(hull.isDegenerate());

        for (final Vector3D p : points) {
            Assertions.assertTrue(region.contains(p));
        }

        checkFacets(hull);
    }

    private void checkFacets(final ConvexHull3D hull) {
        final  List<ConvexPolygon3D> polygons = hull.getFacets();
        Assertions.assertFalse(polygons.isEmpty());

        // Build an edge map to check if every facet has a neighbor and all edges share two facets.
        final Vector3D centroid = hull.getRegion().getCentroid();
        final Set<ConvexHull3D.Facet> facets = polygons.stream()
                .map(p -> new ConvexHull3D.Facet(p, centroid, TEST_PRECISION))
                .collect(Collectors.toSet());

        // Populate edgeMap.
        final Map<ConvexHull3D.Edge, ConvexHull3D.Facet> edgeMap = new HashMap<>();
        for (ConvexHull3D.Facet f : facets) {
            for (ConvexHull3D.Edge e : f.getEdges()) {
                edgeMap.put(e, f);
            }
        }

        // Check if all edges are shared by two facets.
        for (final ConvexHull3D.Facet f : facets) {
            for (final ConvexHull3D.Edge e : f.getEdges()) {
                Assertions.assertTrue(edgeMap.containsKey(e.getInverse()));
            }
        }
    }

    private void checkDegenerateHull(final ConvexHull3D hull, final Collection<Vector3D> points) {
        Assertions.assertTrue(hull.isDegenerate());
        Assertions.assertNull(hull.getRegion());
        Assertions.assertTrue(hull.getFacets().isEmpty());

        final List<Vector3D> vertices = hull.getVertices();
        for (final Vector3D p : points) {
            Assertions.assertTrue(vertices.contains(p));
        }
    }

}

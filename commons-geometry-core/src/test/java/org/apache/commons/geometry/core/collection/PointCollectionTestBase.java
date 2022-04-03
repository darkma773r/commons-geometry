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
package org.apache.commons.geometry.core.collection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.commons.geometry.core.Point;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;

/** Base test class for point collection types.
 * @param <P> Point type
 */
public abstract class PointCollectionTestBase<P extends Point<P>> {

    public static final double EPS = 1e-10;

    public static final Precision.DoubleEquivalence PRECISION =
            Precision.doubleEquivalenceOfEpsilon(EPS);

    /** Create an empty array of the target point type.
     * @return empty array of the target point type
     */
    protected abstract P[] createPointArray();

    /** Get a list of points with {@code NaN} coordinates.
     * @return list of points with {@code NaN} coordinates
     */
    protected abstract List<P> getNaNPoints();

    /** Get a list of points with infinite coordinates.
     * @return list of points with infinite coordinates
     */
    protected abstract List<P> getInfPoints();

    /** Get {@code cnt} number of unique test points that differ from each other in
     * each dimension by <em>at least</em> {@code eps}.
     * @param cnt number of points to return
     * @param eps minimum value that each point must differ from other points along
     *      each dimension
     * @return list of test points
     */
    protected abstract List<P> getTestPoints(int cnt, double eps);

    /** Get a list of points that lie {@code dist} distance from {@code pt}.
     * @param pt input point
     * @param dist distance from {@code pt}
     * @return list of points that lie {@code dist} distance from {@code pt}
     */
    protected abstract List<P> getTestPointsAtDistance(P pt, double dist);

    /** Get {@code cnt} number of unique test points that differ from each other in
     * each dimension by <em>at least</em> {@code eps}. The returned list is shuffled
     * using {@code rnd}.
     * @param cnt number of points to return
     * @param eps minimum value that each point must differ from other points along
     *      each dimension
     * @param rnd random instance used to shuffle the order of the points
     * @return randomly ordered list of test points
     */
    protected List<P> getTestPoints(final int cnt, final double eps, final Random rnd) {
        final List<P> pts = new ArrayList<>(getTestPoints(cnt, eps));
        Collections.shuffle(pts, rnd);

        return pts;
    }

    /** Return true if the given points are equivalent to each other using the given precision.
     * @param a first point
     * @param b second point
     * @param precision precision context
     * @return true if the two points are equivalent when compared using the given precision
     */
    protected abstract boolean eq(P a, P b, Precision.DoubleEquivalence precision);

    /** Compare two points with equal distances computed during a "closest first" ordering.
     * @param a first point
     * @param b second point
     * @return comparison of the two points
     */
    protected abstract int disambiguateClosestFirstOrder(P a, P b);

    /** Assert that {@code a} and {@code b} are equivalent using the given precision context.
     * @param a first point
     * @param b second point
     * @param precision precision context
     */
    protected void assertEq(final P a, final P b, final Precision.DoubleEquivalence precision) {
        assertTrue(eq(a, b, precision), () -> "Expected " + a + " and " + b + " to be equivalent");
    }

    /** Assert that {@code a} and {@code b} are not equivalent using the given precision context.
     * @param a first point
     * @param b second point
     * @param precision precision context
     */
    protected void assertNotEq(final P a, final P b, final Precision.DoubleEquivalence precision) {
        assertFalse(eq(a, b, precision), () -> "Expected " + a + " and " + b + " to not be equivalent");
    }

    /** Create a comparator for use in testing "closest first" ordering.
     * @param refPt reference point
     * @return comparator for use in testing "closest first" ordering
     */
    protected Comparator<P> createClosestFirstComparator(final P refPt) {
        final Comparator<P> cmp = (a, b) -> Double.compare(a.distance(refPt), b.distance(refPt));
        return cmp.thenComparing(this::disambiguateClosestFirstOrder);
    }

    /** Create a comparator for use in testing "farthest first" ordering.
     * @param refPt reference point
     * @return comparator for use in testing "farthest first" ordering
     */
    protected Comparator<P> createFarthestFirstComparator(final P refPt) {
        return createClosestFirstComparator(refPt).reversed();
    }

    /** Find the element in {@code list} farthest away from {@code refPt}.
     * @param refPt reference point
     * @param list list to search
     * @return element in {@code list} farthest from {@code refPt}
     */
    protected P findFarthest(final P refPt, final List<P> list) {
        final Comparator<P> cmp = createFarthestFirstComparator(refPt);

        P result = null;
        for (final P pt : list) {
            if (result == null || cmp.compare(pt, result) < 0) {
                result = pt;
            }
        }

        return result;
    }

    /** Check basic {@link DistanceOrdering} functionality.
     * @param <T> Element type
     * @param ordering instance under test
     * @param elements elements in the collection
     * @param nearToFarCmp comparator used to sort the elements in the collection
     *      from near to far
     */
    protected static <T> void checkDistanceOrdering(
            final DistanceOrdering<T> ordering,
            final List<T> elements,
            final Comparator<T> nearToFarCmp) {
        new DistanceOrderingChecker<>(ordering, elements, nearToFarCmp)
            .check();
    }

    /** Class used to assert basic {@link DistanceOrdering} functionality.
     * @param <T> Element type
     */
    public static class DistanceOrderingChecker<T> {

        /** View under test. */
        private final DistanceOrdering<T> ordering;

        /** Expected near to far list. */
        private final List<T> expectedNearToFar;

        /** Expected far to near list. */
        private final List<T> expectedFarToNear;

        /** Construct a new instance for testing the given {@code ordering}.
         * @param ordering instance under test
         * @param elements elements in the collection
         * @param nearToFarCmp comparator used to sort the elements in the collection
         *      from near to far
         */
        public DistanceOrderingChecker(
                final DistanceOrdering<T> ordering,
                final List<T> elements,
                final Comparator<T> nearToFarCmp) {
            this.ordering = ordering;

            this.expectedNearToFar = new ArrayList<>(elements);
            Collections.sort(this.expectedNearToFar, nearToFarCmp);

            this.expectedFarToNear = new ArrayList<>(this.expectedNearToFar);
            Collections.reverse(expectedFarToNear);
        }

        public void check() {
            checkNearest();
            checkKNearest();
            checkNearToFar();

            checkFarthest();
            checkKFarthest();
            checkFarToNear();
        }

        private void checkNearest() {
            final T expected = expectedNearToFar.isEmpty() ?
                    null :
                    expectedNearToFar.get(0);

            Assertions.assertEquals(expected, ordering.nearest());
        }

        private void checkKNearest() {
            Assertions.assertThrows(IllegalArgumentException.class, () -> ordering.kNearest(-1));

            Assertions.assertEquals(Collections.emptyList(), ordering.kNearest(0));

            for (int i = 1; i <= expectedNearToFar.size(); ++i) {
                Assertions.assertEquals(expectedNearToFar.subList(0, i), ordering.kNearest(i));
            }

            Assertions.assertEquals(expectedNearToFar, ordering.kNearest(expectedNearToFar.size() + 1));
        }

        private void checkNearToFar() {
            final List<T> actual = ordering.nearToFar().stream()
                    .collect(Collectors.toList());

            Assertions.assertEquals(expectedNearToFar.size(), actual.size(), "Unexpected size");

            for (int i = 0; i < expectedNearToFar.size(); ++i) {
                Assertions.assertEquals(expectedNearToFar.get(i), actual.get(i),
                        "Unexpected element at index " + i);
            }
        }

        private void checkFarthest() {
            final T expected = expectedFarToNear.isEmpty() ?
                    null :
                        expectedFarToNear.get(0);

            Assertions.assertEquals(expected, ordering.farthest());
        }

        private void checkKFarthest() {
            Assertions.assertThrows(IllegalArgumentException.class, () -> ordering.kFarthest(-1));

            Assertions.assertEquals(Collections.emptyList(), ordering.kFarthest(0));

            for (int i = 1; i <= expectedFarToNear.size(); ++i) {
                Assertions.assertEquals(expectedFarToNear.subList(0, i), ordering.kFarthest(i));
            }

            Assertions.assertEquals(expectedFarToNear, ordering.kFarthest(expectedFarToNear.size() + 1));
        }

        private void checkFarToNear() {
            final List<T> actual = ordering.farToNear().stream()
                    .collect(Collectors.toList());

            Assertions.assertEquals(expectedFarToNear.size(), actual.size(), "Unexpected size");

            for (int i = 0; i < expectedFarToNear.size(); ++i) {
                Assertions.assertEquals(expectedFarToNear.get(i), actual.get(i),
                        "Unexpected element at index " + i);
            }
        }
    }
}

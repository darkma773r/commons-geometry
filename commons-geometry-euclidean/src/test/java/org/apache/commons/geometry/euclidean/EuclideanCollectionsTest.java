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
package org.apache.commons.geometry.euclidean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.geometry.core.collection.EquivalenceMap;
import org.apache.commons.geometry.core.collection.EquivalenceSet;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EuclideanCollectionsTest {

    private static final double EPS = 1e-1;

    private static final Precision.DoubleEquivalence PRECISION =
            Precision.doubleEquivalenceOfEpsilon(EPS);

//    @Test
//    void scratch() {
//        final Vector3D v = Vector3D.of(1, -0.1, 1.7);
//        final EquivalenceSet<Vector3D> set = EuclideanCollections.equivalenceSet3D(PRECISION);
//
//        set.add(v);
//
//        final Vector3D a = Vector3D.parse("(0.9200000000000006, -0.15999999999999967, 1.6400000000000012)");
//        final Vector3D b = Vector3D.parse("(1.440000000000001, 1.7600000000000013, 1.480000000000001)");
//
//        set.add(Vector3D.of(-0.3199999999999997, -0.9199999999999999, 1.0000000000000007));
////        5281
////        > (0.9600000000000006, 0.32000000000000034, 1.8800000000000014)
////        5281
////        > (1.440000000000001, 1.7600000000000013, 1.480000000000001)
//
//        System.out.println(set.resolve(a));
//
//        System.out.println();
//
//        final List<Vector3D> testPoints = new ArrayList<>();
//        EuclideanTestUtils.permute(-1, 2, 0.04, (x, y, z) -> testPoints.add(Vector3D.of(x, y, z)));
//        Collections.shuffle(testPoints, new Random(2L));
//
//        for (final Vector3D t : testPoints) {
//            Vector3D resolved = set.resolve(t);
//
//            System.out.println(set.size());
//
//            // the resolved entry should be the same as the input key iff
//            // the test point is equivalent to the input
//            if (t.eq(v, PRECISION)) {
//                System.out.println(v + " eq " + t + ", resolved = " + resolved);
//                Assertions.assertSame(v, resolved);
//                Assertions.assertTrue(set.contains(t));
//            } else {
//                Assertions.assertNotSame(v, resolved);
//
//                if (set.size() > 5279) {
//                    System.out.println("> " + t);
//                }
//                set.add(t);
//            }
//        }
//    }

    @Test
    void testEquivalenceSet3D_nullArgument() {
        // act/assert
        Assertions.assertThrows(NullPointerException.class,
                () -> EuclideanCollections.equivalenceSet3D(null));
    }

    @Test
    void testEquivalenceSet3D_consistencyWithEq() {
        // arrange
        final Vector3D v = Vector3D.of(1, -0.1, 1.7);
        final EquivalenceSet<Vector3D> set = EuclideanCollections.equivalenceSet3D(PRECISION);

        set.add(v);

        final List<Vector3D> testPoints = new ArrayList<>();
        EuclideanTestUtils.permute(-1, 2, 0.04, (x, y, z) -> testPoints.add(Vector3D.of(x, y, z)));
        Collections.shuffle(testPoints, new Random(2L));

        final Vector3D x = Vector3D.parse("(0.9200000000000006, -0.15999999999999967, 1.6400000000000012)");
        final Vector3D y = Vector3D.parse("(1.440000000000001, 1.7600000000000013, 1.480000000000001)");
        set.add(y);
        System.out.println("eq " + x.eq(v, PRECISION));
        System.out.println("eq " + v.eq(x, PRECISION));
        System.out.println("contains " + set.contains(x));
        System.out.println("resolved " + set.resolve(x));

        System.out.println();

        // act/assert
        int eqCount = 0;
        int neqCount = 0;
        for (final Vector3D t : testPoints) {
            Vector3D resolved = set.resolve(t);

            // the resolved entry should be the same as the input key iff
            // the test point is equivalent to the input
            if (t.eq(v, PRECISION)) {
                ++eqCount;

                System.out.println(v + " eq " + t + ", resolved = " + resolved);
                Assertions.assertSame(v, resolved);
                Assertions.assertTrue(set.contains(t));
            } else {
                ++neqCount;

                Assertions.assertNotSame(v, resolved);

//                System.out.println("> " + t);
                set.add(t);
            }
        }

        Assertions.assertTrue(eqCount > 0);
        Assertions.assertTrue(neqCount > 0);
    }

    @Test
    void testEquivalenceMap3D_nullArgument() {
        // act/assert
        Assertions.assertThrows(NullPointerException.class,
                () -> EuclideanCollections.equivalenceMap3D(null));
    }

    @Test
    void testEquivalenceMap3D_consistencyWithEq() {
        // arrange
        final Vector3D v = Vector3D.of(1, -0.1, 1.7);
        final EquivalenceMap<Vector3D, Integer> map = EuclideanCollections.equivalenceMap3D(PRECISION);

        map.put(v, 1);

        final List<Vector3D> testPoints = new ArrayList<>();
        EuclideanTestUtils.permute(-1, 2, 0.04, (x, y, z) -> testPoints.add(Vector3D.of(x, y, z)));
        Collections.shuffle(testPoints, new Random(1L));

        // act/assert
        int eqCount = 0;
        int neqCount = 0;
        for (final Vector3D t : testPoints) {
            Vector3D resolvedKey = map.resolveKey(t);
            Integer value = map.get(t);

            // the resolved key should be the same as the input key iff
            // the test point is equivalent to the input key
            if (t.eq(v, PRECISION)) {
                ++eqCount;

                Assertions.assertSame(v, resolvedKey);
                Assertions.assertEquals(1, map.get(t));
                Assertions.assertTrue(map.containsKey(t));

            } else {
                ++neqCount;

                Assertions.assertNotSame(v, resolvedKey);
                Assertions.assertNotEquals(1, value);

                map.put(t, -1);
            }
        }

        Assertions.assertTrue(eqCount > 0);
        Assertions.assertTrue(neqCount > 0);
    }
}

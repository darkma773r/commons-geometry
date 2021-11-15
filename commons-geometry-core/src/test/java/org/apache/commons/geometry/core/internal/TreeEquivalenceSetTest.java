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
package org.apache.commons.geometry.core.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TreeEquivalenceSetTest {
    private static final double EPS = 1e-1;

    private static final Precision.DoubleEquivalence PRECISION =
            Precision.doubleEquivalenceOfEpsilon(EPS);

    @Test
    void testResolve_emptySet() {
        // arrange
        final TreeEquivalenceSet<Double> set =
                new TreeEquivalenceSet<>(PRECISION::compare);

        // act
        Assertions.assertNull(set.resolve(1.0));
    }

    @Test
    void testResolve_populated() {
        // arrange
        final TreeEquivalenceSet<Double> set =
                new TreeEquivalenceSet<>(PRECISION::compare);

        for (double x = -5.0; x < -0.5; x += 0.7) {
            set.add(x);
        }

        set.add(0.0);
        set.add(1.0);

        for (double x = 1.6; x < 10.0; x += 0.7) {
            set.add(x);
        }

        // act
        Assertions.assertNull(set.resolve(-0.11));
        Assertions.assertEquals(0.0, set.resolve(-0.09));
        Assertions.assertEquals(0.0, set.resolve(0.0));
        Assertions.assertEquals(0.0, set.resolve(0.09));
        Assertions.assertNull(set.resolve(0.11));

        Assertions.assertNull(set.resolve(0.89));
        Assertions.assertEquals(1, set.resolve(0.91));
        Assertions.assertEquals(1, set.resolve(1.0));
        Assertions.assertEquals(1, set.resolve(1.09));
        Assertions.assertNull(set.resolve(1.11));
    }

    @Test
    void testContains() {
        // arrange
        final TreeEquivalenceSet<Double> set =
                new TreeEquivalenceSet<>(PRECISION::compare);

        set.add(0.0);
        set.add(1.0);

        // act
        Assertions.assertFalse(set.contains(-0.11));
        Assertions.assertTrue(set.contains(-0.09));
        Assertions.assertTrue(set.contains(0.0));
        Assertions.assertTrue(set.contains(0.09));
        Assertions.assertFalse(set.contains(0.11));

        Assertions.assertFalse(set.contains(0.89));
        Assertions.assertTrue(set.contains(0.91));
        Assertions.assertTrue(set.contains(1.0));
        Assertions.assertTrue(set.contains(1.09));
        Assertions.assertFalse(set.contains(1.11));
    }

    @Test
    void testInsertionOrder() {
        // arrange
        final TreeEquivalenceSet<Double> set =
                new TreeEquivalenceSet<>(PRECISION::compare);

        final double x = 1.0;
        final double y = 1.075;
        final double z = 1.15;

        // act/assert
        set.add(x);
        set.add(y);
        set.add(z);

        Assertions.assertEquals(2, set.size());
        Assertions.assertEquals(x, set.resolve(x));
        Assertions.assertEquals(x, set.resolve(y));
        Assertions.assertEquals(z, set.resolve(z));

        Assertions.assertTrue(set.remove(x));
        Assertions.assertTrue(set.remove(y));
        Assertions.assertFalse(set.remove(z));

        set.add(y);
        set.add(x);
        set.add(z);

        Assertions.assertEquals(1, set.size());
        Assertions.assertEquals(y, set.resolve(x));
        Assertions.assertEquals(y, set.resolve(y));
        Assertions.assertEquals(y, set.resolve(z));

        Assertions.assertTrue(set.remove(x));
        Assertions.assertFalse(set.remove(y));
        Assertions.assertFalse(set.remove(z));
    }

    @Test
    void testConsistency() {
        // arrange
        final Comparator<Double> cmp = PRECISION::compare;
        final TreeEquivalenceSet<Double> set = new TreeEquivalenceSet<>(cmp);

        final double x = 1.1;

        final List<Double> testValues = new ArrayList<>();
        for (double v = -2.0; v < 4.0; v += 0.03) {
            testValues.add(v);
        }
        Collections.shuffle(testValues, new Random());

        set.add(x);

        // act/assert
        int eqCount = 0;
        int neqCount = 0;
        for (Double v : testValues) {
            final Double resolved = set.resolve(v);

            // the resolved value should be the same as x iff
            // the test value is equivalent to x
            if (cmp.compare(x, v) == 0) {
                ++eqCount;

                Assertions.assertEquals(resolved.doubleValue(), x);
                Assertions.assertTrue(set.contains(v));
            } else {
                ++neqCount;
            }

            set.add(v);
        }

        Assertions.assertTrue(eqCount > 0);
        Assertions.assertTrue(neqCount > 0);

        Assertions.assertEquals(x, set.resolve(x).doubleValue());
        for (Double v : testValues) {
            Assertions.assertTrue(set.contains(v));
        }
    }
}

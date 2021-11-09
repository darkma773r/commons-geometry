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
                new TreeEquivalenceSet<>((a, b) -> PRECISION.compare(a, b));

        // act
        Assertions.assertNull(set.resolve(1.0));
    }

    @Test
    void testResolve_populated() {
        // arrange
        final TreeEquivalenceSet<Double> set =
                new TreeEquivalenceSet<>((a, b) -> PRECISION.compare(a, b));

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
                new TreeEquivalenceSet<>((a, b) -> PRECISION.compare(a, b));

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
                new TreeEquivalenceSet<>((a, b) -> PRECISION.compare(a, b));

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
}

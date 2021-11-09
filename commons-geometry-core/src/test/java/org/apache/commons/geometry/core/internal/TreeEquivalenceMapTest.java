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

class TreeEquivalenceMapTest {

    private static final double EPS = 1e-1;

    private static final Precision.DoubleEquivalence PRECISION =
            Precision.doubleEquivalenceOfEpsilon(EPS);

    @Test
    void testResolveKey_emptyMap() {
        // arrange
        final TreeEquivalenceMap<Double, Integer> map =
                new TreeEquivalenceMap<>((a, b) -> PRECISION.compare(a, b));

        // act
        Assertions.assertNull(map.resolveKey(1.0));
    }

    @Test
    void testResolveKey_populated() {
        // arrange
        final TreeEquivalenceMap<Double, Integer> map =
                new TreeEquivalenceMap<>((a, b) -> PRECISION.compare(a, b));

        map.put(0.0, 1);
        map.put(1.0, 2);

        // act
        Assertions.assertNull(map.resolveKey(-0.11));
        Assertions.assertEquals(0.0, map.resolveKey(-0.09));
        Assertions.assertEquals(0.0, map.resolveKey(0.0));
        Assertions.assertEquals(0.0, map.resolveKey(0.09));
        Assertions.assertNull(map.resolveKey(0.11));

        Assertions.assertNull(map.resolveKey(0.89));
        Assertions.assertEquals(1.0, map.resolveKey(0.91));
        Assertions.assertEquals(1.0, map.resolveKey(1.0));
        Assertions.assertEquals(1.0, map.resolveKey(1.09));
        Assertions.assertNull(map.resolveKey(1.11));
    }

    @Test
    void testPutGet() {
        // arrange
        final TreeEquivalenceMap<Double, Integer> map =
                new TreeEquivalenceMap<>((a, b) -> PRECISION.compare(a, b));

        for (double x = -5.0; x < -0.5; x += 0.7) {
            map.put(x, -1);
        }

        map.put(0.0, 1);
        map.put(1.0, 2);

        for (double x = 1.6; x < 10.0; x += 0.7) {
            map.put(x, -1);
        }

        // act
        Assertions.assertNull(map.get(-0.11));
        Assertions.assertEquals(1, map.get(-0.09));
        Assertions.assertEquals(1, map.get(0.0));
        Assertions.assertEquals(1, map.get(0.09));
        Assertions.assertNull(map.get(0.11));

        Assertions.assertNull(map.get(0.89));
        Assertions.assertEquals(2, map.get(0.91));
        Assertions.assertEquals(2, map.get(1.0));
        Assertions.assertEquals(2, map.get(1.09));
        Assertions.assertNull(map.get(1.11));
    }

    @Test
    void testContainsKey() {
        // arrange
        final TreeEquivalenceMap<Double, Integer> map =
                new TreeEquivalenceMap<>((a, b) -> PRECISION.compare(a, b));

        map.put(0.0, 1);
        map.put(1.0, 2);

        // act
        Assertions.assertFalse(map.containsKey(-0.11));
        Assertions.assertTrue(map.containsKey(-0.09));
        Assertions.assertTrue(map.containsKey(0.0));
        Assertions.assertTrue(map.containsKey(0.09));
        Assertions.assertFalse(map.containsKey(0.11));

        Assertions.assertFalse(map.containsKey(0.89));
        Assertions.assertTrue(map.containsKey(0.91));
        Assertions.assertTrue(map.containsKey(1.0));
        Assertions.assertTrue(map.containsKey(1.09));
        Assertions.assertFalse(map.containsKey(1.11));
    }

    @Test
    void testInsertionOrder() {
        // arrange
        final TreeEquivalenceMap<Double, Integer> map =
                new TreeEquivalenceMap<>((a, b) -> PRECISION.compare(a, b));

        final double x = 1.0;
        final double y = 1.075;
        final double z = 1.15;

        // act/assert
        map.put(x, 0);
        map.put(y, 1);
        map.put(z, 2);

        Assertions.assertEquals(2, map.size());
        Assertions.assertEquals(1, map.get(x));
        Assertions.assertEquals(1, map.get(y));
        Assertions.assertEquals(2, map.get(z));

        Assertions.assertEquals(1, map.remove(x));
        Assertions.assertEquals(2, map.remove(y));
        Assertions.assertNull(map.remove(z));

        map.put(y, 1);
        map.put(x, 0);
        map.put(z, 2);

        Assertions.assertEquals(1, map.size());
        Assertions.assertEquals(2, map.get(x));
        Assertions.assertEquals(2, map.get(y));
        Assertions.assertEquals(2, map.get(z));

        Assertions.assertEquals(2, map.remove(x));
        Assertions.assertNull(map.remove(y));
        Assertions.assertNull(map.remove(z));
    }
}

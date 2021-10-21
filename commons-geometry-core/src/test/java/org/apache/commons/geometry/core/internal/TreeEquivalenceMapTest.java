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
import java.util.List;
import java.util.Random;

import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class TreeEquivalenceMapTest {

    private static final double EPS = 1e-3;

    private static final Precision.DoubleEquivalence PRECISION =
            Precision.doubleEquivalenceOfEpsilon(EPS);

    @Test
    public void testGetStoredKey_returnsCorrectKeyForInsertionOrder() {
        // arrange
        final Random rand = new Random(1L);
        final double min = 0;
        final double max = 1;
        final double[] arr = createShuffledArray(min, max, EPS * 0.3, rand);

        // act/assert
        for (double test = min; test <= max; test += EPS * 0.2) {
            checkGetStoredKey(arr, test);
        }
    }

    private static void checkGetStoredKey(final double[] arr, final double test) {
        final TreeEquivalenceMap<Double, Integer> map =
                new TreeEquivalenceMap<>((a, b) -> PRECISION.compare(a, b));
        for (int i = 0; i < arr.length; ++i) {
            map.put(arr[i], i);
        }

        final double key = map.getStoredKey(test);
        final int value = -1;

        map.put(key, value);

        Assertions.assertEquals(value, map.get(test), () -> "Invalid value for map value for " + test);
    }

    private static double[] createShuffledArray(final double start, final double end, final double delta,
            final Random rand) {
        final List<Double> list = new ArrayList<>();
        for (double val = start; val <= end; val += delta) {
            list.add(val);
        }
        Collections.shuffle(list, rand);

        final double[] arr = new double[list.size()];
        for (int i = 0; i < arr.length; ++i) {
            arr[i] = list.get(i);
        }

        return arr;
    }
}

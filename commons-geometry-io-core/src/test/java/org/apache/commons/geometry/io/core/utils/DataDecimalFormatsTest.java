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
package org.apache.commons.geometry.io.core.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DataDecimalFormatsTest {

    @Test
    public void testDoubleToString() {
        // arrange
        final DataDecimalFormat fmt = DataDecimalFormats.DOUBLE_TO_STRING;

        // act/assert
        checkFormatSpecial(fmt);

        checkFormat(fmt, 0.0, "0.0");
        checkFormat(fmt, -0.0, "-0.0");

        checkFormat(fmt, 0.5 * Float.MAX_VALUE, "1.7014117331926443E38");
        checkFormat(fmt, -1.0 / 1.9175e20, "-5.2151238591916555E-21");


        checkFormat(fmt, Double.MAX_VALUE, "1.7976931348623157E308");
        checkFormat(fmt, Double.MIN_VALUE, "4.9E-324");
        checkFormat(fmt, Double.MIN_NORMAL, "2.2250738585072014E-308");
        checkFormat(fmt, Math.PI, "3.141592653589793");
        checkFormat(fmt, Math.E, "2.718281828459045");
    }

    @Test
    public void testFloatToString() {
        // arrange
        final DataDecimalFormat fmt = DataDecimalFormats.FLOAT_TO_STRING;

        // act/assert
        checkFormatSpecial(fmt);

        checkFormat(fmt, 0.0, "0.0");
        checkFormat(fmt, -0.0, "-0.0");

        checkFormat(fmt, 0.5 * Float.MAX_VALUE, "1.7014117E38");
        checkFormat(fmt, -1.0 / 1.9175e20, "-5.2151238E-21");

        checkFormat(fmt, Double.MAX_VALUE, "Infinity");
        checkFormat(fmt, Double.MIN_VALUE, "0.0");
        checkFormat(fmt, Double.MIN_NORMAL, "0.0");
        checkFormat(fmt, Math.PI, "3.1415927");
        checkFormat(fmt, Math.E, "2.7182817");
    }

    @Test
    public void testDefault() {
        // arrange
        final int precision = 4;
        final int maxFractionDigits = 2;

        // act
        final DataDecimalFormat fmt = DataDecimalFormats.createDefault(precision, maxFractionDigits);

        // act/assert
        checkFormatSpecial(fmt);

        checkFormat(fmt, 0.0, "0.0");
        checkFormat(fmt, -0.0, "-0.0");
        checkFormat(fmt, 1.0, "1.0");
        checkFormat(fmt, -1.0, "-1.0");

        checkFormat(fmt, 12345.01, "12350.0");
        checkFormat(fmt, 1.2345, "1.23");

        checkFormat(fmt, 1.25e-3, "0.0012");
        checkFormat(fmt, -9.975e-4, "-9.98E-4");
        checkFormat(fmt, -9_999_999, "-10000000.0");
        checkFormat(fmt, 1.00001e7, "1.0E7");

        checkFormat(fmt, Double.MAX_VALUE, "1.8E308");
        checkFormat(fmt, Double.MIN_VALUE, "4.9E-324");
        checkFormat(fmt, Double.MIN_NORMAL, "2.23E-308");
        checkFormat(fmt, Math.PI, "3.14");
        checkFormat(fmt, Math.E, "2.72");
    }

    private static void checkFormat(final DataDecimalFormat fmt, final double d, final String str) {
        Assertions.assertEquals(str, fmt.format(d));
    }

    private static void checkFormatSpecial(final DataDecimalFormat fmt) {
        checkFormat(fmt, Double.NaN, "NaN");
        checkFormat(fmt, Double.POSITIVE_INFINITY, "Infinity");
        checkFormat(fmt, Double.NEGATIVE_INFINITY, "-Infinity");
    }
}

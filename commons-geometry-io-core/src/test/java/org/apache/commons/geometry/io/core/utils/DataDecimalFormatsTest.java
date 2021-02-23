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

        checkFormat(fmt, 0.00001, "1.0E-5");
        checkFormat(fmt, -0.0001, "-1.0E-4");
        checkFormat(fmt, 0.001, "0.001");
        checkFormat(fmt, -0.01, "-0.01");
        checkFormat(fmt, 0.1, "0.1");
        checkFormat(fmt, -0.0, "-0.0");
        checkFormat(fmt, 0.0, "0.0");
        checkFormat(fmt, -1.0, "-1.0");
        checkFormat(fmt, 10.0, "10.0");
        checkFormat(fmt, -100.0, "-100.0");
        checkFormat(fmt, 1000.0, "1000.0");
        checkFormat(fmt, -10000.0, "-10000.0");
        checkFormat(fmt, 100000.0, "100000.0");
        checkFormat(fmt, -1000000.0, "-1000000.0");
        checkFormat(fmt, 10000000.0, "1.0E7");
        checkFormat(fmt, -100000000.0, "-1.0E8");

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

        checkFormat(fmt, 0.00001, "1.0E-5");
        checkFormat(fmt, -0.0001, "-1.0E-4");
        checkFormat(fmt, 0.001, "0.001");
        checkFormat(fmt, -0.01, "-0.01");
        checkFormat(fmt, 0.1, "0.1");
        checkFormat(fmt, -0.0, "-0.0");
        checkFormat(fmt, 0.0, "0.0");
        checkFormat(fmt, -1.0, "-1.0");
        checkFormat(fmt, 10.0, "10.0");
        checkFormat(fmt, -100.0, "-100.0");
        checkFormat(fmt, 1000.0, "1000.0");
        checkFormat(fmt, -10000.0, "-10000.0");
        checkFormat(fmt, 100000.0, "100000.0");
        checkFormat(fmt, -1000000.0, "-1000000.0");
        checkFormat(fmt, 10000000.0, "1.0E7");
        checkFormat(fmt, -100000000.0, "-1.0E8");

        checkFormat(fmt, 0.5 * Float.MAX_VALUE, "1.7014117E38");
        checkFormat(fmt, -1.0 / 1.9175e20, "-5.2151238E-21");

        checkFormat(fmt, Double.MAX_VALUE, "Infinity");
        checkFormat(fmt, Double.MIN_VALUE, "0.0");
        checkFormat(fmt, Double.MIN_NORMAL, "0.0");
        checkFormat(fmt, Math.PI, "3.1415927");
        checkFormat(fmt, Math.E, "2.7182817");
    }

    @Test
    public void testDefault_noPrecisionLimit() {
        // arrange
        final int precision = -1;

        // act
        final DataDecimalFormat fmt = DataDecimalFormats.createDefault(precision);

        // act/assert
        checkFormatSpecial(fmt);

        checkFormat(fmt, 0.00001, "1.0E-5");
        checkFormat(fmt, -0.0001, "-1.0E-4");
        checkFormat(fmt, 0.001, "0.001");
        checkFormat(fmt, -0.01, "-0.01");
        checkFormat(fmt, 0.1, "0.1");
        checkFormat(fmt, -0.0, "-0.0");
        checkFormat(fmt, 0.0, "0.0");
        checkFormat(fmt, -1.0, "-1.0");
        checkFormat(fmt, 10.0, "10.0");
        checkFormat(fmt, -100.0, "-100.0");
        checkFormat(fmt, 1000.0, "1000.0");
        checkFormat(fmt, -10000.0, "-10000.0");
        checkFormat(fmt, 100000.0, "100000.0");
        checkFormat(fmt, -1000000.0, "-1000000.0");
        checkFormat(fmt, 10000000.0, "1.0E7");
        checkFormat(fmt, -100000000.0, "-1.0E8");

        checkFormat(fmt, 1.25e-3, "0.00125");
        checkFormat(fmt, -9.975e-4, "-9.975E-4");
        checkFormat(fmt, -9_999_999, "-9999999.0");
        checkFormat(fmt, 1.00001e7, "1.00001E7");

        checkFormat(fmt, Double.MAX_VALUE, "1.7976931348623157E308");
        checkFormat(fmt, Double.MIN_VALUE, "4.9E-324");
        checkFormat(fmt, Double.MIN_NORMAL, "2.2250738585072014E-308");
        checkFormat(fmt, Math.PI, "3.141592653589793");
        checkFormat(fmt, Math.E, "2.718281828459045");
    }

    @Test
    public void testDefault_withPrecisionLimit() {
        // arrange
        final int precision = 4;
        // act
        final DataDecimalFormat fmt = DataDecimalFormats.createDefault(precision);

        // act/assert
        checkFormatSpecial(fmt);

        checkFormat(fmt, 0.00001, "1.0E-5");
        checkFormat(fmt, -0.0001, "-1.0E-4");
        checkFormat(fmt, 0.001, "0.001");
        checkFormat(fmt, -0.01, "-0.01");
        checkFormat(fmt, 0.1, "0.1");
        checkFormat(fmt, -0.0, "-0.0");
        checkFormat(fmt, 0.0, "0.0");
        checkFormat(fmt, -1.0, "-1.0");
        checkFormat(fmt, 10.0, "10.0");
        checkFormat(fmt, -100.0, "-100.0");
        checkFormat(fmt, 1000.0, "1000.0");
        checkFormat(fmt, -10000.0, "-10000.0");
        checkFormat(fmt, 100000.0, "100000.0");
        checkFormat(fmt, -1000000.0, "-1000000.0");
        checkFormat(fmt, 10000000.0, "1.0E7");
        checkFormat(fmt, -100000000.0, "-1.0E8");

        checkFormat(fmt, 12345.01, "12350.0");
        checkFormat(fmt, 1.2345, "1.234");

        checkFormat(fmt, 1.25e-3, "0.00125");
        checkFormat(fmt, -9.975e-4, "-9.975E-4");
        checkFormat(fmt, -9_999_999, "-1.0E7");
        checkFormat(fmt, 1.00001e7, "1.0E7");

        checkFormat(fmt, Double.MAX_VALUE, "1.798E308");
        checkFormat(fmt, Double.MIN_VALUE, "4.9E-324");
        checkFormat(fmt, Double.MIN_NORMAL, "2.225E-308");
        checkFormat(fmt, Math.PI, "3.142");
        checkFormat(fmt, Math.E, "2.718");
    }

    @Test
    public void testPlain_noPrecisionLimit_noMinExponent() {
        // arrange
        final int precision = -1;
        final int minExponent = Integer.MIN_VALUE;

        // act
        final DataDecimalFormat fmt = DataDecimalFormats.createPlain(precision, minExponent);

        // act/assert
        checkFormatSpecial(fmt);

        checkFormat(fmt, 0.00001, "0.00001");
        checkFormat(fmt, -0.0001, "-0.0001");
        checkFormat(fmt, 0.001, "0.001");
        checkFormat(fmt, -0.01, "-0.01");
        checkFormat(fmt, 0.1, "0.1");
        checkFormat(fmt, -0.0, "-0.0");
        checkFormat(fmt, 0.0, "0.0");
        checkFormat(fmt, -1.0, "-1.0");
        checkFormat(fmt, 10.0, "10.0");
        checkFormat(fmt, -100.0, "-100.0");
        checkFormat(fmt, 1000.0, "1000.0");
        checkFormat(fmt, -10000.0, "-10000.0");
        checkFormat(fmt, 100000.0, "100000.0");
        checkFormat(fmt, -1000000.0, "-1000000.0");
        checkFormat(fmt, 10000000.0, "10000000.0");
        checkFormat(fmt, -100000000.0, "-100000000.0");

        checkFormat(fmt, 1.25e-3, "0.00125");
        checkFormat(fmt, -9.975e-4, "-0.0009975");
        checkFormat(fmt, -9_999_999, "-9999999.0");
        checkFormat(fmt, 1.00001e7, "10000100.0");

        checkFormat(fmt, Float.MAX_VALUE, "340282346638528860000000000000000000000.0");
        checkFormat(fmt, Float.MIN_VALUE, "0.000000000000000000000000000000000000000000001401298464324817");
        checkFormat(fmt, Float.MIN_NORMAL, "0.000000000000000000000000000000000000011754943508222875");
        checkFormat(fmt, Math.PI, "3.141592653589793");
        checkFormat(fmt, Math.E, "2.718281828459045");
    }

    @Test
    public void testPlain_withPrecisionLimit_noMinExponent() {
        // arrange
        final int precision = 3;
        final int maxFractionDigits = Integer.MIN_VALUE;

        // act
        final DataDecimalFormat fmt = DataDecimalFormats.createPlain(precision, maxFractionDigits);

        // act/assert
        checkFormatSpecial(fmt);

        checkFormat(fmt, 0.00001, "0.00001");
        checkFormat(fmt, -0.0001, "-0.0001");
        checkFormat(fmt, 0.001, "0.001");
        checkFormat(fmt, -0.01, "-0.01");
        checkFormat(fmt, 0.1, "0.1");
        checkFormat(fmt, -0.0, "-0.0");
        checkFormat(fmt, 0.0, "0.0");
        checkFormat(fmt, -1.0, "-1.0");
        checkFormat(fmt, 10.0, "10.0");
        checkFormat(fmt, -100.0, "-100.0");
        checkFormat(fmt, 1000.0, "1000.0");
        checkFormat(fmt, -10000.0, "-10000.0");
        checkFormat(fmt, 100000.0, "100000.0");
        checkFormat(fmt, -1000000.0, "-1000000.0");
        checkFormat(fmt, 10000000.0, "10000000.0");
        checkFormat(fmt, -100000000.0, "-100000000.0");

        checkFormat(fmt, 1.25e-3, "0.00125");
        checkFormat(fmt, -9.975e-4, "-0.000998");
        checkFormat(fmt, -9_999_999, "-10000000.0");
        checkFormat(fmt, 1.00001e7, "10000000.0");

        checkFormat(fmt, Float.MAX_VALUE, "340000000000000000000000000000000000000.0");
        checkFormat(fmt, Float.MIN_VALUE, "0.0000000000000000000000000000000000000000000014");
        checkFormat(fmt, Float.MIN_NORMAL, "0.0000000000000000000000000000000000000118");
        checkFormat(fmt, Math.PI, "3.14");
        checkFormat(fmt, Math.E, "2.72");
    }

    @Test
    public void testPlain_withPrecisionLimit_withMinExponent() {
        // arrange
        final int precision = 4;
        final int minExponent = -2;

        // act
        final DataDecimalFormat fmt = DataDecimalFormats.createPlain(precision, minExponent);

        // act/assert
//        checkFormatSpecial(fmt);
//
//        checkFormat(fmt, 0.00001, "0.0");
//        checkFormat(fmt, -0.0001, "0.0");
        checkFormat(fmt, 0.001, "0.0");
        checkFormat(fmt, -0.01, "-0.01");
        checkFormat(fmt, 0.1, "0.1");
        checkFormat(fmt, -0.0, "-0.0");
        checkFormat(fmt, 0.0, "0.0");
        checkFormat(fmt, -1.0, "-1.0");
        checkFormat(fmt, 10.0, "10.0");
        checkFormat(fmt, -100.0, "-100.0");
        checkFormat(fmt, 1000.0, "1000.0");
        checkFormat(fmt, -10000.0, "-10000.0");
        checkFormat(fmt, 100000.0, "100000.0");
        checkFormat(fmt, -1000000.0, "-1000000.0");
        checkFormat(fmt, 10000000.0, "10000000.0");
        checkFormat(fmt, -100000000.0, "-100000000.0");

        checkFormat(fmt, 1.25e-3, "0.0");
        checkFormat(fmt, -9.975e-4, "0.0");
        checkFormat(fmt, -9_999_999, "-10000000.0");
        checkFormat(fmt, 1.00001e7, "10000000.0");

        checkFormat(fmt, Float.MAX_VALUE, "340300000000000000000000000000000000000.0");
        checkFormat(fmt, Float.MIN_VALUE, "0.0");
        checkFormat(fmt, Float.MIN_NORMAL, "0.0");
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
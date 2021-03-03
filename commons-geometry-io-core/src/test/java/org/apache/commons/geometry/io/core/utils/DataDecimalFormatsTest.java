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

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.IntFunction;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DataDecimalFormatsTest {

    private static final double[] EXAMPLE_VALUES = {
            0.0001, -0.0635, 510.751, -123456.0, 42078500.0
    };

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
    public void testDefault_noPrecisionLimit_noMinExponent() {
        // arrange
        final int maxPrecision = 0;

        // act
        final DataDecimalFormat fmt = DataDecimalFormats.createDefault(maxPrecision);

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
    public void testDefault_noPrecisionLimit_withMinExponent() {
        // arrange
        final int maxPrecision = 0;
        final int minExponent = -3;

        final DataDecimalFormat fmt = DataDecimalFormats.createDefault(maxPrecision, minExponent);

        // act/assert
        checkFormatSpecial(fmt);

        checkFormat(fmt, 0.00001, "0.0");
        checkFormat(fmt, -0.0001, "0.0");
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

        checkFormat(fmt, 1.25e-3, "0.001");
        checkFormat(fmt, -9.975e-4, "-0.001");
        checkFormat(fmt, -9_999_999, "-9999999.0");
        checkFormat(fmt, 1.00001e7, "1.00001E7");

        checkFormat(fmt, Double.MAX_VALUE, "1.7976931348623157E308");
        checkFormat(fmt, Double.MIN_VALUE, "0.0");
        checkFormat(fmt, Double.MIN_NORMAL, "0.0");
        checkFormat(fmt, Math.PI, "3.142");
        checkFormat(fmt, Math.E, "2.718");
    }

    @Test
    public void testDefault_withPrecisionLimit_noMinExponent() {
        // arrange
        final int maxPrecision = 4;
        final int minExponent = Integer.MIN_VALUE;

        final DataDecimalFormat fmt = DataDecimalFormats.createDefault(maxPrecision, minExponent);

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
    public void testDefault_withPrecisionLimit_withMinExponent() {
        // arrange
        final int maxPrecision = 3;
        final int minExponent = -3;

        final DataDecimalFormat fmt = DataDecimalFormats.createDefault(maxPrecision, minExponent);

        // act/assert
        checkFormatSpecial(fmt);

        checkFormat(fmt, 0.00001, "0.0");
        checkFormat(fmt, -0.0001, "0.0");
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

        checkFormat(fmt, 1.25e-3, "0.001");
        checkFormat(fmt, -9.975e-4, "-0.001");
        checkFormat(fmt, -9_999_999, "-1.0E7");
        checkFormat(fmt, 1.00001e7, "1.0E7");

        checkFormat(fmt, Double.MAX_VALUE, "1.8E308");
        checkFormat(fmt, Double.MIN_VALUE, "0.0");
        checkFormat(fmt, Double.MIN_NORMAL, "0.0");
        checkFormat(fmt, Math.PI, "3.14");
        checkFormat(fmt, Math.E, "2.72");
    }

    @Test
    public void testPlain_noPrecisionLimit_noMinExponent() {
        // arrange
        final int maxPrecision = 0;

        final DataDecimalFormat fmt = DataDecimalFormats.createPlain(maxPrecision);

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
    public void testPlain_noPrecisionLimit_withMinExponent() {
        // arrange
        final int maxPrecision = 0;
        final int minExponent = -2;

        final DataDecimalFormat fmt = DataDecimalFormats.createPlain(maxPrecision, minExponent);

        // act/assert
        checkFormatSpecial(fmt);

        checkFormat(fmt, 0.00001, "0.0");
        checkFormat(fmt, -0.0001, "0.0");
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
        checkFormat(fmt, -9_999_999, "-9999999.0");
        checkFormat(fmt, 1.00001e7, "10000100.0");

        checkFormat(fmt, Float.MAX_VALUE, "340282346638528860000000000000000000000.0");
        checkFormat(fmt, Float.MIN_VALUE, "0.0");
        checkFormat(fmt, Float.MIN_NORMAL, "0.0");
        checkFormat(fmt, Math.PI, "3.14");
        checkFormat(fmt, Math.E, "2.72");
    }

    @Test
    public void testPlain_withPrecisionLimit_noMinExponent() {
        // arrange
        final int maxPrecision = 3;
        final int minExponent = Integer.MIN_VALUE;

        final DataDecimalFormat fmt = DataDecimalFormats.createPlain(maxPrecision, minExponent);

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
        final int maxPrecision = 4;
        final int minExponent = -2;

        final DataDecimalFormat fmt = DataDecimalFormats.createPlain(maxPrecision, minExponent);

        // act/assert
        checkFormatSpecial(fmt);

        checkFormat(fmt, 0.00001, "0.0");
        checkFormat(fmt, -0.0001, "0.0");
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

    @Test
    public void testScientific_noPrecisionLimit_noMinExponent() {
        // arrange
        final int maxPrecision = 0;

        // act
        final DataDecimalFormat fmt = DataDecimalFormats.createScientific(maxPrecision);

        // act/assert
        checkFormatSpecial(fmt);

        checkFormat(fmt, 0.00001, "1.0E-5");
        checkFormat(fmt, -0.0001, "-1.0E-4");
        checkFormat(fmt, 0.001, "1.0E-3");
        checkFormat(fmt, -0.01, "-1.0E-2");
        checkFormat(fmt, 0.1, "1.0E-1");
        checkFormat(fmt, -0.0, "-0.0");
        checkFormat(fmt, 0.0, "0.0");
        checkFormat(fmt, -1.0, "-1.0");
        checkFormat(fmt, 10.0, "1.0E1");
        checkFormat(fmt, -100.0, "-1.0E2");
        checkFormat(fmt, 1000.0, "1.0E3");
        checkFormat(fmt, -10000.0, "-1.0E4");
        checkFormat(fmt, 100000.0, "1.0E5");
        checkFormat(fmt, -1000000.0, "-1.0E6");
        checkFormat(fmt, 10000000.0, "1.0E7");
        checkFormat(fmt, -100000000.0, "-1.0E8");

        checkFormat(fmt, 1.25e-3, "1.25E-3");
        checkFormat(fmt, -9.975e-4, "-9.975E-4");
        checkFormat(fmt, -9_999_999, "-9.999999E6");
        checkFormat(fmt, 1.00001e7, "1.00001E7");

        checkFormat(fmt, Double.MAX_VALUE, "1.7976931348623157E308");
        checkFormat(fmt, Double.MIN_VALUE, "4.9E-324");
        checkFormat(fmt, Double.MIN_NORMAL, "2.2250738585072014E-308");
        checkFormat(fmt, Math.PI, "3.141592653589793");
        checkFormat(fmt, Math.E, "2.718281828459045");
    }

    @Test
    public void testScientific_noPrecisionLimit_withMinExponent() {
        // arrange
        final int maxPrecision = 0;
        final int minExponent = -3;

        final DataDecimalFormat fmt = DataDecimalFormats.createScientific(maxPrecision, minExponent);

        // act/assert
        checkFormatSpecial(fmt);

        checkFormat(fmt, 0.00001, "0.0");
        checkFormat(fmt, -0.0001, "0.0");
        checkFormat(fmt, 0.001, "1.0E-3");
        checkFormat(fmt, -0.01, "-1.0E-2");
        checkFormat(fmt, 0.1, "1.0E-1");
        checkFormat(fmt, -0.0, "-0.0");
        checkFormat(fmt, 0.0, "0.0");
        checkFormat(fmt, -1.0, "-1.0");
        checkFormat(fmt, 10.0, "1.0E1");
        checkFormat(fmt, -100.0, "-1.0E2");
        checkFormat(fmt, 1000.0, "1.0E3");
        checkFormat(fmt, -10000.0, "-1.0E4");
        checkFormat(fmt, 100000.0, "1.0E5");
        checkFormat(fmt, -1000000.0, "-1.0E6");
        checkFormat(fmt, 10000000.0, "1.0E7");
        checkFormat(fmt, -100000000.0, "-1.0E8");

        checkFormat(fmt, 1.25e-3, "1.0E-3");
        checkFormat(fmt, -9.975e-4, "-1.0E-3");
        checkFormat(fmt, -9_999_999, "-9.999999E6");
        checkFormat(fmt, 1.00001e7, "1.00001E7");

        checkFormat(fmt, Double.MAX_VALUE, "1.7976931348623157E308");
        checkFormat(fmt, Double.MIN_VALUE, "0.0");
        checkFormat(fmt, Double.MIN_NORMAL, "0.0");
        checkFormat(fmt, Math.PI, "3.142");
        checkFormat(fmt, Math.E, "2.718");
    }

    @Test
    public void testScientific_withPrecisionLimit_noMinExponent() {
        // arrange
        final int maxPrecision = 3;
        final int minExponent = Integer.MIN_VALUE;

        final DataDecimalFormat fmt = DataDecimalFormats.createScientific(maxPrecision, minExponent);

        // act/assert
        checkFormatSpecial(fmt);

        checkFormat(fmt, 0.00001, "1.0E-5");
        checkFormat(fmt, -0.0001, "-1.0E-4");
        checkFormat(fmt, 0.001, "1.0E-3");
        checkFormat(fmt, -0.01, "-1.0E-2");
        checkFormat(fmt, 0.1, "1.0E-1");
        checkFormat(fmt, -0.0, "-0.0");
        checkFormat(fmt, 0.0, "0.0");
        checkFormat(fmt, -1.0, "-1.0");
        checkFormat(fmt, 10.0, "1.0E1");
        checkFormat(fmt, -100.0, "-1.0E2");
        checkFormat(fmt, 1000.0, "1.0E3");
        checkFormat(fmt, -10000.0, "-1.0E4");
        checkFormat(fmt, 100000.0, "1.0E5");
        checkFormat(fmt, -1000000.0, "-1.0E6");
        checkFormat(fmt, 10000000.0, "1.0E7");
        checkFormat(fmt, -100000000.0, "-1.0E8");

        checkFormat(fmt, 12345.01, "1.23E4");
        checkFormat(fmt, 1.2345, "1.23");

        checkFormat(fmt, 1.25e-3, "1.25E-3");
        checkFormat(fmt, -9.975e-4, "-9.98E-4");
        checkFormat(fmt, -9_999_999, "-1.0E7");
        checkFormat(fmt, 1.00001e7, "1.0E7");

        checkFormat(fmt, Double.MAX_VALUE, "1.8E308");
        checkFormat(fmt, Double.MIN_VALUE, "4.9E-324");
        checkFormat(fmt, Double.MIN_NORMAL, "2.23E-308");
        checkFormat(fmt, Math.PI, "3.14");
        checkFormat(fmt, Math.E, "2.72");
    }

    @Test
    public void testScientific_withPrecisionLimit_withMinExponent() {
        // arrange
        final int maxPrecision = 3;
        final int minExponent = -3;

        final DataDecimalFormat fmt = DataDecimalFormats.createScientific(maxPrecision, minExponent);

        // act/assert
        checkFormatSpecial(fmt);

        checkFormat(fmt, 0.00001, "0.0");
        checkFormat(fmt, -0.0001, "0.0");
        checkFormat(fmt, 0.001, "1.0E-3");
        checkFormat(fmt, -0.01, "-1.0E-2");
        checkFormat(fmt, 0.1, "1.0E-1");
        checkFormat(fmt, -0.0, "-0.0");
        checkFormat(fmt, 0.0, "0.0");
        checkFormat(fmt, -1.0, "-1.0");
        checkFormat(fmt, 10.0, "1.0E1");
        checkFormat(fmt, -100.0, "-1.0E2");
        checkFormat(fmt, 1000.0, "1.0E3");
        checkFormat(fmt, -10000.0, "-1.0E4");
        checkFormat(fmt, 100000.0, "1.0E5");
        checkFormat(fmt, -1000000.0, "-1.0E6");
        checkFormat(fmt, 10000000.0, "1.0E7");
        checkFormat(fmt, -100000000.0, "-1.0E8");

        checkFormat(fmt, 1.25e-3, "1.0E-3");
        checkFormat(fmt, -9.975e-4, "-1.0E-3");
        checkFormat(fmt, -9_999_999, "-1.0E7");
        checkFormat(fmt, 1.00001e7, "1.0E7");

        checkFormat(fmt, Double.MAX_VALUE, "1.8E308");
        checkFormat(fmt, Double.MIN_VALUE, "0.0");
        checkFormat(fmt, Double.MIN_NORMAL, "0.0");
        checkFormat(fmt, Math.PI, "3.14");
        checkFormat(fmt, Math.E, "2.72");
    }

    @Test
    public void testEngineering_noPrecisionLimit_noMinExponent() {
        // arrange
        final int maxPrecision = 0;

        // act
        final DataDecimalFormat fmt = DataDecimalFormats.createEngineering(maxPrecision);

        // act/assert
        checkFormatSpecial(fmt);

        checkFormat(fmt, 0.00001, "10.0E-6");
        checkFormat(fmt, -0.0001, "-100.0E-6");
        checkFormat(fmt, 0.001, "1.0E-3");
        checkFormat(fmt, -0.01, "-10.0E-3");
        checkFormat(fmt, 0.1, "100.0E-3");
        checkFormat(fmt, -0.0, "-0.0");
        checkFormat(fmt, 0.0, "0.0");
        checkFormat(fmt, -1.0, "-1.0");
        checkFormat(fmt, 10.0, "10.0");
        checkFormat(fmt, -100.0, "-100.0");
        checkFormat(fmt, 1000.0, "1.0E3");
        checkFormat(fmt, -10000.0, "-10.0E3");
        checkFormat(fmt, 100000.0, "100.0E3");
        checkFormat(fmt, -1000000.0, "-1.0E6");
        checkFormat(fmt, 10000000.0, "10.0E6");
        checkFormat(fmt, -100000000.0, "-100.0E6");

        checkFormat(fmt, 1.25e-3, "1.25E-3");
        checkFormat(fmt, -9.975e-4, "-997.5E-6");
        checkFormat(fmt, -9_999_999, "-9.999999E6");
        checkFormat(fmt, 1.00001e7, "10.0001E6");

        checkFormat(fmt, Double.MAX_VALUE, "179.76931348623157E306");
        checkFormat(fmt, Double.MIN_VALUE, "4.9E-324");
        checkFormat(fmt, Double.MIN_NORMAL, "22.250738585072014E-309");
        checkFormat(fmt, Math.PI, "3.141592653589793");
        checkFormat(fmt, Math.E, "2.718281828459045");
    }

    @Test
    public void testEngineering_noPrecisionLimit_withMinExponent() {
        // arrange
        final int maxPrecision = 0;
        final int minExponent = -3;

        final DataDecimalFormat fmt = DataDecimalFormats.createEngineering(maxPrecision, minExponent);

        // act/assert
        checkFormatSpecial(fmt);

        checkFormat(fmt, 0.00001, "0.0");
        checkFormat(fmt, -0.0001, "0.0");
        checkFormat(fmt, 0.001, "1.0E-3");
        checkFormat(fmt, -0.01, "-10.0E-3");
        checkFormat(fmt, 0.1, "100.0E-3");
        checkFormat(fmt, -0.0, "-0.0");
        checkFormat(fmt, 0.0, "0.0");
        checkFormat(fmt, -1.0, "-1.0");
        checkFormat(fmt, 10.0, "10.0");
        checkFormat(fmt, -100.0, "-100.0");
        checkFormat(fmt, 1000.0, "1.0E3");
        checkFormat(fmt, -10000.0, "-10.0E3");
        checkFormat(fmt, 100000.0, "100.0E3");
        checkFormat(fmt, -1000000.0, "-1.0E6");
        checkFormat(fmt, 10000000.0, "10.0E6");
        checkFormat(fmt, -100000000.0, "-100.0E6");

        checkFormat(fmt, 1.25e-3, "1.0E-3");
        checkFormat(fmt, -9.975e-4, "-1.0E-3");
        checkFormat(fmt, -9_999_999, "-9.999999E6");
        checkFormat(fmt, 1.00001e7, "10.0001E6");

        checkFormat(fmt, Double.MAX_VALUE, "179.76931348623157E306");
        checkFormat(fmt, Double.MIN_VALUE, "0.0");
        checkFormat(fmt, Double.MIN_NORMAL, "0.0");
        checkFormat(fmt, Math.PI, "3.142");
        checkFormat(fmt, Math.E, "2.718");
    }

    @Test
    public void testEngineering_withPrecisionLimit_noMinExponent() {
        // arrange
        final int maxPrecision = 3;
        final int minExponent = Integer.MIN_VALUE;

        final DataDecimalFormat fmt = DataDecimalFormats.createEngineering(maxPrecision, minExponent);

        // act/assert
        checkFormatSpecial(fmt);

        checkFormat(fmt, 0.00001, "10.0E-6");
        checkFormat(fmt, -0.0001, "-100.0E-6");
        checkFormat(fmt, 0.001, "1.0E-3");
        checkFormat(fmt, -0.01, "-10.0E-3");
        checkFormat(fmt, 0.1, "100.0E-3");
        checkFormat(fmt, -0.0, "-0.0");
        checkFormat(fmt, 0.0, "0.0");
        checkFormat(fmt, -1.0, "-1.0");
        checkFormat(fmt, 10.0, "10.0");
        checkFormat(fmt, -100.0, "-100.0");
        checkFormat(fmt, 1000.0, "1.0E3");
        checkFormat(fmt, -10000.0, "-10.0E3");
        checkFormat(fmt, 100000.0, "100.0E3");
        checkFormat(fmt, -1000000.0, "-1.0E6");
        checkFormat(fmt, 10000000.0, "10.0E6");
        checkFormat(fmt, -100000000.0, "-100.0E6");

        checkFormat(fmt, 1.25e-3, "1.25E-3");
        checkFormat(fmt, -9.975e-4, "-998.0E-6");
        checkFormat(fmt, -9_999_999, "-10.0E6");
        checkFormat(fmt, 1.00001e7, "10.0E6");

        checkFormat(fmt, Double.MAX_VALUE, "180.0E306");
        checkFormat(fmt, Double.MIN_VALUE, "4.9E-324");
        checkFormat(fmt, Double.MIN_NORMAL, "22.3E-309");
        checkFormat(fmt, Math.PI, "3.14");
        checkFormat(fmt, Math.E, "2.72");
    }

    @Test
    public void testEngineering_withPrecisionLimit_withMinExponent() {
        // arrange
        final int maxPrecision = 3;
        final int minExponent = -3;

        final DataDecimalFormat fmt = DataDecimalFormats.createEngineering(maxPrecision, minExponent);

        // act/assert
        checkFormatSpecial(fmt);

        checkFormat(fmt, 0.00001, "0.0");
        checkFormat(fmt, -0.0001, "0.0");
        checkFormat(fmt, 0.001, "1.0E-3");
        checkFormat(fmt, -0.01, "-10.0E-3");
        checkFormat(fmt, 0.1, "100.0E-3");
        checkFormat(fmt, -0.0, "-0.0");
        checkFormat(fmt, 0.0, "0.0");
        checkFormat(fmt, -1.0, "-1.0");
        checkFormat(fmt, 10.0, "10.0");
        checkFormat(fmt, -100.0, "-100.0");
        checkFormat(fmt, 1000.0, "1.0E3");
        checkFormat(fmt, -10000.0, "-10.0E3");
        checkFormat(fmt, 100000.0, "100.0E3");
        checkFormat(fmt, -1000000.0, "-1.0E6");
        checkFormat(fmt, 10000000.0, "10.0E6");
        checkFormat(fmt, -100000000.0, "-100.0E6");

        checkFormat(fmt, 1.25e-3, "1.0E-3");
        checkFormat(fmt, -9.975e-4, "-1.0E-3");
        checkFormat(fmt, -9_999_999, "-10.0E6");
        checkFormat(fmt, 1.00001e7, "10.0E6");

        checkFormat(fmt, Double.MAX_VALUE, "180.0E306");
        checkFormat(fmt, Double.MIN_VALUE, "0.0");
        checkFormat(fmt, Double.MIN_NORMAL, "0.0");
        checkFormat(fmt, Math.PI, "3.14");
        checkFormat(fmt, Math.E, "2.72");
    }

    @Test
    public void testPrecisionValidation() {
        // arrange
        final List<IntFunction<DataDecimalFormat>> fns = Arrays.asList(
                    DataDecimalFormats::createDefault,
                    p -> DataDecimalFormats.createDefault(p, Integer.MIN_VALUE),
                    DataDecimalFormats::createPlain,
                    p -> DataDecimalFormats.createPlain(p, Integer.MIN_VALUE),
                    DataDecimalFormats::createScientific,
                    p -> DataDecimalFormats.createScientific(p, Integer.MIN_VALUE),
                    DataDecimalFormats::createEngineering,
                    p -> DataDecimalFormats.createEngineering(p, Integer.MIN_VALUE)
                );

        final String msg = "Max precision must be greater than or equal to zero; was -1";

        // act/assert
        for (final IntFunction<DataDecimalFormat> fn : fns) {
            GeometryTestUtils.assertThrowsWithMessage(
                    () -> fn.apply(-1),
                    IllegalArgumentException.class, msg);
        }
    }

    /** Utility method used to generate the tables of format examples in the Javadocs.
     * This helps to ensure accuracy and consistency in the documentation. The HTML tables
     * are printed to stdout and can then be copied into the correct locations in the source.
     */
    // @Test
    public void generateExampleTables() {
        System.out.println("Default - one arg");
        System.out.println(generateOneArgExamplesTable(DataDecimalFormats::createDefault));

        System.out.println("Default - two arg");
        System.out.println(generateTwoArgExamplesTable(DataDecimalFormats::createDefault));

        System.out.println("Plain - one arg");
        System.out.println(generateOneArgExamplesTable(DataDecimalFormats::createPlain));

        System.out.println("Plain - two arg");
        System.out.println(generateTwoArgExamplesTable(DataDecimalFormats::createPlain));

        System.out.println("Scientific - one arg");
        System.out.println(generateOneArgExamplesTable(DataDecimalFormats::createScientific));

        System.out.println("Scientific - two arg");
        System.out.println(generateTwoArgExamplesTable(DataDecimalFormats::createScientific));

        System.out.println("Engineering - one arg");
        System.out.println(generateOneArgExamplesTable(DataDecimalFormats::createEngineering));

        System.out.println("Engineering - two arg");
        System.out.println(generateTwoArgExamplesTable(DataDecimalFormats::createEngineering));
    }

    private static String generateOneArgExamplesTable(final IntFunction<DataDecimalFormat> fn) {
        final int aMaxPrecision = 0;
        final int bMaxPrecision = 4;

        final DataDecimalFormat aFmt = fn.apply(aMaxPrecision);
        final DataDecimalFormat bFmt = fn.apply(bMaxPrecision);

        final String descTemplate = "(maxPrecision= %d)";

        return generateExamplesTable(
                    Arrays.asList(aFmt, bFmt),
                    Arrays.asList(String.format(descTemplate, aMaxPrecision), String.format(descTemplate, bMaxPrecision))
                );
    }

    private static String generateTwoArgExamplesTable(final BiFunction<Integer, Integer, DataDecimalFormat> fn) {
        final int aMaxPrecision = 0;
        final int aMinExponent = -2;

        final int bMaxPrecision = 4;
        final int bMinExponent = -2;

        final DataDecimalFormat aFmt = fn.apply(aMaxPrecision, aMinExponent);
        final DataDecimalFormat bFmt = fn.apply(bMaxPrecision, bMinExponent);

        final String descTemplate = "(maxPrecision= %d, minExponent= %d)";

        return generateExamplesTable(
                    Arrays.asList(aFmt, bFmt),
                    Arrays.asList(
                            String.format(descTemplate, aMaxPrecision, aMinExponent),
                            String.format(descTemplate, bMaxPrecision, bMinExponent))
                );
    }

    private static String generateExamplesTable(final List<DataDecimalFormat> fmts,
            final List<String> fmtDescriptions) {
        final StringBuilder sb = new StringBuilder();

        sb.append("<table>\n")
            .append("  <tr><th>Value</th>");

        for (String desc : fmtDescriptions) {
            sb.append("<th>")
                .append(desc)
                .append("</th>");
        }
        sb.append("</tr>\n");

        for (double value : EXAMPLE_VALUES) {
            sb.append("  <tr><td>")
                .append(value)
                .append("</td>");

            for (DataDecimalFormat fmt : fmts) {
                sb.append("<td>")
                    .append(fmt.format(value))
                    .append("</td>");
            }

            sb.append("</tr>\n");
        }

        sb.append("</table>");

        return sb.toString();
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
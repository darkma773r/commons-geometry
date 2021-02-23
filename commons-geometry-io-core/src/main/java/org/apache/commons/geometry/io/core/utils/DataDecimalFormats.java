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

/** Class containing static utility methods and constants for {@link DataDecimalFormat}
 * instances.
 */
public final class DataDecimalFormats {

    /** {@link DataDecimalFormat} instance that simply calls {@link Double#toString(double)}.
     */
    public static final DataDecimalFormat DOUBLE_TO_STRING = Double::toString;

    /** {@link DataDecimalFormat} instance that converts the argument to a float and calls
     * {@link Float#toString(float)}.
     */
    public static final DataDecimalFormat FLOAT_TO_STRING = d -> Float.toString((float) d);

    /** Minimum possible decimal exponent for double values. */
    private static final int MIN_DOUBLE_EXPONENT = -325;

    /** Utility class; no instantiation. */
    private DataDecimalFormats() {}

    public static DataDecimalFormat createDefault(final int precision) {
        return createDefault(precision, MIN_DOUBLE_EXPONENT);
    }

    public static DataDecimalFormat createDefault(final int precision, final int minExponent) {
        return new DefaultFormat(precision, minExponent);
    }

    public static DataDecimalFormat createPlain(final int precision) {
        return createPlain(precision, MIN_DOUBLE_EXPONENT);
    }

    public static DataDecimalFormat createPlain(final int precision, final int minExponent) {
        return new PlainFormat(precision, minExponent);
    }

    public static DataDecimalFormat createScientific(final int precision) {
        return createScientific(precision, MIN_DOUBLE_EXPONENT);
    }

    public static DataDecimalFormat createScientific(final int precision, final int minExponent) {
        return new ScientificFormat(precision, minExponent);
    }

    public static DataDecimalFormat createEngineering(final int precision) {
        return createEngineering(precision, MIN_DOUBLE_EXPONENT);
    }

    public static DataDecimalFormat createEngineering(final int precision, final int minExponent) {
        return new EngineeringFormat(precision, minExponent);
    }

    /** Base class for standard {@link DataDecimalFormat} implementations.
     */
    private static abstract class AbstractFormat implements DataDecimalFormat {

        /** Precision to use when formatting values. */
        private final int precision;

        /** The minimum exponent to allow in the result. Value with exponents less than this are
         * rounded to positive zero.
         */
        private final int minExponent;

        AbstractFormat(final int precision, final int minExponent) {
            this.precision = precision;
            this.minExponent = minExponent;
        }

        /** {@inheritDoc} */
        @Override
        public String format(final double d) {
            if (Double.isFinite(d)) {
                final ParsedDouble n = ParsedDouble.from(d);

                int roundExponent = Math.max(n.getExponent(), minExponent);
                if (precision > 0) {
                    roundExponent = Math.max(n.getScientificExponent() - precision + 1, roundExponent);
                }

                final ParsedDouble rounded = n.round(roundExponent);

                return formatInternal(rounded);
            }

            return Double.toString(d); // NaN or infinite; use default Double toString() method
        }

        /** Format the given parsed double value.
         * @param val value to format
         * @return formatted double value
         */
        protected abstract String formatInternal(ParsedDouble val);
    }

    /** {@link DataDecimalFormat} that produces plain decimal strings that do not use
     * scientific notation.
     */
    private static class PlainFormat extends AbstractFormat {

        PlainFormat(final int precision, final int minExponent) {
            super(precision, minExponent);
        }

        /** {@inheritDoc} */
        @Override
        protected String formatInternal(final ParsedDouble val) {
            return val.toPlainString(true);
        }
    }

    /** {@link DataDecimalFormat} similar to {@link Double#toString()} that uses
     * plain decimal notation for small numbers relatively close to zero and scientific
     * notation otherwise.
     */
    private static class DefaultFormat extends AbstractFormat {

        DefaultFormat(final int precision, final int minExponent) {
            super(precision, minExponent);
        }

        /** {@inheritDoc} */
        @Override
        protected String formatInternal(final ParsedDouble val) {
            final int sciExp = val.getScientificExponent();
            return sciExp < 7 && sciExp > -4 ?
                    val.toPlainString(true) :
                    val.toScientificString(true);
        }
    }

    /** {@link DataDecimalFormat} that uses scientific notation for all values.
     */
    private static class ScientificFormat extends AbstractFormat {

        ScientificFormat(final int precision, final int minExponent) {
            super(precision, minExponent);
        }

        /** {@inheritDoc} */
        @Override
        public String formatInternal(final ParsedDouble val) {
            return val.toScientificString(true);
        }
    }

    /** {@link DataDecimalFormat} that uses engineering notation for all values.
     */
    private static class EngineeringFormat extends AbstractFormat {

        EngineeringFormat(final int precision, final int minExponent) {
            super(precision, minExponent);
        }

        /** {@inheritDoc} */
        @Override
        public String formatInternal(final ParsedDouble val) {
            return val.toEngineeringString(true);
        }
    }
}
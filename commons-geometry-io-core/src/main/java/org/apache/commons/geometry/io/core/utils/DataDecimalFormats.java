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

    private static final double MIN_PLAIN = 1e-3;

    private static final double MAX_PLAIN = 1e7;

    /** Utility class; no instantiation. */
    private DataDecimalFormats() {}

    public static DataDecimalFormat createDefault(final int precision, final int maxFractionDigits) {
        return createDefault(precision, maxFractionDigits, true);
    }

    public static DataDecimalFormat createDefault(final int precision, final int maxFractionDigits,
            final boolean includeDecimalPlaceholder) {
        final DataDecimalFormat plain = new PlainFormat(precision, maxFractionDigits, includeDecimalPlaceholder);
        final DataDecimalFormat scientific =
                new ScientificFormat(precision, maxFractionDigits, includeDecimalPlaceholder);

        return d ->  {
            final double abs = Math.abs(d);
            return (Double.compare(abs, 0.0) != 0 && (abs < MIN_PLAIN || abs >= MAX_PLAIN)) ?
                    scientific.format(d) :
                    plain.format(d);
        };
    }

    public static DataDecimalFormat createPlain(final int precision, final int maxFractionDigits) {
        return createPlain(precision, maxFractionDigits, true);
    }

    public static DataDecimalFormat createPlain(final int precision, final int maxFractionDigits,
            final boolean includeDecimalPlaceholder) {
        return new PlainFormat(precision, maxFractionDigits, includeDecimalPlaceholder);
    }

    public static DataDecimalFormat createScientific(final int precision) {
        return createScientific(precision, true);
    }

    public static DataDecimalFormat createScientific(final int precision, final boolean includeDecimalPlaceholder) {
        return new ScientificFormat(precision, -1, includeDecimalPlaceholder);
    }

    public static DataDecimalFormat createEngineering(final int precision, final int maxFractionDigits) {
        return createEngineering(precision, maxFractionDigits, true);
    }

    public static DataDecimalFormat createEngineering(final int precision, final int maxFractionDigits,
            final boolean includeDecimalPlaceholder) {
        return new EngineeringFormat(precision, maxFractionDigits, includeDecimalPlaceholder);
    }

    private static abstract class AbstractFormat implements DataDecimalFormat {

        private final int precision;

        private final int maxFractionDigits;

        private final boolean includeDecimalPlaceholder;

        AbstractFormat(final int precision, final int maxFractionDigits, final boolean includeDecimalPlaceholder) {
            this.precision = precision;
            this.maxFractionDigits = maxFractionDigits;
            this.includeDecimalPlaceholder = includeDecimalPlaceholder;
        }

        /** {@inheritDoc} */
        @Override
        public String format(final double d) {
            if (Double.isFinite(d)) {
                final ParsedDouble raw = ParsedDouble.from(d);
                final int targetPrecision = determinePrecision(raw, getWholeDigits(raw));
                final ParsedDouble scaled = raw.withPrecision(targetPrecision);

                return formatInternal(scaled, includeDecimalPlaceholder);
            }

            return Double.toString(d);
        }

        /** Determine the precision that should be used for {@code val} when creating a
         * string representation with the given number of whole digits.
         * @param val value to determine the target precision for
         * @param wholeDigits number of whole digits to be included in the magnitude of
         *      the string representation of the value
         * @return target precision for the value
         */
        private int determinePrecision(final ParsedDouble val, final int wholeDigits) {
            final int digits = val.getDigits().length();

            if (precision > -1 || maxFractionDigits > -1) {
                int targetPrecision = digits;

                final int fractionDigits = digits - wholeDigits;
                if (maxFractionDigits > -1 && fractionDigits > maxFractionDigits) {
                    targetPrecision -= fractionDigits - maxFractionDigits;
                }

                targetPrecision = precision > 0 ?
                        Math.min(precision, targetPrecision) :
                        targetPrecision;

                // precision must always be at least one since we need to have at least
                // a single digit to display
                return Math.max(1, targetPrecision);
            }

            return digits;
        }

        /** Get the number of whole (non-fraction) digits to be placed in the magnitude
         * portion of the formatted string.
         * @param val value to get the number of whole digits for
         * @return the number of whole digits to be used for the argument
         */
        protected abstract int getWholeDigits(ParsedDouble val);

        /** Format the given double value.
         * @param val value to format
         * @param includeDecimalPlaceholder if true, a decimal placeholder should be added if
         *      no fractional component is needed
         * @return formatted double value
         */
        protected abstract String formatInternal(ParsedDouble val, boolean includeDecimalPlaceholder);
    }

    private static class PlainFormat extends AbstractFormat {

        PlainFormat(final int precision, final int maxFractionDigits, final boolean includeDecimalPlaceholder) {
            super(precision, maxFractionDigits, includeDecimalPlaceholder);
        }

        /** {@inheritDoc} */
        @Override
        protected int getWholeDigits(final ParsedDouble val) {
            return Math.max(0, val.getDigits().length() + val.getExponent());
        }

        /** {@inheritDoc} */
        @Override
        public String formatInternal(final ParsedDouble val, final boolean includeDecimalPlaceholder) {
            return val.toPlainString(includeDecimalPlaceholder);
        }
    }

    private static class ScientificFormat extends AbstractFormat {

        ScientificFormat(final int precision, final int maxFractionDigits, final boolean includeDecimalPlaceholder) {
            super(precision, maxFractionDigits, includeDecimalPlaceholder);
        }

        /** {@inheritDoc} */
        @Override
        protected int getWholeDigits(final ParsedDouble val) {
            return 1;
        }

        /** {@inheritDoc} */
        @Override
        public String formatInternal(final ParsedDouble val, final boolean includeDecimalPlaceholder) {
            return val.toScientificString(includeDecimalPlaceholder);
        }
    }

    private static class EngineeringFormat extends AbstractFormat {

        EngineeringFormat(final int precision, final int maxFractionDigits, final boolean includeDecimalPlaceholder) {
            super(precision, maxFractionDigits, includeDecimalPlaceholder);
        }

        /** {@inheritDoc} */
        @Override
        protected int getWholeDigits(final ParsedDouble val) {
            return 1 + Math.floorMod(val.getExponent() + val.getDigits().length() - 1, 3);
        }

        /** {@inheritDoc} */
        @Override
        public String formatInternal(final ParsedDouble val, boolean includeDecimalPlaceholder) {
            return val.toEngineeringString(includeDecimalPlaceholder);
        }
    }
}

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
package org.apache.commons.geometry.examples.io.internal;

import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;

public abstract class AbstractTextFormatWriter {

    /** The default maximum number of fraction digits in formatted numbers. */
    private static final int DEFAULT_MAXIMUM_FRACTION_DIGITS = 6;

    /** The default line separator value. This is not directly specified by the OBJ format
     * but the value used here matches that
     * <a href="https://docs.blender.org/manual/en/2.80/addons/io_scene_obj.html">used by Blender</a>.
     */
    private static final String DEFAULT_LINE_SEPARATOR = "\n";

    /** Underlying writer instance. */
    private final Writer writer;

    /** Line separator string. */
    private String lineSeparator = DEFAULT_LINE_SEPARATOR;

    /** Decimal formatter. */
    private DecimalFormat decimalFormat;

    protected AbstractTextFormatWriter(final Writer writer) {
        this.writer = writer;

        this.decimalFormat = new DecimalFormat();
        this.decimalFormat.setMaximumFractionDigits(DEFAULT_MAXIMUM_FRACTION_DIGITS);
    }

    /** Get the current line separator. This value defaults to {@value #DEFAULT_LINE_SEPARATOR}.
     * @return the current line separator
     */
    public String getLineSeparator() {
        return lineSeparator;
    }

    /** Set the line separator.
     * @param lineSeparator the line separator to use
     */
    public void setLineSeparator(final String lineSeparator) {
        this.lineSeparator = lineSeparator;
    }

    /** Get the {@link DecimalFormat} instance used to format floating point output.
     * @return the decimal format instance
     */
    public DecimalFormat getDecimalFormat() {
        return decimalFormat;
    }

    /** Set the {@link DecimalFormat} instance used to format floatin point output.
     * @param decimalFormat decimal format instance
     */
    public void setDecimalFormat(final DecimalFormat decimalFormat) {
        this.decimalFormat = decimalFormat;
    }

    protected Writer getWriter() {
        return writer;
    }

    protected void write(final double n) throws IOException {
        write(decimalFormat.format(n));
    }

    protected void write(final int n) throws IOException {
        write(String.valueOf(n));
    }

    protected void write(final String str) throws IOException {
        writer.write(str);
    }

    protected void writeNewLine() throws IOException {
        write(lineSeparator);
    }
}

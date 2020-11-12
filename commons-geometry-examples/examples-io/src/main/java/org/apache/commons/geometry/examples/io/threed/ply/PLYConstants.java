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
package org.apache.commons.geometry.examples.io.threed.ply;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/** Class containing constants for the PLY file format.
 */
final class PLYConstants {

    public enum Format {
        ASCII("ascii"),
        BINARY_BIG_ENDIAN("binary_big_endian"),
        BINARY_LITTLE_ENDIAN("binary_little_endian");

        private final String name;

        Format(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public enum DataType {
        CHAR("char", 1, false),
        UCHAR("uchar", 1, false),
        SHORT("short", 2, false),
        USHORT("ushort", 2, false),
        INT("int", 4, false),
        UINT("uint", 4, false),
        FLOAT("float", 4, true),
        DOUBLE("double", 8, true);

        private final String name;

        private final int byteCount;

        private final boolean floatingPoint;

        DataType(final String name, final int byteCount, final boolean floatingPoint) {
            this.name = name;
            this.byteCount = byteCount;
            this.floatingPoint = floatingPoint;
        }

        public String getName() {
            return name;
        }

        public int getByteCount() {
            return byteCount;
        }

        public boolean isFloatingPoint() {
            return floatingPoint;
        }
    }

    /** Charset used for the header of both ascii and binary file formats. */
    public static final Charset HEADER_CHARSET = StandardCharsets.US_ASCII;

    /** PLY file start keyword. The bytes of this string in ASCII are the
     * magic number for the file type.
     */
    public static final String FILE_START_KEYWORD = "ply";

    /** Keyword used to indicate the format of the file. */
    public static final String FORMAT_KEYWORD = "format";

    /** Name of the ascii file format. */
    public static final String ASCII_FORMAT_NAME = "ascii";

    /** Name of the binary little endian file format. */
    public static final String BINARY_LITTLE_ENDIAN_FORMAT_NAME = "binary_little_endian";

    /** Name of the binary big endian file format. */
    public static final String BINARY_BIG_ENDIAN_FORMAT_NAME = "binary_big_endian";

    /** Keyword beginning a comment line. */
    public static final String COMMENT_KEYWORD = "comment";

    /** Keyword beginning an element definition. */
    public static final String ELEMENT_KEYWORD = "element";

    /** Keyword beginning an element property definition. */
    public static final String PROPERTY_KEYWORD = "property";

    /** Keyword used to indicate that a property is a list. */
    public static final String LIST_PROPERTY_KEYWORD = "list";

    /** Keyword ending the file header. */
    public static final String END_HEADER_KEYWORD = "end_header";

    /** Utility class; no instantiation. */
    private PLYConstants() {}
}

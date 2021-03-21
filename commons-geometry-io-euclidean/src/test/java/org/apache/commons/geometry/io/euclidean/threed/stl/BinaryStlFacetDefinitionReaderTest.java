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
package org.apache.commons.geometry.io.euclidean.threed.stl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BinaryStlFacetDefinitionReaderTest {

    private static final String LONG_STRING =
            "A long string that will most definitely exceed the 80 byte length of the binary STL file format header.";

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    @Test
    public void testHeader_zeros() throws IOException {
        // arrange
        final byte[] bytes = new byte[StlConstants.BINARY_HEADER_BYTES + 4];
        out.write(bytes);

        final byte[] expectedHeader = new byte[StlConstants.BINARY_HEADER_BYTES];
        System.arraycopy(bytes, 0, expectedHeader, 0, expectedHeader.length);

        try (BinaryStlFacetDefinitionReader reader = new BinaryStlFacetDefinitionReader(getInput())) {
            // act/assert
            Assertions.assertArrayEquals(expectedHeader, reader.getHeader().array());
            Assertions.assertEquals(0L, reader.getNumTriangles());

            Assertions.assertNull(reader.readFacet());
        }
    }

    @Test
    public void testHeader_ones() throws IOException {
        // arrange
        final byte[] bytes = new byte[StlConstants.BINARY_HEADER_BYTES + 4];
        Arrays.fill(bytes, (byte) -1);
        out.write(bytes);

        final byte[] expectedHeader = new byte[StlConstants.BINARY_HEADER_BYTES];
        System.arraycopy(bytes, 0, expectedHeader, 0, expectedHeader.length);

        try (BinaryStlFacetDefinitionReader reader = new BinaryStlFacetDefinitionReader(getInput())) {
            // act/assert
            Assertions.assertArrayEquals(expectedHeader, reader.getHeader().array());
            Assertions.assertEquals(0xffffffffL, reader.getNumTriangles());
        }
    }

    @Test
    public void testHeader_shortString() throws IOException {
        // arrange
        out.write(createHeader("Hello!", StandardCharsets.UTF_8, 1));

        // act
        try (BinaryStlFacetDefinitionReader reader = new BinaryStlFacetDefinitionReader(getInput())) {
            // act/assert
            Assertions.assertEquals("Hello!", reader.getHeaderAsString());
            Assertions.assertEquals(1L, reader.getNumTriangles());
        }
    }

    @Test
    public void testHeader_longString() throws IOException {
        // arrange
        out.write(createHeader(LONG_STRING, StandardCharsets.UTF_8, 8736720));

        // act
        try (BinaryStlFacetDefinitionReader reader = new BinaryStlFacetDefinitionReader(getInput())) {
            // act/assert
            Assertions.assertEquals(LONG_STRING.substring(0, StlConstants.BINARY_HEADER_BYTES),
                    reader.getHeaderAsString());
            Assertions.assertEquals(8736720L, reader.getNumTriangles());
        }
    }

    @Test
    public void testHeader_longString_givenCharset() throws IOException {
        // arrange
        out.write(createHeader(LONG_STRING, StandardCharsets.UTF_16, 256));

        // act
        try (BinaryStlFacetDefinitionReader reader = new BinaryStlFacetDefinitionReader(getInput())) {
            // act/assert
            Assertions.assertEquals("A long string that will most definitely",
                    reader.getHeaderAsString(StandardCharsets.UTF_16));
            Assertions.assertEquals(256L, reader.getNumTriangles());
        }
    }

    @Test
    public void testGetHeader_noData() throws IOException {
        // arrange
        out.write(new byte[32]);

        try (BinaryStlFacetDefinitionReader reader = new BinaryStlFacetDefinitionReader(getInput())) {
            // act/assert
            GeometryTestUtils.assertThrowsWithMessage(
                    () -> reader.getHeader(),
                    IOException.class, "Failed to read STL header: data not available");
        }
    }

    @Test
    public void testGetHeader_noTriangleCount() throws IOException {
        // arrange
        out.write(new byte[StlConstants.BINARY_HEADER_BYTES]);

        try (BinaryStlFacetDefinitionReader reader = new BinaryStlFacetDefinitionReader(getInput())) {
            // act/assert
            GeometryTestUtils.assertThrowsWithMessage(
                    () -> reader.getHeader(),
                    IOException.class, "Failed to read STL triangle count: data not available");
        }
    }

    @Test
    public void testReadFacet_noData() throws IOException {
        // arrange
        out.write(createHeader(1));

        // act/assert
        try (BinaryStlFacetDefinitionReader reader = new BinaryStlFacetDefinitionReader(getInput())) {
            // act/assert
            GeometryTestUtils.assertThrowsWithMessage(
                    () -> reader.readFacet(),
                    IOException.class, "Failed to read STL triangle at index 0: data not available");
        }
    }

    private ByteArrayInputStream getInput() {
        return new ByteArrayInputStream(out.toByteArray());
    }

    private static byte[] createHeader(final int count) {
        return createHeader("", StandardCharsets.UTF_8, count);
    }

    private static byte[] createHeader(final String str, final Charset charset, final int count) {
        final byte[] result = new byte[StlConstants.BINARY_HEADER_BYTES + 4];

        final byte[] strBytes = str.getBytes(charset);
        System.arraycopy(strBytes, 0, result, 0, Math.min(StlConstants.BINARY_HEADER_BYTES, strBytes.length));

        result[result.length - 4] = (byte) (count & 0x000000ff);
        result[result.length - 3] = (byte) ((count & 0x0000ff00) >> 8);
        result[result.length - 2] = (byte) ((count & 0x00ff0000) >> 16);
        result[result.length - 1] = (byte) ((count & 0xff000000) >> 24);

        return result;
    }
}

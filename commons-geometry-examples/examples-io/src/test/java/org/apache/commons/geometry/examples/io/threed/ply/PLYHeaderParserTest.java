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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;

public class PLYHeaderParserTest {

    @Test
    public void testParseStream_noElements() throws IOException {
        // arrange
        String content =
                "ply\n" +
                "format ascii 1.0\n" +
                "comment nothing to see here\n" +
                "end_header\r" +
                "other content";
        ByteArrayInputStream in = new ByteArrayInputStream(content.getBytes(StandardCharsets.US_ASCII));

        // act
        PLYHeader header = PLYHeaderParser.parse(in);

        // assert
        Assert.assertEquals(PLYConstants.Format.ASCII, header.getFormat());
        Assert.assertEquals(0, header.getElements().size());

        Assert.assertEquals("other content", readRemaining(in));
    }

    @Test
    public void testParseReader_noElements() throws IOException {
        // arrange
        String content =
                "ply\n" +
                "format ascii 1.0\n" +
                "comment nothing to see here\n" +
                "end_header\r" +
                "other content";
        ByteArrayInputStream in = new ByteArrayInputStream(content.getBytes(StandardCharsets.US_ASCII));
        Reader reader = new InputStreamReader(in, StandardCharsets.US_ASCII);

        // act
        PLYHeader header = PLYHeaderParser.parse(reader);

        // assert
        Assert.assertEquals(PLYConstants.Format.ASCII, header.getFormat());
        Assert.assertEquals(0, header.getElements().size());

        Assert.assertEquals("other content", readRemaining(reader));
    }

    private static String readRemaining(final InputStream in) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.US_ASCII)) {
            return readRemaining(reader);
        }
    }

    private static String readRemaining(final Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder();

        int ch;
        while ((ch = reader.read()) != -1) {
            sb.append((char) ch);
        }

        return sb.toString();
    }
}

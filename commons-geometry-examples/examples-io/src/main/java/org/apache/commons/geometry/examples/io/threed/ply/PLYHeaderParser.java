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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.geometry.examples.io.internal.SimpleTextParser;

class PLYHeaderParser {

    private static final int MAX_HEADER_LEN = 2048;

    private static final String END_HEADER_SEQUENCE = PLYConstants.END_HEADER_KEYWORD + "\r";

    private static final byte[] END_HEADER_BYTES = END_HEADER_SEQUENCE.getBytes(StandardCharsets.US_ASCII);

    private final SimpleTextParser parser;

    PLYHeaderParser(final String content) {
        this.parser = new SimpleTextParser(new StringReader(content));
    }

    public PLYHeader parseHeader() throws IOException {
        // read the magic number file
        readFileStart();

        // get the format
        PLYHeader header = new PLYHeader(readFormat());

        // read the element definitions
        int choice;
        while (parser.hasMoreCharacters()) {
            consumeCommentLines();

            choice = parser.choose(
                    PLYConstants.ELEMENT_KEYWORD,
                    PLYConstants.END_HEADER_KEYWORD);

            if (choice == 0) {
                header.getElements().add(readElement());
            } else {
                break; // end of header
            }
        }

        return header;
    }

    private void readFileStart() throws IOException {
        nextWordOnLine();
        parser.match(PLYConstants.FILE_START_KEYWORD);
        nextLine();
    }

    private PLYConstants.Format readFormat() throws IOException {
        consumeCommentLines();

        parser.match(PLYConstants.FORMAT_KEYWORD);
        nextWordOnLine();

        final PLYConstants.Format format = chooseEnum(
                Arrays.asList(PLYConstants.Format.values()),
                PLYConstants.Format::getName);

        nextLine(); // ignore the version number

        return format;
    }

    private PLYHeader.Element readElement() throws IOException {
        nextWordOnLine();
        final String name = parser.getCurrentToken();

        nextWordOnLine();
        final int count = parser.getCurrentTokenAsInt();

        nextLine();

        final PLYHeader.Element element = new PLYHeader.Element(name, count);

        consumeCommentLines();
        while (parser.tryMatch(PLYConstants.PROPERTY_KEYWORD)) {
            nextWordOnLine();

            parser.match(PLYConstants.PROPERTY_KEYWORD);

            element.getProperties().add(readProperty());

            consumeCommentLines();
        }

        return element;
    }

    private PLYHeader.ElementProperty readProperty() throws IOException {
        nextWordOnLine();
        if (parser.tryMatch(PLYConstants.LIST_PROPERTY_KEYWORD)) {
            nextWordOnLine();
            parser.match(PLYConstants.LIST_PROPERTY_KEYWORD);

            return readListProperty();
        }

        return readSimpleProperty();
    }

    private PLYHeader.ElementProperty readSimpleProperty() throws IOException {
        nextWordOnLine();
        final PLYConstants.DataType dataType = readDataType();

        nextWordOnLine();
        final String name = parser.getCurrentToken();

        return new PLYHeader.ElementProperty(name, dataType);
    }

    private PLYHeader.ElementListProperty readListProperty() throws IOException {
        nextWordOnLine();
        final PLYConstants.DataType countDataType = readDataType();

        nextWordOnLine();
        final PLYConstants.DataType dataType = readDataType();

        nextWordOnLine();
        final String name = parser.getCurrentToken();

        return new PLYHeader.ElementListProperty(name, countDataType, dataType);
    }

    private void consumeCommentLines() throws IOException {
        while (parser.tryMatch(PLYConstants.COMMENT_KEYWORD)) {
            nextLine();
        }
    }

    private void nextWordOnLine() throws IOException {
        parser.discardLineWhitespace()
            .next(SimpleTextParser::isNotWhitespace);
    }

    private void nextLine() throws IOException {
        parser.discardLine()
            .next(SimpleTextParser::isNotWhitespace);
    }

    private PLYConstants.DataType readDataType() throws IOException {
        return chooseEnum(
                Arrays.asList(PLYConstants.DataType.values()),
                PLYConstants.DataType::getName);
    }

    private <E extends Enum<E>> E chooseEnum(final List<E> values, final Function<E, String> nameFn) {
        final List<String> names = values.stream()
                .map(nameFn)
                .collect(Collectors.toList());

        final int choice = parser.choose(names);

        return values.get(choice);
    }

    public static PLYHeader parse(final InputStream in) throws IOException  {
        final String str = readHeaderString(in);
        return parse(str);
    }

    public static PLYHeader parse(final Reader reader) throws IOException {
        final String content = readHeaderString(reader);
        return parse(content);
    }

    public static PLYHeader parse(final String content) throws IOException {
        return new PLYHeaderParser(content).parseHeader();
    }

    private static String readHeaderString(final InputStream in) throws IOException {
        ByteBuffer bytes = ByteBuffer.allocate(MAX_HEADER_LEN);

        int i = 0;
        int val;
        while ((val= in.read()) != -1) {
            bytes.put((byte) val);

            if (val == END_HEADER_BYTES[i]) {
                ++i;
                if (i == END_HEADER_BYTES.length) {
                    break;
                }
            } else {
                i = 0;
            }
        }

        bytes.flip();

        return StandardCharsets.US_ASCII.decode(bytes).toString();
    }

    public static String readHeaderString(final Reader reader) throws IOException {
        final StringBuilder sb = new StringBuilder();

        int len = PLYConstants.END_HEADER_KEYWORD.length();

        int i = 0;
        int ch;
        while ((ch = reader.read()) != -1) {
            sb.append((char) ch);

            if (i == len && Character.isWhitespace(ch)) {
                break; // done
            } else if (i < len && ch == PLYConstants.END_HEADER_KEYWORD.charAt(i)) {
                ++i;
            } else {
                i = 0;
            }
        }

        // TODO

        return sb.toString();
    }
}

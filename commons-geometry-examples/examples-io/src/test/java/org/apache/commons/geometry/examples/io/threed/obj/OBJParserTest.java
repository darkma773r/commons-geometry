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
package org.apache.commons.geometry.examples.io.threed.obj;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.IntFunction;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.junit.Assert;
import org.junit.Test;

public class OBJParserTest {

    private static final double EPS = 1e-10;

    @Test
    public void testInitialState() {
        // act
        final OBJParser p = parser("");

        // assert
        Assert.assertNull(p.getKeyword());
        Assert.assertEquals(0, p.getVertexCount());
        Assert.assertEquals(0, p.getVertexNormalCount());
        Assert.assertEquals(0, p.getTextureCoordinateCount());
    }

    @Test
    public void testNextKeyword() throws IOException {
        // arrange
        final OBJParser p = parser(lines(
                "#comment",
                "",
                "  \t",
                "o test",
                "v",
                "v 1 0 0 1",
                "v 0 1 0",
                " # comment",
                " ",
                " g triangle",
                " f 1 2 3",
                "",
                "# end"
        ));

        // act/assert
        assertNextKeyword("o", p);
        assertNextKeyword("v", p);
        assertNextKeyword("v", p);
        assertNextKeyword("v", p);
        assertNextKeyword("g", p);
        assertNextKeyword("f", p);

        assertNextKeyword(null, p);
    }

    @Test
    public void testNextKeyword_emptyContent() throws IOException {
        // arrange
        final OBJParser p = parser("");

        // act/assert
        assertNextKeyword(null, p);
    }

    @Test
    public void testReadLine() throws IOException {
        // arrange
        final OBJParser p = parser(lines(
                "  line\t",
                ""
        ));

        // act
        Assert.assertEquals("line", p.readLine());
        Assert.assertEquals("", p.readLine());
        Assert.assertNull(p.readLine());
    }

    @Test
    public void testReadVector() throws IOException {
        // arrange
        final OBJParser p = parser(lines(
                "1.01 3e-02 123.999 extra"
        ));

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1.01, 0.03, 123.999), p.readVector(), EPS);
    }

    @Test
    public void testReadVector_parseFailures() throws IOException {
        // arrange
        final OBJParser p = parser(lines(
                "0.1 0.2 a",
                "1",
                ""
        ));

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            try {
                p.readVector();
            } catch (IOException exc) {
                throw new RuntimeException(exc);
            }
        }, IllegalStateException.class, "Parsing failed at line 1, column 9: expected double but found [a]");

        p.readLine();

        GeometryTestUtils.assertThrows(() -> {
            try {
                p.readVector();
            } catch (IOException exc) {
                throw new RuntimeException(exc);
            }
        }, IllegalStateException.class, "Parsing failed at line 2, column 2: expected double but found end of line");
    }

    @Test
    public void testReadDoubles() throws IOException {
        // arrange
        final OBJParser p = parser(lines(
                "0.1 0.2 3e2 4e2 500.01",
                "  12.001  ",
                "  ",
                ""
        ));

        // act/assert
        Assert.assertArrayEquals(new double[] {
                0.1, 0.2, 3e2, 4e2, 500.01
        }, p.readDoubles(), EPS);
        Assert.assertArrayEquals(new double[] { }, p.readDoubles(), EPS);

        p.readLine();

        Assert.assertArrayEquals(new double[] { 12.001 }, p.readDoubles(), EPS);

        p.readLine();

        Assert.assertArrayEquals(new double[] { }, p.readDoubles(), EPS);

        p.readLine();

        Assert.assertArrayEquals(new double[] { }, p.readDoubles(), EPS);
    }

    @Test
    public void testReadDoubles_parseFailures() throws IOException {
        // arrange
        final OBJParser p = parser(lines(
                "0.1 0.2 a",
                "b"
        ));

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            try {
                p.readDoubles();
            } catch (IOException exc) {
                throw new RuntimeException(exc);
            }
        }, IllegalStateException.class, "Parsing failed at line 1, column 9: expected double but found [a]");

        p.readLine();

        GeometryTestUtils.assertThrows(() -> {
            try {
                p.readDoubles();
            } catch (IOException exc) {
                throw new RuntimeException(exc);
            }
        }, IllegalStateException.class, "Parsing failed at line 2, column 1: expected double but found [b]");
    }

    @Test
    public void testReadFace() throws IOException {
        // arrange
        final OBJParser p = parser(lines(
                "# test content",
                "o test",
                "v 0 0 0",
                "v 1 0 0",
                "v 1 1 0",
                "v 0 1 0",
                "vt 1 2",
                "vt 3 4",
                "vt 5 6",
                "vt 7 8",
                "vt 9 10",
                "vn 0 0 1",
                "vn 0 0 -1",

                "f 1 2 3 4",
                "f -4// -3// -2// -1//",

                "f 1//1 2//2 3//1 4//2",
                "f -4//-2 -3//-1 -2//-2 -1//-1",

                "f 1/4/1 2/3/2 3/2/1 4/1/2",
                "f -4/-1/-2 -3/-2/-1 -2/-3/-2 -1/-4/-1",

                "f 1/4 2/3 3/2 4/1",
                "f -4/-1 -3/-2 -2/-3 -1/-4"
        ));

        nextFace(p);

        // act/assert
        assertFace(new int[][] {
            { 0, -1, -1 },
            { 1, -1, -1 },
            { 2, -1, -1 },
            { 3, -1, -1 },
        }, p.readFace());

        nextFace(p);

        assertFace(new int[][] {
            { 0, -1, -1 },
            { 1, -1, -1 },
            { 2, -1, -1 },
            { 3, -1, -1 },
        }, p.readFace());

        nextFace(p);

        assertFace(new int[][] {
            { 0, -1, 0 },
            { 1, -1, 1 },
            { 2, -1, 0 },
            { 3, -1, 1 },
        }, p.readFace());

        nextFace(p);

        assertFace(new int[][] {
            { 0, -1, 0 },
            { 1, -1, 1 },
            { 2, -1, 0 },
            { 3, -1, 1 },
        }, p.readFace());

        nextFace(p);

        assertFace(new int[][] {
            { 0, 3, 0 },
            { 1, 2, 1 },
            { 2, 1, 0 },
            { 3, 0, 1 },
        }, p.readFace());

        nextFace(p);

        assertFace(new int[][] {
            { 0, 4, 0 },
            { 1, 3, 1 },
            { 2, 2, 0 },
            { 3, 1, 1 },
        }, p.readFace());

        nextFace(p);

        assertFace(new int[][] {
            { 0, 3, -1 },
            { 1, 2, -1 },
            { 2, 1, -1 },
            { 3, 0, -1 },
        }, p.readFace());

        nextFace(p);

        assertFace(new int[][] {
            { 0, 4, -1 },
            { 1, 3, -1 },
            { 2, 2, -1 },
            { 3, 1, -1 },
        }, p.readFace());
    }

    @Test
    public void testReadFace_notEnoughVertices() throws IOException {
        // arrange
        final OBJParser p = parser(lines(
                "# test content",
                "v 0 0 0",
                "v 1 0 0",
                "v 1 1 0",
                "f 1 2"
        ));

        // act/assert
        nextFace(p);
        GeometryTestUtils.assertThrows(() -> {
            try {
                p.readFace();
            } catch (IOException exc) {
                throw new RuntimeException(exc);
            }
        }, IllegalStateException.class, "Parsing failed at line 5, column 6: " +
            "face must contain at least 3 vertices but found only 2");
    }

    @Test
    public void testReadFace_invalidVertexIndex() throws IOException {
        // arrange
        final OBJParser p = parser(lines(
                "# test content",
                "f 1 2 3",
                "v 0 0 0",
                "v 1 0 0",
                "v 1 1 0",
                "f 1 2 -4",
                "f 1 0 3",
                "f 4 2 3"
        ));

        // act/assert
        nextFace(p);
        GeometryTestUtils.assertThrows(() -> {
            try {
                p.readFace();
            } catch (IOException exc) {
                throw new RuntimeException(exc);
            }
        }, IllegalStateException.class, "Parsing failed at line 2, column 3: " +
            "vertex index cannot be used because no values of that type have been defined");

        nextFace(p);
        GeometryTestUtils.assertThrows(() -> {
            try {
                p.readFace();
            } catch (IOException exc) {
                throw new RuntimeException(exc);
            }
        }, IllegalStateException.class, "Parsing failed at line 6, column 7: " +
            "vertex index must evaluate to be within the range [1, 3] but was -4");

        nextFace(p);
        GeometryTestUtils.assertThrows(() -> {
            try {
                p.readFace();
            } catch (IOException exc) {
                throw new RuntimeException(exc);
            }
        }, IllegalStateException.class, "Parsing failed at line 7, column 5: " +
            "vertex index must evaluate to be within the range [1, 3] but was 0");

        nextFace(p);
        GeometryTestUtils.assertThrows(() -> {
            try {
                p.readFace();
            } catch (IOException exc) {
                throw new RuntimeException(exc);
            }
        }, IllegalStateException.class, "Parsing failed at line 8, column 3: " +
            "vertex index must evaluate to be within the range [1, 3] but was 4");
    }

    @Test
    public void testReadFace_invalidTextureIndex() throws IOException {
        // arrange
        final OBJParser p = parser(lines(
                "# test content",
                "v 0 0 0",
                "v 1 0 0",
                "v 1 1 0",
                "f 1/1 2/2 3/3",
                "vt 1 2",
                "vt 3 4",
                "vt 5 6",
                "f 1/1 2/2 3/-4",
                "f 1/1 1/0 3/3",
                "f 1/4 2/2 3/3"
        ));

        // act/assert
        nextFace(p);
        GeometryTestUtils.assertThrows(() -> {
            try {
                p.readFace();
            } catch (IOException exc) {
                throw new RuntimeException(exc);
            }
        }, IllegalStateException.class, "Parsing failed at line 5, column 5: " +
            "texture index cannot be used because no values of that type have been defined");

        nextFace(p);
        GeometryTestUtils.assertThrows(() -> {
            try {
                p.readFace();
            } catch (IOException exc) {
                throw new RuntimeException(exc);
            }
        }, IllegalStateException.class, "Parsing failed at line 9, column 13: " +
            "texture index must evaluate to be within the range [1, 3] but was -4");

        nextFace(p);
        GeometryTestUtils.assertThrows(() -> {
            try {
                p.readFace();
            } catch (IOException exc) {
                throw new RuntimeException(exc);
            }
        }, IllegalStateException.class, "Parsing failed at line 10, column 9: " +
            "texture index must evaluate to be within the range [1, 3] but was 0");

        nextFace(p);
        GeometryTestUtils.assertThrows(() -> {
            try {
                p.readFace();
            } catch (IOException exc) {
                throw new RuntimeException(exc);
            }
        }, IllegalStateException.class, "Parsing failed at line 11, column 5: " +
            "texture index must evaluate to be within the range [1, 3] but was 4");
    }

    @Test
    public void testReadFace_invalidNormalIndex() throws IOException {
        // arrange
        final OBJParser p = parser(lines(
                "# test content",
                "v 0 0 0",
                "v 1 0 0",
                "v 1 1 0",
                "f 1//1 2//2 3//3",
                "vn 1 0 0",
                "vn 0 1 0",
                "vn 0 0 1",
                "f 1//1 2//2 3//-4",
                "f 1//1 1//0 3//3",
                "f 1//4 2//2 3//3"
        ));

        // act/assert
        nextFace(p);
        GeometryTestUtils.assertThrows(() -> {
            try {
                p.readFace();
            } catch (IOException exc) {
                throw new RuntimeException(exc);
            }
        }, IllegalStateException.class, "Parsing failed at line 5, column 6: " +
            "normal index cannot be used because no values of that type have been defined");

        nextFace(p);
        GeometryTestUtils.assertThrows(() -> {
            try {
                p.readFace();
            } catch (IOException exc) {
                throw new RuntimeException(exc);
            }
        }, IllegalStateException.class, "Parsing failed at line 9, column 16: " +
            "normal index must evaluate to be within the range [1, 3] but was -4");

        nextFace(p);
        GeometryTestUtils.assertThrows(() -> {
            try {
                p.readFace();
            } catch (IOException exc) {
                throw new RuntimeException(exc);
            }
        }, IllegalStateException.class, "Parsing failed at line 10, column 11: " +
            "normal index must evaluate to be within the range [1, 3] but was 0");

        nextFace(p);
        GeometryTestUtils.assertThrows(() -> {
            try {
                p.readFace();
            } catch (IOException exc) {
                throw new RuntimeException(exc);
            }
        }, IllegalStateException.class, "Parsing failed at line 11, column 6: " +
            "normal index must evaluate to be within the range [1, 3] but was 4");
    }

    @Test
    public void testParse() throws IOException {
        // arrange
        final OBJParser p = parser(lines(
                "# test content",
                "o test",
                "",
                "v 0 0 0",
                "v 1 0 0",
                "v 1 1 0",
                "v 0 1 0",
                "",
                "vt 0 0",
                "vt 1 0",
                "vt 1 1",
                "",
                "vn 0 0 1",
                "",
                "f 1 2 4",
                "f 1/1/1 2/2/1 3/3/1"
        ));

        // act/assert
        assertNextKeyword("o", p);
        Assert.assertEquals("test", p.readLine());

        assertNextKeyword("v", p);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, p.readVector(), EPS);

        assertNextKeyword("v", p);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_X, p.readVector(), EPS);

        assertNextKeyword("v", p);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 1, 0), p.readVector(), EPS);

        assertNextKeyword("v", p);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_Y, p.readVector(), EPS);

        assertNextKeyword("vt", p);
        Assert.assertArrayEquals(new double[] { 0, 0 }, p.readDoubles(),  EPS);

        assertNextKeyword("vt", p);
        Assert.assertArrayEquals(new double[] { 1, 0 }, p.readDoubles(),  EPS);

        assertNextKeyword("vt", p);
        Assert.assertArrayEquals(new double[] { 1, 1 }, p.readDoubles(),  EPS);

        assertNextKeyword("vn", p);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_Z, p.readVector(), EPS);

        assertNextKeyword("f", p);
        assertFace(new int[][] {
            { 0, -1, -1 },
            { 1, -1, -1 },
            { 3, -1, -1 },
        }, p.readFace());

        assertNextKeyword("f", p);
        assertFace(new int[][] {
            { 0, 0, 0 },
            { 1, 1, 0 },
            { 2, 2, 0 },
        }, p.readFace());

        Assert.assertEquals(4, p.getVertexCount());
        Assert.assertEquals(3, p.getTextureCoordinateCount());
        Assert.assertEquals(1, p.getVertexNormalCount());
    }

    @Test
    public void testFace_getDefinedCompositeNormal() throws IOException {
        // arrange
        final OBJParser p = parser(lines(
                "v 0 0 0",
                "v 1 0 0",
                "v 1 1 0",
                "v 0 1 0",
                "",
                "vn 0 0 1",
                "vn 0 0 -1",
                "vn 2 2 2",
                "vn -2 2 2",
                "",
                "f 1 2 3 4",
                "f 1//1 2 3",
                "f 1//1 2//1 3//1 4//1",
                "f 1//1 2//2 3//1 4//2",
                "f 1//-2 2//-1 3//3 4//4"
        ));

        final List<Vector3D> normals = Arrays.asList(
                Vector3D.Unit.PLUS_Z,
                Vector3D.Unit.MINUS_Z,
                Vector3D.of(1, 1, 1),
                Vector3D.of(-1, 1, 1));
        final IntFunction<Vector3D> normalFn = normals::get;

        // act/assert
        nextMatchingKeyword("f", p);
        Assert.assertNull(p.readFace().getDefinedCompositeNormal(normalFn));

        nextMatchingKeyword("f", p);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_Z,
                p.readFace().getDefinedCompositeNormal(normalFn), EPS);

        nextMatchingKeyword("f", p);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_Z,
                p.readFace().getDefinedCompositeNormal(normalFn), EPS);

        nextMatchingKeyword("f", p);
        Assert.assertNull(p.readFace().getDefinedCompositeNormal(normalFn));

        nextMatchingKeyword("f", p);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 1, 1).normalize(),
                p.readFace().getDefinedCompositeNormal(normalFn), EPS);
    }

    @Test
    public void testFace_computeNormalFromVertices() throws IOException {
        // arrange
        final OBJParser p = parser(lines(
                "v 0 0 0",
                "v 1 0 0",
                "v 2 0 0",
                "v 0 1 0",
                "",
                "vn 0 0 1",
                "",
                "f 1 2 4",
                "f 1//1 2//1 3//1"
        ));

        final List<Vector3D> vertices = Arrays.asList(
                Vector3D.ZERO,
                Vector3D.Unit.PLUS_X,
                Vector3D.of(2, 0, 0),
                Vector3D.of(0, 1, 0));
        final IntFunction<Vector3D> vertexFn = vertices::get;

        // act/assert
        nextMatchingKeyword("f", p);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_Z,
                p.readFace().computeNormalFromVertices(vertexFn), EPS);

        nextMatchingKeyword("f", p);
        Assert.assertNull(p.readFace().computeNormalFromVertices(vertexFn));
    }

    @Test
    public void testFace_getOrientedVertexAttributes() throws IOException {
        // arrange
        final OBJParser p = parser(lines(
                "v 0 0 0",
                "v 1 0 0",
                "v 0 1 0",
                "f 1 2 3"
        ));

        final List<Vector3D> vertices = Arrays.asList(
                Vector3D.ZERO,
                Vector3D.Unit.PLUS_X,
                Vector3D.Unit.PLUS_Y,
                Vector3D.of(2, 0, 0));
        final IntFunction<Vector3D> vertexFn = vertices::get;

        nextMatchingKeyword("f", p);
        final OBJParser.Face f = p.readFace();

        final List<OBJParser.VertexAttributes> attrs = f.getVertexAttributes();

        final List<OBJParser.VertexAttributes> reverseAttrs = new ArrayList<>(attrs);
        Collections.reverse(reverseAttrs);

        // act/assert
        Assert.assertEquals(attrs, f.getOrientedVertexAttributes(null, vertexFn));

        Assert.assertEquals(attrs, f.getOrientedVertexAttributes(Vector3D.Unit.PLUS_Z, vertexFn));
        Assert.assertEquals(attrs, f.getOrientedVertexAttributes(Vector3D.of(1, 0, 0.1), vertexFn));
        Assert.assertEquals(attrs, f.getOrientedVertexAttributes(Vector3D.Unit.PLUS_X, vertexFn));

        Assert.assertEquals(reverseAttrs, f.getOrientedVertexAttributes(Vector3D.Unit.MINUS_Z, vertexFn));
        Assert.assertEquals(reverseAttrs, f.getOrientedVertexAttributes(Vector3D.of(1, 0, -0.1), vertexFn));
    }

    @Test
    public void testFace_getVertices() throws IOException {
        // arrange
        final OBJParser p = parser(lines(
                "v 0 0 0",
                "v 1 0 0",
                "v 1 1 0",
                "v 0 1 0",
                "v 0 0 1",
                "v 0 0 -1",
                "",
                "f 2 3 4"
        ));

        final List<Vector3D> vertices = Arrays.asList(
                Vector3D.ZERO,
                Vector3D.Unit.PLUS_X,
                Vector3D.of(1, 1, 0),
                Vector3D.Unit.PLUS_Y,
                Vector3D.of(0, 0, 1),
                Vector3D.of(0, 0, -1));
        final IntFunction<Vector3D> vertexFn = vertices::get;

        // act/assert
        nextMatchingKeyword("f", p);
        Assert.assertEquals(vertices.subList(1, 4), p.readFace().getVertices(vertexFn));
    }

    @Test
    public void testFace_getOrientedVertices() throws IOException {
        // arrange
        final OBJParser p = parser(lines(
                "v 0 0 0",
                "v 1 0 0",
                "v 0 1 0",
                "v 0 0 -1",
                "f 1 2 3"
        ));

        final List<Vector3D> vertices = Arrays.asList(
                Vector3D.ZERO,
                Vector3D.Unit.PLUS_X,
                Vector3D.Unit.PLUS_Y,
                Vector3D.of(0, 0, -1));
        final IntFunction<Vector3D> vertexFn = vertices::get;

        final List<Vector3D> faceVertices = vertices.subList(0, 3);
        final List<Vector3D> reverseFaceVertices = new ArrayList<>(faceVertices);
        Collections.reverse(reverseFaceVertices);

        nextMatchingKeyword("f", p);
        final OBJParser.Face f = p.readFace();

        // act/assert
        Assert.assertEquals(faceVertices, f.getOrientedVertices(null, vertexFn));

        Assert.assertEquals(faceVertices, f.getOrientedVertices(Vector3D.Unit.PLUS_Z, vertexFn));
        Assert.assertEquals(faceVertices, f.getOrientedVertices(Vector3D.of(1, 0, 0.1), vertexFn));
        Assert.assertEquals(faceVertices, f.getOrientedVertices(Vector3D.Unit.PLUS_X, vertexFn));

        Assert.assertEquals(reverseFaceVertices, f.getOrientedVertices(Vector3D.Unit.MINUS_Z, vertexFn));
        Assert.assertEquals(reverseFaceVertices, f.getOrientedVertices(Vector3D.of(1, 0, -0.1), vertexFn));
    }

    private static OBJParser parser(final String content) {
        return new OBJParser(new StringReader(content));
    }

    private static String lines(final String... lines) {
        final String[] newlineOptions = { "\n", "\r", "\r\n" };

        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length; ++i) {
            sb.append(lines[i])
                .append(newlineOptions[i % newlineOptions.length]);
        }

        return sb.toString();
    }

    private static void nextFace(final OBJParser parser) throws IOException {
        nextMatchingKeyword(OBJConstants.FACE_KEYWORD, parser);
    }

    private static void nextMatchingKeyword(final String keyword, final OBJParser parser) throws IOException {
        while (parser.nextKeyword() && !keyword.equals(parser.getKeyword())) {
        }
    }

    private static void assertNextKeyword(final String expected, final OBJParser parser) throws IOException {
        Assert.assertEquals(expected != null, parser.nextKeyword());
        Assert.assertEquals(expected, parser.getKeyword());
    }

    private static void assertFace(final int[][] vertexAttributes, final OBJParser.Face face) {
        Assert.assertEquals(vertexAttributes.length, face.getVertexAttributes().size());

        final int[] expectedVertexIndices = new int[vertexAttributes.length];
        final int[] expectedTextureIndices = new int[vertexAttributes.length];
        final int[] expectedNormalIndices = new int[vertexAttributes.length];

        // check the indices directly on the vertex attributes
        OBJParser.VertexAttributes attrs;
        String msg;
        for (int i = 0; i < vertexAttributes.length; ++i) {
            attrs = face.getVertexAttributes().get(i);

            msg = "Unexpected face vertex attributes at index " + i;
            Assert.assertArrayEquals(msg, vertexAttributes[i], new int[] {
                    attrs.getVertexIndex(),
                    attrs.getTextureIndex(),
                    attrs.getNormalIndex()
            });

            expectedVertexIndices[i] = attrs.getVertexIndex();
            expectedTextureIndices[i] = attrs.getTextureIndex();
            expectedNormalIndices[i] = attrs.getNormalIndex();
        }

        // check the individual index arrays from the face
        Assert.assertArrayEquals(expectedVertexIndices, face.getVertexIndices());
        Assert.assertArrayEquals(expectedTextureIndices, face.getTextureIndices());
        Assert.assertArrayEquals(expectedNormalIndices, face.getNormalIndices());
    }
}

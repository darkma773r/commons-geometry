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
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import org.apache.commons.geometry.euclidean.internal.Vectors;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.examples.io.internal.SimpleTextParser;

/** Low-level parser class for the OBJ file format. This class provides access
 * to OBJ data structures but does not retain any of the parsed data. For example,
 * it is up to callers to store vertices as they are parsed for later reference.
 * This allows callers to determine what values are stored and in what format.
 */
public class OBJParser {

    /** Text parser instance. */
    private final SimpleTextParser parser;

    /** Most recently encountered keyword. */
    private String keyword;

    /** Number of vertex keywords encountered in the file so far. */
    private int vertexCount;

    /** Number of vertex normal keywords encountered in the file so far. */
    private int vertexNormalCount;

    /** Number of texture coordinate keywords encountered in the file so far. */
    private int textureCoordinateCount;

    /** Construct a new instance for parsing OBJ content from the given reader.
     * @param reader reader to parser content from
     */
    public OBJParser(final Reader reader) {
        this(new SimpleTextParser(reader));
    }

    /** Construct a new instance for parsing OBJ content from the given text parser.
     * @param parser text parser to read content from
     */
    public OBJParser(final SimpleTextParser parser) {
        this.parser = parser;
    }

    /** Get the keyword most recently parsed via the {@link #nextKeyword()} method.
     * Null is returned parsing has not started or the end of the content has been
     * reached.
     * @return the current keyword or null if parsing has not started or the end
     *      of the content has been reached
     */
    public String getKeyword() {
        return keyword;
    }

    /** Get the number of {@link OBJConstants#VERTEX_KEYWORD vertex keywords} parsed
     * so far.
     * @return the number of vertex keywords parsed so far
     */
    public int getVertexCount() {
        return vertexCount;
    }

    /** Get the number of {@link OBJConstants#VERTEX_NORMAL_KEYWORD vertex normal keywords} parsed
     * so far.
     * @return the number of vertex normal keywords parsed so far
     */
    public int getVertexNormalCount() {
        return vertexNormalCount;
    }

    /** Get the number of {@link OBJConstants#TEXTURE_COORDINATE_KEYWORD texture coordinate keywords} parsed
     * so far.
     * @return the number of texture coordinate keywords parsed so far
     */
    public int getTextureCoordinateCount() {
        return textureCoordinateCount;
    }

    /** Advance the parser to the next keyword, returning true if a keyword has been found
     * and false if the end of the content has been reached. Keywords consist of alphanumeric
     * strings placed at the beginning of lines. No attempt is made to validate that the keyword
     * is valid for the OBJ format. Comments and blank lines are ignored.
     * @return true if a keyword has been found and false if the end of content has been reached
     * @throws IOException if an I/O error occurs
     */
    public boolean nextKeyword() throws IOException {
        keyword = null;

        // advance to the next line if not at the start of a line
        if (parser.getColumnNumber() != 1) {
            parser.discardLine();
        }

        // advance past comments and blank lines
        while (parser.hasMoreCharacters() &&
                (!parser.discardLineWhitespace().nextAlphanumeric().hasNonEmptyToken() ||
                parser.tryMatch(OBJConstants.COMMENT_START))) {
            parser.discardLine();
        }

        if (parser.hasMoreCharacters() && parser.hasNonEmptyToken()) {
            keyword = parser.getCurrentToken();

            // update counts for indexed elements
            switch (keyword) {
                case OBJConstants.VERTEX_KEYWORD:
                    ++vertexCount;
                    break;
                case OBJConstants.VERTEX_NORMAL_KEYWORD:
                    ++vertexNormalCount;
                    break;
                case OBJConstants.TEXTURE_COORDINATE_KEYWORD:
                    ++textureCoordinateCount;
                    break;
            }
        }

        return keyword != null;
    }

    /** Read the remaining content on the current line. Leading and trailing whitespace is removed.
     * @return remaining content on the current line or null if the end of the content has
     *      been reached
     * @throws IOException if an I/O error occurs
     */
    public String readLine() throws IOException{
        String line = parser.nextLine().getCurrentToken();

        return line != null ?
                line.trim() :
                null;
    }

    /** Read a whitespace-delimited 3D vector from the current line.
     * @return vector vector read from the current line
     * @throws IOException if an I/O error occurs
     */
    public Vector3D readVector() throws IOException {
        final double x = nextDouble();
        final double y = nextDouble();
        final double z = nextDouble();

        return Vector3D.of(x, y, z);
    }

    /** Read whitespace-delimited double values from the current line.
     * @return double values read from the current line
     * @throws IOException if an I/O error occurs
     * @throws IllegalStateException if double values are not able to be parsed
     */
    public double[] readDoubles() throws IOException {
        final List<Double> list = new ArrayList<>();

        while (lineHasContent()) {
            list.add(nextDouble());
        }

        final double[] arr = new double[list.size()];
        for (int i = 0; i < list.size(); ++i) {
            arr[i] = list.get(i);
        }

        return arr;
    }

    /** Read an OBJ face definition from the current line.
     * @return OBJ face definition read from the current line
     * @throws IOException if an I/O error occurs
     * @throws IllegalStateException if a face definition is not able to be parsed
     */
    public Face readFace() throws IOException {
        final List<VertexAttributes> vertices = new ArrayList<>();

        while (lineHasContent()) {
            vertices.add(readFaceVertex());
        }

        if (vertices.size() < 3) {
            throw parser.parseError(parser.getLineNumber(), parser.getColumnNumber(),
                    "face must contain at least 3 vertices but found only " + vertices.size());
        }

        parser.discardLine();

        return new Face(vertices);
    }

    /** Read an OBJ face vertex definition from the current parser position.
     * @return OBJ face vertex definition
     * @throws IOException if an I/O error occurs
     * @throws IllegalStateException if a vertex definition is not able to be parsed
     */
    private VertexAttributes readFaceVertex() throws IOException {
        parser.discardLineWhitespace();

        final int vertexIndex = readNormalizedVertexAttributeIndex("vertex", vertexCount);

        int textureIndex = -1;
        if (parser.peekChar() == OBJConstants.FACE_VALUE_SEP_CHAR) {
            parser.discard(1);

            if (parser.peekChar() != OBJConstants.FACE_VALUE_SEP_CHAR) {
                textureIndex = readNormalizedVertexAttributeIndex("texture", textureCoordinateCount);
            }
        }

        int normalIndex = -1;
        if (parser.peekChar() == OBJConstants.FACE_VALUE_SEP_CHAR) {
            parser.discard(1);

            if (SimpleTextParser.isIntegerPart(parser.peekChar())) {
                normalIndex = readNormalizedVertexAttributeIndex("normal", vertexNormalCount);
            }
        }

        return new VertexAttributes(vertexIndex, textureIndex, normalIndex);
    }

    /** Read a vertex attribute index from the current parser position and normalize it to
     * be 0-based and positive.
     * @param type type of attribute being read; this value is used in error messages
     * @param available number of available values of the given type parsed from the content
     *      so far
     * @return 0-based positive attribute index
     * @throws IOException if an I/O error occurs
     * @throws IllegalStateException if the integer index cannot be parsed or the index is
     *      out of range for the number of parsed elements of the given type
     */
    private int readNormalizedVertexAttributeIndex(final String type, final int available) throws IOException {
        final int objIndex = parser.next(SimpleTextParser::isIntegerPart)
                .getCurrentTokenAsInt();

        final int normalizedIndex = objIndex < 0 ?
                available + objIndex :
                objIndex - 1;

        if (normalizedIndex < 0 || normalizedIndex >= available) {
            final StringBuilder err = new StringBuilder();
            err.append(type)
                .append(" index ");

            if (available < 1) {
                err.append("cannot be used because no values of that type have been defined");
            } else {
                err.append("must evaluate to be within the range [1, ")
                    .append(available)
                    .append("] but was ")
                    .append(objIndex);
            }

            throw parser.tokenError(err.toString());
        }

        return normalizedIndex;
    }

    /** Get the next whitespace-delimited double on the current line.
     * @return the next whitespace-delimited double on the current line
     * @throws IOException if an I/O error occurs
     * @throws IllegalStateException if a double value is not able to be parsed
     */
    private double nextDouble() throws IOException {
        return parser.discardLineWhitespace()
            .next(SimpleTextParser::isNotWhitespace)
            .getCurrentTokenAsDouble();
    }

    /** Discard whitespace on the current line and return true if any more characters
     * remain on the line.
     * @return true if more non-whitespace characters remain on the current line
     * @throws IOException if an I/O error occurs
     */
    private boolean lineHasContent() throws IOException {
        return parser.discardLineWhitespace().hasMoreCharactersOnLine();
    }

    /** Class representing an OBJ face definition. Faces are defined with the format
     * <pre>
     * f v<sub>1</sub>/vt<sub>1</sub>/vn<sub>1</sub> v<sub>2</sub>/vt<sub>2</sub>/vn<sub>2</sub> v<sub>3</sub>/vt<sub>3</sub>/vn<sub>3</sub> ...
     * </pre>
     * where the {@code v} elements are indices into the model vertices, the {@code vt}
     * elements are indices into the model texture coordinates, and the {@code vn} elements
     * are indices into the model normal coordinates. Only the vertex indices are required.
     *
     * <p>All vertex attribute indices are normalized to be 0-based and positive and all
     * faces are assumed to define geometrically valid convex polygons.</p>
     */
    public static final class Face {

        /** List of vertex attributes for the face. */
        private final List<VertexAttributes> vertexAttributes;

        /** Construct a new instance with the given vertex attributes.
         * @param vertexAttributes face vertex attributes
         */
        Face(final List<VertexAttributes> vertexAttributes) {
            this.vertexAttributes = Collections.unmodifiableList(vertexAttributes);
        }

        /** Get the list of vertex attributes for the instance.
         * @return list of vertex attribute
         */
        public List<VertexAttributes> getVertexAttributes() {
            return vertexAttributes;
        }

        /** Get a composite normal for the face by computing the sum of all defined vertex
         * normals and normalizing the result. Null is returned if no vertex normals are
         * defined or the defined normals sum to zero.
         * @param modelNormalFn function used to access normals parsed earlier in the model;
         *      callers are responsible for storing these values as they are parsed
         * @return composite face normal or null if no composite normal can be determined from the
         *      normals defined for the face
         */
        public Vector3D getDefinedCompositeNormal(final IntFunction<Vector3D> modelNormalFn) {
            Vector3D sum = Vector3D.ZERO;

            int normalIdx;
            for (final VertexAttributes vertex : vertexAttributes) {
                normalIdx = vertex.getNormalIndex();
                if (normalIdx > -1) {
                    sum = sum.add(modelNormalFn.apply(normalIdx));
                }
            }

            return Vectors.tryNormalize(sum);
        }

        /** Compute a normal for the face using its first three vertices. The vertices will wind in a
         * counter-clockwise direction when viewed looking down the returned normal. Null is returned
         * if the normal could not be determined, which would be the case if the vertices lie in the
         * same line or two or more are equal.
         * @param modelVertexFn function used to access model vertices parsed earlier in the content;
         *      callers are responsible for storing these values as they are passed
         * @return a face normal computed from the first 3 vertices or null if a normal cannot
         *      be determined
         */
        public Vector3D computeNormalFromVertices(final IntFunction<Vector3D> modelVertexFn) {
            final Vector3D p0 = modelVertexFn.apply(vertexAttributes.get(0).getVertexIndex());
            final Vector3D p1 = modelVertexFn.apply(vertexAttributes.get(1).getVertexIndex());
            final Vector3D p2 = modelVertexFn.apply(vertexAttributes.get(2).getVertexIndex());

            return Vectors.tryNormalize(p0.vectorTo(p1).cross(p0.vectorTo(p2)));
        }

        /** Get the vertex attributes for the face listed in the order that produces a counter-clockwise
         * winding of vertices when viewed looking down the given normal direction. If {@code normal}
         * is null, the original vertex sequence is used.
         * @param normal requested face normal; may be null
         * @param modelVertexFn function used to access model vertices parsed earlier in the content;
         *      callers are responsible for storing these values as they are passed
         * @return list of vertex attributes for the face, oriented to correspond with the given
         *      face normal
         */
        public List<VertexAttributes> getOrientedVertexAttributes(final Vector3D normal,
                final IntFunction<Vector3D> modelVertexFn) {
            List<VertexAttributes> result = vertexAttributes;

            if (normal != null) {
                final Vector3D computedNormal = computeNormalFromVertices(modelVertexFn);
                if (computedNormal != null && normal.dot(computedNormal) < 0) {
                    // face is oriented the opposite way; reverse the order of the vertices
                    result = new ArrayList<>(vertexAttributes);
                    Collections.reverse(result);
                }
            }

            return result;
        }

        /** Get the face vertices in the order defined in the face definition.
         * @param modelVertexFn function used to access model vertices parsed earlier in the content;
         *      callers are responsible for storing these values as they are passed
         * @return face vertices in their defined ordering
         */
        public List<Vector3D> getVertices(final IntFunction<Vector3D> modelVertexFn) {
            return vertexAttributes.stream()
                    .map(v -> modelVertexFn.apply(v.getVertexIndex()))
                    .collect(Collectors.toList());
        }

        /** Get the face vertices in the order that produces a counter-clockwise winding when viewed
         * looking down the given normal.
         * @param normal requested face normal
         * @param modelVertexFn function used to access model vertices parsed earlier in the content;
         *      callers are responsible for storing these values as they are passed
         * @return face vertices in the order that produces a counter-clockwise winding when viewed
         *      looking down the given normal
         * @see #getOrientedVertexAttributes(Vector3D, IntFunction)
         */
        public List<Vector3D> getOrientedVertices(final Vector3D normal,
                final IntFunction<Vector3D> modelVerticesFn) {
            return getOrientedVertexAttributes(normal, modelVerticesFn).stream()
                    .map(v -> modelVerticesFn.apply(v.getVertexIndex()))
                    .collect(Collectors.toList());
        }

        /** Get the vertex indices for the face.
         * @return vertex indices for the face
         */
        public int[] getVertexIndices() {
            return getIndices(VertexAttributes::getVertexIndex);
        }

        /** Get the texture indices for the face. The value {@code -1} is used if a texture index
         * is not set.
         * @return texture indices
         */
        public int[] getTextureIndices() {
            return getIndices(VertexAttributes::getTextureIndex);
        }

        /** Get the normal indices for the face. The value {@code -1} is used if a texture index
         * is not set.
         * @return normal indices
         */
        public int[] getNormalIndices() {
            return getIndices(VertexAttributes::getNormalIndex);
        }

        /** Get indices for the face, using the given function to extract the value from
         * the vertex attributes.
         * @param fn function used to extract the required value from each vertex attribute
         * @return extracted indices
         */
        private int[] getIndices(final ToIntFunction<VertexAttributes> fn) {
            final int len = vertexAttributes.size();
            final int[] indices = new int[len];

            for (int i = 0; i < len; ++i) {
                indices[i] = fn.applyAsInt(vertexAttributes.get(i));
            }

            return indices;
        }
    }

    /** Class representing a set of attributes for a face vertex. All index values are 0-based
     * and positive, in contrast with OBJ indices which are 1-based and support negative
     * values. If an index value is not given in the OBJ content, it is set to {@code -1}.
     */
    public static final class VertexAttributes {

        /** Vertex index. */
        private final int vertexIndex;

        /** Texture coordinate index. */
        private final int textureIndex;

        /** Vertex normal index. */
        private final int normalIndex;

        /** Construct a new instance with the given vertices.
         * @param vertexIndex vertex index
         * @param textureIndex texture index
         * @param normalIndex vertex normal index
         */
        VertexAttributes(final int vertexIndex, final int textureIndex, final int normalIndex) {
            this.vertexIndex = vertexIndex;
            this.textureIndex = textureIndex;
            this.normalIndex = normalIndex;
        }

        /** Get the vertex position index for this instance. This value is required and is guaranteed to
         * be a valid index into the list of vertex positions parsed so far in the OBJ content.
         * @return vertex index
         */
        public int getVertexIndex() {
            return vertexIndex;
        }

        /** Get the texture index for this instance or {@code -1} if not specified in the
         * OBJ content.
         * @return texture index or {@code -1} if not specified in the OBJ content.
         */
        public int getTextureIndex() {
            return textureIndex;
        }

        /** Get the normal index for this instance or {@code -1} if not specified in the
         * OBJ content.
         * @return normal index or {@code -1} if not specified in the OBJ content.
         */
        public int getNormalIndex() {
            return normalIndex;
        }
    }
}

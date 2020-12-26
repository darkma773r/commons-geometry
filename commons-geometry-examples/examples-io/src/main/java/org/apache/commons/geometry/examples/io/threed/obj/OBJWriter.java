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
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.mesh.Mesh;
import org.apache.commons.geometry.examples.io.internal.AbstractTextFormatWriter;
import org.apache.commons.geometry.examples.io.threed.facet.FacetDefinition;

/** Class for writing OBJ files containing 3D mesh data.
 */
public final class OBJWriter extends AbstractTextFormatWriter implements AutoCloseable {

    private static final int DEFAULT_MESH_BUFFER_SIZE = 1024;

    /** Space character. */
    private static final char SPACE = ' ';

    /** Number of vertices written to the output. */
    private int vertexCount = 0;

    /** Number of normals written to the output. */
    private int normalCount = 0;

    /** Create a new instance that writes output with the given writer.
     * @param writer writer used to write output
     */
    public OBJWriter(final Writer writer) {
        super(writer);
    }

    /** Write an OBJ comment with the given value.
     * @param comment comment to write
     * @throws IOException if an IO operation fails
     */
    public void writeComment(final String comment) throws IOException {
        for (final String line : comment.split("\\R")) {
            write(OBJConstants.COMMENT_CHAR);
            write(SPACE);
            write(line);
            writeNewLine();
        }
    }

    /** Write an object name to the output. This is metadata for the file and
     * does not affect the geometry, although it may affect how the file content
     * is read by other programs.
     * @param objectName the name to write
     * @throws IOException if an IO operation fails
     */
    public void writeObjectName(final String objectName) throws IOException {
        writeKeywordLine(OBJConstants.OBJECT_KEYWORD, objectName);
    }

    /** Write a group name to the output. This is metadata for the file and
     * does not affect the geometry, although it may affect how the file content
     * is read by other programs.
     * @param groupName the name to write
     * @throws IOException if an IO operation fails
     */
    public void writeGroupName(final String groupName) throws IOException {
        writeKeywordLine(OBJConstants.GROUP_KEYWORD, groupName);
    }

    /** Write a vertex and return the 0-based index of the vertex in the output.
     * @param vertex vertex to write
     * @throws IOException if an IO operation fails
     * @return 0-based index of the written vertex
     */
    public int writeVertex(final Vector3D vertex) throws IOException {
        writeKeywordLine(OBJConstants.VERTEX_KEYWORD, createVectorString(vertex));

        return vertexCount++;
    }

    /** Write a vertex normal and return the 0-based index of the normal in the output.
     * @param normal normal to write
     * @throws IOException if an IO operation fails
     * @return 0-based index of the written normal
     */
    public int writeNormal(final Vector3D normal) throws IOException {
        writeKeywordLine(OBJConstants.VERTEX_NORMAL_KEYWORD, createVectorString(normal));

        return normalCount++;
    }

    /** Write a face with the given vertex indices. Indices are 0-based.
     * @param vertexIndices 0-based vertex indices for the face
     * @throws IOException if an IO operation fails
     * @throw IllegalArgumentException if fewer than 3 vertex indices are given
     */
    public void writeFace(final int... vertexIndices) throws IOException {
        writeFaceWithOffsets(0, vertexIndices, 0, null);
    }

    public void writeFace(final int[] vertexIndices, final int normalIndex) throws IOException {
        final int[] normalIndices = new int[vertexIndices.length];
        Arrays.fill(normalIndices, normalIndex);

        writeFaceWithOffsets(0, vertexIndices, 0, normalIndices);
    }

    /** Write a face with the given vertex and normal indices. Indices are 0-based.
     * The {@code normalIndices} argument may be null, but if present, must contain the
     * same number of indices as {@code vertexIndices}.
     * @param vertexIndices 0-based vertex indices; may not be null
     * @param normalIndices 0-based normal indices; may be null but if present must contain
     *      the same number of indices as {@code vertexIndices}
     * @throws IOException if an IO operation fails
     * @throws IllegalArgumentException if fewer than 3 vertex indices are given or {@code normalIndices}
     *      is not null but has a different length than {@code vertexIndices}
     */
    public void writeFace(final int[] vertexIndices, final int[] normalIndices) throws IOException {
        writeFaceWithOffsets(0, vertexIndices, 0, normalIndices);
    }

    /** Write the boundaries present in the given boundary source. If the argument is a {@link Mesh},
     * it is written using {@link #writeMesh(Mesh)}. Otherwise, each boundary is written to the output
     * separately.
     * @param boundarySource boundary source containing the boundaries to write to the output
     * @throws IllegalArgumentException if any boundary in the argument is infinite
     * @throws IOException if an IO operation fails
     */
    public void writeBoundaries(final BoundarySource3D boundarySource) throws IOException {
        if (boundarySource instanceof Mesh) {
            // write directly as a mesh
            writeMesh((Mesh<?>) boundarySource);
        } else {
            // write using a buffer
            final MeshBuffer buffer = createMeshBuffer();

            try (final Stream<PlaneConvexSubset> stream = boundarySource.boundaryStream()) {
                final Iterator<PlaneConvexSubset> it = stream.iterator();
                while (it.hasNext()) {
                    buffer.add(it.next());
                }
            }

            buffer.flush();
        }
    }

    /** Write a mesh to the output. All vertices and faces in the mesh are written.
     * @param mesh the mesh to write
     * @throws IOException if an IO operation fails
     */
    public void writeMesh(final Mesh<?> mesh) throws IOException {
        final int vertexOffset = vertexCount;

        for (final Vector3D vertex : mesh.vertices()) {
            writeVertex(vertex);
        }

        for (final Mesh.Face face : mesh.faces()) {
            writeFaceWithOffsets(vertexOffset, face.getVertexIndices(), 0, null);
        }
    }

    public MeshBuffer createMeshBuffer() {
        return createMeshBuffer(DEFAULT_MESH_BUFFER_SIZE);
    }

    public MeshBuffer createMeshBuffer(final int batchSize) {
        return new MeshBuffer(batchSize);
    }

    /** Write a face with the given offsets and indices. The offsets are added to each
     * index before being written.
     * @param vertexOffset vertex offset value
     * @param vertexIndices 0-based vertex indices for the face
     * @param normalOffset normal offset value
     * @param normalIndices 0-based normal indices for the face; may be null if no normal are
     *      defined for the face
     * @throws IOException if an IO operation fails
     * @throws IllegalArgumentException if fewer than 3 vertex indices are given or {@code normalIndices}
     *      is not null but has a different length than {@code vertexIndices}
     */
    private void writeFaceWithOffsets(final int vertexOffset, final int[] vertexIndices,
            final int normalOffset, final int[] normalIndices) throws IOException {
        if (vertexIndices.length < 3) {
            throw new IllegalArgumentException("Face must have more than 3 vertices; found " + vertexIndices.length);
        } else if (normalIndices != null && normalIndices.length != vertexIndices.length) {
            throw new IllegalArgumentException("Face normal index count must equal vertex index count; expected " +
                    vertexIndices.length + " but was " + normalIndices.length);
        }

        write(OBJConstants.FACE_KEYWORD);

        for (int i = 0; i < vertexIndices.length; ++i) {
            write(SPACE);
            write(vertexIndices[i] + vertexOffset + 1); // convert to OBJ 1-based convention

            if (normalIndices != null) {
                // two separator chars since there is no texture coordinate
                write(OBJConstants.FACE_VERTEX_ATTRIBUTE_SEP_CHAR);
                write(OBJConstants.FACE_VERTEX_ATTRIBUTE_SEP_CHAR);

                write(normalIndices[i] + normalOffset + 1); // convert to OBJ 1-based convention
            }
        }

        writeNewLine();
    }

    private String createVectorString(final Vector3D vec) {
        final DecimalFormat df = getDecimalFormat();

        final StringBuilder sb = new StringBuilder();
        sb.append(df.format(vec.getX()))
            .append(SPACE)
            .append(df.format(vec.getY()))
            .append(SPACE)
            .append(df.format(vec.getZ()));

        return sb.toString();
    }

    private void writeKeywordLine(final String keyword, final String content) throws IOException {
        write(keyword);
        write(SPACE);
        write(content);
        writeNewLine();
    }

    public class MeshBuffer {

        private final int batchSize;

        private final Map<String, Integer> vertexMap = new LinkedHashMap<>();

        private final Map<String, Integer> normalMap = new LinkedHashMap<>();

        private final List<int[]> faceVertices;

        private final Map<Integer, Integer> faceToNormalMap = new HashMap<>();

        MeshBuffer(final int batchSize) {
            this.batchSize = batchSize;
            this.faceVertices = new ArrayList<>(batchSize);
        }

        public void add(final FacetDefinition facet) throws IOException {
            addFace(facet.getVertices(), facet.getDefinedNormal());
        }

        public void add(final PlaneConvexSubset boundary) throws IOException {
            if (boundary.isInfinite()) {
                throw new IllegalArgumentException("OBJ input geometry cannot be infinite: " + boundary);
            } else if (!boundary.isEmpty()) {
                addFace(boundary.getVertices(), null);
            }
        }

        public int addVertex(final Vector3D vertex) throws IOException {
            return addToMap(vertex, vertexMap);
        }

        public int addNormal(final Vector3D normal) throws IOException {
            return addToMap(normal, normalMap);
        }

        private int addToMap(final Vector3D vertex, final Map<String, Integer> map) {
            final String str = createVectorString(vertex);

            Integer idx = map.get(str);
            if (idx == null) {
                idx = map.size();
                map.put(str, idx);
            }

            return idx;
        }

        private void addFace(final List<Vector3D> vertices, final Vector3D normal) throws IOException {
            final int faceIndex = faceVertices.size();

            final int[] vertexIndices = new int[vertices.size()];

            int i = -1;
            for (final Vector3D vertex : vertices) {
                vertexIndices[++i] = addVertex(vertex);
            }
            faceVertices.add(vertexIndices);

            if (normal != null) {
                faceToNormalMap.put(faceIndex, addNormal(normal));
            }

            if (faceVertices.size() >= batchSize) {
                flush();
            }
        }

        public void flush() throws IOException {
            final int vertexOffset = vertexCount;
            final int normalOffset = normalCount;

            // write vertices
            for (final String vertexStr : vertexMap.keySet()) {
                writeKeywordLine(OBJConstants.VERTEX_KEYWORD, vertexStr);
            }

            // write normals
            for (final String normalStr : normalMap.keySet()) {
                writeKeywordLine(OBJConstants.VERTEX_NORMAL_KEYWORD, normalStr);
            }

            // write faces
            Integer normalIndex;
            int[] normalIndices;
            int faceIndex = 0;
            for (final int[] vertexIndices : faceVertices) {
                normalIndex = faceToNormalMap.get(faceIndex);
                if (normalIndex != null) {
                    normalIndices = new int[vertexIndices.length];
                    Arrays.fill(normalIndices, normalIndex);
                } else {
                    normalIndices = null;
                }

                writeFaceWithOffsets(vertexOffset, vertexIndices, normalOffset, normalIndices);

                ++faceIndex;
            }

            reset();
        }

        private void reset() {
            vertexMap.clear();
            normalMap.clear();
            faceVertices.clear();
            faceToNormalMap.clear();
        }
    }
}

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
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.mesh.Mesh;
import org.apache.commons.geometry.examples.io.internal.AbstractTextFormatWriter;

/** Class for writing OBJ files containing 3D mesh data.
 */
public final class OBJWriter extends AbstractTextFormatWriter implements AutoCloseable {

    /** Space character. */
    private static final char SPACE = ' ';

    /** Number of vertices written to the output. */
    private int vertexCount = 0;

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
        write(OBJConstants.OBJECT_KEYWORD);
        write(SPACE);
        write(objectName);
        writeNewLine();
    }

    /** Write a group name to the output. This is metadata for the file and
     * does not affect the geometry, although it may affect how the file content
     * is read by other programs.
     * @param groupName the name to write
     * @throws IOException if an IO operation fails
     */
    public void writeGroupName(final String groupName) throws IOException {
        write(OBJConstants.GROUP_KEYWORD);
        write(SPACE);
        write(groupName);
        writeNewLine();
    }

    /** Write a vertex to the output. The OBJ 1-based index of the vertex is returned. This
     * index can be used to reference the vertex in faces via {@link #writeFace(int...)}.
     * @param vertex vertex to write
     * @throws IOException if an IO operation fails
     * @return the index of the written vertex in the OBJ 1-based convention
     * @throws IOException if an IO operation fails
     */
    public int writeVertex(final Vector3D vertex) throws IOException {
        write(OBJConstants.VERTEX_KEYWORD);
        write(SPACE);
        write(vertex.getX());
        write(SPACE);
        write(vertex.getY());
        write(SPACE);
        write(vertex.getZ());
        writeNewLine();

        return ++vertexCount;
    }

    /** Write a face with the given 0-based vertex indices.
     * @param vertexIndices 0-based vertex indices for the face
     * @throws IOException if an IO operation fails
     */
    public void writeFace(final int... vertexIndices) throws IOException {
        writeFaceWithVertexOffset(0, vertexIndices);
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
            writeMesh((Mesh<?>) boundarySource);
        } else {
            try (final Stream<PlaneConvexSubset> stream = boundarySource.boundaryStream()) {
                writeBoundaries(stream.iterator());
            }
        }
    }

    /** Write the boundaries in the argument to the output. Each boundary is written separately.
     * @param it boundary iterator
     * @throws IllegalArgumentException if any boundary in the argument is infinite
     * @throws IOException if an IO operation fails
     */
    private void writeBoundaries(final Iterator<PlaneConvexSubset> it) throws IOException {
        PlaneConvexSubset boundary;
        List<Vector3D> vertices;
        int[] vertexIndices;

        while (it.hasNext()) {
            boundary = it.next();
            if (boundary.isInfinite()) {
                throw new IllegalArgumentException("OBJ input geometry cannot be infinite: " + boundary);
            }

            vertices = boundary.getVertices();
            vertexIndices = new int[vertices.size()];

            for (int i = 0; i < vertexIndices.length; ++i) {
                vertexIndices[i] = writeVertex(vertices.get(i));
            }

            writeFace(vertexIndices);
        }
    }

    /** Write a mesh to the output.
     * @param mesh the mesh to write
     * @throws IOException if an IO operation fails
     */
    public void writeMesh(final Mesh<?> mesh) throws IOException {
        final int vertexOffset = vertexCount;

        for (final Vector3D vertex : mesh.vertices()) {
            writeVertex(vertex);
        }

        for (final Mesh.Face face : mesh.faces()) {
            writeFaceWithVertexOffset(vertexOffset, face.getVertexIndices());
        }
    }

    /** Write a face with the given vertex offset value and 0-based indices. The offset is added to each
     * index before being written.
     * @param vertexOffset vertex offset value
     * @param vertexIndices 0-based vertex indices for the face
     * @throws IOException if an IO operation fails
     */
    private void writeFaceWithVertexOffset(final int vertexOffset, final int... vertexIndices)
            throws IOException {
        if (vertexIndices.length < 3) {
            throw new IllegalArgumentException("Face must have more than 3 vertices; found " + vertexIndices.length);
        }

        write(OBJConstants.FACE_KEYWORD);

        for (final int vertexIndex : vertexIndices) {
            write(SPACE);
            write(vertexIndex + vertexOffset + 1); // convert to OBJ 1-based convention
        }

        writeNewLine();
    }
}

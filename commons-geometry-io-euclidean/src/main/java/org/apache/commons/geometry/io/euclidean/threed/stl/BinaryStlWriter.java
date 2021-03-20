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

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.euclidean.threed.Planes;
import org.apache.commons.geometry.euclidean.threed.Triangle3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.io.euclidean.threed.FacetDefinition;

/** Class for writing binary STL content.
 */
public class BinaryStlWriter implements Closeable {

    /** Initial size of the data buffer. */
    private static final int DEFAULT_DATA_BUFFER_SIZE = 1000 * StlConstants.BINARY_TRIANGLE_BYTES;

    /** Output stream to write to. */
    private final OutputStream out;

    /** Output stream used to store the written triangle data until all triangles
     * have been written.
     */
    private final ByteArrayOutputStream data = new ByteArrayOutputStream(DEFAULT_DATA_BUFFER_SIZE);

    /** Buffer used to construct triangle definitions. */
    private final ByteBuffer triangleBuffer =
            ByteBuffer.allocate(StlConstants.BINARY_TRIANGLE_BYTES)
                .order(StlConstants.BINARY_BYTE_ORDER);

    /** Number of triangles in the data buffer. */
    private int triangleCount;

    /** Construct a new instance for writing to the given output.
     * @param out output stream to write to
     * @param triangleCount number of triangles that will be written
     */
    public BinaryStlWriter(final OutputStream out) {
        this.out = out;
    }

    /** Write the given boundary to the output as triangles.
     * @param boundary boundary to write
     * @throws IOException if an I/O error occurs
     * @see PlaneConvexSubset#toTriangles()
     */
    public void write(final PlaneConvexSubset boundary, int attributeValue) throws IOException {
        for (final Triangle3D tri : boundary.toTriangles()) {
            write(tri.getVertices(), tri.getPlane().getNormal(), attributeValue);
        }
    }

    /** Write the given facet definition to the output as triangles.
     * @param facet facet definition to write
     * @throws IOException if an I/O error occurs
     */
    public void write(final FacetDefinition facet, int attributeValue) throws IOException {
        write(facet.getVertices(), facet.getNormal(), attributeValue);
    }

    /** Write the facet defined by the given vertices and normal to the output as triangles.
     * @param vertices vertices defining the facet
     * @param normal facet normal; may be null
     * @throws IOException if an I/O error occurs
     */
    public void write(final List<Vector3D> vertices, final Vector3D normal, int attributeValue)
            throws IOException {
        for (final List<Vector3D> triangle : Planes.convexPolygonToTriangleFan(vertices, Function.identity())) {
            write(triangle.get(0), triangle.get(1), triangle.get(2), normal, attributeValue);
        }
    }

    /** Write the given triangle to the output.
     * @param p1 first point
     * @param p2 second point
     * @param p3 third point
     * @param normal normal; may be null, in which case the zero vector is used
     * @param attributeValue 2-byte STL triangle attribute value
     * @throws IOException
     */
    public void write(final Vector3D p1, final Vector3D p2, final Vector3D p3,
            final Vector3D normal, final int attributeValue) throws IOException {
        triangleBuffer.rewind();

        putVector(normal == null ? Vector3D.ZERO : normal);
        putVector(p1);
        putVector(p2);
        putVector(p3);
        triangleBuffer.putShort((short) attributeValue);

        triangleBuffer.flip();
        data.write(triangleBuffer.array());

        ++triangleCount;
    }

    /** {@inheritDoc} */
    @Override
    public void close() throws IOException {
        flush();
        out.close();
    }

    /** Flush all data to the output.
     */
    private void flush() throws IOException {
        // write 80 bytes of 0s for the main header
        byte[] header = new byte[StlConstants.BINARY_HEADER_BYTES];
        Arrays.fill(header, (byte) 0);

        out.write(header);

        // write the triangle count number
        ByteBuffer countBuffer = ByteBuffer.allocate(Integer.BYTES)
                .order(StlConstants.BINARY_BYTE_ORDER);
        countBuffer.putInt(triangleCount);
        countBuffer.flip();

        out.write(countBuffer.array());

        // write the data to the output
        data.writeTo(out);
    }

    /** Put all double components of {@code vec} into the internal buffer.
     * @param vec vector to place into the buffer
     */
    private void putVector(final Vector3D vec) {
        triangleBuffer.putFloat((float) vec.getX());
        triangleBuffer.putFloat((float) vec.getY());
        triangleBuffer.putFloat((float) vec.getZ());
    }
}

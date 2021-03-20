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

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import org.apache.commons.geometry.euclidean.internal.Vectors;
import org.apache.commons.geometry.euclidean.threed.Vector3D;

/** Low-level class for writing binary STL content.
 */
public class BinaryStlWriter implements Closeable {

    /** Output stream to write to. */
    private final OutputStream out;

    /** Buffer used to construct triangle definitions. */
    private final ByteBuffer triangleBuffer = StlUtils.byteBuffer(StlConstants.BINARY_TRIANGLE_BYTES);

    /** Construct a new instance for writing to the given output.
     * @param out output stream to write to
     */
    public BinaryStlWriter(final OutputStream out) {
        this.out = out;
    }

    /** Write binary STL header content. If {@code headerContent} is null, the written header
     * will consist entirely of zeros. Otherwise, up to 80 bytes from {@code headerContent}
     * are written to the header, with any remaining bytes of the header filled with zeros.
     * @param headerContent bytes to include in the header; may be null
     * @param triangleCount number of triangles to be included in the content
     * @throws IOException if an I/O error occurs
     */
    public void writeHeader(final byte[] headerContent, final int triangleCount) throws IOException {
        writeHeader(headerContent, triangleCount, out);
    }

    /** Write a triangle to the output using a default attribute value of 0.
     * Callers are responsible for ensuring that the number of triangles written
     * matches the number given in the header.
     * @param p1 first point
     * @param p2 second point
     * @param p3 third point
     * @param normal normal; may be null, in which case the zero vector is used
     * @throws IOException if an I/O error occurs
     */
    public void writeTriangle(final Vector3D p1, final Vector3D p2, final Vector3D p3,
            final Vector3D normal) throws IOException {
        writeTriangle(p1, p2, p3, normal, 0);
    }

    /** Write a triangle to the output. Callers are responsible for ensuring
     * that the number of triangles written matches the number given in the header.
     * @param p1 first point
     * @param p2 second point
     * @param p3 third point
     * @param normal normal; may be null, in which case the zero vector is used
     * @param attributeValue 2-byte STL triangle attribute value
     * @throws IOException if an I/O error occurs
     */
    public void writeTriangle(final Vector3D p1, final Vector3D p2, final Vector3D p3,
            final Vector3D normal, final int attributeValue) throws IOException {
        triangleBuffer.rewind();

        putVector(normal == null ? Vector3D.ZERO : normal);
        putVector(p1);

        if (pointsAreCounterClockwise(p1, p2, p3, normal)) {
            putVector(p2);
            putVector(p3);
        } else {
            putVector(p3);
            putVector(p2);
        }

        triangleBuffer.putShort((short) attributeValue);

        out.write(triangleBuffer.array());
    }

    /** {@inheritDoc} */
    @Override
    public void close() throws IOException {
        out.close();
    }

    /** Put all double components of {@code vec} into the internal buffer.
     * @param vec vector to place into the buffer
     */
    private void putVector(final Vector3D vec) {
        triangleBuffer.putFloat((float) vec.getX());
        triangleBuffer.putFloat((float) vec.getY());
        triangleBuffer.putFloat((float) vec.getZ());
    }

    /** Return true if the given points are arranged counter-clockwise relative to the
     * given normal. Returns true if {@code normal} is null.
     * @param p1 first point
     * @param p2 second point
     * @param p3 third point
     * @param normal normal; may be null, in which case the zero vector is used
     * @return true if {@code normal} is null or if the given points are arranged counter-clockwise
     *      relative to {@code normal}
     */
    private boolean pointsAreCounterClockwise(final Vector3D p1, final Vector3D p2, final Vector3D p3,
            final Vector3D normal) {
        if (normal != null) {
            final Vector3D computedNormal = Vectors.tryNormalize(p1.vectorTo(p2).cross(p1.vectorTo(p3)));
            if (computedNormal != null && normal.dot(computedNormal) < 0) {
                return false;
            }
        }

        return true;
    }

    /** Write binary STL header content to the given output stream. If {@code headerContent}
     * is null, the written header will consist entirely of zeros. Otherwise, up to 80 bytes
     * from {@code headerContent} are written to the header, with any remaining bytes of the
     * header filled with zeros.
     * @param headerContent
     * @param triangleCount
     * @param out
     */
    static void writeHeader(final byte[] headerContent, final int triangleCount, final OutputStream out)
            throws IOException {

        // write the header
        final byte[] bytes = new byte[StlConstants.BINARY_HEADER_BYTES];
        if (headerContent != null) {
            System.arraycopy(
                    headerContent, 0,
                    bytes, 0,
                    Math.min(headerContent.length, StlConstants.BINARY_HEADER_BYTES));
        }

        out.write(bytes);

        // write the triangle count number
        ByteBuffer countBuffer = StlUtils.byteBuffer(Integer.BYTES);
        countBuffer.putInt(triangleCount);
        countBuffer.flip();

        out.write(countBuffer.array());
    }
}

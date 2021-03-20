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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.Arrays;

import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.io.euclidean.threed.FacetDefinitionReader;

/** Class used to read the binary form of the STL file format.
 * @see <a href="https://en.wikipedia.org/wiki/STL_(file_format)#Binary_STL">Binary STL</a>
 */
public class BinaryStlFacetDefinitionReader implements FacetDefinitionReader {

    /** Input stream to read from. */
    private final InputStream in;

    /** Buffer used to read triangle definitions. */
    private final ByteBuffer triangleBuffer =
            ByteBuffer.allocate(StlConstants.BINARY_TRIANGLE_BYTES)
                .order(StlConstants.BINARY_BYTE_ORDER);

    /** Number of bytes remaining to be read in the header. */
    private int headerBytesRemaining;

    /** Total number of triangles declared to be present in the input. */
    private long triangleTotal;

    /** Number of triangles read so far. */
    private long trianglesRead;

    /** Construct a new instance that reads from the given input stream.
     * @param in input stream to read from.
     */
    public BinaryStlFacetDefinitionReader(final InputStream in) {
        this(in, 0);
    }

    /** Construct a new instance that reads from the given input stream. Callers can
     * specify how many bytes of the header content have already been read outside
     * of this class.
     * @param in input stream
     * @param headerBytesRead number of header bytes that have been read from the
     *      input stream at the time of instantiation
     * @throws IllegalArgumentException if {@code headerBytesRead} is less than zero
     *      or greater than the number of bytes in a binary STL header
     */
    BinaryStlFacetDefinitionReader(final InputStream in, final int headerBytesRead) {
        if (headerBytesRead < 0 || headerBytesRead > StlConstants.BINARY_HEADER_BYTES) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Invalid number of header bytes read: value must be between {} and {} but was {}",
                    0, StlConstants.BINARY_HEADER_BYTES, headerBytesRead));
        }

        this.in = in;
        this.headerBytesRemaining = StlConstants.BINARY_HEADER_BYTES - headerBytesRead;
    }

    /** Get the total number of triangles (i.e. facets) declared to be present in the input.
     * @return total number of triangle declared to be present in the input
     * @throws IOException if an I/O error occurs
     */
    public long getTriangleCount() throws IOException {
        beginRead();
        return triangleTotal;
    }

    /** Get the number of triangles (i.e. facets) read so far.
     * @return number of triangles read so far
     */
    public long getTrianglesRead() {
        return trianglesRead;
    }

    /** {@inheritDoc} */
    @Override
    public BinaryStlFacetDefinition readFacet() throws IOException {
        beginRead();

        BinaryStlFacetDefinition facet = null;

        if (trianglesRead < triangleTotal) {
            facet = readFacetInternal();

            ++trianglesRead;
        }

        return facet;
    }

    /** {@inheritDoc} */
    @Override
    public void close() throws IOException {
        in.close();
    }

    /** Read the file header content and triangle count.
     * @throws IOException if an I/O error occurs
     */
    private void beginRead() throws IOException {
        if (headerBytesRemaining > 0) {
            headerBytesRemaining -= in.skip(headerBytesRemaining);
            if (headerBytesRemaining > 0) {
                throw new IOException("Failed to locate end of header content");
            }

            ByteBuffer buf = ByteBuffer.allocate(Integer.BYTES)
                    .order(StlConstants.BINARY_BYTE_ORDER);

            int read = fill(buf);
            if (read < buf.capacity()) {
                throw new IOException(MessageFormat.format(
                        "Failed to read triangle total count: expected {} bytes but only found {} available",
                        buf.capacity(), read));
            }

            triangleTotal = Integer.toUnsignedLong(buf.getInt());
        }
    }

    /** Internal method to read a single facet from the input.
     * @return facet read from the input
     */
    private BinaryStlFacetDefinition readFacetInternal() throws IOException {
        int read = fill(triangleBuffer);
        if (read < triangleBuffer.capacity()) {
            throw new IOException(MessageFormat.format(
                    "Failed to read triangle at index {}: expected {} bytes but only found {} available",
                    trianglesRead, triangleBuffer.capacity(), read));
        }

        final Vector3D normal = readVector(triangleBuffer);
        final Vector3D p1 = readVector(triangleBuffer);
        final Vector3D p2 = readVector(triangleBuffer);
        final Vector3D p3 = readVector(triangleBuffer);

        final int attr = Short.toUnsignedInt(triangleBuffer.getShort());

        return new BinaryStlFacetDefinition(Arrays.asList(p1, p2, p3), normal, attr);
    }

    /** Fill the buffer with data from the input stream. The buffer is then flipped and
     * made ready for reading.
     * @param buf buffer to fill
     * @param in input stream to read from
     * @return number of bytes read
     * @throws IOException if an I/O error occurs
     */
    private int fill(final ByteBuffer buf) throws IOException {
        buf.rewind();
        int read = in.read(buf.array());

        return read;
    }

    /** Read a vector from the given byte buffer.
     * @param buf buffer to read from
     * @return vector containing the next 3 double values from the
     *      given buffer
     */
    private Vector3D readVector(final ByteBuffer buf) {
        final double x = buf.getFloat();
        final double y = buf.getFloat();
        final double z = buf.getFloat();

        return Vector3D.of(x, y, z);
    }
}

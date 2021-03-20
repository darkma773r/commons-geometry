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
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.euclidean.threed.Planes;
import org.apache.commons.geometry.euclidean.threed.Triangle3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.io.core.GeometryFormat;
import org.apache.commons.geometry.io.core.output.GeometryOutput;
import org.apache.commons.geometry.io.euclidean.threed.AbstractBoundaryWriteHandler3D;
import org.apache.commons.geometry.io.euclidean.threed.FacetDefinition;
import org.apache.commons.geometry.io.euclidean.threed.GeometryFormat3D;

/** {@link org.apache.commons.geometry.io.euclidean.threed.BoundaryWriteHandler3D BoundaryWriteHandler3D}
 * implementation for writing STL content. Because of its compact nature, all STL content is written in
 * binary format, as opposed the text (i.e. "ASCII") format. Callers should use the {@link TextStlWriter}
 * class directly in order to create text STL content.
 */
public class StlBoundaryWriteHandler3D extends AbstractBoundaryWriteHandler3D {

    /** Initial size of the data buffer. */
    private static final int DEFAULT_BUFFER_SIZE = 1000 * StlConstants.BINARY_TRIANGLE_BYTES;

    /** Size of the data buffer. */
    private int bufferSize = DEFAULT_BUFFER_SIZE;

    /** {@inheritDoc} */
    @Override
    public GeometryFormat getFormat() {
        return GeometryFormat3D.STL;
    }

    /** Get the size of the data buffers used by this instance.
     * @return buffer size
     */
    public int getBufferSize() {
        return bufferSize;
    }

    /** Set the size of the data buffers used by this instance.
     * @param bufferSize buffer size
     */
    public void setBufferSize(final int bufferSize) {
        if (bufferSize < 1) {
            throw new IllegalArgumentException("Buffer size must be greater than 1");
        }
        this.bufferSize = bufferSize;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final Stream<? extends PlaneConvexSubset> boundaries, GeometryOutput out)
            throws IOException {

        // write the triangle data to a buffer and track how many we write
        int triangleCount = 0;
        final ByteArrayOutputStream data = new ByteArrayOutputStream(bufferSize);

        try (BinaryStlWriter dataWriter = new BinaryStlWriter(data)) {
            final Iterator<? extends PlaneConvexSubset> it = boundaries.iterator();

            while (it.hasNext()) {
                for (final Triangle3D tri : it.next().toTriangles()) {

                    dataWriter.writeTriangle(
                            tri.getPoint1(),
                            tri.getPoint2(),
                            tri.getPoint3(),
                            tri.getPlane().getNormal());

                    ++triangleCount;
                }
            }
        }

        // write the header and copy the data
        try (OutputStream os = out.getOutputStream()) {
            BinaryStlWriter.writeHeader(null, triangleCount, os);
            data.writeTo(os);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void writeFacets(final Stream<? extends FacetDefinition> facets, GeometryOutput out)
            throws IOException {

        // write the triangle data to a buffer and track how many we write
        int triangleCount = 0;
        final ByteArrayOutputStream data = new ByteArrayOutputStream(bufferSize);

        try (BinaryStlWriter dataWriter = new BinaryStlWriter(data)) {
            final Iterator<? extends FacetDefinition> it = facets.iterator();

            FacetDefinition facet;
            int attributeValue;

            while (it.hasNext()) {
                facet = it.next();
                attributeValue = getFacetAttributeValue(facet);

                for (final List<Vector3D> tri : Planes.convexPolygonToTriangleFan(facet.getVertices(), t -> t)) {

                    dataWriter.writeTriangle(
                            tri.get(0),
                            tri.get(1),
                            tri.get(2),
                            facet.getNormal(),
                            attributeValue);

                    ++triangleCount;
                }
            }
        }

        // write the header and copy the data
        try (OutputStream os = out.getOutputStream()) {
            BinaryStlWriter.writeHeader(null, triangleCount, os);
            data.writeTo(os);
        }
    }

    /** Get the attribute value that should be used for the given facet.
     * @param facet facet to get the attribute value for
     * @return attribute value
     */
    private int getFacetAttributeValue(final FacetDefinition facet) {
        if (facet instanceof BinaryStlFacetDefinition) {
            return ((BinaryStlFacetDefinition) facet).getAttributeValue();
        }

        return 0;
    }
}

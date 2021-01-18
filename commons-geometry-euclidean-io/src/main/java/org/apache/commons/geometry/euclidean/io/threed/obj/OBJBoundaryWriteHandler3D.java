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
package org.apache.commons.geometry.euclidean.io.threed.obj;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.Collection;

import org.apache.commons.geometry.core.io.internal.GeometryIOUtils;
import org.apache.commons.geometry.euclidean.io.threed.BoundaryWriteHandler3D;
import org.apache.commons.geometry.euclidean.io.threed.FacetDefinition;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;

/** {@link BoundaryWriteHandler3D} implementation for writing OBJ content.
 * Output is written using the UTF-8 charset by default.
 */
public class OBJBoundaryWriteHandler3D implements BoundaryWriteHandler3D {

    /** The default line separator value. */
    private static final String DEFAULT_LINE_SEPARATOR = "\n";

    /** Default mesh buffer batch size. */
    private static final int DEFAULT_MESH_BUFFER_BATCH_SIZE = -1;

    /** Charset used for text output. */
    private Charset charset = OBJConstants.DEFAULT_CHARSET;

    /** Line separator string. */
    private String lineSeparator = DEFAULT_LINE_SEPARATOR;

    /** Decimal format pattern. */
    private String decimalFormatPattern;

    /** Batch size used for mesh buffer creation. */
    private int meshBufferBatchSize = DEFAULT_MESH_BUFFER_BATCH_SIZE;

    /** Get the text output charset.
     * @return text output charset
     */
    public Charset getCharset() {
        return charset;
    }

    /** Set the text output charset.
     * @param charset text output charset
     */
    public void setCharset(final Charset charset) {
        this.charset = charset;
    }

    /** Get the line separator. This value defaults to {@value #DEFAULT_LINE_SEPARATOR}.
     * @return the current line separator
     */
    public String getLineSeparator() {
        return lineSeparator;
    }

    /** Set the line separator.
     * @param lineSeparator the line separator to use
     */
    public void setLineSeparator(final String lineSeparator) {
        this.lineSeparator = lineSeparator;
    }

    /** Get the format string used to construct {@link DecimalFormat} instances for
     * formatting decimal output. If null, default instances are used.
     * @return format string used to construct {@link DecimalFormat} instances; may be null
     */
    public String getDecimalFormatPattern() {
        return decimalFormatPattern;
    }

    /** Set the format string used to construct {@link DecimalFormat} instances for
     * formatting decimal output. If set to null, default instances are used.
     * @param decimalFormatPattern format pattern; may be null
     */
    public void setDecimalFormatPattern(final String decimalFormatPattern) {
        this.decimalFormatPattern = decimalFormatPattern;
    }

    /** Get the batch size when generating OBJ mesh content from facet sequences. Larger batch sizes
     * allow for reuse of vertex definitions but at the cost of more memory usage. The buffer size is
     * unlimited if set to {@code -1}. Default value is {@value #DEFAULT_MESH_BUFFER_BATCH_SIZE}.
     * @return mesh buffer batch size
     * @see OBJWriter#meshBuffer(int)
     */
    public int getMeshBufferBatchSize() {
        return meshBufferBatchSize;
    }

    /** Set the batch size when generating OBJ mesh content from facet sequences. Larger batch sizes
     * allow for reuse of vertex definitions but at the cost of more memory usage. Set to {@code -1}
     * to allow unlimited buffer size. Default value is {@value #DEFAULT_MESH_BUFFER_BATCH_SIZE}.
     * @param batchSize mesh buffer batch size; set to {@code -1} to allow unlimited buffer sizes
     * @see OBJWriter#meshBuffer(int)
     */
    public void setMeshBufferBatchSize(final int batchSize) {
        this.meshBufferBatchSize = batchSize;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final BoundarySource3D src, final OutputStream out)
            throws IOException {
        try (OBJWriter writer = createOBJWriter(out)) {
            writer.writeBoundaries(src, meshBufferBatchSize);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void writeFacets(final Collection<? extends FacetDefinition> facets, final OutputStream out)
            throws IOException {
        try (OBJWriter writer = createOBJWriter(out)) {
            final OBJWriter.MeshBuffer meshBuffer = writer.meshBuffer(meshBufferBatchSize);

            for (final FacetDefinition facet : facets) {
                meshBuffer.add(facet);
            }

            meshBuffer.flush();
        }
    }

    /** Construct a new, configured {@link OBJWriter} instance for writing content to the given
     * output stream.
     * @param out output stream to write to
     * @return new {@code OBJWriter} for writing content to the given output stream
     */
    private OBJWriter createOBJWriter(final OutputStream out) {
        final OBJWriter writer = new OBJWriter(GeometryIOUtils.createCloseShieldWriter(out, charset));
        writer.setLineSeparator(lineSeparator);

        if (decimalFormatPattern != null) {
            writer.setDecimalFormat(new DecimalFormat(decimalFormatPattern));
        }

        return writer;
    }
}

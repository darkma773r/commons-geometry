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
package org.apache.commons.geometry.examples.io.threed.text;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.euclidean.threed.Triangle3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.examples.io.internal.AbstractTextFormatWriter;
import org.apache.commons.geometry.examples.io.threed.facet.FacetDefinition;

public class TextFacetDefinitionWriter extends AbstractTextFormatWriter {

    private static final String CSV_SEPARATOR = ",";

    private static final int CSV_FACET_VERTEX_COUNT = 3;

    private static final String DEFAULT_VERTEX_COMPONENT_SEPARATOR = " ";

    private static final String DEFAULT_VERTEX_SEPARATOR = "; ";

    private static final String DEFAULT_COMMENT_TOKEN = "# ";

    private String vertexComponentSeparator = DEFAULT_VERTEX_COMPONENT_SEPARATOR;

    private String vertexSeparator = DEFAULT_VERTEX_SEPARATOR;

    private int facetVertexCount = -1;

    private String commentToken = DEFAULT_COMMENT_TOKEN;

    public TextFacetDefinitionWriter(final Writer writer) {
        super(writer);
    }

    public String getVertexComponentSeparator() {
        return vertexComponentSeparator;
    }

    public void setVertexComponentSeparator(final String sep) {
        this.vertexComponentSeparator = sep;
    }

    public String getVertexSeparator() {
        return vertexSeparator;
    }

    public void setVertexSeparator(final String sep) {
        this.vertexSeparator = sep;
    }

    public int getFacetVertexCount() {
        return facetVertexCount;
    }

    public void setFacetVertexCount(final int vertexCount) {
        if (vertexCount > -1 &&  vertexCount < 3) {
            throw new IllegalArgumentException("Facet vertex count must be less than 0 or greater than 2; was " +
                    vertexCount);
        }

        this.facetVertexCount = vertexCount;
    }

    public String getCommentToken() {
        return commentToken;
    }

    public void setCommentToken(final String commentToken) {
        if (commentToken != null) {
            if (commentToken.isEmpty()) {
                throw new IllegalArgumentException("Comment token cannot be empty");
            } else if (Character.isWhitespace(commentToken.charAt(0))) {
                throw new IllegalArgumentException("Comment token cannot begin with whitespace");
            }

        }

        this.commentToken = commentToken;
    }

    public void writeComment(final String comment) throws IOException {
        if (commentToken == null) {
            throw new IllegalStateException("Cannot write comment: no comment token configured");
        }

        if (comment != null) {
            for (final String line : comment.split("\\R")) {
                write(commentToken + line);
                writeNewLine();
            }
        }
    }

    public void writeBlankLine() throws IOException {
        writeNewLine();
    }

    public void write(final BoundarySource3D src) throws IOException {
        try (final Stream<PlaneConvexSubset> stream = src.boundaryStream()) {
            final Iterator<PlaneConvexSubset> it = stream.iterator();
            while (it.hasNext()) {
                write(it.next());
            }
        }
    }

    public void write(final PlaneConvexSubset convexSubset) throws IOException {
        if (convexSubset.isInfinite()) {
            throw new IllegalArgumentException("Cannot write infinite convex subset");
        }

        if (facetVertexCount == 3) {
            // force conversion to triangles
            for (final Triangle3D tri : convexSubset.toTriangles()) {
                write(tri.getVertices());
            }
        } else {
            // write as-is; callers are responsible for making sure that the number of
            // vertices matches the required number for the writer
            write(convexSubset.getVertices());
        }
    }

    public void write(final FacetDefinition facet) throws IOException {
        write(facet.getVertices());
    }

    /** Write a list of vertices defining a facet as a single line of text to the output. Vertex components
     * (ie, individual x, y, z values) are separated with the configured
     * {@link #getVertexComponentSeparator() vertex component separator} and vertices are separated with the
     * configured {@link #getVertexSeparator() vertex separator}.
     * @param vertices vertices to write
     * @throws IOException if an I/O error occurs
     * @throws IllegalArgumentException if the vertex list contains less than 3 vertices or a
     *      {@link #getFacetVertexCount() facet vertex count} has been configured and the number of required
     *      vertices does not match that given
     * @see #getVertexComponentSeparator()
     * @see #getVertexSeparator()
     * @see #getFacetVertexCount()
     */
    public void write(final List<Vector3D> vertices) throws IOException {
        final int size = vertices.size();
        if (size < 3) {
            throw new IllegalArgumentException("At least 3 vertices are required per facet; found " + size);
        } else if (facetVertexCount > -1 && size != facetVertexCount) {
            throw new IllegalArgumentException("Writer requires " + facetVertexCount +
                    " vertices per facet; found " + size);
        }

        final Iterator<Vector3D> it = vertices.iterator();

        write(it.next());
        while (it.hasNext()) {
            write(vertexSeparator);
            write(it.next());
        }

        writeNewLine();
    }

    /** Write a single vertex to the output.
     * @param vertex vertex to write
     * @throws IOException if an I/O error occurs
     */
    private void write(final Vector3D vertex) throws IOException {
        write(vertex.getX());
        write(vertexComponentSeparator);
        write(vertex.getY());
        write(vertexComponentSeparator);
        write(vertex.getZ());
    }

    public static TextFacetDefinitionWriter csvFormat(final Writer writer) {
        final TextFacetDefinitionWriter fdWriter = new TextFacetDefinitionWriter(writer);
        fdWriter.setVertexComponentSeparator(CSV_SEPARATOR);
        fdWriter.setVertexSeparator(CSV_SEPARATOR);
        fdWriter.setFacetVertexCount(CSV_FACET_VERTEX_COUNT);
        fdWriter.setCommentToken(null);

        return fdWriter;
    }
}

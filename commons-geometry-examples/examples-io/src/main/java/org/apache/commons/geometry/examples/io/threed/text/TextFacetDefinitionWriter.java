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

    private static final String DEFAULT_VERTEX_COORDINATE_SEPARATOR = " ";

    private static final String DEFAULT_VERTEX_SEPARATOR = "; ";

    private String vertexCoordinateSeparator = DEFAULT_VERTEX_COORDINATE_SEPARATOR;

    private String vertexSeparator = DEFAULT_VERTEX_SEPARATOR;

    private int facetVertexCount = -1;

    public TextFacetDefinitionWriter(final Writer writer) {
        super(writer);
    }

    public String getVertexCoordinateSeparator() {
        return vertexCoordinateSeparator;
    }

    public void setVertexCoordinateSeparator(final String sep) {
        this.vertexCoordinateSeparator = sep;
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

    public void write(final BoundarySource3D src) throws IOException {
        try (final Stream<PlaneConvexSubset> stream = src.boundaryStream()) {
            final Iterator<PlaneConvexSubset> it = stream.iterator();
            while (it.hasNext()) {
                write(it.next());
            }
        }
    }

    public void write(final PlaneConvexSubset convexSubset) throws IOException {
        if (convexSubset.isEmpty()) {
            throw new IllegalArgumentException("Cannot write empty convex subset");
        } else if (convexSubset.isInfinite()) {
            throw new IllegalArgumentException("Cannot write infinite convex subset");
        }

        if (facetVertexCount == 3) {
            // force conversion to triangles
            for (final Triangle3D tri : convexSubset.toTriangles()) {
                write(tri);
            }
        } else {
            // write as-is
            write(convexSubset.getVertices());
        }
    }

    public void write(final FacetDefinition facet) throws IOException {
        write(facet.getVertices());
    }

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

    private void write(final Vector3D vertex) throws IOException {
        write(vertex.getX());
        write(vertexCoordinateSeparator);
        write(vertex.getY());
        write(vertexCoordinateSeparator);
        write(vertex.getZ());
    }

    public static TextFacetDefinitionWriter csvFormat(final Writer writer) {
        final TextFacetDefinitionWriter fdWriter = new TextFacetDefinitionWriter(writer);
        fdWriter.setVertexCoordinateSeparator(CSV_SEPARATOR);
        fdWriter.setVertexCoordinateSeparator(CSV_SEPARATOR);
        fdWriter.setFacetVertexCount(CSV_FACET_VERTEX_COUNT);

        return fdWriter;
    }
}

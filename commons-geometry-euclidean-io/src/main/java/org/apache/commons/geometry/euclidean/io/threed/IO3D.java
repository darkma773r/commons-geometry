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
package org.apache.commons.geometry.euclidean.io.threed;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Stream;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.euclidean.threed.Triangle3D;
import org.apache.commons.geometry.euclidean.threed.mesh.TriangleMesh;

public final class IO3D {

    /** String representing the OBJ file format.
     * @see <a href="https://en.wikipedia.org/wiki/Wavefront_.obj_file">Wavefront .obj file</a>
     */
    public static final String OBJ = "obj";

    /** String representing the simple text format described by
     * {@link org.apache.commons.geometry.euclidean.io.threed.text.TextFacetDefinitionReader TextFacetDefinitionReader}
     * and
     * {@link org.apache.commons.geometry.euclidean.io.threed.text.TextFacetDefinitionWriter TextFacetDefinitionWriter}.
     * This format describes facets by listing the coordinates of its vertices in order, with one facet
     * described per line. Facets may have 3 or more vertices and do not need to all have the same
     * number of vertices.
     */
    public static final String TXT = "txt";

    /** String representing the CSV file format as described by
     * {@link org.apache.commons.geometry.euclidean.io.threed.text.TextFacetDefinitionWriter#csvFormat(java.io.Writer)
     * TextFacetDefinitionWriter}. When used to represent 3D geometry information, the coordinates of the vertices of
     * the facets are listed in order, with one facet defined per row. This is similar to the {@link #TXT} format
     * with the exception that facets are are converted to triangles before writing so that all rows have the same
     * number of columns.
     */
    public static final String CSV = "csv";

    /** Utility class; no instantiation. */
    private IO3D() {}

    public static FacetDefinitionReader facetDefinitionReader(final Path path) throws IOException {
        return getDefaultManager().facetDefinitionReader(path);
    }

    public static FacetDefinitionReader facetDefinitionReader(final URL url) throws IOException {
        return getDefaultManager().facetDefinitionReader(url);
    }

    public static FacetDefinitionReader facetDefinitionReader(final String formatName, final InputStream in)
            throws IOException {
        return getDefaultManager().facetDefinitionReader(formatName, in);
    }

    public static Stream<FacetDefinition> facets(final Path path) throws IOException {
        return getDefaultManager().facets(path);
    }

    public static Stream<FacetDefinition> facets(final URL url) throws IOException {
        return getDefaultManager().facets(url);
    }

    public static Stream<FacetDefinition> facets(final String formatName, final InputStream in)
            throws IOException {
        return getDefaultManager().facets(formatName, in);
    }

    public static Stream<PlaneConvexSubset> boundaries(final Path path, final DoublePrecisionContext precision)
            throws IOException {
        return getDefaultManager().boundaries(path, precision);
    }

    public static Stream<PlaneConvexSubset> boundaries(final URL url, final DoublePrecisionContext precision)
            throws IOException {
        return getDefaultManager().boundaries(url, precision);
    }

    public static Stream<PlaneConvexSubset> boundaries(final String formatName, final InputStream in,
            final DoublePrecisionContext precision) throws IOException {
        return getDefaultManager().boundaries(formatName, in, precision);
    }

    public static Stream<Triangle3D> triangles(final Path path, final DoublePrecisionContext precision)
            throws IOException {
        return getDefaultManager().triangles(path, precision);
    }

    public static Stream<Triangle3D> triangles(final URL url, final DoublePrecisionContext precision)
            throws IOException {
        return getDefaultManager().triangles(url, precision);
    }

    public static Stream<Triangle3D> triangles(final String formatName, final InputStream in,
            final DoublePrecisionContext precision) throws IOException {
        return getDefaultManager().triangles(formatName, in, precision);
    }

    public static BoundarySource3D read(final Path path, final DoublePrecisionContext precision)
            throws IOException {
        return getDefaultManager().read(path, precision);
    }

    public static BoundarySource3D read(final URL url, final DoublePrecisionContext precision)
            throws IOException {
        return getDefaultManager().read(url, precision);
    }

    public static BoundarySource3D read(final String formatName, final InputStream in,
            final DoublePrecisionContext precision) throws IOException {
        return getDefaultManager().read(formatName, in, precision);
    }

    public static TriangleMesh readTriangleMesh(final Path path, final DoublePrecisionContext precision)
            throws IOException {
        return getDefaultManager().readTriangleMesh(path, precision);
    }

    public static TriangleMesh readTriangleMesh(final URL url, final DoublePrecisionContext precision)
            throws IOException {
        return getDefaultManager().readTriangleMesh(url, precision);
    }

    public static TriangleMesh readTriangleMesh(final String formatName, final InputStream in,
            final DoublePrecisionContext precision) throws IOException {
        return getDefaultManager().readTriangleMesh(formatName, in, precision);
    }

    public static void write(final BoundarySource3D src, final Path path)
            throws IOException {
        getDefaultManager().write(src, path);
    }

    public static void write(final BoundarySource3D src, final String formatName, final OutputStream out)
            throws IOException {
        getDefaultManager().write(src, formatName, out);
    }

    public static void writeFacets(final Collection<? extends FacetDefinition> facets, final Path path)
            throws IOException {
        getDefaultManager().writeFacets(facets, path);
    }

    public static void writeFacets(final Collection<? extends FacetDefinition> facets, final String formatName,
            final OutputStream out) throws IOException {
        getDefaultManager().writeFacets(facets, formatName, out);
    }

    public static BoundaryIOManager3D getDefaultManager() {
        return ManagerHolder.DEFAULT_MANAGER;
    }

    private static final class ManagerHolder {
        private static final BoundaryIOManager3D DEFAULT_MANAGER = new DefaultBoundaryIOManager3D();
    }
}

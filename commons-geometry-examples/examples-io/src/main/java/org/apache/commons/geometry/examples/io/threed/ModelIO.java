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
package org.apache.commons.geometry.examples.io.threed;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;

/** Utility class containing constants and static convenience methods related to 3D model
 * input and output.
 */
public final class ModelIO {

    /** String representing the OBJ file format.
     * @see <a href="https://en.wikipedia.org/wiki/Wavefront_.obj_file">Wavefront .obj file</a>
     */
    public static final String OBJ = "obj";

    /** String representing the simple text format described by
     * {@link org.apache.commons.geometry.examples.io.threed.text.TextFacetDefinitionReader TextFacetDefinitionReader}
     * and
     * {@link org.apache.commons.geometry.examples.io.threed.text.TextFacetDefinitionWriter TextFacetDefinitionWriter}.
     * This format describes facets by listing the coordinates of its vertices in order, with one facet
     * described per line. Facets may have 3 or more vertices and do not need to all have the same
     * number of vertices.
     */
    public static final String TXT = "txt";

    /** String representing the CSV file format as described by
     * {@link org.apache.commons.geometry.examples.io.threed.text.TextFacetDefinitionWriter#csvFormat(java.io.Writer)
     * TextFacetDefinitionWriter}. When used to represent 3D geometry information, the coordinates of the vertices of
     * the facets are listed in order, with one facet defined per row. This is similar to the {@link #TXT} format
     * with the exception that facets are are converted to triangles before writing so that all rows have the same
     * number of columns.
     */
    public static final String CSV = "csv";

    /** Singleton manager. */
    private static final ModelIOManager DEFAULT_MANAGER = new DefaultModelIOManager();

    /** Utility class; no instantiation. */
    private ModelIO() {}

    /** Get the default {@link ModelIOManager} singleton instance.
     * @return the default {@link ModelIOManager} singleton instance
     */
    public static ModelIOManager getDefaultManager() {
        return DEFAULT_MANAGER;
    }

    public static BoundarySource3D read(final Path path, final DoublePrecisionContext precision)
            throws IOException {
        return DEFAULT_MANAGER.read(path, precision);
    }

    public static BoundarySource3D read(final String formatName, final InputStream in,
            final DoublePrecisionContext precision) throws IOException {
        return DEFAULT_MANAGER.read(formatName, in, precision);
    }

    public static void write(final BoundarySource3D model, final Path path) throws IOException {
        DEFAULT_MANAGER.write(model, path);
    }

    public static void write(final BoundarySource3D model, final String formatName, final OutputStream out)
            throws IOException {
        DEFAULT_MANAGER.write(model, formatName, out);
    }
}

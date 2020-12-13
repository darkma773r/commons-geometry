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

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.stream.Stream;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.ConvexPolygon3D;
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.euclidean.threed.Triangle3D;
import org.apache.commons.geometry.examples.io.threed.facet.FacetDefinition;
import org.apache.commons.geometry.examples.io.threed.facet.FacetDefinitionReader;

/** Interface for classes that handle reading and writing of 3D model files types.
 * For convenience and better compatibility with streams and functional programming,
 * all IO methods throw {@link java.io.UncheckedIOException} instead of {@link java.io.IOException}.
 *
 * <p>Implementations of this interface are expected to be thread-safe.</p>
 */
public interface ModelIOHandler {

    /** Return true if this instance handles 3D model files of the given type.
     * @param type type 3D model type, indicated by file extension
     * @return true if this instance can handle the 3D model file type
     */
    boolean handlesType(String type);

    FacetDefinitionReader newFacetDefinitionReader(File in);

    FacetDefinitionReader newFacetDefinitionReader(URL in);

    FacetDefinitionReader newFacetDefinitionReader(String type, File in);

    FacetDefinitionReader newFacetDefinitionReader(String type, URL in);

    FacetDefinitionReader newFacetDefinitionReader(String type, InputStream in);

    Stream<FacetDefinition> facets(File in);

    Stream<FacetDefinition> facets(URL in);

    Stream<FacetDefinition> facets(String type, File in);

    Stream<FacetDefinition> facets(String type, URL in);

    Stream<FacetDefinition> facets(String type, InputStream in);

    Stream<ConvexPolygon3D> boundaries(File in, DoublePrecisionContext precision);

    Stream<ConvexPolygon3D> boundaries(URL in, DoublePrecisionContext precision);

    Stream<ConvexPolygon3D> boundaries(String type, File in, DoublePrecisionContext precision);

    Stream<ConvexPolygon3D> boundaries(String type, URL in, DoublePrecisionContext precision);

    Stream<ConvexPolygon3D> boundaries(String type, InputStream in, DoublePrecisionContext precision);

    Stream<Triangle3D> triangles(File in, DoublePrecisionContext precision);

    Stream<Triangle3D> triangles(URL in, DoublePrecisionContext precision);

    Stream<Triangle3D> triangles(String type, File in, DoublePrecisionContext precision);

    Stream<Triangle3D> triangles(String type, URL in, DoublePrecisionContext precision);

    Stream<Triangle3D> triangles(String type, InputStream in, DoublePrecisionContext precision);

    BoundarySource3D read(File in, DoublePrecisionContext precision);

    BoundarySource3D read(URL in, DoublePrecisionContext precision);

    /** Read a 3D model represented as a {@link BoundarySource3D} from the given file.
     * @param type the model file type
     * @param in file to read
     * @param precision precision context to use in model construction
     * @return a 3D model represented as a boundary source
     * @throws java.io.UncheckedIOException if an IO operation fails
     * @throws IllegalArgumentException if the file type is not supported
     */
    BoundarySource3D read(String type, File in, DoublePrecisionContext precision);

    /** Read a 3D model represented as a {@link BoundarySource3D} from the given URL.
     * @param type the model file type
     * @param in url to read from
     * @param precision precision context to use in model construction
     * @return a 3D model represented as a boundary source
     * @throws java.io.UncheckedIOException if an IO operation fails
     * @throws IllegalArgumentException if the file type is not supported
     */
    BoundarySource3D read(String type, URL in, DoublePrecisionContext precision);

    /** Read a 3D model represented as a {@link BoundarySource3D} from the given input stream.
     * The input stream is closed before method return.
     * @param type the model input type
     * @param in input stream
     * @param precision precision context to use in model construction
     * @return a 3D model represented as a boundary source
     * @throws java.io.UncheckedIOException if an IO operation fails
     * @throws IllegalArgumentException if the file type is not supported
     */
    BoundarySource3D read(String type, InputStream in, DoublePrecisionContext precision);

    void write(BoundarySource3D model, File out);

    /** Write the model to the file using the specified file type.
     * @param model model to write
     * @param type the model file type
     * @param out output file
     * @throws java.io.UncheckedIOException if an IO operation fails
     * @throws IllegalArgumentException if the file type is not supported
     */
    void write(BoundarySource3D model, String type, File out);

    /** Write the model to the given output stream, using the specified model type.
     * @param model model to write
     * @param type the model file type
     * @param out output stream
     * @throws java.io.UncheckedIOException if an IO operation fails
     * @throws IllegalArgumentException if the file type is not supported
     */
    void write(BoundarySource3D model, String type, OutputStream out);

    void writeFacets(Stream<? extends FacetDefinition> facets, File out);

    void writeFacets(Stream<? extends FacetDefinition> facets, String type, File out);

    void writeFacets(Stream<? extends FacetDefinition> facets, String type, OutputStream  out);

    void writeBoundaries(Stream<? extends PlaneConvexSubset> boundaries, File out);

    void writeBoundaries(Stream<? extends PlaneConvexSubset> boundaries, String type, File out);

    void writeBoundaries(Stream<? extends PlaneConvexSubset> boundaries, String type, OutputStream  out);
}

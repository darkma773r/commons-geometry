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
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.ConvexPolygon3D;
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.euclidean.threed.mesh.TriangleMesh;
import org.apache.commons.geometry.examples.io.threed.facet.FacetDefinition;
import org.apache.commons.geometry.examples.io.threed.facet.FacetDefinitionReader;

public class ModelIOManager {

    public interface ReadHandler {

        FacetDefinitionReader facetDefinitionReader(InputStream in) throws IOException;

        BoundarySource3D read(InputStream in, DoublePrecisionContext precision) throws IOException;

        TriangleMesh readTriangleMesh(InputStream in, DoublePrecisionContext precision) throws IOException;
    }

    public interface WriteHandler {

        void write(BoundarySource3D model, OutputStream out) throws IOException;

        void writeFacets(Stream<? extends FacetDefinition> facets, OutputStream out) throws IOException;

        void writeBoundaries(Stream<? extends PlaneConvexSubset> boundaries, OutputStream out) throws IOException;
    }

    /** Map of file formats to readers. */
    private final Map<String, ReadHandler> readers = new HashMap<>();

    /** Map of file formats to writers. */
    private final Map<String, WriteHandler> writers = new HashMap<>();

    /** Return true if this instance supports reading input in the given 3D
     * model file format.
     * @param formatName 3D model file format
     * @return true if this instance can read the given 3D model file format
     */
    public boolean readsFormat(String formatName) {
        return formatName != null && getReadHandler(formatName) != null;
    }

    /** Return true if this instance supports writing output in the given 3D
     * model file format.
     * @param formatName 3D model file format
     * @return true if this instance can read the given 3D model file format
     */
    public boolean writesFormat(String formatName) {
        return formatName != null && getWriteHandler(formatName) != null;
    }

    /** Get the read handler for the given file format name or null if not found.
     * @param formatName 3D model file format
     * @return read handler for the given file format name or null if not found
     */
    public ReadHandler getReadHandler(final String formatName) {
        final String normalizedFormat = normalizeFormat(formatName);
        synchronized (readers) {
            return readers.get(normalizedFormat);
        }
    }

    /** Get the write handler for the given file format name or null if not found.
     * @param formatName 3D model file format
     * @return write handler for the given file format name or null if not found
     */
    public WriteHandler getWriteHandler(final String formatName) {
        final String normalizedFormat = normalizeFormat(formatName);
        synchronized (writers) {
            return writers.get(normalizedFormat);
        }
    }

    public void registerReadHandler(final String formatName, final ReadHandler readHandler) {
        final String normalizedFormat = normalizeFormat(formatName);
        Objects.requireNonNull(readHandler, "Read handler cannot be null");

        synchronized (readers) {
            readers.put(normalizedFormat, readHandler);
        }
    }

    public void registerWriteHandler(final String formatName, final WriteHandler readHandler) {
        final String normalizedFormat = normalizeFormat(formatName);
        Objects.requireNonNull(readHandler, "Write handler cannot be null");

        synchronized (readers) {
            writers.put(normalizedFormat, readHandler);
        }
    }

    public FacetDefinitionReader facetDefinitionReader(final String formatName, final InputStream in)
            throws IOException {
        final ReadHandler reader = requireReadHandler(formatName);
        return reader.facetDefinitionReader(in);
    }

    /** Return a {@link BoundarySource3D} containing the boundary information from the given 3D model file.
     * The file format is determined from the file extension.
     * @param path 3D model file
     * @param precision precision context used for floating point comparisons
     * @return object containing the boundary information from the given 3D model file
     * @throws IOException if an I/O error occurs
     * @throws IllegalArgumentException if the file does not have a file extension matching a registered
     *      file format
     */
    public BoundarySource3D read(final Path path, final DoublePrecisionContext precision) throws IOException {
        return read(toUrl(path), precision);
    }

    /** Return a {@link BoundarySource3D} containing the boundary information from the given 3D model file URL.
     * The file format is determined from the file extension.
     * @param url 3D model file location
     * @param precision precision context used for floating point comparisons
     * @return object containing the boundary information from the given 3D model file
     * @throws IOException if an I/O error occurs
     * @throws IllegalArgumentException if the url does not have a file extension matching a registered
     *      file format
     */
    public BoundarySource3D read(final URL url, final DoublePrecisionContext precision) throws IOException {
        final String fileExt = getFileExtension(url);
        final ReadHandler reader = requireReadHandler(fileExt);

        try (InputStream in = url.openStream()) {
            return reader.read(in, precision);
        }
    }

    /** Return a {@link BoundarySource3D} containing the boundary information from the given input stream.
     * @param formatName 3D model file format used in the input strema
     * @param in input stream containing 3D model file data
     * @param precision precision context used for floating point comparisons
     * @return a boundary source containing the boundary information from the input stream
     * @throws IOException if an I/O error occurs
     * @throws IllegalArgumentException if no reader can be found for the given format
     */
    public BoundarySource3D read(final String formatName, final InputStream in,
            final DoublePrecisionContext precision) throws IOException {
        final ReadHandler reader = requireReadHandler(formatName);
        return reader.read(in, precision);
    }

    public TriangleMesh readTriangleMesh(final Path path, final DoublePrecisionContext precision)
            throws IOException {
        return readTriangleMesh(toUrl(path), precision);
    }

    public TriangleMesh readTriangleMesh(final URL url, final DoublePrecisionContext precision)
            throws IOException {
        final String fileExt = getFileExtension(url);
        final ReadHandler reader = requireReadHandler(fileExt);

        try (InputStream in = url.openStream()) {
            return reader.readTriangleMesh(in, precision);
        }
    }

    public TriangleMesh readTriangleMesh(final String formatName, final InputStream in,
            final DoublePrecisionContext precision) throws IOException {
        final ReadHandler reader = requireReadHandler(formatName);
        return reader.readTriangleMesh(in, precision);
    }

    public Stream<FacetDefinition> facets(final Path path) throws IOException {
        return facets(toUrl(path));
    }

    public Stream<FacetDefinition> facets(final URL url) throws IOException {
        final String fileExt = getFileExtension(url);
        final ReadHandler reader = requireReadHandler(fileExt);

        InputStream in = null;
        try {
            in = url.openStream();
            return createFacetStream(reader, in)
                    .onClose(closeAsUncheckedRunnable(in));
        } catch (IOException|RuntimeException exc) {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException suppressed) {
                    exc.addSuppressed(suppressed);
                }
            }

            throw exc;
        }
    }

    public Stream<FacetDefinition> facets(final String formatName, final InputStream in) throws IOException {
        final ReadHandler reader = requireReadHandler(formatName);
        return createFacetStream(reader, in);
    }

    public Stream<ConvexPolygon3D> boundaries(final Path path, final DoublePrecisionContext precision)
            throws IOException {
        return boundaries(toUrl(path), precision);
    }

    public Stream<ConvexPolygon3D> boundaries(final URL url, final DoublePrecisionContext precision)
            throws IOException {
        return facets(url)
                .map(f -> f.toPolygon(precision));
    }

    public Stream<ConvexPolygon3D> boundaries(final String formatName, final InputStream in,
            final DoublePrecisionContext precision) throws IOException {
        return facets(formatName, in)
                .map(f -> f.toPolygon(precision));
    }

    public void write(final BoundarySource3D model, final Path path) throws IOException {
        final String fileExt = getFileExtension(path);
        final WriteHandler writer = requireWriteHandler(fileExt);

        try (OutputStream out = Files.newOutputStream(path)) {
            writer.write(model, out);
        }
    }

    public void write(final BoundarySource3D model, final String formatName, final OutputStream out)
            throws IOException {
        final WriteHandler writer = getWriteHandler(formatName);
        writer.write(model, out);
    }

    public void writeFacets(final Stream<? extends FacetDefinition> facets, final Path path)
            throws IOException {
        final String fileExt = getFileExtension(path);
        final WriteHandler writer = requireWriteHandler(fileExt);

        try (OutputStream out = Files.newOutputStream(path)) {
            writer.writeFacets(facets, out);
        }
    }

    public void writeFacets(final Stream<? extends FacetDefinition> facets, final String formatName,
            final OutputStream out) throws IOException {
        final WriteHandler writer = requireWriteHandler(formatName);
        writer.writeFacets(facets, out);
    }

    public void writeBoundaries(final Stream<? extends PlaneConvexSubset> boundaries, final Path path)
            throws IOException {
        final String fileExt = getFileExtension(path);
        final WriteHandler writer = requireWriteHandler(fileExt);

        try (OutputStream out = Files.newOutputStream(path)) {
            writer.writeBoundaries(boundaries, out);
        }
    }

    public void writeBoundaries(final Stream<? extends PlaneConvexSubset> boundaries, final String formatName,
            final OutputStream out) throws IOException {
        final WriteHandler writer = requireWriteHandler(formatName);
        writer.writeBoundaries(boundaries, out);
    }

    private ReadHandler requireReadHandler(final String formatName) {
        final ReadHandler reader = getReadHandler(formatName);
        if (reader == null) {
            throw new IllegalArgumentException("No read handler configured for format \"" + formatName + "\"");
        }
        return reader;
    }

    private WriteHandler requireWriteHandler(final String formatName) {
        final WriteHandler writer = getWriteHandler(formatName);
        if (writer == null) {
            throw new IllegalArgumentException("No write handler configured for format \"" + formatName + "\"");
        }
        return writer;
    }

    private Stream<FacetDefinition> createFacetStream(final ReadHandler reader, final InputStream in)
            throws IOException {
        final FacetDefinitionReader fdReader = reader.facetDefinitionReader(in);
        final FacetDefinitionReaderIterator it = new FacetDefinitionReaderIterator(fdReader);

        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED), false);
    }

    /** Normalize the given model file format name.
     * @param formatName file format name
     * @return normalized form of the file format name
     */
    private static String normalizeFormat(final String formatName) {
        Objects.requireNonNull("Model file format name cannot be null");
        return formatName.toLowerCase();
    }

    /** Create an unchecked exception from the given checked exception.
     * @param exc exception to wrap in an unchecked exception
     * @return the unchecked exception
     */
    private static UncheckedIOException createUnchecked(final IOException exc) {
        final String msg = exc.getClass().getSimpleName() + ": " + exc.getMessage();
        return new UncheckedIOException(msg, exc);
    }

    private static Runnable closeAsUncheckedRunnable(final InputStream in) {
        return () -> {
            try {
                in.close();
            } catch (IOException exc) {
                throw createUnchecked(exc);
            }
        };
    }

    private static String getFileExtension(final Path path) {
        return getFileExtension(path.getFileName());
    }

    private static String getFileExtension(final URL url) {
        return getFileExtension(url.getPath());
    }

    /** Get the file extension of the given file name, throwing an exception if one cannot be found.
     * @param name file name to determine the extension for
     * @return the file extension, converted to lower case
     * @throws IllegalArgumentException if the name does not have a file extension
     */
    private static String getFileExtension(final String name) {
        final int idx = name.lastIndexOf('.');
        if (idx > -1) {
            return name.substring(idx + 1).toLowerCase();
        }

        throw new IllegalArgumentException("Cannot determine target file format: \"" + name +
                "\" does not have a file extension");
    }

    /** Convert the given path to a URL.
     * @param path path to convert to a URL
     * @return URL for the path
     * @throws NullPointerException if path is null
     * @throws IOException if the conversion fails
     */
    private static URL toUrl(final Path path) throws IOException {
        Objects.requireNonNull(path, "Path cannot be null");
        return path.toAbsolutePath().toUri().toURL();
    }

    private static final class FacetDefinitionReaderIterator implements Iterator<FacetDefinition> {

        private final FacetDefinitionReader reader;

        private int loadCount = 0;

        private FacetDefinition next;

        FacetDefinitionReaderIterator(final FacetDefinitionReader reader) {
            this.reader = reader;
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasNext() {
            ensureLoaded();
            return next != null;
        }

        /** {@inheritDoc} */
        @Override
        public FacetDefinition next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            final FacetDefinition result = next;
            loadNext();

            return result;
        }

        private void ensureLoaded() {
            if (loadCount < 1) {
                loadNext();
            }
        }

        private void loadNext() {
            ++loadCount;
            try {
                next = reader.readFacet();
            } catch (IOException exc) {
                throw createUnchecked(exc);
            }
        }
    }
}

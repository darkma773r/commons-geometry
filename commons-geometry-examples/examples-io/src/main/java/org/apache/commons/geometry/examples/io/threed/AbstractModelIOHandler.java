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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.ConvexPolygon3D;
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.examples.io.threed.facet.FacetDefinition;
import org.apache.commons.geometry.examples.io.threed.facet.FacetDefinitionReader;

/** Abstract base class for {@link ModelIOHandler} implementations.
 */
public abstract class AbstractModelIOHandler implements ModelIOHandler {

    /** {@inheritDoc} */
    @Override
    public FacetDefinitionReader newFacetDefinitionReader(final File in) {
        return newFacetDefinitionReader(toURL(in));
    }

    /** {@inheritDoc} */
    @Override
    public FacetDefinitionReader newFacetDefinitionReader(final URL in) {
        return newFacetDefinitionReader(getFileExtension(in), in);
    }

    /** {@inheritDoc} */
    @Override
    public FacetDefinitionReader newFacetDefinitionReader(final String type, File in) {
        return newFacetDefinitionReader(type, toURL(in));
    }

    /** {@inheritDoc} */
    @Override
    public FacetDefinitionReader newFacetDefinitionReader(final String type, URL in) {
        ensureTypeSupported(type);
        try {
            return newFacetDefinitionReaderInternal(type, in.openStream());
        } catch (IOException exc) {
            throw createUnchecked(exc);
        }
    }

    /** {@inheritDoc} */
    @Override
    public FacetDefinitionReader newFacetDefinitionReader(final String type, InputStream in) {
        ensureTypeSupported(type);
        return newFacetDefinitionReaderInternal(type, in);
    }

    protected abstract FacetDefinitionReader newFacetDefinitionReaderInternal(String type, InputStream in);

    /** {@inheritDoc} */
    @Override
    public Stream<FacetDefinition> facets(final File in) {
        return facets(toURL(in));
    }

    /** {@inheritDoc} */
    @Override
    public Stream<FacetDefinition> facets(final URL in) {
        return facets(getFileExtension(in), in);
    }

    /** {@inheritDoc} */
    @Override
    public Stream<FacetDefinition> facets(final String type, final File in) {
        return facets(type, toURL(in));
    }

    /** {@inheritDoc} */
    @Override
    public Stream<FacetDefinition> facets(final String type, final URL in) {
        ensureTypeSupported(type);

        InputStream is = null;
        try {
            is = in.openStream();
            return facets(type, in)
                    .onClose(closeAsUncheckedRunnable(is));
        } catch (IOException exc) {
            throw createUnchecked(exc);
        } catch (Error|RuntimeException exc) {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException suppressed) {
                exc.addSuppressed(suppressed);
            }

            throw exc;
        }
    }

    /** {@inheritDoc} */
    @Override
    public Stream<FacetDefinition> facets(final String type, final InputStream in) {
        ensureTypeSupported(type);

        final FacetDefinitionReader reader = newFacetDefinitionReaderInternal(type, in);
        final FacetDefinitionReaderIterator it = new FacetDefinitionReaderIterator(reader);

        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED), false);
    }

    /** {@inheritDoc} */
    @Override
    public Stream<ConvexPolygon3D> boundaries(final File in, final DoublePrecisionContext precision) {
        return boundaries(toURL(in), precision);
    }

    /** {@inheritDoc} */
    @Override
    public Stream<ConvexPolygon3D> boundaries(final URL in, final DoublePrecisionContext precision) {
        return boundaries(getFileExtension(in), in, precision);
    }

    /** {@inheritDoc} */
    @Override
    public Stream<ConvexPolygon3D> boundaries(final String type, final File in,
            final DoublePrecisionContext precision) {
        return boundaries(type, toURL(in), precision);
    }

    /** {@inheritDoc} */
    @Override
    public Stream<ConvexPolygon3D> boundaries(final String type, final URL in,
            final DoublePrecisionContext precision) {
        ensureTypeSupported(type);

        InputStream is = null;
        try {
            is = in.openStream();
            return boundaries(type, is, precision);
        } catch (IOException exc) {
            throw createUnchecked(exc);
        } catch (Error|RuntimeException exc) {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException suppressed) {
                exc.addSuppressed(suppressed);
            }

            throw exc;
        }
    }

    /** {@inheritDoc} */
    @Override
    public Stream<ConvexPolygon3D> boundaries(final String type, final InputStream in,
            final DoublePrecisionContext precision) {
        return facets(type, in)
                .map(f -> f.toPolygon(precision));
    }

    /** {@inheritDoc} */
    @Override
    public BoundarySource3D read(final File in, final DoublePrecisionContext precision) {
        return read(toURL(in), precision);
    }

    /** {@inheritDoc} */
    @Override
    public BoundarySource3D read(final URL in, final DoublePrecisionContext precision) {
        return read(getFileExtension(in), in, precision);
    }

    /** {@inheritDoc} */
    @Override
    public BoundarySource3D read(final String type, final File in, final DoublePrecisionContext precision) {
        return read(type, toURL(in), precision);
    }

    /** {@inheritDoc} */
    @Override
    public BoundarySource3D read(final String type, final URL in, final DoublePrecisionContext precision) {
        ensureTypeSupported(type);

        try (InputStream is = in.openStream()) {
            return readInternal(type, is, precision);
        } catch (IOException exc) {
            throw createUnchecked(exc);
        }
    }

    /** {@inheritDoc} */
    @Override
    public BoundarySource3D read(final String type, final InputStream in, final DoublePrecisionContext precision) {
        ensureTypeSupported(type);
        try {
            return readInternal(type, in, precision);
        } catch (IOException exc) {
            throw createUnchecked(exc);
        }
    }

    protected abstract BoundarySource3D readInternal(final String type, final InputStream in,
            final DoublePrecisionContext precision) throws IOException;

    /** {@inheritDoc} */
    @Override
    public void write(final BoundarySource3D model, final File out) {
        write(model, getFileExtension(out.getName()), out);
    }

    /** {@inheritDoc} */
    @Override
    public void writeFacets(final Stream<? extends FacetDefinition> facets, final File out) {
        writeFacets(facets, getFileExtension(out.getName()), out);
    }

    /** {@inheritDoc} */
    @Override
    public void writeFacets(Stream<? extends FacetDefinition> facets, final String type, final File out) {
        ensureTypeSupported(type);
        try {
            try (OutputStream os = Files.newOutputStream(out.toPath())) {
                writeFacetsInternal(facets, type, os);
            }
        } catch (IOException exc) {
            throw createUnchecked(exc);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void writeFacets(final Stream<? extends FacetDefinition> facets, final String type,
            final OutputStream out) {
        ensureTypeSupported(type);
        try {
            writeFacetsInternal(facets, type, out);
        } catch (IOException exc) {
            throw createUnchecked(exc);
        } finally {
            facets.close();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void writeBoundaries(final Stream<? extends PlaneConvexSubset> boundaries, final File out) {
        writeBoundaries(boundaries, getFileExtension(out.getName()), out);
    }

    /** {@inheritDoc} */
    @Override
    public void writeBoundaries(final Stream<? extends PlaneConvexSubset> boundaries, final String type,
            final File out) {
         ensureTypeSupported(type);
         try (OutputStream os = Files.newOutputStream(out.toPath())) {
             writeBoundariesInternal(boundaries, type, os);
         } catch (IOException exc) {
             throw createUnchecked(exc);
         } finally {
             boundaries.close();
         }
    }

    /** {@inheritDoc} */
    @Override
    public void writeBoundaries(final Stream<? extends PlaneConvexSubset> boundaries, final String type,
            final OutputStream out) {
        try {
            writeBoundariesInternal(boundaries, type, out);
        } catch (IOException exc) {
            throw createUnchecked(exc);
        } finally {
            boundaries.close();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void write(final BoundarySource3D model, final String type, final File out) {
        ensureTypeSupported(type);
        try (OutputStream os = Files.newOutputStream(out.toPath())) {
            writeBoundariesInternal(model, type, os);
        } catch (IOException exc) {
            throw createUnchecked(exc);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void write(final BoundarySource3D model, final String type, OutputStream out) {
        ensureTypeSupported(type);
        try {
            writeBoundariesInternal(model, type, out);
        } catch (IOException exc) {
            throw createUnchecked(exc);
        }
    }

    protected abstract void writeFacetsInternal(Stream<? extends FacetDefinition> facets,
            String type, OutputStream out) throws IOException;

    protected abstract void writeBoundariesInternal(BoundarySource3D model,
            String type, OutputStream out) throws IOException;

    protected abstract void writeBoundariesInternal(Stream<? extends PlaneConvexSubset> boundaries,
            String type, OutputStream out) throws IOException;

    /** Throw an exception if the given type is not supported by this instance.
     * @param type model type to check
     */
    private void ensureTypeSupported(final String type) {
        if (!handlesType(type)) {
            throw new IllegalArgumentException("File type is not supported by this handler: " + type);
        }
    }

    /** Create an unchecked exception from the given checked exception.
     * @param exc exception to wrap in an unchecked exception
     * @return the unchecked exception
     */
    private static UncheckedIOException createUnchecked(final IOException exc) {
        final String msg = exc.getClass().getSimpleName() + ": " + exc.getMessage();
        return new UncheckedIOException(msg, exc);
    }

    private static String getFileExtension(final URL url) {
        return getFileExtension(url.getFile());
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

        throw new IllegalArgumentException("Cannot determine target file type: \"" + name +
                "\" does not have a file extension");
    }

    /** Convert the given file to a URL, throwing an unchecked exception on failure.
     * @param file file to convert to a URL
     * @return URL for the file
     * @throws UncheckedIOException if the conversion fails
     */
    private static URL toURL(final File file) {
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException exc) {
            throw new UncheckedIOException(exc);
        }
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

    private static final class FacetDefinitionReaderIterator implements Iterator<FacetDefinition> {

        private final FacetDefinitionReader reader;

        private FacetDefinition next;

        FacetDefinitionReaderIterator(final FacetDefinitionReader reader) {
            this.reader = reader;

            loadNext();
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasNext() {
            return next != null;
        }

        /** {@inheritDoc} */
        @Override
        public FacetDefinition next() {
            if (next == null) {
                throw new NoSuchElementException();
            }

            final FacetDefinition result = next;
            loadNext();

            return result;
        }

        private void loadNext() {
            try {
                next = reader.readFacet();
            } catch (IOException exc) {
                throw createUnchecked(exc);
            }
        }
    }
}

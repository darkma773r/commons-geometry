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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.io.test.CloseCountInputStream;
import org.apache.commons.geometry.core.io.test.CloseCountOutputStream;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.euclidean.threed.Triangle3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.mesh.SimpleTriangleMesh;
import org.apache.commons.geometry.euclidean.threed.mesh.TriangleMesh;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class BoundaryIOManager3DTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION = new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final FacetDefinitionReader FACET_DEF_READER = new FacetDefinitionReader() {

        @Override
        public FacetDefinition readFacet() throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void close() throws IOException {
            // do nothing
        }
    };

    private static final FacetDefinition FACET = new SimpleFacetDefinition(Arrays.asList(
            Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(1, 1, 0), Vector3D.of(0, 1, 0)));

    private static final List<FacetDefinition> FACET_LIST = Arrays.asList(FACET);

    private static final TriangleMesh TRI_MESH = SimpleTriangleMesh.builder(TEST_PRECISION).build();

    @TempDir
    public Path tempDir;

    private final TestManager3D manager = new TestManager3D();

    @Test
    public void testFacetDefinitionReader_inputStream() throws IOException {
        // arrange
        final StubReadHandler3D readHandler = new StubReadHandler3D();
        manager.registerReadHandler("test", readHandler);

        final CloseCountInputStream in = new CloseCountInputStream(new ByteArrayInputStream(new byte[0]));

        // act
        final FacetDefinitionReader reader = manager.facetDefinitionReader(in, "TEST");

        // assert
        Assertions.assertSame(FACET_DEF_READER, reader);
        Assertions.assertSame(in, readHandler.in);

        Assertions.assertEquals(0, in.getCloseCount());
    }

    @Test
    public void testFacetDefinitionReader_inputStream_unknownFormat() throws IOException {
        // arrange
        final CloseCountInputStream in = new CloseCountInputStream(new ByteArrayInputStream(new byte[0]));

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.facetDefinitionReader(in, "TEST"),
                IllegalArgumentException.class, "No read handler registered for format \"TEST\"");

        Assertions.assertEquals(0, in.getCloseCount());
    }

    @Test
    public void testFacetDefinitionReader_path() throws IOException {
        // arrange
        final StubReadHandler3D readHandler = new StubReadHandler3D();
        manager.registerReadHandler("test", readHandler);

        final Path path = tempDir.resolve("data.test");
        Files.createFile(path);

        // act
        try (FacetDefinitionReader reader = manager.facetDefinitionReader(path)) {
            // assert
            Assertions.assertSame(FACET_DEF_READER, reader);
            Assertions.assertNotNull(readHandler.in);
        }
    }

    @Test
    public void testFacetDefinitionReader_path_unknownFormat() throws IOException {
        // arrange
        final Path path = tempDir.resolve("data.test");
        Files.createFile(path);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.facetDefinitionReader(path),
                IllegalArgumentException.class, "No read handler registered for format \"test\"");
    }

    @Test
    public void testFacetDefinitionReader_path_readerCreationFail() throws IOException {
        // arrange
        final StubReadHandler3D readHandler = new StubReadHandler3D();
        readHandler.fail = true;
        manager.registerReadHandler("test", readHandler);

        final Path path = tempDir.resolve("data.test");
        Files.createFile(path);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.facetDefinitionReader(path),
                IOException.class, "Test failure");

        Assertions.assertEquals(1, manager.inputCloseCount);
    }

    @Test
    public void testFacetDefinitionReader_path_readerCreationFails_inputCloseFails() throws IOException {
        // arrange
        manager.inputStreamFailOnClose = true;

        final StubReadHandler3D readHandler = new StubReadHandler3D();
        readHandler.fail = true;
        manager.registerReadHandler("test", readHandler);

        final Path path = tempDir.resolve("data.test");
        Files.createFile(path);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.facetDefinitionReader(path),
                IOException.class, "Test failure");

        Assertions.assertEquals(1, manager.inputCloseCount);
    }

    @Test
    public void testFacets_inputStream() throws IOException {
        // arrange
        final StubReadHandler3D readHandler = new StubReadHandler3D();
        manager.registerReadHandler("test", readHandler);

        final CloseCountInputStream in = new CloseCountInputStream(new ByteArrayInputStream(new byte[0]));

        // act
        final Stream<FacetDefinition> stream = manager.facets(in, "test");

        // assert
        Assertions.assertSame(in, readHandler.in);

        Assertions.assertEquals(1, stream.collect(Collectors.toList()).size());

        stream.close();
        Assertions.assertEquals(0, in.getCloseCount());
    }

    @Test
    public void testFacets_inputStream_unknownFormat() throws IOException {
        // arrange
        final InputStream in = new ByteArrayInputStream(new byte[0]);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.facets(in, "TEST"),
                IllegalArgumentException.class, "No read handler registered for format \"TEST\"");
    }

    @Test
    public void testFacets_path() throws IOException {
        // arrange
        final StubReadHandler3D readHandler = new StubReadHandler3D();
        manager.registerReadHandler("test", readHandler);

        final Path path = tempDir.resolve("data.test");
        Files.createFile(path);

        // act
        final List<FacetDefinition> result;
        try (Stream<FacetDefinition> stream = manager.facets(path)) {
            result = stream.collect(Collectors.toList());

            // assert
            Assertions.assertEquals(1, result.size());
            Assertions.assertNotNull(readHandler.in);
            Assertions.assertEquals(0, manager.inputCloseCount);
        }

        Assertions.assertEquals(1, manager.inputCloseCount);
    }

    @Test
    public void testFacets_path_unknownFormat() throws IOException {
        // arrange
        final Path path = tempDir.resolve("data.test");
        Files.createFile(path);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.facets(path),
                IllegalArgumentException.class, "No read handler registered for format \"test\"");
    }

    @Test
    public void testFacets_path_readerCreationFail() throws IOException {
        // arrange
        final StubReadHandler3D readHandler = new StubReadHandler3D();
        readHandler.fail = true;
        manager.registerReadHandler("test", readHandler);

        final Path path = tempDir.resolve("data.test");
        Files.createFile(path);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.facets(path),
                IOException.class, "Test failure");

        Assertions.assertEquals(1, manager.inputCloseCount);
    }

    @Test
    public void testFacets_path_readerCreationFails_inputCloseFails() throws IOException {
        // arrange
        manager.inputStreamFailOnClose = true;

        final StubReadHandler3D readHandler = new StubReadHandler3D();
        readHandler.fail = true;
        manager.registerReadHandler("test", readHandler);

        final Path path = tempDir.resolve("data.test");
        Files.createFile(path);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.facets(path),
                IOException.class, "Test failure");

        Assertions.assertEquals(1, manager.inputCloseCount);
    }

    @Test
    public void testTriangles_inputStream() throws IOException {
        // arrange
        final StubReadHandler3D readHandler = new StubReadHandler3D();
        manager.registerReadHandler("test", readHandler);

        final CloseCountInputStream in = new CloseCountInputStream(new ByteArrayInputStream(new byte[0]));

        // act
        final Stream<Triangle3D> stream = manager.triangles(in, "test", TEST_PRECISION);

        // assert
        Assertions.assertSame(in, readHandler.in);

        Assertions.assertEquals(2, stream.collect(Collectors.toList()).size());

        stream.close();
        Assertions.assertEquals(0, in.getCloseCount());
    }

    @Test
    public void testTriangles_inputStream_unknownFormat() throws IOException {
        // arrange
        final InputStream in = new ByteArrayInputStream(new byte[0]);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.triangles(in, "TEST", TEST_PRECISION),
                IllegalArgumentException.class, "No read handler registered for format \"TEST\"");
    }

    @Test
    public void testTriangles_path() throws IOException {
        // arrange
        final StubReadHandler3D readHandler = new StubReadHandler3D();
        manager.registerReadHandler("test", readHandler);

        final Path path = tempDir.resolve("data.test");
        Files.createFile(path);

        // act
        final List<Triangle3D> result;
        try (Stream<Triangle3D> stream = manager.triangles(path, TEST_PRECISION)) {
            result = stream.collect(Collectors.toList());

            // assert
            Assertions.assertEquals(2, result.size());
            Assertions.assertNotNull(readHandler.in);
            Assertions.assertEquals(0, manager.inputCloseCount);
        }

        Assertions.assertEquals(1, manager.inputCloseCount);
    }

    @Test
    public void testTriangles_path_unknownFormat() throws IOException {
        // arrange
        final Path path = tempDir.resolve("data.test");
        Files.createFile(path);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.triangles(path, TEST_PRECISION),
                IllegalArgumentException.class, "No read handler registered for format \"test\"");
    }

    @Test
    public void testTriangles_path_readerCreationFail() throws IOException {
        // arrange
        final StubReadHandler3D readHandler = new StubReadHandler3D();
        readHandler.fail = true;
        manager.registerReadHandler("test", readHandler);

        final Path path = tempDir.resolve("data.test");
        Files.createFile(path);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.triangles(path, TEST_PRECISION),
                IOException.class, "Test failure");

        Assertions.assertEquals(1, manager.inputCloseCount);
    }

    @Test
    public void testTriangles_path_readerCreationFails_inputCloseFails() throws IOException {
        // arrange
        manager.inputStreamFailOnClose = true;

        final StubReadHandler3D readHandler = new StubReadHandler3D();
        readHandler.fail = true;
        manager.registerReadHandler("test", readHandler);

        final Path path = tempDir.resolve("data.test");
        Files.createFile(path);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.triangles(path, TEST_PRECISION),
                IOException.class, "Test failure");

        Assertions.assertEquals(1, manager.inputCloseCount);
    }

    @Test
    public void testReadTriangleMesh_inputStream() throws IOException {
        // arrange
        final StubReadHandler3D readHandler = new StubReadHandler3D();
        manager.registerReadHandler("test", readHandler);

        final CloseCountInputStream in = new CloseCountInputStream(new ByteArrayInputStream(new byte[0]));

        // act
        final TriangleMesh mesh = manager.readTriangleMesh(in, "TEST", TEST_PRECISION);

        // assert
        Assertions.assertSame(TRI_MESH, mesh);
        Assertions.assertSame(TEST_PRECISION, readHandler.precision);
        Assertions.assertSame(in, readHandler.in);

        Assertions.assertEquals(0, in.getCloseCount());
    }

    @Test
    public void testReadTriangleMesh_inputStream_unknownFormat() throws IOException {
        // arrange
        final CloseCountInputStream in = new CloseCountInputStream(new ByteArrayInputStream(new byte[0]));

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.readTriangleMesh(in, "TEST", TEST_PRECISION),
                IllegalArgumentException.class, "No read handler registered for format \"TEST\"");

        Assertions.assertEquals(0, in.getCloseCount());
    }

    @Test
    public void testReadTriangleMesh_path() throws IOException {
        // arrange
        final StubReadHandler3D readHandler = new StubReadHandler3D();
        manager.registerReadHandler("test", readHandler);

        final Path path = tempDir.resolve("data.test");
        Files.createFile(path);

        // act
        final TriangleMesh mesh = manager.readTriangleMesh(path, TEST_PRECISION);

        // assert
        Assertions.assertSame(TRI_MESH, mesh);
        Assertions.assertSame(TEST_PRECISION, readHandler.precision);
        Assertions.assertNotNull(readHandler.in);

        Assertions.assertEquals(1, manager.inputCloseCount);
    }

    @Test
    public void testReadTriangleMesh_path_unknownFormat() throws IOException {
        // arrange
        final Path path = tempDir.resolve("data.test");
        Files.createFile(path);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.readTriangleMesh(path, TEST_PRECISION),
                IllegalArgumentException.class, "No read handler registered for format \"test\"");
    }

    @Test
    public void testReadTriangleMesh_path_readerCreationFail() throws IOException {
        // arrange
        final StubReadHandler3D readHandler = new StubReadHandler3D();
        readHandler.fail = true;
        manager.registerReadHandler("test", readHandler);

        final Path path = tempDir.resolve("data.test");
        Files.createFile(path);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.readTriangleMesh(path, TEST_PRECISION),
                IOException.class, "Test failure");

        Assertions.assertEquals(1, manager.inputCloseCount);
    }

    @Test
    public void testReadTriangleMesh_path_readerCreationFails_inputCloseFails() throws IOException {
        // arrange
        manager.inputStreamFailOnClose = true;

        final StubReadHandler3D readHandler = new StubReadHandler3D();
        readHandler.fail = true;
        manager.registerReadHandler("test", readHandler);

        final Path path = tempDir.resolve("data.test");
        Files.createFile(path);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.readTriangleMesh(path, TEST_PRECISION),
                IOException.class, "Test failure");

        Assertions.assertEquals(1, manager.inputCloseCount);
    }

    @Test
    public void testWriteFacets_outputStream() throws IOException {
        // arrange
        final StubWriteHandler3D writeHandler = new StubWriteHandler3D();
        manager.registerWriteHandler("TEST", writeHandler);

        final CloseCountOutputStream out = new CloseCountOutputStream(new ByteArrayOutputStream());

        // act
        manager.writeFacets(FACET_LIST, out, "test");

        // assert
        Assertions.assertSame(FACET_LIST, writeHandler.facets);
        Assertions.assertSame(out, writeHandler.out);

        Assertions.assertEquals(0, out.getCloseCount());
    }

    @Test
    public void testWriteFacets_outputStream_unknownFormat() throws IOException {
        // arrange
        final CloseCountOutputStream out = new CloseCountOutputStream(new ByteArrayOutputStream());

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.writeFacets(FACET_LIST, out, "test"),
                IllegalArgumentException.class, "No write handler registered for format \"test\"");

        Assertions.assertEquals(0, out.getCloseCount());
    }

    @Test
    public void testWriteFacets_path() throws IOException {
        // arrange
        final Path path = tempDir.resolve("output.test");

        final StubWriteHandler3D writeHandler = new StubWriteHandler3D();
        manager.registerWriteHandler("TEST", writeHandler);

        // act
        manager.writeFacets(FACET_LIST, path);

        // assert
        Assertions.assertSame(FACET_LIST, writeHandler.facets);
        Assertions.assertNotNull(writeHandler.out);

        Assertions.assertTrue(Files.exists(path));
    }

    @Test
    public void testWriteFacets_path_overwritesExisting() throws IOException {
        // arrange
        final Path path = tempDir.resolve("output.test");
        Files.write(path, new byte[8]);

        final StubWriteHandler3D writeHandler = new StubWriteHandler3D();
        manager.registerWriteHandler("TEST", writeHandler);

        // act
        manager.writeFacets(FACET_LIST, path);

        // assert
        Assertions.assertSame(FACET_LIST, writeHandler.facets);
        Assertions.assertNotNull(writeHandler.out);

        Assertions.assertTrue(Files.exists(path));
        Assertions.assertEquals(0L, Files.size(path));
    }

    @Test
    public void testWriteFacets_path_unknownFormat() throws IOException {
        // arrange
        final Path path = tempDir.resolve("output.test");

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.writeFacets(FACET_LIST, path),
                IllegalArgumentException.class, "No write handler registered for format \"test\"");
    }

    @Test
    public void testWriteFacets_path_noFileExtension() throws IOException {
        // arrange
        final Path path = tempDir.resolve("output");

        final Pattern expectedMsgPattern = Pattern.compile(
                "^Cannot determine file data format: file name \".*output\" does not have a file extension$");

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.writeFacets(FACET_LIST, path),
                IllegalArgumentException.class, expectedMsgPattern);
    }

    private static final class TestManager3D extends BoundaryIOManager3D {

        /** If true, an exception will be thrown when close is called on input streams created by the registry. */
        private boolean inputStreamFailOnClose = false;

        /** Number of times close is called on an input stream created by the registry. */
        private int inputCloseCount;

        /** {@inheritDoc} */
        @Override
        protected InputStream getInputStream(final URL url) throws IOException {
            final InputStream in = super.getInputStream(url);

            return new FilterInputStream(in) {

                /** {@inheritDoc} */
                @Override
                public void close() throws IOException {
                    ++inputCloseCount;

                    IOException suppressed = null;
                    try {
                        super.close();
                    } catch (IOException exc) {
                        suppressed = exc;
                    }

                    if (inputStreamFailOnClose) {
                        final IOException exc = new IOException("close fail");

                        if (suppressed != null) {
                            exc.addSuppressed(suppressed);
                        }

                        throw exc;
                    }
                }
            };
        }
    }

    private static final class StubReadHandler3D implements BoundaryReadHandler3D {

        private InputStream in;

        private DoublePrecisionContext precision;

        private boolean fail;

        /** {@inheritDoc} */
        @Override
        public BoundarySource3D read(final InputStream in, final DoublePrecisionContext precision)
                throws IOException {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public Stream<PlaneConvexSubset> boundaries(final InputStream in,
                final DoublePrecisionContext precision) throws IOException {
            this.in = in;

            checkFail();

            return Stream.of(FacetDefinitions.toPolygon(FACET, TEST_PRECISION));
        }

        /** {@inheritDoc} */
        @Override
        public FacetDefinitionReader facetDefinitionReader(final InputStream in) throws IOException {
            this.in = in;

            checkFail();

            return FACET_DEF_READER;
        }

        /** {@inheritDoc} */
        @Override
        public Stream<FacetDefinition> facets(final InputStream in) throws IOException {
            this.in = in;

            checkFail();

            return Stream.of(FACET);
        }

        /** {@inheritDoc} */
        @Override
        public TriangleMesh readTriangleMesh(final InputStream in, final DoublePrecisionContext precision)
                throws IOException {
            this.in = in;
            this.precision = precision;

            checkFail();

            return TRI_MESH;
        }

        private void checkFail() throws IOException {
            if (fail) {
                throw new IOException("Test failure");
            }
        }
    }

    private static final class StubWriteHandler3D implements BoundaryWriteHandler3D {

        private Collection<? extends FacetDefinition> facets;

        private OutputStream out;

        /** {@inheritDoc} */
        @Override
        public void write(final BoundarySource3D boundarySource, final OutputStream out) throws IOException {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public void writeFacets(Collection<? extends FacetDefinition> facets, OutputStream out) throws IOException {
            this.facets = facets;
            this.out = out;
        }
    }
}

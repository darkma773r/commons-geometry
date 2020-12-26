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
package org.apache.commons.geometry.examples.io.threed.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.euclidean.threed.mesh.TriangleMesh;
import org.apache.commons.geometry.examples.io.threed.ModelIOManager;
import org.apache.commons.geometry.examples.io.threed.facet.FacetDefinition;
import org.apache.commons.geometry.examples.io.threed.facet.FacetDefinitionReader;
import org.apache.commons.geometry.examples.io.threed.facet.FacetDefinitions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public abstract class ModelReadWriteHandlerTestBase {

    /** Epsilon value used in test comparisons. This is smaller than in other tests
     * in order to account for storage precision.
     */
    public static final double MODEL_TEST_EPS = 1e-4;

    public static final double TEST_EPS = 1e-5;

    public static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testFacets_empty() throws IOException {
        // act
        final List<FacetDefinition> facets = runFacetReadWrite("empty");

        // assert
        Assertions.assertEquals(0, facets.size());
    }

    @Test
    public void testFacets_cube() throws IOException {
        // act
        final List<FacetDefinition> facets = runFacetReadWrite("cube");

        // assert
        final BoundarySource3D boundaries = FacetDefinitions.toBoundaryList(facets, TEST_PRECISION);
        ModelIOTestUtils.assertCube(boundaries, MODEL_TEST_EPS);
    }

    @Test
    public void testFacets_cubeMinusSphere() throws IOException {
        // act
        final List<FacetDefinition> facets = runFacetReadWrite("cube-minus-sphere");

        // assert
        final BoundarySource3D boundaries = FacetDefinitions.toBoundaryList(facets, TEST_PRECISION);
        ModelIOTestUtils.assertCubeMinusSphere(boundaries, MODEL_TEST_EPS);
    }

    @Test
    public void testBoundarySource_empty() throws IOException {
        // act
        final BoundarySource3D src = runBoundaryReadWrite("empty");

        // act
        Assertions.assertEquals(0, src.toList().count());
    }

    @Test
    public void testBoundarySource_cube() throws IOException {
        // act
        final BoundarySource3D src = runBoundaryReadWrite("cube");

        // assert
        ModelIOTestUtils.assertCube(src, MODEL_TEST_EPS);
    }

    @Test
    public void testBoundarySource_cubeMinusSphere() throws IOException {
        // act
        final BoundarySource3D src = runBoundaryReadWrite("cube-minus-sphere");

        // assert
        ModelIOTestUtils.assertCubeMinusSphere(src, MODEL_TEST_EPS);
    }

    @Test
    public void testTriangleMesh_empty() throws IOException {
        // act
        final TriangleMesh mesh = runTriangleMeshReadWrite("empty");

        // act
        Assertions.assertEquals(0, mesh.getVertexCount());
        Assertions.assertEquals(0, mesh.getFaceCount());
    }

    @Test
    public void testTriangleMesh_cube() throws IOException {
        // act
        final TriangleMesh mesh = runTriangleMeshReadWrite("cube");

        // assert
        Assertions.assertEquals(8, mesh.getVertexCount());
        Assertions.assertEquals(12, mesh.getFaceCount());

        ModelIOTestUtils.assertCube(mesh, MODEL_TEST_EPS);
    }

    @Test
    public void testTriangleMesh_cubeMinusSphere() throws IOException {
        // act
        final TriangleMesh mesh = runTriangleMeshReadWrite("cube-minus-sphere");

        // assert
        ModelIOTestUtils.assertCubeMinusSphere(mesh, MODEL_TEST_EPS);
    }

    protected abstract ModelIOManager.ReadHandler getReadHandler();

    protected abstract ModelIOManager.WriteHandler getWriteHandler();

    protected abstract String getModelLocation(String baseName);

    protected List<FacetDefinition> runFacetReadWrite(final String baseName) throws IOException {
        final byte[] bytes1 = ModelIOTestUtils.resourceBytes(getModelLocation(baseName));
        final List<FacetDefinition> facets1 = readFacets(bytes1);

        final byte[] bytes2 = writeFacets(facets1);
        return readFacets(bytes2);
    }

    protected BoundarySource3D runBoundaryReadWrite(final String baseName) throws IOException {
        final byte[] bytes1 = ModelIOTestUtils.resourceBytes(getModelLocation(baseName));
        final BoundarySource3D src1 = readBoundarySource(bytes1);

        final byte[] bytes2 = writeBoundarySource(src1);
        final BoundarySource3D src2 = readBoundarySource(bytes2);

        final byte[] bytes3 = writeBoundaryStream(src2.boundaryStream());
        return readBoundarySource(bytes3);
    }

    protected TriangleMesh runTriangleMeshReadWrite(final String baseName) throws IOException {
        final byte[] bytes1 = ModelIOTestUtils.resourceBytes(getModelLocation(baseName));
        final TriangleMesh mesh1 = readTriangleMesh(bytes1);

        final byte[] bytes2 = writeBoundarySource(mesh1);
        final TriangleMesh mesh2 = readTriangleMesh(bytes2);

        final byte[] bytes3 = writeBoundarySource(mesh2.toTree());
        return readTriangleMesh(bytes3);
    }

    protected List<FacetDefinition> readFacets(final byte[] bytes) throws IOException {
        final ModelIOManager.ReadHandler readHandler = getReadHandler();

        final CloseCountInputStream in = new CloseCountInputStream(new ByteArrayInputStream(bytes));

        final List<FacetDefinition> facets;
        try (FacetDefinitionReader fr = readHandler.facetDefinitionReader(in)) {
            facets = ModelIOTestUtils.readAll(fr);
        }

        Assertions.assertEquals(1, in.getCloseCount());

        return facets;
    }

    protected byte[] writeFacets(final List<FacetDefinition> facets) throws IOException {
        final ModelIOManager.WriteHandler writeHandler = getWriteHandler();

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final CloseCountOutputStream closeCountOut = new CloseCountOutputStream(out);

        writeHandler.writeFacets(facets.stream(), closeCountOut);

        Assertions.assertEquals(0, closeCountOut.getCloseCount());

        return out.toByteArray();
    }

    protected BoundarySource3D readBoundarySource(final byte[] bytes) throws IOException {
        final ModelIOManager.ReadHandler readHandler = getReadHandler();

        final CloseCountInputStream in = new CloseCountInputStream(new ByteArrayInputStream(bytes));

        final BoundarySource3D src = readHandler.read(in, TEST_PRECISION);

        Assertions.assertEquals(0, in.getCloseCount());

        return src;
    }

    protected TriangleMesh readTriangleMesh(final byte[] bytes) throws IOException {
        final ModelIOManager.ReadHandler readHandler = getReadHandler();

        final CloseCountInputStream in = new CloseCountInputStream(new ByteArrayInputStream(bytes));

        final TriangleMesh mesh = readHandler.readTriangleMesh(in, TEST_PRECISION);

        Assertions.assertEquals(0, in.getCloseCount());

        return mesh;
    }

    protected byte[] writeBoundarySource(final BoundarySource3D src) throws IOException {
        final ModelIOManager.WriteHandler writeHandler = getWriteHandler();

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final CloseCountOutputStream closeCountOut = new CloseCountOutputStream(out);

        writeHandler.write(src, closeCountOut);

        Assertions.assertEquals(0, closeCountOut.getCloseCount());

        return out.toByteArray();
    }

    protected byte[] writeBoundaryStream(final Stream<? extends PlaneConvexSubset> stream) throws IOException {
        final ModelIOManager.WriteHandler writeHandler = getWriteHandler();

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final CloseCountOutputStream closeCountOut = new CloseCountOutputStream(out);

        writeHandler.writeBoundaries(stream, closeCountOut);

        Assertions.assertEquals(0, closeCountOut.getCloseCount());

        return out.toByteArray();
    }
}

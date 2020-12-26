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

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.geometry.euclidean.threed.BoundaryList3D;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.ConvexPolygon3D;
import org.apache.commons.geometry.euclidean.threed.mesh.TriangleMesh;
import org.apache.commons.geometry.examples.io.threed.ModelIOManager;
import org.apache.commons.geometry.examples.io.threed.facet.FacetDefinition;
import org.apache.commons.geometry.examples.io.threed.facet.FacetDefinitionReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Base class for {@link ModelIOManager.ReadHandler} implementations.
 */
public abstract class ModelReadHandlerTestBase extends ModelResourceTestBase {

    @Test
    public void testFacetDefinitionReader_close() throws Exception {
        runWithModelResource("empty", location -> {
            // arrange
            final ModelIOManager.ReadHandler readHandler = createReadHandler();
            final CloseCountInputStream in = ModelIOTestUtils.resourceStream(location);

            // act
            try (FacetDefinitionReader facetReader = readHandler.facetDefinitionReader(in)) {
                // do nothing
            }

            // assert
            Assertions.assertEquals(1, in.getCloseCount(), "Close check failed for resource " + location);
        });
    }

    @Test
    public void testFacetDefinitionReader_empty() throws Exception {
        runWithModelResource("empty", location -> {
            // act
            final List<FacetDefinition> facets = readModelResourceFacets(location);

            // assert
            Assertions.assertEquals(0, facets.size(), "Facet count failed for resource " + location);
        });
    }

    @Test
    public void testFacetDefinitionReader_cube() throws Exception {
        runWithModelResource("cube", location -> {
            // act
            final List<FacetDefinition> facets = readModelResourceFacets(location);

            // assert

            ModelIOTestUtils.assertCube(toBoundaryList(facets), MODEL_TEST_EPS);
        });
    }

    @Test
    public void testFacetDefinitionReader_cubeMinusSphere() throws Exception {
        runWithModelResource("cube-minus-sphere", location -> {
            // act
            final List<FacetDefinition> facets = readModelResourceFacets(location);

            // assert
            ModelIOTestUtils.assertCubeMinusSphere(toBoundaryList(facets), MODEL_TEST_EPS);
        });
    }

    @Test
    public void testRead_doesNotCloseStream() throws Exception {
        runWithModelResource("empty", location -> {
            // arrange
            final ModelIOManager.ReadHandler readHandler = createReadHandler();
            final CloseCountInputStream in = ModelIOTestUtils.resourceStream(location);

            // act
            readHandler.read(in, MODEL_TEST_PRECISION);

            // assert
            Assertions.assertEquals(0, in.getCloseCount(), "Close check failed for resource " + location);
        });
    }

    @Test
    public void testRead_empty() throws Exception {
        runWithModelResource("empty", location -> {
            // arrange
            final ModelIOManager.ReadHandler readHandler = createReadHandler();
            final CloseCountInputStream in = ModelIOTestUtils.resourceStream(location);

            // act
            final BoundarySource3D model = readHandler.read(in, MODEL_TEST_PRECISION);

            // assert
            Assertions.assertEquals(0, model.toList().count(), "Invalid boundary");
        });
    }

    @Test
    public void testRead_cube() throws Exception {
        runWithModelResource("cube", location -> {
            // arrange
            final ModelIOManager.ReadHandler readHandler = createReadHandler();
            final CloseCountInputStream in = ModelIOTestUtils.resourceStream(location);

            // act
            final BoundarySource3D model = readHandler.read(in, MODEL_TEST_PRECISION);

            // assert
            ModelIOTestUtils.assertCube(model, MODEL_TEST_EPS);
        });
    }

    @Test
    public void testRead_cubeMinusSphere() throws Exception {
        runWithModelResource("cube-minus-sphere", location -> {
            // arrange
            final ModelIOManager.ReadHandler readHandler = createReadHandler();
            final CloseCountInputStream in = ModelIOTestUtils.resourceStream(location);

            // act
            final BoundarySource3D model = readHandler.read(in, MODEL_TEST_PRECISION);

            // assert
            ModelIOTestUtils.assertCubeMinusSphere(model, MODEL_TEST_EPS);
        });
    }

    @Test
    public void testReadTriangleMesh_doesNotCloseStream() throws Exception {
        runWithModelResource("empty", location -> {
            // arrange
            final ModelIOManager.ReadHandler readHandler = createReadHandler();
            final CloseCountInputStream in = ModelIOTestUtils.resourceStream(location);

            // act
            readHandler.readTriangleMesh(in, MODEL_TEST_PRECISION);

            // assert
            Assertions.assertEquals(0, in.getCloseCount(), "Close check failed for resource " + location);
        });
    }

    @Test
    public void testReadTriangleMesh_empty() throws Exception {
        runWithModelResource("empty", location -> {
            // arrange
            final ModelIOManager.ReadHandler readHandler = createReadHandler();
            final CloseCountInputStream in = ModelIOTestUtils.resourceStream(location);

            // act
            final TriangleMesh mesh = readHandler.readTriangleMesh(in, MODEL_TEST_PRECISION);

            // assert
            Assertions.assertEquals(0, mesh.getFaceCount(), "Invalid boundary");
        });
    }

    @Test
    public void testReadTriangleMesh_cube() throws Exception {
        runWithModelResource("cube", location -> {
            // arrange
            final ModelIOManager.ReadHandler readHandler = createReadHandler();
            final CloseCountInputStream in = ModelIOTestUtils.resourceStream(location);

            // act
            final TriangleMesh mesh = readHandler.readTriangleMesh(in, MODEL_TEST_PRECISION);

            // assert
            Assertions.assertEquals(12, mesh.getFaceCount());

            ModelIOTestUtils.assertCube(mesh, MODEL_TEST_EPS);
        });
    }

    @Test
    public void testReadTriangleMesh_cubeMinusSphere() throws Exception {
        runWithModelResource("cube-minus-sphere", location -> {
            // arrange
            final ModelIOManager.ReadHandler readHandler = createReadHandler();
            final CloseCountInputStream in = ModelIOTestUtils.resourceStream(location);

            // act
            final TriangleMesh mesh = readHandler.readTriangleMesh(in, MODEL_TEST_PRECISION);

            // assert
            ModelIOTestUtils.assertCubeMinusSphere(mesh, MODEL_TEST_EPS);
        });
    }

    /** Read all facets available from the classpath resource at the given location.
     * @param location classpath location of the resource
     * @return all facet available from the resource
     * @throws IOException if an I/O error occurs
     */
    protected List<FacetDefinition> readModelResourceFacets(final String location) throws IOException {
        final ModelIOManager.ReadHandler readHandler = createReadHandler();
        try (FacetDefinitionReader facetReader =
                readHandler.facetDefinitionReader(ModelIOTestUtils.resourceStream(location))) {
            return ModelIOTestUtils.readAll(facetReader);
        }
    }

    protected BoundaryList3D toBoundaryList(final List<FacetDefinition> facets) {
        final List<ConvexPolygon3D> polygons = facets.stream()
                .map(f -> f.toPolygon(MODEL_TEST_PRECISION))
                .collect(Collectors.toList());

        return new BoundaryList3D(polygons);
    }

    protected abstract ModelIOManager.ReadHandler createReadHandler();
}

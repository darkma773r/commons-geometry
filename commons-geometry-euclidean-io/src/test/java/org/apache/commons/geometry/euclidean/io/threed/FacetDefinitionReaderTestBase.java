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
import java.io.Reader;
import java.util.List;

import org.apache.commons.geometry.core.io.test.CloseCountReader;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.io.EuclideanIOTestUtils;
import org.apache.commons.geometry.euclidean.threed.RegionBSPTree3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingConsumer;

/** Base class for tests of {@link FacetDefinitionReader} implementations. This class
 * provides some basic tests and utility methods.
 */
public abstract class FacetDefinitionReaderTestBase {

    /** Epsilon value used in test comparisons. This is smaller than in other tests
     * in order to account for storage precision.
     */
    public static final double MODEL_TEST_EPS = 1e-6;

    public static final DoublePrecisionContext MODEL_TEST_PRECISION =
            new EpsilonDoublePrecisionContext(MODEL_TEST_EPS);

    @Test
    public void testClose() throws Exception {
        runWithModelResource("empty", location -> {
            // arrange
            final CloseCountReader reader = EuclideanIOTestUtils.resourceReader(location);

            // act
            try (FacetDefinitionReader facetReader = createFacetDefinitionReader(reader)){
                // do nothing
            }

            // assert
            Assertions.assertEquals(1, reader.getCloseCount(), "Close check failed for resource " + location);
        });
    }

    @Test
    public void testEmpty() throws Exception {
        runWithModelResource("empty", location -> {
            // act
            final List<FacetDefinition> facets = readModelResourceFacets(location);

            // assert
            Assertions.assertEquals(0, facets.size(), "Facet count failed for resource " + location);
        });
    }

    @Test
    public void testCube() throws Exception {
        runWithModelResource("cube", location -> {
            // act
            final List<FacetDefinition> facets = readModelResourceFacets(location);

            // assert
            final RegionBSPTree3D tree = EuclideanIOTestUtils.toBoundaryList(facets, MODEL_TEST_PRECISION).toTree();

            Assertions.assertEquals(1.0, tree.getSize(), MODEL_TEST_EPS,
                    "Size check failed for resource " + location);
            EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, tree.getCentroid(), MODEL_TEST_EPS);
        });
    }

    @Test
    public void testCubeMinusSphere() throws Exception {
        runWithModelResource("cube-minus-sphere", location -> {
            // act
            final List<FacetDefinition> facets = readModelResourceFacets(location);

            // assert
            final RegionBSPTree3D tree = EuclideanIOTestUtils.toBoundaryList(facets, MODEL_TEST_PRECISION).toTree();

            Assertions.assertEquals(0.11509505362599505, tree.getSize(), MODEL_TEST_EPS,
                    "Size check failed for resource " + location);
            EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, tree.getCentroid(), MODEL_TEST_EPS);
        });
    }

    /** Read all facets available from the classpath resource at the given location.
     * @param location classpath location of the resource
     * @return all facet available from the resource
     * @throws IOException if an I/O error occurs
     */
    protected List<FacetDefinition> readModelResourceFacets(final String location) throws IOException {
        try (FacetDefinitionReader facetReader =
                createFacetDefinitionReader(EuclideanIOTestUtils.resourceReader(location))) {
            return EuclideanIOTestUtils.readAll(facetReader);
        }
    }

    /** Run the given function with each model resource returned for the given base name.
     * @param baseName model base name
     * @param fn function to execute for each resolved classpath location
     * @throws Exception on failure
     */
    protected void runWithModelResource(final String baseName, final ThrowingConsumer<String> fn)
            throws Exception {
        try {
            for (final String location : getModelResourceLocations(baseName)) {
                fn.accept(location);
            }
        } catch (Error err) {
            throw err;
        } catch (Throwable thr) {
            throw new Exception(thr);
        }
    }

    /** Get the classpath locations of the models with the given base name. A list is returned
     * so that multiple model variations containing the same geometry may be tested simultaneously.
     * @param baseName resource base name
     * @return the classpath locations of the models with the given base name
     */
    protected abstract List<String> getModelResourceLocations(String baseName);

    /** Create a new facet definition reader instance.
     * @param reader underlying reader
     * @return a new facet definition reader instance.
     */
    protected abstract FacetDefinitionReader createFacetDefinitionReader(Reader reader);
}

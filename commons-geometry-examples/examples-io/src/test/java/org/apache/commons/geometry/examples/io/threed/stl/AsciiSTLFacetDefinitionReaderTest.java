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
package org.apache.commons.geometry.examples.io.threed.stl;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.examples.io.threed.facet.FacetDefinition;
import org.apache.commons.geometry.examples.io.threed.facet.FacetDefinitionReader;
import org.junit.Assert;
import org.junit.Test;

public class AsciiSTLFacetDefinitionReaderTest {

    private static final double EPS = 1e-10;

    @Test
    public void testSingleFacet() throws IOException {
        // arrange
        AsciiSTLFacetDefinitionReader reader = facetReader(
                "solid test\n" +
                "facet normal 1 2 3 " +
                    "outer loop " +
                        "vertex 4 5 6 " +
                        "vertex 7 8 9 " +
                        "vertex 10 11 12 " +
                    "endloop " +
                "endfacet " +
                "endsolid test");

        // act
        List<FacetDefinition> facets = readAll(reader);

        // assert
        Assert.assertEquals("test", reader.getSolidName());

        Assert.assertEquals(1, facets.size());

        assertFacet(facets.get(0),
                Vector3D.of(1, 2, 3),
                Vector3D.of(4, 5, 6),
                Vector3D.of(7, 8, 9),
                Vector3D.of(10, 11, 12));
    }

    private static AsciiSTLFacetDefinitionReader facetReader(final String content) {
        return new AsciiSTLFacetDefinitionReader(new StringReader(content));
    }

    private static List<FacetDefinition> readAll(final FacetDefinitionReader reader) throws IOException {
        List<FacetDefinition> facets = new ArrayList<>();

        FacetDefinition facet;
        while ((facet = reader.readFacet()) != null) {
            facets.add(facet);
        }

        return facets;
    }

    private static void assertFacet(final FacetDefinition facet, final Vector3D normal, final Vector3D... pts) {
        if (normal != null) {
            EuclideanTestUtils.assertCoordinatesEqual(normal, facet.getDefinedNormal(), EPS);
        } else {
            Assert.assertNull(facet.getDefinedNormal());
        }

        List<Vector3D> vertices = facet.getVertices();
        Assert.assertEquals(pts.length, vertices.size());

        for (int i = 0; i < pts.length; ++i) {
            EuclideanTestUtils.assertCoordinatesEqual(pts[i], facet.getVertices().get(i), EPS);
        }
    }
}

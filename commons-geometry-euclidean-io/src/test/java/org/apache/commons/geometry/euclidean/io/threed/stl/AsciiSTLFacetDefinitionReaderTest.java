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
package org.apache.commons.geometry.euclidean.io.threed.stl;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.io.EuclideanIOTestUtils;
import org.apache.commons.geometry.euclidean.io.threed.FacetDefinition;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AsciiSTLFacetDefinitionReaderTest {

    private static final double EPS = 1e-10;

    @Test
    public void testSingleFacet() throws IOException {
        // arrange
        final AsciiSTLFacetDefinitionReader reader = facetReader(
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
        List<FacetDefinition> facets = EuclideanIOTestUtils.readAll(reader);

        // assert
        Assertions.assertEquals("test", reader.getSolidName());

        Assertions.assertEquals(1, facets.size());

        assertFacet(facets.get(0),
                Vector3D.of(1, 2, 3),
                Vector3D.of(4, 5, 6),
                Vector3D.of(7, 8, 9),
                Vector3D.of(10, 11, 12));
    }

    private static AsciiSTLFacetDefinitionReader facetReader(final String content) {
        return new AsciiSTLFacetDefinitionReader(new StringReader(content));
    }

    private static void assertFacet(final FacetDefinition facet, final Vector3D normal, final Vector3D... pts) {
        if (normal != null) {
            EuclideanTestUtils.assertCoordinatesEqual(normal, facet.getNormal(), EPS);
        } else {
            Assertions.assertNull(facet.getNormal());
        }

        List<Vector3D> vertices = facet.getVertices();
        Assertions.assertEquals(pts.length, vertices.size());

        for (int i = 0; i < pts.length; ++i) {
            EuclideanTestUtils.assertCoordinatesEqual(pts[i], facet.getVertices().get(i), EPS);
        }
    }
}

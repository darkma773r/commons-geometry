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
package org.apache.commons.geometry.examples.io.threed.text;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.ConvexPolygon3D;
import org.apache.commons.geometry.euclidean.threed.RegionBSPTree3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.examples.io.threed.facet.FacetDefinition;
import org.apache.commons.geometry.examples.io.threed.facet.FacetDefinitionReader;
import org.junit.Assert;
import org.junit.Test;

public class TextFacetDefinitionReaderTest {

    private static final double EPS = 1e-10;

    private static final DoublePrecisionContext PRECISION = new EpsilonDoublePrecisionContext(EPS);

    @Test
    public void testPropertyDefaults() {
        // arrange
        TextFacetDefinitionReader reader = facetReader("");

        // act/assert
        Assert.assertEquals("#", reader.getCommentToken());
    }

    @Test
    public void testSetCommentToken_invalidArgs() {
        // arrange
        TextFacetDefinitionReader reader = facetReader("");
        String baseMsg = "Comment token cannot contain whitespace; was [";

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            reader.setCommentToken(" ");
        }, IllegalArgumentException.class, baseMsg + " ]");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            reader.setCommentToken("a\tb");
        }, IllegalArgumentException.class, baseMsg + "a\tb]");
    }

    @Test
    public void testReadFacet_empty() throws IOException {
        // arrange
        TextFacetDefinitionReader reader = facetReader("");

        // act
        List<FacetDefinition> facets = readAll(reader);

        // assert
        Assert.assertEquals(0, facets.size());
    }

    @Test
    public void testReadFacet_singleFacet() throws IOException {
        // arrange
        TextFacetDefinitionReader reader = facetReader(
                "1.0 2.0 3.0 40 50 60 7.0e-2 8e-2 9E-02 1.01e+1 -11.02 +12");

        // act
        List<FacetDefinition> facets = readAll(reader);

        // assert
        Assert.assertEquals(1, facets.size());

        assertFacet(facets.get(0),
                Vector3D.of(1, 2, 3),
                Vector3D.of(40, 50, 60),
                Vector3D.of(0.07, 0.08, 0.09),
                Vector3D.of(10.1, -11.02, 12));
    }

    @Test
    public void testReadFacet_multipeFacets() throws IOException {
        // arrange
        TextFacetDefinitionReader reader = facetReader(
                "1,2,3    4,5,6 7,8,9    10,11,12\r" +
                "1 1 1;2 2 2;3 3 3;4 4 4;5 5 5\r\n" +
                "6 6 6 6 6 6 6 6 6");

        // act
        List<FacetDefinition> facets = readAll(reader);

        // assert
        Assert.assertEquals(3, facets.size());

        assertFacet(facets.get(0),
                Vector3D.of(1, 2, 3),
                Vector3D.of(4, 5, 6),
                Vector3D.of(7, 8, 9),
                Vector3D.of(10, 11, 12));

        assertFacet(facets.get(1),
                Vector3D.of(1, 1, 1),
                Vector3D.of(2, 2, 2),
                Vector3D.of(3, 3, 3),
                Vector3D.of(4, 4, 4),
                Vector3D.of(5, 5, 5));

        assertFacet(facets.get(2),
                Vector3D.of(6, 6, 6),
                Vector3D.of(6, 6, 6),
                Vector3D.of(6, 6, 6));
    }

    @Test
    public void testReadFacet_blankLinesAndComments() throws IOException {
        // arrange
        TextFacetDefinitionReader reader = facetReader(
                "# some ignored numbers: 1 2 3 4 5 6\n" +
                "\n" +
                " \n" +
                "1 2 3 4 5 6 7 8 9 # end of line comment\n" +
                "1 1 1 2 2 2 3 3 3\n" +
                "\t\n" +
                "#line comment\n" +
                "5 5 5 5 5 5 5 5 5\n\n  \n");

        // act
        List<FacetDefinition> facets = readAll(reader);

        // assert
        Assert.assertEquals(3, facets.size());

        assertFacet(facets.get(0),
                Vector3D.of(1, 2, 3),
                Vector3D.of(4, 5, 6),
                Vector3D.of(7, 8, 9));

        assertFacet(facets.get(1),
                Vector3D.of(1, 1, 1),
                Vector3D.of(2, 2, 2),
                Vector3D.of(3, 3, 3));

        assertFacet(facets.get(2),
                Vector3D.of(5, 5, 5),
                Vector3D.of(5, 5, 5),
                Vector3D.of(5, 5, 5));
    }

    @Test
    public void testReadFacet_nonDefaultCommentToken() throws IOException {
        // arrange
        TextFacetDefinitionReader reader = facetReader(
                "5$ some ignored numbers: 1 2 3 4 5 6\n" +
                "\n" +
                " \n" +
                "1 2 3 4 5 6 7 8 9 5$ end of line comment\n" +
                "1 1 1 2 2 2 3 3 3\n" +
                "\t\n" +
                "5$line comment\n" +
                "5 5 5 5 5 5 5 5 5\n");

        reader.setCommentToken("5$");

        // act
        List<FacetDefinition> facets = readAll(reader);

        // assert
        Assert.assertEquals(3, facets.size());

        assertFacet(facets.get(0),
                Vector3D.of(1, 2, 3),
                Vector3D.of(4, 5, 6),
                Vector3D.of(7, 8, 9));

        assertFacet(facets.get(1),
                Vector3D.of(1, 1, 1),
                Vector3D.of(2, 2, 2),
                Vector3D.of(3, 3, 3));

        assertFacet(facets.get(2),
                Vector3D.of(5, 5, 5),
                Vector3D.of(5, 5, 5),
                Vector3D.of(5, 5, 5));
    }

    @Test
    public void testReadFacet_longCommentToken() throws IOException {
        // arrange
        TextFacetDefinitionReader reader = facetReader(
                "this_is-a-comment some ignored numbers: 1 2 3 4 5 6\n" +
                "\n" +
                " \n" +
                "1 2 3 4 5 6 7 8 9 this_is-a-comment end of line comment\n" +
                "1 1 1 2 2 2 3 3 3\n" +
                "\t\n" +
                "this_is-a-commentline comment\n" +
                "5 5 5 5 5 5 5 5 5\n");

        reader.setCommentToken("this_is-a-comment");

        // act
        List<FacetDefinition> facets = readAll(reader);

        // assert
        Assert.assertEquals(3, facets.size());

        assertFacet(facets.get(0),
                Vector3D.of(1, 2, 3),
                Vector3D.of(4, 5, 6),
                Vector3D.of(7, 8, 9));

        assertFacet(facets.get(1),
                Vector3D.of(1, 1, 1),
                Vector3D.of(2, 2, 2),
                Vector3D.of(3, 3, 3));

        assertFacet(facets.get(2),
                Vector3D.of(5, 5, 5),
                Vector3D.of(5, 5, 5),
                Vector3D.of(5, 5, 5));
    }

    @Test
    public void testReadFacet_emptyCommentToken() {
        // arrange
        TextFacetDefinitionReader reader = facetReader("# line comment\n");
        reader.setCommentToken("");

        // act
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            reader.readFacet();
        }, IllegalStateException.class,
                "Parsing failed at line 1, column 1: expected double but found empty token followed by [#]");
    }

    @Test
    public void testReadFacet_nullCommentToken() {
        // arrange
        TextFacetDefinitionReader reader = facetReader("# line comment\n");
        reader.setCommentToken(null);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            reader.readFacet();
        }, IllegalStateException.class,
                "Parsing failed at line 1, column 1: expected double but found empty token followed by [#]");
    }

    @Test
    public void testReadFacet_invalidTokens() {
        // arrange
        TextFacetDefinitionReader reader = facetReader("1 abc 3 ; 4 5 6 ; 7 8 9");

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            reader.readFacet();
        }, IllegalStateException.class,
                "Parsing failed at line 1, column 3: expected double but found [abc]");
    }

    @Test
    public void testReadFacet_notEnoughVectors() {
        // arrange
        TextFacetDefinitionReader reader = facetReader(
                "1\n" +
                "1 2\n" +
                "1 2 3\n" +
                "1 2 3 ; 4 5 6;\n");

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            reader.readFacet();
        }, IllegalStateException.class,
                "Parsing failed at line 1, column 2: expected double but found end of line");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            reader.readFacet();
        }, IllegalStateException.class,
                "Parsing failed at line 2, column 4: expected double but found end of line");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            reader.readFacet();
        }, IllegalStateException.class,
                "Parsing failed at line 3, column 6: expected double but found end of line");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            reader.readFacet();
        }, IllegalStateException.class,
                "Parsing failed at line 4, column 15: expected double but found end of line");
    }

    @Test
    public void testReadFacet_cube_txt() throws IOException {
        // act/assert
        checkCubeResource("/models/cube.txt");
    }

    @Test
    public void testReadFacet_cube_csv() throws IOException {
        // act/assert
        checkCubeResource("/models/cube.csv");
    }

    private void checkCubeResource(final String path) throws IOException{
        // arrange
        try (Reader reader = resourceReader(path)) {
            TextFacetDefinitionReader facetReader = new TextFacetDefinitionReader(reader);

            // act
            List<FacetDefinition> facets = readAll(facetReader);

            // assert
            Assert.assertEquals(6, facets.size());

            List<ConvexPolygon3D> polygons = facets.stream()
                    .map(f -> f.toPolygon(PRECISION))
                    .collect(Collectors.toList());

            RegionBSPTree3D tree = RegionBSPTree3D.empty();
            tree.insert(polygons);

            Assert.assertEquals(1.0, tree.getSize(), EPS);
            EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5, 0.5, 0.5), tree.getCentroid(), EPS);
        }
    }

    private static TextFacetDefinitionReader facetReader(final String content) {
        return new TextFacetDefinitionReader(new StringReader(content));
    }

    private static Reader resourceReader(final String path) {
        final InputStream in = TextFacetDefinitionReaderTest.class.getResourceAsStream(path);
        return new InputStreamReader(in, StandardCharsets.UTF_8);
    }

    private static List<FacetDefinition> readAll(final FacetDefinitionReader reader) throws IOException {
        List<FacetDefinition> facets = new ArrayList<>();

        FacetDefinition facet;
        while ((facet = reader.readFacet()) != null) {
            facets.add(facet);
        }

        return facets;
    }

    private static void assertFacet(final FacetDefinition facet, final Vector3D... pts) {
        Assert.assertNull(facet.getDefinedNormal());

        List<Vector3D> vertices = facet.getVertices();
        Assert.assertEquals(pts.length, vertices.size());

        for (int i = 0; i < pts.length; ++i) {
            EuclideanTestUtils.assertCoordinatesEqual(pts[i], facet.getVertices().get(i), EPS);
        }
    }
}

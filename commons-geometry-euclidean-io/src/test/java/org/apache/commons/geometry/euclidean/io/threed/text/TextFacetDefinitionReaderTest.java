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
package org.apache.commons.geometry.euclidean.io.threed.text;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.io.EuclideanIOTestUtils;
import org.apache.commons.geometry.euclidean.io.threed.FacetDefinition;
import org.apache.commons.geometry.euclidean.io.threed.FacetDefinitionReader;
import org.apache.commons.geometry.euclidean.io.threed.FacetDefinitionReaderTestBase;
import org.apache.commons.geometry.euclidean.threed.RegionBSPTree3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TextFacetDefinitionReaderTest extends FacetDefinitionReaderTestBase {

    @Test
    public void testPropertyDefaults() {
        // arrange
        TextFacetDefinitionReader reader = facetReader("");

        // act/assert
        Assertions.assertEquals("#", reader.getCommentToken());
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
        List<FacetDefinition> facets = EuclideanIOTestUtils.readAll(reader);

        // assert
        Assertions.assertEquals(0, facets.size());
    }

    @Test
    public void testReadFacet_singleFacet() throws IOException {
        // arrange
        TextFacetDefinitionReader reader = facetReader(
                "1.0 2.0 3.0 40 50 60 7.0e-2 8e-2 9E-02 1.01e+1 -11.02 +12");

        // act
        List<FacetDefinition> facets = EuclideanIOTestUtils.readAll(reader);

        // assert
        Assertions.assertEquals(1, facets.size());

        assertFacet(facets.get(0),
                Vector3D.of(1, 2, 3),
                Vector3D.of(40, 50, 60),
                Vector3D.of(0.07, 0.08, 0.09),
                Vector3D.of(10.1, -11.02, 12));
    }

    @Test
    public void testReadFacet_multipleFacets() throws IOException {
        // arrange
        TextFacetDefinitionReader reader = facetReader(
                "1,2,3    4,5,6 7,8,9    10,11,12\r" +
                "1 1 1;2 2 2;3 3 3;4 4 4;5 5 5\r\n" +
                "6 6 6 6 6 6 6 6 6");

        // act
        List<FacetDefinition> facets = EuclideanIOTestUtils.readAll(reader);

        // assert
        Assertions.assertEquals(3, facets.size());

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
        List<FacetDefinition> facets = EuclideanIOTestUtils.readAll(reader);

        // assert
        Assertions.assertEquals(3, facets.size());

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
        List<FacetDefinition> facets = EuclideanIOTestUtils.readAll(reader);

        // assert
        Assertions.assertEquals(3, facets.size());

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
        List<FacetDefinition> facets = EuclideanIOTestUtils.readAll(reader);

        // assert
        Assertions.assertEquals(3, facets.size());

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
        }, IOException.class,
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
        }, IOException.class,
                "Parsing failed at line 1, column 1: expected double but found empty token followed by [#]");
    }

    @Test
    public void testReadFacet_invalidTokens() {
        // arrange
        TextFacetDefinitionReader reader = facetReader("1 abc 3 ; 4 5 6 ; 7 8 9");

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            reader.readFacet();
        }, IOException.class,
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
        }, IOException.class,
                "Parsing failed at line 1, column 2: expected double but found end of line");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            reader.readFacet();
        }, IOException.class,
                "Parsing failed at line 2, column 4: expected double but found end of line");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            reader.readFacet();
        }, IOException.class,
                "Parsing failed at line 3, column 6: expected double but found end of line");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            reader.readFacet();
        }, IOException.class,
                "Parsing failed at line 4, column 15: expected double but found end of line");
    }

    @Test
    public void testCube_csv() throws IOException {
        // arrange
        try (Reader reader = EuclideanIOTestUtils.resourceReader("/models/cube.csv")) {
            TextFacetDefinitionReader facetReader = new TextFacetDefinitionReader(reader);

            // act
            List<FacetDefinition> facets = EuclideanIOTestUtils.readAll(facetReader);

            // assert
            Assertions.assertEquals(12, facets.size());

            RegionBSPTree3D tree = EuclideanIOTestUtils.toBoundaryList(facets, MODEL_TEST_PRECISION).toTree();

            Assertions.assertEquals(1.0, tree.getSize(), MODEL_TEST_EPS);
            EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, tree.getCentroid(), MODEL_TEST_EPS);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected List<String> getModelResourceLocations(final String baseName) {
        return Arrays.asList(
                "/models/" + baseName + ".txt",
                "/models/" + baseName + ".csv");
    }

    /** {@inheritDoc} */
    @Override
    protected FacetDefinitionReader createFacetDefinitionReader(final Reader reader) {
        return new TextFacetDefinitionReader(reader);
    }

    private static TextFacetDefinitionReader facetReader(final String content) {
        return new TextFacetDefinitionReader(new StringReader(content));
    }

    private static void assertFacet(final FacetDefinition facet, final Vector3D... pts) {
        Assertions.assertNull(facet.getNormal());

        List<Vector3D> vertices = facet.getVertices();
        Assertions.assertEquals(pts.length, vertices.size());

        for (int i = 0; i < pts.length; ++i) {
            EuclideanTestUtils.assertCoordinatesEqual(pts[i], facet.getVertices().get(i), MODEL_TEST_EPS);
        }
    }
}

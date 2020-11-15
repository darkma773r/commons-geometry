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
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.ConvexPolygon3D;
import org.apache.commons.geometry.euclidean.threed.Planes;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.examples.io.threed.facet.SimpleFacetDefinition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TextFacetDefinitionWriterTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private StringWriter writer;

    private TextFacetDefinitionWriter fdWriter;

    @Before
    public void setup() {
        writer = new StringWriter();
        fdWriter = new TextFacetDefinitionWriter(writer);
    }

    @Test
    public void testPropertyDefaults() {
        // act/assert
        Assert.assertEquals("\n", fdWriter.getLineSeparator());
        Assert.assertEquals(6, fdWriter.getDecimalFormat().getMaximumFractionDigits());
        Assert.assertEquals(" ", fdWriter.getVertexCoordinateSeparator());
        Assert.assertEquals("; ", fdWriter.getVertexSeparator());
        Assert.assertEquals(-1, fdWriter.getFacetVertexCount());
    }

    @Test
    public void testWriteVertices() throws IOException {
        // arrange
        final List<Vector3D> vertices1 = Arrays.asList(
                Vector3D.ZERO, Vector3D.of(0.5, 0, 0), Vector3D.of(0, -0.5, 0));
        final List<Vector3D> vertices2 = Arrays.asList(
                Vector3D.of(0.5, 0.7, 1.2), Vector3D.of(10.01, -4, 2), Vector3D.of(-10.0 / 3.0, 0, 0), Vector3D.ZERO);

        // act
        fdWriter.write(vertices1);
        fdWriter.write(vertices2);

        // assert
        Assert.assertEquals(
                "0 0 0; 0.5 0 0; 0 -0.5 0\n" +
                "0.5 0.7 1.2; 10.01 -4 2; -3.333333 0 0; 0 0 0\n", writer.toString());
    }

    @Test
    public void testWriteVertices_invalidCount() throws IOException {
        // arrange
        fdWriter.setFacetVertexCount(4);
        final List<Vector3D> notEnough = Arrays.asList(Vector3D.ZERO, Vector3D.Unit.PLUS_X);
        final List<Vector3D> tooMany = Arrays.asList(
                Vector3D.ZERO, Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y,
                Vector3D.Unit.MINUS_X, Vector3D.Unit.MINUS_Y);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            try {
                fdWriter.write(notEnough);
            } catch (IOException exc) {
                throw new RuntimeException(exc);
            }
        }, IllegalArgumentException.class, "At least 3 vertices are required per facet; found 2");

        GeometryTestUtils.assertThrows(() -> {
            try {
                fdWriter.write(tooMany);
            } catch (IOException exc) {
                throw new RuntimeException(exc);
            }
        }, IllegalArgumentException.class, "Writer requires 4 vertices per facet; found 5");
    }

    @Test
    public void testWriteFacetDefinition() throws IOException {
        // arrange
        final SimpleFacetDefinition f1 = new SimpleFacetDefinition(Arrays.asList(
                Vector3D.ZERO, Vector3D.of(0.5, 0, 0), Vector3D.of(0, -0.5, 0)));
        final SimpleFacetDefinition f2 = new SimpleFacetDefinition(Arrays.asList(
                Vector3D.of(0.5, 0.7, 1.2), Vector3D.of(10.01, -4, 2), Vector3D.of(-10.0 / 3.0, 0, 0), Vector3D.ZERO));

        // act
        fdWriter.write(f1);
        fdWriter.write(f2);

        // assert
        Assert.assertEquals(
                "0 0 0; 0.5 0 0; 0 -0.5 0\n" +
                "0.5 0.7 1.2; 10.01 -4 2; -3.333333 0 0; 0 0 0\n", writer.toString());
    }

    @Test
    public void testWriteFacetDefinition_invalidCount() throws IOException {
        // arrange
        fdWriter.setFacetVertexCount(4);
        final SimpleFacetDefinition tooMany = new SimpleFacetDefinition(Arrays.asList(
                Vector3D.ZERO, Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y,
                Vector3D.Unit.MINUS_X, Vector3D.Unit.MINUS_Y));

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            try {
                fdWriter.write(tooMany);
            } catch (IOException exc) {
                throw new RuntimeException(exc);
            }
        }, IllegalArgumentException.class, "Writer requires 4 vertices per facet; found 5");
    }

    @Test
    public void testWritePlaneConvexSubset() throws IOException {
        // arrange
        final ConvexPolygon3D poly1 = Planes.convexPolygonFromVertices(Arrays.asList(
                    Vector3D.ZERO, Vector3D.of(0, 0, -0.5), Vector3D.of(0, -0.5, 0)
                ), TEST_PRECISION);
        final ConvexPolygon3D poly2 = Planes.convexPolygonFromVertices(Arrays.asList(
                Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(1, 1, 0), Vector3D.of(0, 1, 0)
            ), TEST_PRECISION);

        // act
        fdWriter.write(poly1);
        fdWriter.write(poly2);

        // assert
        Assert.assertEquals(
                "0 0 0; 0 0 -0.5; 0 -0.5 0\n" +
                "0 0 0; 1 0 0; 1 1 0; 0 1 0\n", writer.toString());
    }

    @Test
    public void testWriteBoundarySource_empty() throws IOException {
        // act
        fdWriter.write(BoundarySource3D.from(Collections.emptyList()));

        // assert
        Assert.assertEquals("", writer.toString());
    }
}

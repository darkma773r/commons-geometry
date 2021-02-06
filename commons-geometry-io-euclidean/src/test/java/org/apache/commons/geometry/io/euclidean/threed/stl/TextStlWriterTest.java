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
package org.apache.commons.geometry.io.euclidean.threed.stl;

import java.io.IOException;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.io.core.test.CloseCountWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TextStlWriterTest {

    private final StringWriter out = new StringWriter();

    @Test
    public void testNoContent() throws IOException {
        // arrange
        final CloseCountWriter countWriter = new CloseCountWriter(out);

        // act
        try (TextStlWriter writer = new TextStlWriter(countWriter)) {
            Assertions.assertEquals(0, countWriter.getCloseCount());
        }

        // assert
        Assertions.assertEquals(1, countWriter.getCloseCount());
        Assertions.assertEquals("", out.toString());
    }

    @Test
    public void testEmpty_noName() throws IOException {
        // arrange
        final CloseCountWriter countWriter = new CloseCountWriter(out);

        // act
        try (TextStlWriter writer = new TextStlWriter(countWriter)) {
            writer.startSolid();
            writer.endSolid();

            Assertions.assertEquals(0, countWriter.getCloseCount());
        }

        // assert
        Assertions.assertEquals(1, countWriter.getCloseCount());
        Assertions.assertEquals(
                "solid \n" +
                "endsolid \n", out.toString());
    }

    @Test
    public void testEmpty_withName() throws IOException {
        // arrange
        final CloseCountWriter countWriter = new CloseCountWriter(out);

        // act
        try (TextStlWriter writer = new TextStlWriter(countWriter)) {
            writer.startSolid("Name of the solid");
            writer.endSolid();

            Assertions.assertEquals(0, countWriter.getCloseCount());
        }

        // assert
        Assertions.assertEquals(1, countWriter.getCloseCount());
        Assertions.assertEquals(
                "solid Name of the solid\n" +
                "endsolid Name of the solid\n", out.toString());
    }

    @Test
    public void testClose_endsSolid() throws IOException {
        // arrange
        final CloseCountWriter countWriter = new CloseCountWriter(out);

        // act
        try (TextStlWriter writer = new TextStlWriter(countWriter)) {
            writer.startSolid("name");

            Assertions.assertEquals(0, countWriter.getCloseCount());
        }

        // assert
        Assertions.assertEquals(1, countWriter.getCloseCount());
        Assertions.assertEquals(
                "solid name\n" +
                "endsolid name\n", out.toString());
    }

    @Test
    public void testStartSolid_containsNewLine() throws IOException {
        // arrange
        try (TextStlWriter writer = new TextStlWriter(out)) {
            final String err = "Solid name cannot contain new line characters";

            // act/assert
            GeometryTestUtils.assertThrowsWithMessage(
                    () -> writer.startSolid("Hi\nthere"),
                    IllegalArgumentException.class, err);
            GeometryTestUtils.assertThrowsWithMessage(
                    () -> writer.startSolid("Hi\r\nthere"),
                    IllegalArgumentException.class, err);
            GeometryTestUtils.assertThrowsWithMessage(
                    () -> writer.startSolid("Hi\rthere"),
                    IllegalArgumentException.class, err);
        }
    }

    @Test
    public void testWrite_verticesAndNormal() throws IOException {
        // arrange
        final List<Vector3D> vertices = Arrays.asList(
                Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0));
        final Vector3D n1 = Vector3D.Unit.PLUS_Z;
        final Vector3D n2 = Vector3D.Unit.MINUS_Z;

        // act
        try (TextStlWriter writer = new TextStlWriter(out)) {
            writer.startSolid();

            writer.write(vertices, n1);
            writer.write(vertices, n2);
            writer.write(vertices, null);
        }

        // assert
        Assertions.assertEquals(
            "solid \n" +
            "facet 0.0 0.0 1.0\n" +
            "outer loop\n" +
            "vertex 0.0 0.0 0.0\n" +
            "vertex 1.0 0.0 0.0\n" +
            "vertex 0.0 1.0 0.0\n" +
            "endloop\n" +
            "endfacet\n" +
            "facet 0.0 0.0 -1.0\n" +
            "outer loop\n" +
            "vertex 0.0 0.0 0.0\n" +
            "vertex 0.0 1.0 0.0\n" +
            "vertex 1.0 0.0 0.0\n" +
            "endloop\n" +
            "endfacet\n" +
            "facet 0.0 0.0 0.0\n" +
            "outer loop\n" +
            "vertex 0.0 0.0 0.0\n" +
            "vertex 1.0 0.0 0.0\n" +
            "vertex 0.0 1.0 0.0\n" +
            "endloop\n" +
            "endfacet\n" +
            "endsolid \n", out.toString());
    }
}

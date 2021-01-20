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
import java.io.Reader;
import java.util.Arrays;

import org.apache.commons.geometry.core.io.internal.SimpleTextParser;
import org.apache.commons.geometry.euclidean.io.threed.FacetDefinition;
import org.apache.commons.geometry.euclidean.io.threed.FacetDefinitionReader;
import org.apache.commons.geometry.euclidean.io.threed.SimpleFacetDefinition;
import org.apache.commons.geometry.euclidean.threed.Vector3D;

class AsciiSTLFacetDefinitionReader implements FacetDefinitionReader {

    private Reader reader;

    private SimpleTextParser parser;

    private boolean skipSolidKeyword = false;

    private boolean foundSolidStart = false;

    private boolean foundSolidEnd = false;

    private String solidName;

    public AsciiSTLFacetDefinitionReader(final Reader reader) {
        this(reader, false);
    }

    AsciiSTLFacetDefinitionReader(final Reader reader, final boolean skipSolidKeyword) {
        this.reader = reader;
        this.parser = new SimpleTextParser(reader);
        this.skipSolidKeyword = skipSolidKeyword;
    }

    public String getSolidName() throws IOException {
        ensureSolidStarted();

        return solidName;
    }

    /** {@inheritDoc} */
    @Override
    public FacetDefinition readFacet() throws IOException {
        if (!foundSolidEnd && parser.hasMoreCharacters()) {
            ensureSolidStarted();

            nextWord();

            int choice = parser.chooseIgnoreCase(
                    STLConstants.FACET_START_KEYWORD,
                    STLConstants.SOLID_END_KEYWORD);

            if (choice == 0) {
                return readFacetInternal();
            } else {
                foundSolidEnd = true;
            }
        }

        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void close() throws IOException {
        reader.close();
    }

    private FacetDefinition readFacetInternal() throws IOException {
        Vector3D normal;
        Vector3D p1;
        Vector3D p2;
        Vector3D p3;

        matchKeyword(STLConstants.NORMAL_KEYWORD);
        normal = readVector();

        matchKeyword(STLConstants.OUTER_KEYWORD);
        matchKeyword(STLConstants.LOOP_START_KEYWORD);

        matchKeyword(STLConstants.VERTEX_KEYWORD);
        p1 = readVector();

        matchKeyword(STLConstants.VERTEX_KEYWORD);
        p2 = readVector();

        matchKeyword(STLConstants.VERTEX_KEYWORD);
        p3 = readVector();

        matchKeyword(STLConstants.LOOP_END_KEYWORD);
        matchKeyword(STLConstants.FACET_END_KEYWORD);

        return new SimpleFacetDefinition(Arrays.asList(p1, p2, p3), normal);
    }

    private void ensureSolidStarted() throws IOException {
        if (!foundSolidStart) {
            beginSolid();

            foundSolidStart = true;
        }
    }

    private void beginSolid() throws IOException {
        if (!skipSolidKeyword) {
            skipSolidKeyword = false;

            matchKeyword(STLConstants.SOLID_START_KEYWORD);
        }

        solidName = parser.nextLine()
                .getCurrentToken();

        if (solidName != null) {
            solidName = solidName.trim();
        }
    }

    private void nextWord() throws IOException {
        parser.discardWhitespace()
            .nextAlphanumeric();
    }

    private void matchKeyword(final String keyword) throws IOException {
        nextWord();
        parser.matchIgnoreCase(keyword);
    }

    private Vector3D readVector() throws IOException {
        final double x = readDouble();
        final double y = readDouble();
        final double z = readDouble();

        return Vector3D.of(x, y, z);
    }

    private double readDouble() throws IOException {
        return parser
                .discardWhitespace()
                .next(SimpleTextParser::isDecimalPart)
                .getCurrentTokenAsDouble();
    }
}

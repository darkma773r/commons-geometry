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
package org.apache.commons.geometry.examples.io.threed.obj;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.examples.io.threed.facet.FacetDefinition;
import org.apache.commons.geometry.examples.io.threed.facet.FacetDefinitionReader;
import org.apache.commons.geometry.examples.io.threed.facet.SimpleFacetDefinition;

public class OBJFacetDefinitionReader implements FacetDefinitionReader {

    private final OBJParser parser;

    private final List<Vector3D> modelVertices = new ArrayList<>();

    private final List<Vector3D> modelNormals = new ArrayList<>();

    public OBJFacetDefinitionReader(final Reader reader) {
        this.parser = new OBJParser(reader);
    }

    /** {@inheritDoc} */
    @Override
    public FacetDefinition readFacet() throws IOException {
        String keyword;
        while (parser.nextKeyword()) {
            keyword = parser.getKeyword();

            switch (keyword) {
                case OBJConstants.VERTEX_KEYWORD:
                    modelVertices.add(parser.readVector());
                    break;
                case OBJConstants.VERTEX_NORMAL_KEYWORD:
                    modelNormals.add(parser.readVector());
                    break;
                case OBJConstants.FACE_KEYWORD:
                    final OBJParser.Face face = parser.readFace();

                    final List<Vector3D> vertices = face.getVertices(modelVertices::get);
                    final Vector3D definedNormal = face.getDefinedCompositeNormal(modelNormals::get);

                    return new SimpleFacetDefinition(vertices, definedNormal);
            }
        }

        return null;
    }
}

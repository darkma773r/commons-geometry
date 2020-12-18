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
import java.util.Iterator;
import java.util.List;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.mesh.SimpleTriangleMesh;
import org.apache.commons.geometry.euclidean.threed.mesh.TriangleMesh;

public class OBJTriangleMeshReader extends AbstractOBJReader {

    private final SimpleTriangleMesh.Builder meshBuilder;

    private final List<Vector3D> normals = new ArrayList<>();

    public OBJTriangleMeshReader(final Reader reader, final DoublePrecisionContext precision) {
        super(reader);

        this.meshBuilder = SimpleTriangleMesh.builder(precision);
    }

    public TriangleMesh readTriangleMesh() throws IOException {
        PolygonOBJParser.Face face;
        Vector3D definedNormal;
        Iterator<PolygonOBJParser.VertexAttributes> attrs;
        while ((face = readFace()) != null) {
            // get the face attributes in the proper counter-clockwise orientation
            definedNormal = face.getDefinedCompositeNormal(normals::get);
            attrs = face.getOrientedVertexAttributes(definedNormal, meshBuilder::getVertex).iterator();

            // add the face vertices using a triangle fan
            int p0 = attrs.next().getVertexIndex();
            int p1 = attrs.next().getVertexIndex();
            int p2;

            while (attrs.hasNext()) {
                p2 = attrs.next().getVertexIndex();

                meshBuilder.addFace(p0, p1, p2);

                p1 = p2;
            }
        }

        return meshBuilder.build();
    }

    /** {@inheritDoc} */
    @Override
    protected void handleVertex(final Vector3D vertex) {
        meshBuilder.addVertex(vertex);
    }

    /** {@inheritDoc} */
    @Override
    protected void handleNormal(final Vector3D normal) {
        normals.add(normal);
    }
}

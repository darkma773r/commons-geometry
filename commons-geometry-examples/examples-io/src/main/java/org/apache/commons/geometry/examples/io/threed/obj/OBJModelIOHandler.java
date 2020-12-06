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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.mesh.SimpleTriangleMesh;
import org.apache.commons.geometry.euclidean.threed.mesh.TriangleMesh;
import org.apache.commons.geometry.examples.io.threed.AbstractModelIOHandler;
import org.apache.commons.geometry.examples.io.threed.ModelIO;

/** {@link org.apache.commons.geometry.examples.io.threed.ModelIOHandler ModelIOHandler}
 * implementation for the OBJ file format.
 */
public class OBJModelIOHandler extends AbstractModelIOHandler {

    /** {@inheritDoc} */
    @Override
    public boolean handlesType(final String type) {
        return ModelIO.OBJ.equalsIgnoreCase(type);
    }

    /** {@inheritDoc} */
    @Override
    protected TriangleMesh readInternal(final String type, final InputStream in,
            final DoublePrecisionContext precision) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(in, OBJConstants.DEFAULT_CHARSET)) {
//            final OBJReader objReader = new OBJReader();
//            return objReader.readTriangleMesh(reader, precision);

            return new OBJTriangleMeshReader(reader, precision).readTriangleMesh();
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void writeInternal(final BoundarySource3D model, final String type, final OutputStream out)
            throws IOException {
        try (OBJWriter objWriter = new OBJWriter(new BufferedWriter(
                new OutputStreamWriter(out, OBJConstants.DEFAULT_CHARSET)))) {
            objWriter.writeBoundaries(model);
        }
    }

    private static final class OBJTriangleMeshReader {

        private final PolygonOBJParser parser;

        private final List<Vector3D> modelNormals = new ArrayList<>();

        private final SimpleTriangleMesh.Builder meshBuilder;

        OBJTriangleMeshReader(final Reader reader, final DoublePrecisionContext precision) {
            parser = new PolygonOBJParser(reader);
            meshBuilder = SimpleTriangleMesh.builder(precision);
        }

        public TriangleMesh readTriangleMesh() throws IOException {
            String keyword;
            while (parser.nextKeyword()) {
                keyword = parser.getKeyword();

                switch (keyword) {
                    case OBJConstants.VERTEX_KEYWORD:
                        meshBuilder.addVertex(parser.readVector());
                        break;
                    case OBJConstants.VERTEX_NORMAL_KEYWORD:
                        modelNormals.add(parser.readVector());
                        break;
                    case OBJConstants.FACE_KEYWORD:
                        final PolygonOBJParser.Face face = parser.readFace();
                        final Vector3D normal = face.getDefinedCompositeNormal(modelNormals::get);

                        addTriangles(face.getOrientedVertexAttributes(normal, meshBuilder::getVertex));
                }
            }

            return meshBuilder.build();
        }

        private void addTriangles(final List<PolygonOBJParser.VertexAttributes> faceVertices) {

            Iterator<PolygonOBJParser.VertexAttributes> it = faceVertices.iterator();

            int p0 = it.next().getVertexIndex();
            int p1 = it.next().getVertexIndex();
            int p2;
            while (it.hasNext()) {
                p2 = it.next().getVertexIndex();

                meshBuilder.addFace(p0, p1, p2);

                p1 = p2;
            }
        }
    }
}

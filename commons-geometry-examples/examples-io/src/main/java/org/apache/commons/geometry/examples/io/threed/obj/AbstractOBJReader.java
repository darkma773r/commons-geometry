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

import org.apache.commons.geometry.euclidean.threed.Vector3D;

public abstract class AbstractOBJReader implements AutoCloseable {
    private final Reader reader;

    private final PolygonOBJParser parser;

    AbstractOBJReader(final Reader reader) {
        this.reader = reader;
        this.parser = new PolygonOBJParser(reader);
    }

    /** {@inheritDoc} */
    @Override
    public void close() throws IOException {
        reader.close();
    }

    protected PolygonOBJParser.Face readFace() throws IOException {
        String keyword;
        while (parser.nextKeyword()) {
            keyword = parser.getKeyword();

            switch (keyword) {
                case OBJConstants.VERTEX_KEYWORD:
                    handleVertex(parser.readVector());
                    break;
                case OBJConstants.VERTEX_NORMAL_KEYWORD:
                    handleNormal(parser.readVector());
                    break;
                case OBJConstants.FACE_KEYWORD:
                    return parser.readFace();
            }
        }

        return null;
    }

    protected abstract void handleVertex(final Vector3D vertex);

    protected abstract void handleNormal(final Vector3D normal);
}

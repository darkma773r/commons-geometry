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
package org.apache.commons.geometry.euclidean.io.threed.obj;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.geometry.core.io.internal.GeometryIOUtils;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.io.threed.AbstractBoundaryReadHandler3D;
import org.apache.commons.geometry.euclidean.io.threed.FacetDefinitionReader;
import org.apache.commons.geometry.euclidean.threed.mesh.TriangleMesh;

public class OBJBoundaryReadHandler3D extends AbstractBoundaryReadHandler3D {

    /** {@inheritDoc} */
    @Override
    public FacetDefinitionReader facetDefinitionReader(final InputStream in) throws IOException {
        final Reader reader = new BufferedReader(new InputStreamReader(in, OBJConstants.DEFAULT_CHARSET));
        return new OBJFacetDefinitionReader(reader);
    }

    /** {@inheritDoc} */
    @Override
    public TriangleMesh readTriangleMesh(final InputStream in, final DoublePrecisionContext precision)
            throws IOException {
        final Reader reader = GeometryIOUtils.createCloseShieldReader(in, OBJConstants.DEFAULT_CHARSET);
        try (OBJTriangleMeshReader meshReader = new OBJTriangleMeshReader(reader, precision)) {
            return meshReader.readTriangleMesh();
        }
    }
}

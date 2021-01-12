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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import org.apache.commons.geometry.core.io.internal.GeometryIOUtils;
import org.apache.commons.geometry.euclidean.io.threed.BoundaryWriteHandler3D;
import org.apache.commons.geometry.euclidean.io.threed.FacetDefinition;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;

public class OBJBoundaryWriteHandler3D implements BoundaryWriteHandler3D {

    private int meshBufferBatchSize = -1;

    /** {@inheritDoc} */
    @Override
    public void write(final BoundarySource3D src, final OutputStream out)
            throws IOException {
        try (OBJWriter writer = createOBJWriter(out)) {
            writer.writeBoundaries(src);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void writeFacets(final Collection<? extends FacetDefinition> facets, final OutputStream out)
            throws IOException {
        try (OBJWriter writer = createOBJWriter(out)) {
            final OBJWriter.MeshBuffer meshBuffer = writer.createMeshBuffer(meshBufferBatchSize);

            for (final FacetDefinition facet : facets) {
                meshBuffer.add(facet);
            }

            meshBuffer.flush();
        }
    }

    private OBJWriter createOBJWriter(final OutputStream out) {
        return new OBJWriter(GeometryIOUtils.createCloseShieldWriter(out, OBJConstants.DEFAULT_CHARSET));
    }
}

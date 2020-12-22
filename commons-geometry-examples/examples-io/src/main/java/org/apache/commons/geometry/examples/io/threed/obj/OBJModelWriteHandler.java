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
import java.io.OutputStream;
import java.io.Writer;
import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.examples.io.internal.IOUtils;
import org.apache.commons.geometry.examples.io.threed.ModelIOManager;
import org.apache.commons.geometry.examples.io.threed.facet.FacetDefinition;

public class OBJModelWriteHandler implements ModelIOManager.WriteHandler {

    /** {@inheritDoc} */
    @Override
    public void write(final BoundarySource3D model, final OutputStream out) throws IOException {
        final OBJWriter writer = createWriter(out);
        writer.writeBoundaries(model);
    }

    /** {@inheritDoc} */
    @Override
    public void writeFacets(final Stream<? extends FacetDefinition> facets, final OutputStream out)
            throws IOException {
        final OBJWriter writer = createWriter(out);
        final OBJWriter.MeshBuffer meshBuffer = writer.createMeshBuffer();

        final Iterator<? extends FacetDefinition> it = facets.iterator();
        while (it.hasNext()) {
            meshBuffer.add(it.next());
        }

        meshBuffer.flush();
    }

    /** {@inheritDoc} */
    @Override
    public void writeBoundaries(final Stream<? extends PlaneConvexSubset> boundaries, final OutputStream out)
            throws IOException {
        final OBJWriter writer = createWriter(out);
        final OBJWriter.MeshBuffer meshBuffer = writer.createMeshBuffer();

        final Iterator<? extends PlaneConvexSubset> it = boundaries.iterator();
        while (it.hasNext()) {
            meshBuffer.add(it.next());
        }

        meshBuffer.flush();
    }

    private OBJWriter createWriter(final OutputStream out) throws IOException {
        final Writer writer = IOUtils.createCloseShieldWriter(out, OBJConstants.DEFAULT_CHARSET);
        return new OBJWriter(writer);
    }
}

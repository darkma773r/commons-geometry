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
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.examples.io.internal.IOUtils;
import org.apache.commons.geometry.examples.io.threed.ModelIOManager;
import org.apache.commons.geometry.examples.io.threed.facet.FacetDefinition;

public class TextModelWriteHandler implements ModelIOManager.WriteHandler {

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /** {@inheritDoc} */
    @Override
    public void write(final BoundarySource3D model, final OutputStream out) throws IOException {
        try (Stream<PlaneConvexSubset> boundaries = model.boundaryStream()) {
            writeBoundaries(boundaries, out);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void writeFacets(final Stream<? extends FacetDefinition> facets, final OutputStream out)
            throws IOException {
        try (TextFacetDefinitionWriter writer = createWriter(out)) {
            final Iterator<? extends FacetDefinition> it = facets.iterator();
            while (it.hasNext()) {
                writer.write(it.next());
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void writeBoundaries(final Stream<? extends PlaneConvexSubset> boundaries, final OutputStream out)
            throws IOException {
        try (TextFacetDefinitionWriter writer = createWriter(out)) {
            final Iterator<? extends PlaneConvexSubset> it = boundaries.iterator();
            while (it.hasNext()) {
                writer.write(it.next());
            }
        }
    }

    private TextFacetDefinitionWriter createWriter(final OutputStream out) {
        final Writer writer = IOUtils.createCloseShieldWriter(out, DEFAULT_CHARSET);
        return createWriter(writer);
    }

    protected TextFacetDefinitionWriter createWriter(final Writer writer) {
        return new TextFacetDefinitionWriter(writer);
    }
}

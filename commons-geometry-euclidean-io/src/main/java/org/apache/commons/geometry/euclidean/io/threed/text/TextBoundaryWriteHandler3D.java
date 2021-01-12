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
package org.apache.commons.geometry.euclidean.io.threed.text;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Collection;

import org.apache.commons.geometry.core.io.internal.GeometryIOUtils;
import org.apache.commons.geometry.euclidean.io.threed.BoundaryWriteHandler3D;
import org.apache.commons.geometry.euclidean.io.threed.FacetDefinition;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;

public class TextBoundaryWriteHandler3D implements BoundaryWriteHandler3D {

    /** {@inheritDoc} */
    @Override
    public void write(final BoundarySource3D boundarySource, final OutputStream out)
            throws IOException {
        try (TextFacetDefinitionWriter writer = createWriter(out)) {
            writer.write(boundarySource);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void writeFacets(final Collection<? extends FacetDefinition> facets, final OutputStream out)
            throws IOException {
        try (TextFacetDefinitionWriter writer = createWriter(out)) {
            for (final FacetDefinition facet : facets) {
                writer.write(facet);
            }
        }
    }

    private TextFacetDefinitionWriter createWriter(final OutputStream out) throws IOException {
        return createWriter(GeometryIOUtils.createCloseShieldWriter(out, TextBoundaryReadHandler3D.DEFAULT_CHARSET));
    }

    protected TextFacetDefinitionWriter createWriter(final Writer writer) {
        return new TextFacetDefinitionWriter(writer);
    }
}

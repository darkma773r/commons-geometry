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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.examples.io.threed.AbstractModelIOHandler;
import org.apache.commons.geometry.examples.io.threed.ModelIO;
import org.apache.commons.geometry.examples.io.threed.facet.FacetDefinition;

public class TextModelIOHandler extends AbstractModelIOHandler {

    /** Charset for use with txt and csv files. */
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /** {@inheritDoc} */
    @Override
    public boolean handlesType(final String type) {
        return ModelIO.TXT.equalsIgnoreCase(type) ||
                ModelIO.CSV.equalsIgnoreCase(type);
    }

    /** {@inheritDoc} */
    @Override
    protected BoundarySource3D readInternal(final String type, final InputStream in,
            final DoublePrecisionContext precision) throws IOException {

        final TextFacetDefinitionReader fdReader = new TextFacetDefinitionReader(
                new InputStreamReader(in, DEFAULT_CHARSET));

        final List<PlaneConvexSubset> polys = new ArrayList<>();

        FacetDefinition facet;
        while ((facet = fdReader.readFacet()) != null) {
            polys.add(facet.toPolygon(precision));
        }

        return BoundarySource3D.from(polys);
    }

    /** {@inheritDoc} */
    @Override
    protected void writeInternal(final BoundarySource3D model, final String type, final OutputStream out)
            throws IOException {
        final Writer writer = new OutputStreamWriter(out, DEFAULT_CHARSET);
        final TextFacetDefinitionWriter fdWriter = ModelIO.CSV.equalsIgnoreCase(type) ?
                TextFacetDefinitionWriter.csvFormat(writer) :
                new TextFacetDefinitionWriter(writer);

        fdWriter.write(model);
    }
}

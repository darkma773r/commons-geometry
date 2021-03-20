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
package org.apache.commons.geometry.io.euclidean.threed.stl;

import java.io.IOException;
import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.io.core.GeometryFormat;
import org.apache.commons.geometry.io.core.output.GeometryOutput;
import org.apache.commons.geometry.io.euclidean.threed.AbstractBoundaryWriteHandler3D;
import org.apache.commons.geometry.io.euclidean.threed.FacetDefinition;
import org.apache.commons.geometry.io.euclidean.threed.GeometryFormat3D;

/** {@link org.apache.commons.geometry.io.euclidean.threed.BoundaryWriteHandler3D BoundaryWriteHandler3D}
 * implementation for writing STL content. Because of its compact nature, all STL content is written in
 * binary format, as opposed the text (i.e. "ASCII") format. Callers should use the {@link TextStlWriter}
 * class directly in order to create text STL content.
 */
public class StlBoundaryWriteHandler3D extends AbstractBoundaryWriteHandler3D {

    /** Default binary STL facet attribute value. */
    private static final int DEFAULT_ATTR = 0;

    /** {@inheritDoc} */
    @Override
    public GeometryFormat getFormat() {
        return GeometryFormat3D.STL;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final Stream<? extends PlaneConvexSubset> boundaries, GeometryOutput out)
            throws IOException {
        try (BinaryStlWriter writer = new BinaryStlWriter(out.getOutputStream())) {
            final Iterator<? extends PlaneConvexSubset> it = boundaries.iterator();

            while (it.hasNext()) {
                writer.write(it.next(), DEFAULT_ATTR);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void writeFacets(final Stream<? extends FacetDefinition> facets, GeometryOutput out)
            throws IOException {
        try (BinaryStlWriter writer = new BinaryStlWriter(out.getOutputStream())) {
            final Iterator<? extends FacetDefinition> it = facets.iterator();

            while (it.hasNext()) {
                writer.write(it.next(), DEFAULT_ATTR);
            }
        }
    }
}

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
package org.apache.commons.geometry.examples.io.threed;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.euclidean.threed.Triangle3D;
import org.apache.commons.geometry.euclidean.threed.mesh.SimpleTriangleMesh;
import org.apache.commons.geometry.euclidean.threed.mesh.TriangleMesh;
import org.apache.commons.geometry.examples.io.threed.facet.FacetDefinition;
import org.apache.commons.geometry.examples.io.threed.facet.FacetDefinitionReader;

public abstract class AbstractModelReadHandler implements ModelReadHandler {

    /** {@inheritDoc} */
    @Override
    public BoundarySource3D read(final InputStream in, final DoublePrecisionContext precision)
            throws IOException {
        final List<PlaneConvexSubset> list = new ArrayList<>();

        final FacetDefinitionReader reader = facetDefinitionReader(in);
        FacetDefinition facet;
        while ((facet = reader.readFacet()) != null) {
            list.add(facet.toPolygon(precision));
        }

        return BoundarySource3D.from(list);
    }

    /** {@inheritDoc} */
    @Override
    public TriangleMesh readTriangleMesh(final InputStream in, final DoublePrecisionContext precision)
            throws IOException {
        final SimpleTriangleMesh.Builder meshBuilder = SimpleTriangleMesh.builder(precision);

        final FacetDefinitionReader reader = facetDefinitionReader(in);
        FacetDefinition facet;
        while ((facet = reader.readFacet()) != null) {
            for (final Triangle3D tri : facet.toPolygon(precision).toTriangles()) {
                meshBuilder.addFaceUsingVertices(
                            tri.getPoint1(),
                            tri.getPoint2(),
                            tri.getPoint3()
                        );
            }
        }

        return meshBuilder.build();
    }
}

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
package org.apache.commons.geometry.examples.io.threed.facet;

import java.util.Collections;
import java.util.List;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.ConvexPolygon3D;
import org.apache.commons.geometry.euclidean.threed.Planes;
import org.apache.commons.geometry.euclidean.threed.Vector3D;

public class SimpleFacetDefinition implements FacetDefinition {

    private final List<Vector3D> vertices;

    private final Vector3D normal;

    public SimpleFacetDefinition(final List<Vector3D> vertices) {
        this(vertices, null);
    }

    public SimpleFacetDefinition(final List<Vector3D> vertices, final Vector3D normal) {
        if (vertices.size() < 3) {
            throw new IllegalArgumentException("Facet vertex list must contain at least 3 points; found " + vertices);
        }

        this.vertices = Collections.unmodifiableList(vertices);
        this.normal = normal;
    }

    /** {@inheritDoc} */
    @Override
    public List<Vector3D> getVertices() {
        return vertices;
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D getDefinedNormal() {
        return normal;
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D computeNormal() {
        List<Vector3D> verts = getVertices();

        Vector3D p1 = verts.get(0);
        Vector3D p2 = verts.get(1);
        Vector3D p3 = verts.get(2);

        return p1.vectorTo(p2)
                .cross(p1.vectorTo(p3))
                .normalize();
    }

    /** {@inheritDoc} */
    @Override
    public ConvexPolygon3D toPolygon(final DoublePrecisionContext precision)
    {
        final ConvexPolygon3D polygon = Planes.convexPolygonFromVertices(getVertices(), precision);

        // ensure that the polygon normal matches whatever normal was defined, if any
        final Vector3D definedNormal = getDefinedNormal();
        if (definedNormal != null &&
                definedNormal.dot(polygon.getPlane().getNormal()) < 0) {
            return polygon.reverse();
        }
        return polygon;
    }
}

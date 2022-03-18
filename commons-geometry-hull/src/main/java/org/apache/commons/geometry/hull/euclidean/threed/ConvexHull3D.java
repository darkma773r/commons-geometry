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
package org.apache.commons.geometry.hull.euclidean.threed;

import java.util.List;

import org.apache.commons.geometry.euclidean.threed.ConvexVolume;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.hull.ConvexHull;

/** Class representing a convex hull in 3D Euclidean space.
 */
public final class ConvexHull3D implements ConvexHull<Vector3D> {

    /** List of vertices on the convex hull. */
    private final List<Vector3D> vertices;

    /** Region representing the convex hull; may be null */
    private final ConvexVolume region;

    ConvexHull3D(final List<Vector3D> vertices, final ConvexVolume region) {
        this.vertices = vertices;
        this.region = region;
    }

    /** {@inheritDoc} */
    @Override
    public List<Vector3D> getVertices() {
        return vertices;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasSize() {
        return region != null;
    }

    /** {@inheritDoc} */
    @Override
    public ConvexVolume getRegion() {
        return region;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName())
            .append("[vertices= ")
            .append(getVertices())
            .append(", region= ")
            .append(getRegion())
            .append(']');

        return sb.toString();
    }
}

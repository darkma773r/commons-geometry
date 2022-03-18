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
package org.apache.commons.geometry.hull;

import java.util.List;

import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.Region;

/**
 * This class represents a convex hull.
 *
 * @param <P> Point implementation type.
 */
public interface ConvexHull<P extends Point<P>> {

    /** Get the vertices of the convex hull.
     * @return vertices of the convex hull
     */
    List<P> getVertices();

    /** Return true if the convex hull region has non-zero size. For example,
     * this will be {@code false} if the convex hull input consists of a single
     * point.
     * @return true if the convex hull has non-zero size
     */
    boolean hasSize();

    /** Return the region representing the convex hull. This will return
     * null in cases where the hull does not define a region with non-zero
     * size, such as when only a single unique point exists or when all points
     * are collinear.
     * @return the region representing by the convex hull or null if the
     *      convex hull does not define a region of non-zero size
     * @see #hasSize()
     */
    Region<P> getRegion();
}

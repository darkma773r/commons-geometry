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
package org.apache.commons.geometry.core.collection;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.geometry.core.Point;

/** {@link Map} type that uses points as keys. This interface is intended for
 * use in cases where effectively equivalent (but not necessarily equal) points must
 * map to the same entry. As such, this interface breaks the strict contract for
 * {@link Map} where key equality is consistent with {@link Object#equals(Object)}.
 * @param <P> Point type
 * @param <V> Value type
 */
public interface PointMap<P extends Point<P>, V> extends Map<P, V> {

    /** Get the map entry with a key equivalent to {@code pt} or {@code null}
     * if no such entry exists. The returned instance supports use of
     * the {@link Entry#setValue(Object)} method to modify the
     * mapping.
     * @param pt point to fetch the map entry for
     * @return map entry for the given point or null if no such entry
     *      exists
     */
    Entry<P, V> getEntry(P pt);

    Entry<P, V> nearestEntry(P pt);

    Entry<P, V> farthestEntry(P pt);

    Collection<Entry<P, V>> entriesNearToFar(P pt);

    Collection<Entry<P, V>> entriesFarToNear(P pt);

    Stream<Entry<P, V>> neighborEntries(P pt, double maxDist);
}

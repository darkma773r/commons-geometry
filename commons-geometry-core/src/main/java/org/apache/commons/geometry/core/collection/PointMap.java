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

import java.util.Iterator;
import java.util.Map;

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
     * the {@link Map.Entry#setValue(Object)} method to modify the
     * mapping.
     * @param pt point to fetch the map entry for
     * @return map entry for the given point or null if no such entry
     *      exists
     */
    Map.Entry<P, V> getEntry(P pt);

    /** Return an {@link Iterable} providing access to map entries in ascending order
     * of distance from {@code pt}. No ordering is guaranteed for entries that are
     * exactly the same distance from {@code pt}.
     * @param pt reference point
     * @return iterable providing access to map entries in ascending order of
     *      distance from {@code pt}
     */
    Iterable<Map.Entry<P, V>> entriesNearToFar(P pt);

    /** Return an entry from the map such that no entry is nearer to {@code pt}.
     * Distance is measured from {@code pt} to the {@link Map.Entry#getKey() key} of each
     * entry. If multiple entries are the exact same distance from {@code pt}, implementations
     * must choose which to return based on whatever criteria is convenient. Callers
     * should not rely on this tie-breaking behavior. Null is returned if the map
     * is empty.
     * @param pt reference point
     * @return map entry such that no entry is nearer to {@code pt}, or {@code null} if
     *      the map is empty
     */
    default Map.Entry<P, V> nearestEntry(final P pt) {
        final Iterator<Map.Entry<P, V>> it = entriesNearToFar(pt).iterator();
        return it.hasNext() ?
                it.next() :
                null;
    }

    /** Return an entry from the map such that no entry is nearer to {@code pt} and the
     * entry satisfies the condition {@code entry.getKey().distance(pt) <= dist} (using
     * standard floating point comparisons). Null is returned if no such entry exists.
     * @param pt reference point
     * @param radius maximum distance from {@code pt}
     * @return a map entry such that no entry is nearer to {@code pt} and the distance
     *      from the entry's key to {@code pt} is less than or equal to {@code dist}
     */
    default Map.Entry<P, V> nearestEntryWithinRadius(final P pt, final double radius) {
        final Map.Entry<P, V> closest = nearestEntry(pt);
        return closest != null && closest.getKey().distance(pt) <= radius ?
                closest :
                null;
    }

    /** Return an {@link Iterable} providing access to map entries in descending order
     * of distance from {@code pt}. No ordering is guaranteed for entries that are
     * exactly the same distance from {@code pt}.
     * @param pt reference point
     * @return iterable providing access to map entries in descending order of
     *      distance from {@code pt}
     */
    Iterable<Map.Entry<P, V>> entriesFarToNear(P pt);

    /** Return an entry from the map such that no entry is farther from {@code pt}.
     * Distance is measured from {@code pt} to the {@link Map.Entry#getKey() key} of each
     * entry. If multiple entries are the exact same distance from {@code pt}, implementations
     * must choose which to return based on whatever criteria is convenient. Callers
     * should not rely on this tie-breaking behavior. Null is returned if the map
     * is empty.
     * @param pt reference point
     * @return map entry such that no entry is farther than {@code pt}, or {@code null} if
     *      the map is empty
     */
    default Map.Entry<P, V> farthestEntry(final P pt) {
        final Iterator<Map.Entry<P, V>> it = entriesFarToNear(pt).iterator();
        return it.hasNext() ?
                it.next() :
                null;
    }
}

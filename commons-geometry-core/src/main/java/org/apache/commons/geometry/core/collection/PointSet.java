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

import java.util.Set;

import org.apache.commons.geometry.core.Point;

/** {@link Set} containing {@link Point} values. This interface is intended for
 * use in cases where effectively equivalent (but not necessarily equal) points must
 * be considered as equal by the set. As such, this interface breaks the strict contract
 * for {@link Set} where membership is consistent with {@link Object#equals(Object)}.
 * @param <P> Point type
 */
public interface PointSet<P extends Point<P>> extends Set<P> {

    /** Get the element equivalent to {@code pt} or null if no
     * such an element exists.
     * @param pt point to find an equivalent for
     * @return set entry equivalent to {@code pt} or null if
     *      no such entry exists
     */
    P get(P pt);

    /** Return an {@link Iterable} providing access to set entries, with those
     * entries closest to {@code pt} returned first. If two or more entries are
     * exactly the same distance from {@code pt}. they may be returned in any order.
     * @param pt reference point
     * @return iterable providing access to set entries in ascending order of
     *      distance from {@code pt}
     */
    Iterable<P> closestFirst(P pt);

    /** Return an element from the map such that no element is closer to {@code pt}.
     * If multiple elements are the exact same distance from {@code pt}, implementations
     * must choose which to return based on whatever criteria is convenient. Callers
     * should not rely on this tie-breaking behavior. Null is returned if the set
     * is empty.
     * @param pt reference point
     * @return an element such that no element is closer to {@code pt}, or {@code null} if
     *      the set is empty
     */
    P closest(P pt);

    /** Return an element from the set such that no element is closer to {@code pt} and
     * the element satisfies the condition {@code element.distance(pt) <= dist} (using
     * standard floating point comparisons). Null is returned if no such element exists.
     * @param pt reference point
     * @param dist maximum distance from {@code pt}
     * @return an element such that no element is closer to {@code pt} and the distance
     *      from the element to {@code pt} is less than or equal to {@code dist}
     */
    P closestWithinDistance(P pt, double dist);

    /** Return an {@link Iterable} providing access to set entries, with those
     * entries farthest from {@code pt} returned first. If two or more entries are
     * exactly the same distance from {@code pt}, they may be returned in any order.
     * @param pt reference point
     * @return iterable providing access to set entries in descending order of
     *      distance from {@code pt}
     */
    Iterable<P> farthestFirst(P pt);

    /** Return an element from the set such that no element is farther from {@code pt}.
     * If multiple elements are the exact same distance from {@code pt}, implementations
     * must choose which to return based on whatever criteria is convenient. Callers
     * should not rely on this tie-breaking behavior. Null is returned if the set
     * is empty.
     * @param pt reference point
     * @return an element such that no element is farther than {@code pt}, or {@code null} if
     *      the set is empty
     */
    P farthest(P pt);
}

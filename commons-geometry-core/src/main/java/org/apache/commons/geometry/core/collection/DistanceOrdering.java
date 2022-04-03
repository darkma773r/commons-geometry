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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Interface representing a view of elements ordered by distance from
 * some externally defined reference point. In cases where the ordering
 * is ambiguous due to multiple elements having the exact same distance
 * from the reference point, implementations are expected to provide a
 * secondary sorting based on whatever criteria is convenient for the
 * implementation. This is done to ensure that the ordering remains
 * consistent in all cases. However, this secondary ordering behavior
 * is not part of the contract for this interface and should not be relied
 * on by callers. For example, consider a view containing the elements
 * {@code x, y, z} with distance values {@code 1, 2, 1} respectively.
 * The call {@code view.farthest()} clearly should return {@code y}, but
 * the call {@code view.nearest()} is ambiguous since both {@code x} and
 * {@code z} have a distance of {@code 1}. In this case, implementations are
 * required to apply another sorting criteria to resolve the ambiguity. Say
 * that the example implementation stores the elements in a list and decides
 * to use the list index as its secondary sort criteria. Then, the call
 * {@code view.nearest()} would return {@code x}. Similarly, {@code view.nearToFar()}
 * would return {@code [x, z, y]} and {@code view.farToNear()} would return
 * {@code [y, z, x]}. As long as the ordering is internally consistent, the
 * choice of secondary sorting criteria is left to the implementations.
 * @param <T> Element type
 */
public interface DistanceOrdering<T> {

    /** Get an element from the view such that no other element lies nearer
     * to the reference point (i.e. has a smaller distance value). If no
     * such element exists, such as when the view contains no elements,
     * then {@code null} is returned. In ambiguous cases where multiple
     * elements lie at the same minimum distance, implementations are free
     * to choose whatever tie-breaking criteria is convenient to determine the
     * element returned by this method.
     * @return element from the view such that no other element lies nearer
     *      to the reference point, or {@code null} if no such element exists
     */
    T nearest();

    /** Get a list containing the {@code k} elements nearest to the
     * reference point (i.e. with the smallest distance values). If fewer
     * than {@code k} elements exist, the returned list contains as many elements
     * as can be found.
     * @param k maximum number of elements to include in the list
     * @return list containing the {@code n} elements nearest to the
     *      reference point, where {@code n >= 0} and {@code n <= k}
     * @throws IllegalArgumentException if {@code k} is less than {@code 0}
     */
    default List<T> kNearest(final int k) {
        final List<T> result = new ArrayList<>(k);

        final Iterator<T> it = nearToFar().iterator();
        int i = 0;
        while (i++ < k && it.hasNext()) {
            result.add(it.next());
        }

        return result;
    }

    /** Get an element from the view such that no other element lies farther
     * from the reference point (i.e. has a larger distance value). If no
     * such element exists, such as when the view contains no elements,
     * then {@code null} is returned. In ambiguous cases where multiple
     * elements lie at the same maximum distance, implementations are free
     * to choose whatever tie-breaking criteria is convenient to determine the
     * element returned by this method.
     * @return element from the view such that no other element lies farther
     *      from the reference point, or {@code null} if no such element exists
     */
    T farthest();

    /** Get a list containing the {@code k} elements farthest from the
     * reference point (i.e. with the largest distance values). If fewer
     * than {@code k} elements exist, the returned list contains as many elements
     * as can be found.
     * @param k maximum number of elements to include in the list
     * @return list containing the {@code n} elements farthest from the
     *      reference point, where {@code n >= 0} and {@code n <= k}
     * @throws IllegalArgumentException if {@code k} is less than {@code 0}
     */
    default List<T> kFarthest(final int k) {
        final List<T> result = new ArrayList<>(k);

        final Iterator<T> it = farToNear().iterator();
        int i = 0;
        while (i++ < k && it.hasNext()) {
            result.add(it.next());
        }

        return result;
    }

    /** Return the elements in this view in order of increasing distance
     * from the reference point, i.e. with near elements coming first and
     * followed by farther elements. In ambiguous cases where multiple
     * elements lie at the same distance, implementations are free to choose
     * whatever tie-breaking criteria is convenient to determine the
     * element ordering.
     * @return elements in order of increasing distance
     */
    StreamingIterable<T> nearToFar();

    /** Return the elements in this view in order of decreasing distance
     * from the reference point, i.e. with far elements coming first and
     * followed by nearer elements. In ambiguous cases where multiple
     * elements lie at the same distance, implementations are free to choose
     * whatever tie-breaking criteria is convenient to determine the
     * element ordering.
     * @return elements in order of decreasing distance
     */
    StreamingIterable<T> farToNear();
}

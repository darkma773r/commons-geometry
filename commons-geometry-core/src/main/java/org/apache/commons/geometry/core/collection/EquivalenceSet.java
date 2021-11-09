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

/** A {@link Set} that uses the concept of "equivalence" or "fuzzy comparison"
 * instead of strict equality when comparing values. If two values are
 * evaluated as being equivalent using the internal comparison function, then
 * they are considered equal from the point of view of the set. This allows the set
 * to be used in situations where objects are frequently close but not exactly equal
 * to each other, as in the case of values subject to floating point errors.
 *
 * <p>It is important to note that the "fuzzy comparison" described above may cause
 * instances of this type to break the contract of the Set interface. This is due to
 * the fact that the Set interface is defined in terms of the
 * {@link Object#equals(Object) equals} method, which is not used by this interface.
 * In general, the comparison method used here is <em>inconsistent with equals</em>,
 * meaning the fact that values {@code x} and {@code y} are evaluated as equal by
 * the set does <em>not</em> imply that {@code x.equals(y)}.
 *
 * <p>Additionally, the internal comparison method cannot be guaranteed to be transitive.
 * This means the fact that {@code x} and {@code y} are considered equal and {@code y}
 * and {@code z} are considered equal does <em>not</em> imply that {@code x} and {@code z}
 * will be considered equal. For example, consider the values {@code x = 1}, {@code y = 1.075},
 * and {@code z = 1.15} and a comparison method using an epsilon value of {@code 0.1}.
 * Using this method, {@code x} and {@code y} are equivalent and {@code y} and {@code z} are
 * equivalent,  but {@code x} and {@code z} are not equivalent since their difference exceeds
 * the epsilon. If these values are inserted into the set, the set state depends on the order
 * of insertion. Inserting the values in the order {@code x, y, z} results in two entries:
 * {@code x, z}. However, inserting in the order {@code y, x, z} results in only one entry
 * ({@code y}) because both {@code x} and {@code z} are considered equal to {@code y}.
 *
 * @param <T> Value type
 */
public interface EquivalenceSet<T> extends Set<T> {

    /** Get the stored value considered equivalent to the argument or null if
     * no such value exists.
     * @param t value
     * @return stored value considered equivalent to the argument or null if
     *      not found
     */
    T getStored(T t);
}

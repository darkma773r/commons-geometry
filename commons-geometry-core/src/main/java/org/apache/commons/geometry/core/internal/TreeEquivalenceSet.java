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
package org.apache.commons.geometry.core.internal;

import java.util.Comparator;
import java.util.TreeSet;

import org.apache.commons.geometry.core.collection.EquivalenceSet;

/** Extension of {@link TreeSet} that fulfills the {@link EquivalenceSet}
 * interface.
 * @param <T> Value type
 */
public class TreeEquivalenceSet<T> extends TreeSet<T>
    implements EquivalenceSet<T> {

    /** Serializable UID. */
    private static final long serialVersionUID = 20211009L;

    /** Construct a new instance with the given comparison function. The
     * argument is not required to be consistent with equals or transitive.
     * @param comparator comparison function
     */
    public TreeEquivalenceSet(final Comparator<T> comparator) {
        super(comparator);
    }

    /** {@inheritDoc} */
    @Override
    public T resolve(final T t) {
        T val = floor(t);
        if (val != null && comparator().compare(val, t) == 0) {
            return val;
        }
        return null;
    }

}

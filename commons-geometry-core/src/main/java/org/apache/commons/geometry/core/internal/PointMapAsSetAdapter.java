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

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.collection.DistanceOrdering;
import org.apache.commons.geometry.core.collection.PointMap;
import org.apache.commons.geometry.core.collection.PointSet;
import org.apache.commons.geometry.core.collection.StreamingIterable;

/** Internal utility class that exposes a {@link PointMap} as a {@link PointSet}.
 * This class is not intended for direct use by users of this library. Users should
 * instead create {@link PointSet} instances using the factory methods available in
 * each space.
 * @param <P> Point type
 * @param <M> Map type
 */
public class PointMapAsSetAdapter<P extends Point<P>, M extends PointMap<P, Object>>
    extends AbstractSet<P>
    implements PointSet<P> {

    /** Dummy map value used to indicate presence in the set. */
    private static final Object PRESENT = new Object();

    /** Backing map. */
    private final M map;

    /** Construct a new instance that use the argument as its backing map.
     * @param backingMap backing map
     */
    public PointMapAsSetAdapter(final M backingMap) {
        this.map = backingMap;
    }

    /** {@inheritDoc} */
    @Override
    public P get(final P pt) {
        return getKey(map.getEntry(pt));
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<P> iterator() {
        return map.keySet().iterator();
    }

    /** {@inheritDoc} */
    @Override
    public DistanceOrdering<P> from(final P pt) {
        return new EntryDistanceOrderingWrapper<>(map.entriesFrom(pt));
    }

    /** {@inheritDoc} */
    @Override
    public DistanceOrdering<P> withinRadius(final P pt, final double maxDist) {
        return new EntryDistanceOrderingWrapper<>(map.entriesWithinRadius(pt, maxDist));
    }

    /** {@inheritDoc} */
    @Override
    public int size() {
        return map.size();
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(final Object obj) {
        return map.containsKey(obj);
    }

    /** {@inheritDoc} */
    @Override
    public boolean add(final P pt) {
        return map.put(pt, PRESENT) == null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean remove(final Object obj) {
        final Object prev = map.remove(obj);
        return GeometryInternalUtils.sameInstance(prev, PRESENT);
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        map.clear();
    }

    /** Get the entry key or {@code null} if {@code entry} is {@code null}.
     * @param <P> Point type
     * @param entry map entry
     * @return entry key or {@code null} if {@code entry} is {@code null}
     */
    private static <P extends Point<P>> P getKey(final Entry<P, ?> entry) {
        return entry != null ?
                entry.getKey() :
                null;
    }

    /** Class that wraps a {@link DistanceOrdering} view from a map and converts
     * it to return only the map keys.
     * @param <P> Point type
     */
    private static final class EntryDistanceOrderingWrapper<P extends Point<P>>
        implements DistanceOrdering<P> {

        /** Underlying map entry view. */
        private final DistanceOrdering<Entry<P, Object>> ordering;

        /** Construct a new instance that wraps the given map entry view.
         * @param ordering map entry view
         */
        EntryDistanceOrderingWrapper(final DistanceOrdering<Entry<P, Object>> ordering) {
            this.ordering = ordering;
        }

        /** {@inheritDoc} */
        @Override
        public P nearest() {
            return getKey(ordering.nearest());
        }

        /** {@inheritDoc} */
        @Override
        public P farthest() {
            return getKey(ordering.farthest());
        }

        /** {@inheritDoc} */
        @Override
        public StreamingIterable<P> nearToFar() {
            return () -> new EntryIteratorWrapper<>(ordering.nearToFar().iterator());
        }

        /** {@inheritDoc} */
        @Override
        public StreamingIterable<P> farToNear() {
            return () -> new EntryIteratorWrapper<>(ordering.farToNear().iterator());
        }

        /** Get the key for the given entry or {@code null} if the entry is null.
         * @param entry entry to get the key for
         * @return the key for the given entry or {@code null} if the entry is null
         */
        private P getKey(final Entry<P, ?> entry) {
            return entry != null ?
                    entry.getKey() :
                    null;
        }
    }

    /** Iterator that converts from a map entry iterator to a key iteration.
     * @param <P> Point type
     */
    private static final class EntryIteratorWrapper<P extends Point<P>>
        implements Iterator<P> {

        /** Underlying entry iterator. */
        private final Iterator<Entry<P, Object>> entryIterator;

        /** Construct a new instance wrapping the given entry iterator.
         * @param entryIterator map entry iterator
         */
        EntryIteratorWrapper(final Iterator<Entry<P, Object>> entryIterator) {
            this.entryIterator = entryIterator;
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasNext() {
            return entryIterator.hasNext();
        }

        /** {@inheritDoc} */
        @Override
        public P next() {
            return entryIterator.next().getKey();
        }
    }
}

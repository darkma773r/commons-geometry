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

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.function.ToDoubleFunction;

import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.collection.DistanceOrdering;
import org.apache.commons.geometry.core.collection.PointMap;
import org.apache.commons.geometry.core.collection.StreamingIterable;
import org.apache.commons.numbers.core.Precision;

/** Abstract base class for 1D {@link PointMap} implementations. This class delegates
 * entry storage to an internal {@link TreeMap} instance. Simple methods, such as
 * {@link Map#size()} are directly implemented here but subclasses must provide their
 * own logic for manipulating the map entries.
 * @param <P> Point type
 * @param <V> Value type
 */
public abstract class AbstractPointMap1D<P extends Point<P>, V>
    implements PointMap<P, V> {

    /** Precision context. */
    private final Precision.DoubleEquivalence precision;

    /** Underlying map. */
    private final NavigableMap<P, V> map;

    /** Construct a new instance that uses the given precision and coordinate accessor
     * function to sort elements.
     * @param precision precision object used for floating point comparisons
     * @param coordinateFn function used to obtain coordinate values from point instance
     */
    protected AbstractPointMap1D(
            final Precision.DoubleEquivalence precision,
            final ToDoubleFunction<P> coordinateFn) {
        this.precision = precision;
        this.map = new TreeMap<>(
                (a, b) -> precision.compare(coordinateFn.applyAsDouble(a), coordinateFn.applyAsDouble(b)));
    }

    /** {@inheritDoc} */
    @Override
    public int size() {
        return map.size();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsValue(final Object value) {
        return map.containsValue(value);
    }


    /** {@inheritDoc} */
    @Override
    public Entry<P, V> getEntry(final P key) {
        return exportEntry(getEntryInternal(key));
    }

    /** {@inheritDoc} */
    @Override
    public V put(final P key, final V value) {
        GeometryInternalUtils.requireFinite(key);
        return putInternal(key, value);
    }

    /** {@inheritDoc} */
    @Override
    public void putAll(final Map<? extends P, ? extends V> m) {
        for (final Entry<? extends P, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    /** {@inheritDoc} */
    @Override
    public Collection<V> values() {
        return map.values();
    }

    /** {@inheritDoc} */
    @Override
    public DistanceOrdering<Entry<P, V>> entriesFrom(final P pt) {
        return entriesWithinRadius(pt, Double.POSITIVE_INFINITY);
    }

    /** {@inheritDoc} */
    @Override
    public DistanceOrdering<Entry<P, V>> entriesWithinRadius(final P pt, final double radius) {
        return new DistanceOrderingImpl(pt, radius);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return map.hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        return map.equals(obj);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return map.toString();
    }

    /** Get the raw {@link Map.Entry} for the given key from the underlying map.
     * @param key map key
     * @return entry for the given key or {@code null} if not found
     */
    protected abstract Map.Entry<P, V> getEntryInternal(P key);

    /** Add or update the entry for the given key/value pair.
     * @param key entry key
     * @param value entry value
     * @return the value of the previous entry for {@code key} or null
     *      if no such entry exists
     */
    protected abstract V putInternal(P key, V value);

    /** Get the underlying map instance.
     * @return map instance
     */
    protected NavigableMap<P, V> getMap() {
        return map;
    }

    /** Return a {@link Map.Entry} instance containing the values from the
     * argument and suitable for direct use by external users. The returned
     * entry supports use of the {@link Map.Entry#setValue(Object)} method.
     * @param entry entry to export
     * @return entry instance suitable for direct user by callers
     */
    protected Entry<P, V> exportEntry(final Entry<P, V> entry) {
        return entry != null ?
                new MutableEntryWrapper(entry) :
                null;
    }

    /** Get the configured precision for the instance.
     * @return precision object
     */
    protected Precision.DoubleEquivalence getPrecision() {
        return precision;
    }

    /** Get an iterator for accessing map entries in order of nearest to farthest
     * from {@code pt}.
     * @param pt reference point
     * @return iterator for accessing map entries in order of nearest to farthest
     * f        from {@code pt}.
     */
    protected abstract Iterator<DistancedValue<Entry<P, V>>> nearToFarIterator(P pt);

    /** Get an iterator for accessing map entries in order of farthest to nearest
     * from {@code pt}.
     * @param pt reference point
     * @return iterator for accessing map entries in order of farthest to nearest
     * f        from {@code pt}.
     */
    protected abstract Iterator<DistancedValue<Entry<P, V>>> farToNearIterator(P pt);

    /** Internal implementation of {@link DistanceOrdering}.
     */
    private class DistanceOrderingImpl implements DistanceOrdering<Entry<P, V>> {

        /** Reference point. */
        private final P refPt;

        /** Maximum distance. */
        private final double maxDist;

        /** Construct a new ordering instance.
         * @param refPt reference point
         * @param maxDist maximum distance
         * @throws NullPointerException if {@code refPt} is null
         * @throws IllegalArgumentException if {@code refPt} is not finite
         */
        DistanceOrderingImpl(final P refPt, final double maxDist) {
            this.refPt = GeometryInternalUtils.requireFinite(refPt);
            this.maxDist = maxDist;
        }

        /** {@inheritDoc} */
        @Override
        public Entry<P, V> nearest() {
            final Iterator<DistancedValue<Entry<P, V>>> it = nearToFarIterator(refPt);
            return it.hasNext() ?
                    it.next().getValue() :
                    null;
        }

        /** {@inheritDoc} */
        @Override
        public Entry<P, V> farthest() {
            final Iterator<DistancedValue<Entry<P, V>>> it = farToNearIterator(refPt);
            return it.hasNext() ?
                    it.next().getValue() :
                    null;
        }

        /** {@inheritDoc} */
        @Override
        public StreamingIterable<Entry<P, V>> nearToFar() {
            return () -> new DistanceOrderIterator(nearToFarIterator(refPt), maxDist);
        }

        /** {@inheritDoc} */
        @Override
        public StreamingIterable<Entry<P, V>> farToNear() {
            return () -> new DistanceOrderIterator(farToNearIterator(refPt), maxDist);
        }
    }

    /** {@link Map.Entry} subclass that adds support for the {@link Map.Entry#setValue(Object)}.
     */
    private final class MutableEntryWrapper extends SimpleEntry<P, V> {

        /** Serializable UID. */
        private static final long serialVersionUID = 20220317L;

        /** Construct a new instance representing the same mapping as the argument.
         * @param entry target entry
         */
        MutableEntryWrapper(final Entry<? extends P, ? extends V> entry) {
            super(entry);
        }

        /** {@inheritDoc} */
        @Override
        public V setValue(final V value) {
            // replace the value in the map
            map.replace(getKey(), value);

            // set the local value
            return super.setValue(value);
        }
    }

    private final class DistanceOrderIterator
        implements Iterator<Entry<P, V>> {

        private final Iterator<DistancedValue<Entry<P, V>>> iterator;

        private final double maxDist;

        private DistancedValue<Entry<P, V>> nextEntry;

        DistanceOrderIterator(
                final Iterator<DistancedValue<Entry<P, V>>> iterator,
                final double dist) {
            this.iterator = iterator;
            this.maxDist = dist;

            queueNext();
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasNext() {
            return nextEntry != null;
        }

        /** {@inheritDoc} */
        @Override
        public Entry<P, V> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            final Entry<P, V> result = nextEntry.getValue();
            nextEntry = null;

            queueNext();

            return result;
        }

        private void queueNext() {
            if (iterator.hasNext()) {
                // continue getting entries until we pass the maximum distance
                final DistancedValue<Entry<P, V>> entry = iterator.next();
                if (getPrecision().lte(entry.getDistance(), maxDist)) {
                    nextEntry = entry;
                }
            }
        }
    }
}

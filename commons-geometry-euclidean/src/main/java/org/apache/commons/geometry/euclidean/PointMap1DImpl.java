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
package org.apache.commons.geometry.euclidean;

import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.geometry.core.internal.AbstractPointMap1D;
import org.apache.commons.geometry.core.internal.DistancedValue;
import org.apache.commons.geometry.core.internal.GeometryInternalUtils;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.numbers.core.Precision;

/** Internal {@link org.apache.commons.geometry.core.collection.PointMap PointMap}
 * implementation for Euclidean 1D space.
 * @param <V> Map value type
 */
final class PointMap1DImpl<V>
    extends AbstractPointMap1D<Vector1D, V> {

    /** Construct a new instance using the given precision context to determine
     * floating point equality.
     * @param precision precision context
     */
    PointMap1DImpl(final Precision.DoubleEquivalence precision) {
        super((a, b) -> precision.compare(a.getX(), b.getX()));
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsKey(final Object key) {
        return getMap().containsKey(key);
    }

    /** {@inheritDoc} */
    @Override
    public V get(final Object key) {
        return getMap().get(key);
    }

    /** {@inheritDoc} */
    @Override
    public V remove(final Object key) {
        return getMap().remove(key);
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        getMap().clear();
    }

    /** {@inheritDoc} */
    @Override
    public Set<Vector1D> keySet() {
        return getMap().keySet();
    }

    /** {@inheritDoc} */
    @Override
    public Set<Entry<Vector1D, V>> entrySet() {
        return getMap().entrySet();
    }

    /** {@inheritDoc} */
    @Override
    public Iterable<Entry<Vector1D, V>> entriesNearToFar(final Vector1D pt) {
        GeometryInternalUtils.requireFinite(pt);
        return () -> new ClosestFirstIterator(pt);
    }

    /** {@inheritDoc} */
    @Override
    public Iterable<Entry<Vector1D, V>> entriesFarToNear(final Vector1D pt) {
        GeometryInternalUtils.requireFinite(pt);
        return () -> new FarthestFirstIterator(pt);
    }

    /** {@inheritDoc} */
    @Override
    protected Map.Entry<Vector1D, V> getEntryInternal(final Vector1D key) {
        final NavigableMap<Vector1D, V> map = getMap();

        final Map.Entry<Vector1D, V> floor = map.floorEntry(key);
        if (floor != null &&
                map.comparator().compare(floor.getKey(), key) == 0) {
            return floor;
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected V putInternal(final Vector1D key, final V value) {
        return getMap().put(key, value);
    }

    /** Iterator that returns the entries in order of ascending distance from
     * a given reference point.
     */
    private final class ClosestFirstIterator
        implements Iterator<Map.Entry<Vector1D, V>> {

        /** Reference point to measure distances against. */
        private final Vector1D refPt;

        /** Low entry iterator. */
        private final Iterator<Map.Entry<Vector1D, V>> low;

        /** High entry iterator. */
        private final Iterator<Map.Entry<Vector1D, V>> high;

        /** Low-valued entry. */
        private DistancedValue<Map.Entry<Vector1D, V>> lowEntry;

        /** High-valued entry. */
        private DistancedValue<Map.Entry<Vector1D, V>> highEntry;

        /** Construct a new instance with the given reference point.
         * @param refPt reference point
         */
        ClosestFirstIterator(final Vector1D refPt) {
            this.refPt = refPt;

            this.low = getMap().descendingMap().tailMap(refPt, false)
                    .entrySet().iterator();
            this.high = getMap().tailMap(refPt).entrySet().iterator();
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasNext() {
            if (lowEntry == null) {
                lowEntry = getNextEntry(low);
            }
            if (highEntry == null) {
                highEntry = getNextEntry(high);
            }

            return lowEntry != null || highEntry != null;
        }

        /** {@inheritDoc} */
        @Override
        public Entry<Vector1D, V> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            final Entry<Vector1D, V> result;
            if (lowEntry != null &&
                    (highEntry == null || lowEntry.getDistance() <= highEntry.getDistance())) {
                result = lowEntry.getValue();
                lowEntry = null;
            } else {
                result = highEntry.getValue();
                highEntry = null;
            }

            return result;
        }

        /** Get a {@link DistancedValue} instance containing the next entry from the given
         * iterator.
         * @param it iterator to get the next value from
         * @return distanced value containing the next entry from the iterator or null if the iterator
         *      does not contain any more elements
         */
        private DistancedValue<Entry<Vector1D, V>> getNextEntry(final Iterator<Entry<Vector1D, V>> it) {
            if (it.hasNext()) {
                final Entry<Vector1D, V> entry = it.next();
                return DistancedValue.of(entry, refPt.distance(entry.getKey()));
            }
            return null;
        }
    }

    /** Iterator that returns the entries in order of descending distance from
     * a given reference point.
     */
    private final class FarthestFirstIterator
        implements Iterator<Map.Entry<Vector1D, V>> {

        /** Reference point to measure distances against. */
        private final Vector1D refPt;

        /** Low entry iterator. */
        private Iterator<Map.Entry<Vector1D, V>> low;

        /** High entry iterator. */
        private Iterator<Map.Entry<Vector1D, V>> high;

        /** Low-valued entry. */
        private DistancedValue<Map.Entry<Vector1D, V>> lowEntry;

        /** High-valued entry. */
        private DistancedValue<Map.Entry<Vector1D, V>> highEntry;

        /** The last value returned by the low iterator. */
        private double lastLowValue = Double.NEGATIVE_INFINITY;

        /** The last value returned by the high iterator. */
        private double lastHighValue = Double.POSITIVE_INFINITY;

        /** Construct a new instance that measures distances against the given
         * reference point.
         * @param refPt reference point
         */
        FarthestFirstIterator(final Vector1D refPt) {
            this.refPt = refPt;

            this.low = getMap().entrySet().iterator();
            this.high = getMap().descendingMap().entrySet().iterator();
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasNext() {
            if (lowEntry == null && low != null && low.hasNext()) {
                final Entry<Vector1D, V> entry = low.next();
                lastLowValue = entry.getKey().getX();

                if (entry.getKey().getX() >= lastHighValue) {
                    // we've crossed over the value returned by the high iterator
                    low = null;
                } else {
                    lowEntry = DistancedValue.of(entry, refPt.distance(entry.getKey()));
                }
            }
            if (highEntry == null && high != null && high.hasNext()) {
                final Entry<Vector1D, V> entry = high.next();
                lastHighValue = entry.getKey().getX();

                if (entry.getKey().getX() <= lastLowValue) {
                    // we've crossed over the values returned by the low iterator
                    high = null;
                } else {
                    highEntry = DistancedValue.of(entry, refPt.distance(entry.getKey()));
                }
            }

            return lowEntry != null || highEntry != null;
        }

        /** {@inheritDoc} */
        @Override
        public Entry<Vector1D, V> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            final Entry<Vector1D, V> result;
            if (lowEntry != null &&
                    (highEntry == null || lowEntry.getDistance() >= highEntry.getDistance())) {
                result = lowEntry.getValue();
                lowEntry = null;
            } else {
                result = highEntry.getValue();
                highEntry = null;
            }

            return result;
        }
    }
}

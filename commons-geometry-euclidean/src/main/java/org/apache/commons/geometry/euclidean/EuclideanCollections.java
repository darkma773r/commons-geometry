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

import java.util.Comparator;
import java.util.Objects;

import org.apache.commons.geometry.core.collection.EquivalenceMap;
import org.apache.commons.geometry.core.collection.EquivalenceSet;
import org.apache.commons.geometry.core.internal.TreeEquivalenceMap;
import org.apache.commons.geometry.core.internal.TreeEquivalenceSet;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.numbers.core.Precision;

/** Utility class containing methods related to collections of Euclidean
 * geometry objects.
 */
public final class EuclideanCollections {

    /** No instantiation. */
    private EuclideanCollections() { }

    /** Construct an {@link EquivalenceSet} for storing {@link Vector1D} objects. Vectors are considered
     * equal if their components are equivalent as evaluated by {@code precision}.
     * @param precision object used to determine floating point equality
     * @return equivalence set using the given precision context to determine object equality
     * @throws NullPointerException if {@code precision} is null
     * @see Vector1D#eq(Vector1D, org.apache.commons.numbers.core.Precision.DoubleEquivalence)
     */
    public static EquivalenceSet<Vector1D> equivalenceSet1D(final Precision.DoubleEquivalence precision) {
        return new TreeEquivalenceSet<>(equivalenceComparator1D(precision));
    }

    /** Construct an {@link EquivalenceMap} that uses {@link Vector1D} objects as keys and the given
     * precision context for determining equality. Vectors are considered equal if their components
     * are equivalent as evaluated by {@code precision}.
     * @param <V> Map value type
     * @param precision object used to determine floating point equality
     * @return equivalence map using the given precision context to determine object equality
     * @throws NullPointerException if {@code precision} is null
     * @see Vector1D#eq(Vector1D, org.apache.commons.numbers.core.Precision.DoubleEquivalence)
     */
    public static <V> EquivalenceMap<Vector1D, V> equivalenceMap1D(final Precision.DoubleEquivalence precision) {
        return new TreeEquivalenceMap<>(equivalenceComparator1D(precision));
    }

    /** Construct an {@link EquivalenceSet} for storing {@link Vector2D} objects. Vectors are considered
     * equal if their components are equivalent as evaluated by {@code precision}.
     * @param precision object used to determine floating point equality
     * @return equivalence set using the given precision context to determine object equality
     * @throws NullPointerException if {@code precision} is null
     * @see Vector2D#eq(Vector2D, org.apache.commons.numbers.core.Precision.DoubleEquivalence)
     */
    public static EquivalenceSet<Vector2D> equivalenceSet2D(final Precision.DoubleEquivalence precision) {
        return new TreeEquivalenceSet<>(equivalenceComparator2D(precision));
    }

    /** Construct an {@link EquivalenceMap} that uses {@link Vector2D} objects as keys and the given
     * precision context for determining equality. Vectors are considered equal if their components
     * are equivalent as evaluated by {@code precision}.
     * @param <V> Map value type
     * @param precision object used to determine floating point equality
     * @return equivalence map using the given precision context to determine object equality
     * @throws NullPointerException if {@code precision} is null
     * @see Vector2D#eq(Vector2D, org.apache.commons.numbers.core.Precision.DoubleEquivalence)
     */
    public static <T> EquivalenceMap<Vector2D, T> equivalenceMap2D(final Precision.DoubleEquivalence precision) {
        return new TreeEquivalenceMap<>(equivalenceComparator2D(precision));
    }

    /** Construct an {@link EquivalenceSet} for storing {@link Vector3D} objects. Vectors are considered
     * equal if their components are equivalent as evaluated by {@code precision}.
     * @param precision object used to determine floating point equality
     * @return an equivalence set using the given precision context to determine object equality
     * @throws NullPointerException if {@code precision} is null
     * @see Vector3D#eq(Vector3D, org.apache.commons.numbers.core.Precision.DoubleEquivalence)
     */
    public static EquivalenceSet<Vector3D> equivalenceSet3D(final Precision.DoubleEquivalence precision) {
        return new TreeEquivalenceSet<>(equivalenceComparator3D(precision));
    }

    /** Construct an {@link EquivalenceMap} that uses {@link Vector3D} objects as keys and the given
     * precision context for determining equality. Vectors are considered equal if their components
     * are equivalent as evaluated by {@code precision}.
     * @param <V> Map value type
     * @param precision object used to determine floating point equality
     * @return equivalence map using the given precision context to determine object equality
     * @throws NullPointerException if {@code precision} is null
     * @see Vector3D#eq(Vector3D, org.apache.commons.numbers.core.Precision.DoubleEquivalence)
     */
    public static <T> EquivalenceMap<Vector3D, T> equivalenceMap3D(final Precision.DoubleEquivalence precision) {
        return new TreeEquivalenceMap<>(equivalenceComparator3D(precision));
    }

    /** Create an equivalence comparator for {@link Vector1D} objects using the given precision context.
     * @param precision object used to determine floating point equality
     * @return a comparator that sorts using floating point equivalence
     */
    private static Comparator<Vector1D> equivalenceComparator1D(final Precision.DoubleEquivalence precision) {
        Objects.requireNonNull(precision);
        return (a, b) -> precision.compare(a.getX(), b.getX());
    }

    /** Create an equivalence comparator for {@link Vector2D} objects using the given precision context.
     * @param precision object used to determine floating point equality
     * @return a comparator that sorts using floating point equivalence
     */
    private static Comparator<Vector2D> equivalenceComparator2D(final Precision.DoubleEquivalence precision) {
        Objects.requireNonNull(precision);
        return (a, b) -> {
            int cmp = precision.compare(a.getX(), b.getX());
            if (cmp == 0) {
                return precision.compare(a.getY(), b.getY());
            }
            return cmp;
        };
    }

    /** Create an equivalence comparator for {@link Vector3D} objects using the given precision context.
     * @param precision object used to determine floating point equality
     * @return a comparator that sorts using floating point equivalence
     */
    private static Comparator<Vector3D> equivalenceComparator3D(final Precision.DoubleEquivalence precision) {
        Objects.requireNonNull(precision);
        return (a, b) -> {
            if (a.eq(b, precision)) {
                return 0;
            }
            return Vector3D.COORDINATE_ASCENDING_ORDER.compare(a, b);
//            int cmp = precision.compare(a.getX(), b.getX());
//            if (cmp == 0) {
//                cmp = precision.compare(a.getY(), b.getY());
//                if (cmp == 0) {
//                    return precision.compare(a.getZ(), b.getZ());
//                }
//            }
//            return cmp;
        };
    }
}

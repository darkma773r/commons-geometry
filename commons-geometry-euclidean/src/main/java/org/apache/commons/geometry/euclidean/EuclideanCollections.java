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

public final class EuclideanCollections {

    /** Utility class; no instantiation. */
    private EuclideanCollections() { }

    public static <T> EquivalenceMap<Vector1D, T> equivalenceMap1D(final Precision.DoubleEquivalence precision) {
        return new TreeEquivalenceMap<>(equivalenceComparator1D(precision));
    }

    public static <T> EquivalenceSet<Vector1D> equivalenceSet1D(final Precision.DoubleEquivalence precision) {
        return new TreeEquivalenceSet<>(equivalenceComparator1D(precision));
    }

    public static <T> EquivalenceMap<Vector2D, T> equivalenceMap2D(final Precision.DoubleEquivalence precision) {
        return new TreeEquivalenceMap<>(equivalenceComparator2D(precision));
    }

    public static <T> EquivalenceSet<Vector2D> equivalenceSet2D(final Precision.DoubleEquivalence precision) {
        return new TreeEquivalenceSet<>(equivalenceComparator2D(precision));
    }

    public static <T> EquivalenceMap<Vector3D, T> equivalenceMap3D(final Precision.DoubleEquivalence precision) {
        return new TreeEquivalenceMap<>(equivalenceComparator3D(precision));
    }

    public static <T> EquivalenceSet<Vector3D> equivalenceSet3D(final Precision.DoubleEquivalence precision) {
        return new TreeEquivalenceSet<>(equivalenceComparator3D(precision));
    }

    private static Comparator<Vector1D> equivalenceComparator1D(final Precision.DoubleEquivalence precision) {
        Objects.requireNonNull(precision);
        return (a, b) -> precision.compare(a.getX(), b.getX());
    }

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

    private static Comparator<Vector3D> equivalenceComparator3D(final Precision.DoubleEquivalence precision) {
        Objects.requireNonNull(precision);
        return (a, b) -> {
            int cmp = precision.compare(a.getX(), b.getX());
            if (cmp == 0) {
                cmp = precision.compare(a.getY(), b.getY());
                if (cmp == 0) {
                    return precision.compare(a.getZ(), b.getZ());
                }
            }
            return cmp;
        };
    }
}

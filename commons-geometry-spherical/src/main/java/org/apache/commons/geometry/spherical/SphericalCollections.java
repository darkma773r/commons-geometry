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
package org.apache.commons.geometry.spherical;

import org.apache.commons.geometry.core.internal.PointMapAsSetAdapter;
import org.apache.commons.geometry.spherical.oned.Point1S;
import org.apache.commons.geometry.spherical.oned.PointMap1S;
import org.apache.commons.geometry.spherical.oned.PointSet1S;
import org.apache.commons.geometry.spherical.twod.Point2S;
import org.apache.commons.geometry.spherical.twod.PointMap2S;
import org.apache.commons.geometry.spherical.twod.PointSet2S;
import org.apache.commons.numbers.core.Precision;

/** Class containing utility methods for spherical collection types.
 */
public final class SphericalCollections {

    /** No instantiation. */
    private SphericalCollections() {}

    /** Construct a new {@link PointSet1S} instance using the given precision context to determine
     * equality between points.
     * @param precision precision context used to determine point equality
     * @return new spherical 1D point set instance
     */
    public static PointSet1S pointSet1S(final Precision.DoubleEquivalence precision) {
        return new PointSet1SImpl(precision);
    }

    /** Construct a new {@link PointMap1S} instance using the given precision context to determine
     * equality between points.
     * @param <V> Map value type
     * @param precision precision context used to determine point equality
     * @return new spherical 1D point map instance
     */
    public static <V> PointMap1S<V> pointMap1S(final Precision.DoubleEquivalence precision) {
        return new PointMap1SImpl<>(precision);
    }

    /** Construct a new {@link PointSet2S} instance using the given precision context to determine
     * equality between points.
     * @param precision precision context used to determine point equality
     * @return new spherical 2D point set instance
     */
    public static PointSet2S pointSet2S(final Precision.DoubleEquivalence precision) {
        return new PointSet2SImpl(precision);
    }

    /** Construct a new {@link PointMap2S} instance using the given precision context to determine
     * equality between points.
     * @param <V> Map value type
     * @param precision precision context used to determine point equality
     * @return new spherical 2D point map instance
     */
    public static <V> PointMap2S<V> pointMap2S(final Precision.DoubleEquivalence precision) {
        return new PointMap2SImpl<>(precision);
    }

    /** Internal {@link PointSet1S} implementation.
     */
    private static final class PointSet1SImpl
        extends PointMapAsSetAdapter<Point1S, PointMap1S<Object>>
        implements PointSet1S {

        PointSet1SImpl(final Precision.DoubleEquivalence precision) {
            super(pointMap1S(precision));
        }
    }

    /** Internal {@link PointSet2S} implementation.
     */
    private static final class PointSet2SImpl
        extends PointMapAsSetAdapter<Point2S, PointMap2S<Object>>
        implements PointSet2S {

        PointSet2SImpl(final Precision.DoubleEquivalence precision) {
            super(pointMap2S(precision));
        }
    }
}

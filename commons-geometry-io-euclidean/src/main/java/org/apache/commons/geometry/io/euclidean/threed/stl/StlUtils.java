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
package org.apache.commons.geometry.io.euclidean.threed.stl;

import java.nio.ByteBuffer;

import org.apache.commons.geometry.euclidean.internal.Vectors;
import org.apache.commons.geometry.euclidean.threed.Vector3D;

/** Utility methods for the STL format.
 */
final class StlUtils {

    /** Utility class; no instantiation. */
    private StlUtils() { }

    /** Create a {@link ByteBuffer} with the given size and the byte order
     * appropriate for binary STL content.
     * @param capacity buffer capacity
     * @return byte buffer
     */
    public static ByteBuffer byteBuffer(final int capacity) {
        return ByteBuffer.allocate(capacity)
                .order(StlConstants.BINARY_BYTE_ORDER);
    }

    /** Return true if the given points are arranged counter-clockwise relative to the
     * given normal. Returns true if {@code normal} is null.
     * @param p1 first point
     * @param p2 second point
     * @param p3 third point
     * @param normal normal; may be null, in which case the zero vector is used
     * @return true if {@code normal} is null or if the given points are arranged counter-clockwise
     *      relative to {@code normal}
     */
    public static boolean pointsAreCounterClockwise(final Vector3D p1, final Vector3D p2, final Vector3D p3,
            final Vector3D normal) {
        if (normal != null) {
            final Vector3D computedNormal = Vectors.tryNormalize(p1.vectorTo(p2).cross(p1.vectorTo(p3)));
            if (computedNormal != null && normal.dot(computedNormal) < 0) {
                return false;
            }
        }

        return true;
    }
}

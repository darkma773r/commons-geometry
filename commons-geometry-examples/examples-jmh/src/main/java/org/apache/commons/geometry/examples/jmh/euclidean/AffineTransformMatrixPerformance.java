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
package org.apache.commons.geometry.examples.jmh.euclidean;

import java.util.concurrent.TimeUnit;

import org.apache.commons.geometry.euclidean.threed.AffineTransformMatrix3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.apache.commons.geometry.examples.jmh.BenchmarkUtils;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

/** Benchmarks for
 * {@link org.apache.commons.geometry.euclidean.AbstractAffineTransformMatrix AbstractAffineTransformMatrix}
 * subclasses.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = {"-server", "-Xms512M", "-Xmx512M"})
public class AffineTransformMatrixPerformance {

    /** Matrix type representing the identity matrix. */
    private static final String IDENTITY_MATRIX_TYPE = "identity";

    /** Matrix type representing a matrix with values in each array element. */
    private static final String FULL_MATRIX_TYPE = "full";

    /** Input class providing random arrays of double values for transformation.
     */
    @State(Scope.Thread)
    public static class TransformArrayInput {

        /** The number of elements in the input array. */
        @Param({"600", "60000"})
        private int size;

        /** Array containing the input elements. */
        private double[] array;

        /** Get the configured size of the input array.
         * @return the configured size of the input array
         */
        public int getSize() {
            return size;
        }

        /** Get the input array.
         * @return input array
         */
        public double[] getArray() {
            return array;
        }

        /** Set up the input array.
         */
        @Setup(Level.Iteration)
        public void setup() {
            final UniformRandomProvider rand = RandomSource.create(RandomSource.XO_RO_SHI_RO_128_PP);

            array = new double[size];

            for (int i = 0; i < array.length; ++i) {
                array[i] = BenchmarkUtils.createRandomDouble(rand);
            }
        }
    }

    /** Base class for matrix inputs.
     */
    @State(Scope.Thread)
    public abstract static class TransformMatrixInputBase {

        /** Type of matrix. */
        @Param({IDENTITY_MATRIX_TYPE, FULL_MATRIX_TYPE})
        private String type;

        /** Return the type of matrix input.
         * @return type of matrix input
         */
        public String getType() {
            return type;
        }
    }

    /** Input class providing a 3D transform matrix.
     */
    @State(Scope.Thread)
    public static class TransformMatrixInput3D extends TransformMatrixInputBase {

        /** Input transform matrix. */
        private AffineTransformMatrix3D transform;

        /** Get the input transform matrix.
         * @return the input transform matrix
         */
        public AffineTransformMatrix3D getTransform() {
            return transform;
        }

        /** Set up the input.
         */
        @Setup
        public void setup() {
            if (IDENTITY_MATRIX_TYPE.equals(getType())) {
                transform = AffineTransformMatrix3D.identity();
            } else {
                transform = AffineTransformMatrix3D.createScale(1, 2, 3)
                        .rotate(QuaternionRotation.fromAxisAngle(Vector3D.of(1, 1, 1), 0.3 * Math.PI))
                        .translate(7, 8, 9);
            }
        }
    }

    /** Baseline benchmark for 3D transforms.
     * @param arrayInput array input
     * @param bh blackhole instance
     */
    @Benchmark
    public void baselineArray3D(final TransformArrayInput arrayInput, final Blackhole bh) {
        final double[] arr = arrayInput.getArray();

        double x;
        double y;
        double z;
        for (int i = 0; i < arr.length; i += 3) {
            x = arr[i];
            y = arr[i + 1];
            z = arr[i + 2];

            bh.consume(x);
            bh.consume(y);
            bh.consume(z);
        }
    }

    /** Benchmark testing the performance of transforming an array of doubles by converting each group
     * to a Vector3D.
     * @param arrayInput array input
     * @param transformInput transform input
     * @param bh blackhole instance
     */
    @Benchmark
    public void transformArrayAsVectors3D(final TransformArrayInput arrayInput,
            final TransformMatrixInput3D transformInput, final Blackhole bh) {
        final double[] arr = arrayInput.getArray();
        final AffineTransformMatrix3D t = transformInput.getTransform();

        Vector3D in;
        Vector3D out;
        for (int i = 0; i < arr.length; i += 3) {
            in = Vector3D.of(
                    arr[i],
                    arr[i + 1],
                    arr[i + 2]);
            out = t.apply(in);

            bh.consume(out.getX());
            bh.consume(out.getY());
            bh.consume(out.getZ());
        }
    }

    /** Benchmark testing the performance of transforming an an array of doubles by transforming
     * the components directly.
     * @param arrayInput array input
     * @param transformInput transform input
     * @param bh blackhole instance
     */
    @Benchmark
    public void transformArrayComponents3D(final TransformArrayInput arrayInput,
            final TransformMatrixInput3D transformInput, final Blackhole bh) {
        final double[] arr = arrayInput.getArray();
        final AffineTransformMatrix3D t = transformInput.getTransform();

        double inX;
        double inY;
        double inZ;
        double outX;
        double outY;
        double outZ;
        for (int i = 0; i < arr.length; i += 3) {
            inX = arr[i];
            inY = arr[i + 1];
            inZ = arr[i + 2];

            outX = t.applyX(inX, inY, inZ);
            outY = t.applyX(inX, inY, inZ);
            outZ = t.applyX(inX, inY, inZ);

            bh.consume(outX);
            bh.consume(outY);
            bh.consume(outZ);
        }
    }
}

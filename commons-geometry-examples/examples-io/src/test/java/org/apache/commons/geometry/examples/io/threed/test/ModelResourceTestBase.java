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
package org.apache.commons.geometry.examples.io.threed.test;

import java.util.List;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.RegionBSPTree3D;
import org.apache.commons.geometry.examples.io.threed.facet.FacetDefinition;
import org.junit.jupiter.api.function.ThrowingConsumer;

public abstract class ModelResourceTestBase {

    /** Epsilon value used in test comparisons. This is smaller than in other tests
     * in order to account for storage precision.
     */
    public static final double MODEL_TEST_EPS = 1e-6;

    public static final DoublePrecisionContext MODEL_TEST_PRECISION =
            new EpsilonDoublePrecisionContext(MODEL_TEST_EPS);

    /** Run the given function with each model resource returned for the given base name.
     * @param baseName model base name
     * @param fn function to execute for each resolved classpath location
     * @throws Exception on failure
     */
    protected void runWithModelResource(final String baseName, final ThrowingConsumer<String> fn)
            throws Exception {
        try {
            for (final String location : getModelResourceLocations(baseName)) {
                fn.accept(location);
            }
        } catch (Error err) {
            throw err;
        } catch (Throwable thr) {
            throw new Exception(thr);
        }
    }

    /** Get the classpath locations of the models with the given base name. A list is returned
     * so that multiple model variations containing the same geometry may be tested simultaneously.
     * @param baseName resource base name
     * @return the classpath locations of the models with the given base name
     */
    protected abstract List<String> getModelResourceLocations(String baseName);
}

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
package org.apache.commons.geometry.io.core.utils;

/** Interface used to create standard, non-localized decimal strings for use in
 * text-based data output.
 *
 * <p><strong>Implementation Note:</strong> Implementations of this interface <em>must</em> be
 * thread-safe.</p>
 */
@FunctionalInterface
public interface DataDecimalFormat {

    /** Return a string representation of the given double value.
     * @param d double to obtain a string representation for
     * @return string representation of the argument
     */
    String format(double d);
}

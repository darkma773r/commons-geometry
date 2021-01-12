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
package org.apache.commons.geometry.core.io;

import java.io.IOException;

/** Exception class used to signal an error in IO operations involving geometric data.
 */
public class GeometryIOException extends IOException {

    /** Serializable UID. */
    private static final long serialVersionUID = 20201228L;

    /** Construct a new instance with the given message string.
     * @param msg message string
     */
    public GeometryIOException(final String msg) {
        super(msg);
    }

    /** Construct a new instance with the given message string and exception cause.
     * @param msg message string
     * @param cause exception cause
     */
    public GeometryIOException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}

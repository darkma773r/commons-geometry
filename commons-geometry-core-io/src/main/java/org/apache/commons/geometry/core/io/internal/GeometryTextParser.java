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
package org.apache.commons.geometry.core.io.internal;

import java.io.Reader;

import org.apache.commons.geometry.core.io.GeometryIOException;

/** Subclass of {@link SimpleTextParser} that throws {@link GeometryIOException}s
 * on parse errors.
 */
public class GeometryTextParser extends SimpleTextParser {

    /** Construct a new instance that parses content from the given reader.
     * @param reader reader to parse content from
     */
    public GeometryTextParser(final Reader reader) {
        super(reader);
    }

    /** {@inheritDoc} */
    @Override
    protected GeometryIOException createParseError(final String msg, final Throwable cause) {
        return new GeometryIOException(msg, cause);
    }
}

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
package org.apache.commons.geometry.euclidean.io.threed.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.geometry.euclidean.io.threed.AbstractBoundaryReadHandler3D;
import org.apache.commons.geometry.euclidean.io.threed.FacetDefinitionReader;

/** Handler class for reading simple text and CSV data.
 * @see TextFacetDefinitionReader
 */
public class TextBoundaryReadHandler3D extends AbstractBoundaryReadHandler3D {

    /** Charset for use with txt and csv files. */
    static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /** {@inheritDoc} */
    @Override
    public FacetDefinitionReader facetDefinitionReader(final InputStream in) throws IOException {
        return new TextFacetDefinitionReader(new BufferedReader(new InputStreamReader(in, DEFAULT_CHARSET)));
    }
}

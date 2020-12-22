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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.geometry.examples.io.threed.facet.FacetDefinition;
import org.apache.commons.geometry.examples.io.threed.facet.FacetDefinitionReader;

/** Class containing utility methods for IO tests.
 */
public final class ModelIOTestUtils {

    /** Utility class; no instantiation. */
    private ModelIOTestUtils() {}

    /** Read all facets available from the given facet reader.
     * @param reader instance to read facets from
     * @return list containing all facets available from the given facet reader
     * @throws IOException
     */
    public static List<FacetDefinition> readAll(final FacetDefinitionReader reader) throws IOException {
        final List<FacetDefinition> facets = new ArrayList<>();

        FacetDefinition f;
        while ((f = reader.readFacet()) != null) {
            facets.add(f);
        }

        return facets;
    }

    /** Get a {@link InputStream} for reading the content of the classpath resource at the given location.
     * @param location classpath location
     * @return input stream for reading the content of the classpath resource at the given location
     * @throws IOException if the resource cannot be found or the stream cannot be constructed
     */
    public static CloseCountInputStream resourceStream(final String location) throws IOException {
        final InputStream in = FacetDefinitionReaderTestBase.class.getResourceAsStream(location);
        if (in == null) {
            throw new FileNotFoundException("Unable to find classpath resource: " + location);
        }

        return new CloseCountInputStream(in);
    }

    /** Get a {@link Reader} for reading the content of the classpath resource at the given location. The
     * UTF-8 charset is used to read the content.
     * @param location classpath location
     * @return reader for the classpath resource at the given location
     * @throws IOException if the resource cannot be found or the reader cannot be constructed
     */
    public static CloseCountReader resourceReader(final String location)
            throws IOException {
        return resourceReader(location, StandardCharsets.UTF_8);
    }

    /** Get a {@link Reader} for reading the content of the classpath resource at the given location.
     * @param location classpath location
     * @param charset input character set
     * @return reader for the classpath resource at the given location
     * @throws IOException if the resource cannot be found or the reader cannot be constructed
     */
    public static CloseCountReader resourceReader(final String location, final Charset charset)
            throws IOException {
        final InputStream in = resourceStream(location);

        return new CloseCountReader(new BufferedReader(new InputStreamReader(in, charset)));
    }
}

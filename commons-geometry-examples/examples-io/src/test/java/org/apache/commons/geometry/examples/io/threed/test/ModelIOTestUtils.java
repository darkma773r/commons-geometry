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
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.RegionBSPTree3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.shape.Parallelepiped;
import org.apache.commons.geometry.euclidean.threed.shape.Sphere;
import org.apache.commons.geometry.examples.io.threed.facet.FacetDefinition;
import org.apache.commons.geometry.examples.io.threed.facet.FacetDefinitionReader;
import org.junit.jupiter.api.Assertions;

/** Class containing utility methods for IO tests.
 */
public final class ModelIOTestUtils {

    public static final double DEFAULT_EPS = 1e-10;

    public static final DoublePrecisionContext DEFAULT_PRECISION =
            new EpsilonDoublePrecisionContext(DEFAULT_EPS);

    /** Utility class; no instantiation. */
    private ModelIOTestUtils() {}

    public static Parallelepiped cube(final DoublePrecisionContext precision) {
        return Parallelepiped.unitCube(precision);
    }

    public static void assertCube(final BoundarySource3D src, final double tolerance) {
        final RegionBSPTree3D tree = src.toTree();

        Assertions.assertEquals(1, tree.getSize(), tolerance);
        Assertions.assertEquals(6, tree.getBoundarySize(), tolerance);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, tree.getCentroid(), tolerance);

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.INSIDE,
                Vector3D.ZERO,
                Vector3D.of(0.25, 0, 0), Vector3D.of(-0.25, 0, 0),
                Vector3D.of(0, 0.25, 0), Vector3D.of(0, -0.25, 0),
                Vector3D.of(0, 0, 0.25), Vector3D.of(0, 0, -0.25));

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.BOUNDARY,
                Vector3D.of(-0.5, -0.5, -0.5), Vector3D.of(-0.5, -0.5, +0.5),
                Vector3D.of(-0.5, +0.5, -0.5), Vector3D.of(-0.5, +0.5, +0.5),
                Vector3D.of(+0.5, -0.5, -0.5), Vector3D.of(+0.5, -0.5, +0.5),
                Vector3D.of(+0.5, +0.5, -0.5), Vector3D.of(+0.5, +0.5, +0.5));

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.OUTSIDE,
                Vector3D.of(0.5, 0.5, 1), Vector3D.of(0.5, 0.5, -1),
                Vector3D.of(0.5, 1, 0.5), Vector3D.of(0.5, -1, 0.5),
                Vector3D.of(1, 0.5, 0.5), Vector3D.of(-1, 0.5, 0.5));
    }

    public static RegionBSPTree3D cubeMinusSphere(final DoublePrecisionContext precision) {
        final RegionBSPTree3D tree = Parallelepiped.unitCube(precision).toTree();
        final Sphere sphere = Sphere.from(Vector3D.ZERO, 0.65, precision);

        tree.difference(sphere.toTree(3));

        return tree;
    }

    public static void assertCubeMinusSphere(final BoundarySource3D src, final double tolerance) {
        final RegionBSPTree3D tree = src.toTree();

        Assertions.assertEquals(0.11509505362599505, tree.getSize(), tolerance);
        Assertions.assertEquals(4.585561662505128, tree.getBoundarySize(), tolerance);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, tree.getCentroid(), tolerance);

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.INSIDE,
                Vector3D.of(0.45, 0.45, 0.45), Vector3D.of(0.45, 0.45, -0.45),
                Vector3D.of(0.45, -0.45, 0.45), Vector3D.of(0.45, -0.45, -0.45),
                Vector3D.of(-0.45, 0.45, 0.45), Vector3D.of(-0.45, 0.45, -0.45),
                Vector3D.of(-0.45, -0.45, 0.45), Vector3D.of(-0.45, -0.45, -0.45));

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.BOUNDARY,
                Vector3D.of(-0.5, -0.5, -0.5), Vector3D.of(-0.5, -0.5, +0.5),
                Vector3D.of(-0.5, +0.5, -0.5), Vector3D.of(-0.5, +0.5, +0.5),
                Vector3D.of(+0.5, -0.5, -0.5), Vector3D.of(+0.5, -0.5, +0.5),
                Vector3D.of(+0.5, +0.5, -0.5), Vector3D.of(+0.5, +0.5, +0.5));

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.OUTSIDE,
                Vector3D.ZERO,
                Vector3D.of(0.5, 0.5, 1), Vector3D.of(0.5, 0.5, -1),
                Vector3D.of(0.5, 1, 0.5), Vector3D.of(0.5, -1, 0.5),
                Vector3D.of(1, 0.5, 0.5), Vector3D.of(-1, 0.5, 0.5));
    }

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

    /** Get the bytes of the classpath resource at the given location.
     * @param location classpath location to read
     * @return the bytes of the resource at the given location
     * @throws IOException if the resource cannot be found or an I/O error occurs
     */
    public static byte[] resourceBytes(final String location) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        final byte[] buf = new byte[1024];
        int read;
        try (InputStream in = resourceStream(location)) {
            while ((read = in.read(buf)) > -1) {
                out.write(buf, 0, read);
            }
        }

        return out.toByteArray();
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

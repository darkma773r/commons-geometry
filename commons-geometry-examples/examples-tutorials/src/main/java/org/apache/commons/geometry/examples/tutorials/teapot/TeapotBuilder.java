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
package org.apache.commons.geometry.examples.tutorials.teapot;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleFunction;

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.AffineTransformMatrix3D;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.Bounds3D;
import org.apache.commons.geometry.euclidean.threed.EmbeddingPlane;
import org.apache.commons.geometry.euclidean.threed.Plane;
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.euclidean.threed.Planes;
import org.apache.commons.geometry.euclidean.threed.RegionBSPTree3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.mesh.SimpleTriangleMesh;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.apache.commons.geometry.euclidean.threed.shape.Sphere;
import org.apache.commons.geometry.euclidean.twod.AffineTransformMatrix2D;
import org.apache.commons.geometry.euclidean.twod.RegionBSPTree2D;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.geometry.euclidean.twod.path.LinePath;
import org.apache.commons.geometry.euclidean.twod.shape.Circle;
import org.apache.commons.geometry.io.euclidean.threed.IO3D;
import org.apache.commons.numbers.angle.PlaneAngleRadians;

/** Class used to construct a simple 3D teapot shape using the
 * {@code commons-geometry-euclidean} module.
 */
public class TeapotBuilder {

    /** Precision context used during region construction. */
    private final DoublePrecisionContext precision;

    /** Directory to place debug output files. If null, no debug files are written. */
    private Path debugDir;

    /** Construct a new build instance.
     * @param precision precision context to use during region construction
     */
    public TeapotBuilder(final DoublePrecisionContext precision) {
        this.precision = precision;
    }

    public Path getDebugDir() {
        return debugDir;
    }

    public void setDebugDir(final Path debugDir) {
        this.debugDir = debugDir;
    }

    public RegionBSPTree3D buildTeapot() {
        final RegionBSPTree3D body = buildBody();
        final Bounds3D bodyBounds = body.getBounds();

        final RegionBSPTree3D top = buildTop(body);
        final RegionBSPTree3D handle = buildHandle(bodyBounds);

        final RegionBSPTree3D spout = buildSpout(bodyBounds);

        final RegionBSPTree3D teapot = unionAll(body, top, handle, spout);

        return teapot;
    }

    public void writeTeapot(final Path outputFile) throws IOException {
        IO3D.write(buildTeapot(), outputFile);
    }

    private RegionBSPTree3D buildBody() {
        // construct a BSP tree sphere approximation
        final Sphere sphere = Sphere.from(Vector3D.ZERO, 1, precision);
        final RegionBSPTree3D body = sphere.toTree(4);

        // squash it a little bit along the z-axis
        final AffineTransformMatrix3D t = AffineTransformMatrix3D.createScale(1, 1, 0.75);
        body.transform(t);

        // cut off part of the bottom to make it flat
        final Plane bottomPlane = Planes.fromPointAndNormal(Vector3D.of(0, 0, -0.6), Vector3D.Unit.PLUS_Z, precision);
        final PlaneConvexSubset bottom = bottomPlane.span();
        body.difference(RegionBSPTree3D.from(Arrays.asList(bottom)));

        debugOutput(body, "body.obj");

        return body;
    }

    private RegionBSPTree3D buildTop(final RegionBSPTree3D body) {
        // make a copy of the body so that we match its curve exactly
        final RegionBSPTree3D top = body.copy();

        // translate the top to be above the body
        final AffineTransformMatrix3D t = AffineTransformMatrix3D.createTranslation(0, 0, 0.03);
        top.transform(t);

        // intersect the translated body with a cylinder
        final EmbeddingPlane extrusionPlane = Planes.fromPointAndPlaneVectors(
                Vector3D.ZERO, Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, precision);
        final Circle circle = Circle.from(Vector2D.ZERO, 0.5, precision);
        final List<PlaneConvexSubset> extrudedCircle = Planes.extrude(
                circle.toTree(20).getBoundaryPaths().get(0),
                extrusionPlane, Vector3D.Unit.PLUS_Z, precision);

        top.intersection(RegionBSPTree3D.from(extrudedCircle));

        // add a small squashed sphere on top; use the bounds of the top in order to place
        // the sphere at the correct position
        final Sphere sphere = Sphere.from(Vector3D.of(0, 0, 0), 0.15, precision);
        final RegionBSPTree3D sphereTree = sphere.toTree(2);

        final Bounds3D topBounds = top.getBounds();
        final double sphereZ = topBounds.getMax().getZ() + 0.075;
        sphereTree.transform(AffineTransformMatrix3D.createScale(1, 1, 0.75)
                .translate(0, 0, sphereZ));

        top.union(sphereTree);

        debugOutput(top, "top.obj");

        return top;
    }

    private SimpleTriangleMesh buildUnitCylinder(final double radius, final double height,
            final int segments, final int circleVertexCount) {

        final SimpleTriangleMesh.Builder builder = SimpleTriangleMesh.builder(precision);

        // add the cylinder vertices
        final double maxZ = Math.abs(0.5 * height);
        final double minZ = -maxZ;
        final double zDelta = height / segments;
        double zValue;

        final double azDelta = PlaneAngleRadians.TWO_PI / circleVertexCount;
        double az;

        for (int i = 0; i <= segments; ++i) {
            zValue = (i * zDelta) + minZ;

            for (int v = 0; v < circleVertexCount; ++v) {
                az = v * azDelta;

                builder.addVertex(Vector3D.of(
                        radius * Math.cos(az),
                        radius * Math.sin(az),
                        zValue
                    ));
            }
        }

        // add the bottom faces using a triangle fan
        for (int i = 1; i < circleVertexCount - 1; ++i) {
            builder.addFace(0, i, i + 1);
        }

        // add the side faces
        int circleStart;
        int v1;
        int v2;
        int v3;
        int v4;
        for (int s = 0; s < segments; ++s) {
            circleStart = s * circleVertexCount;

            for (int i = 0; i < circleVertexCount; ++i) {
                v1 = i + circleStart;
                v2 = ((i + 1) % circleVertexCount) + circleStart;
                v3 = v2 + circleVertexCount;
                v4 = v1 + circleVertexCount;

                builder
                    .addFace(v1, v2, v3)
                    .addFace(v1, v3, v4);
            }
        }

        // add the top faces using a triangle fan
        final int lastCircleStart = circleVertexCount * segments;
        for (int i = 1 + lastCircleStart; i < builder.getVertexCount() - 1; ++i) {
            builder.addFace(lastCircleStart, i, i + 1);
        }

        return builder.build();
    }

    private RegionBSPTree3D buildHandle(final Bounds3D bodyBounds) {
        // create a line path for a circle squashed along the x-axis
        final RegionBSPTree2D pathTree = Circle.from(Vector2D.of(-0.5, 0), 0.1, precision).toTree(10);
        pathTree.transform(AffineTransformMatrix2D.createScale(0.75, 1));

        final LinePath path = pathTree.getBoundaryPaths().get(0);
        final List<Vector2D> pathSequence = path.getVertexSequence();

        // build a mesh following the curved path of the handle
        final SimpleTriangleMesh.Builder builder = SimpleTriangleMesh.builder(precision);

        final QuaternionRotation startRotation = QuaternionRotation.identity();
        final QuaternionRotation endRotation = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, Math.PI);
        final DoubleFunction<QuaternionRotation> slerp = startRotation.slerp(endRotation);

        final EmbeddingPlane startPlane = Planes.fromPointAndPlaneVectors(
                Vector3D.ZERO, Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, precision);
        final List<Vector3D> segmentVertices = startPlane.toSpace(pathSequence.subList(0, pathSequence.size() - 1));

        final int numCurvedSegments = 20;
        final int numTotalSegments = 22;
        final int numSegmentVertices = segmentVertices.size();

        final int straightLength = 1;

        // add a set of vertices at the start that are not part of the curve
        final AffineTransformMatrix3D startTransform =
                AffineTransformMatrix3D.createTranslation(0, 0, -straightLength);
        segmentVertices.stream()
            .map(startTransform)
            .forEach(builder::addVertex);

        // add the curved vertices
        QuaternionRotation rotation;
        for (int i = 0; i <= numCurvedSegments; ++i) {
            rotation = slerp.apply(i / (double) numCurvedSegments);

            for (int v = 0; v < numSegmentVertices; ++v) {
                builder.addVertex(rotation.apply(segmentVertices.get(v)));
            }
        }

        // add a set of end vertices at the end that are not curved
        final AffineTransformMatrix3D endTransform = endRotation.toMatrix()
                .translate(0, 0, -straightLength);
        segmentVertices.stream()
            .map(endTransform)
            .forEach(builder::addVertex);

        // build the faces
        int segmentStartIdx;
        int v1;
        int v2;
        int v3;
        int v4;
        for (int i = 0; i < numTotalSegments; ++i) {
            segmentStartIdx = i * numSegmentVertices;

            for (int v = 0; v < numSegmentVertices; ++v) {
                v1 = segmentStartIdx + v;
                v2 = segmentStartIdx + ((v + 1) % numSegmentVertices);
                v3 = v2 + numSegmentVertices;
                v4 = v1 + numSegmentVertices;

                builder.addFace(v1, v2, v3);
                builder.addFace(v1, v3, v4);
            }
        }

        // construct the mesh
        final SimpleTriangleMesh mesh = builder.build();

        // convert to a BSP tree
        final RegionBSPTree3D handleTree = mesh.toTree();

        // add caps at the ends of the handles to close the region
        final Plane capPlane = Planes.fromPointAndNormal(
                Vector3D.of(0, 0, -straightLength), Vector3D.Unit.MINUS_Z, precision);
        handleTree.intersection(RegionBSPTree3D.from(Arrays.asList(capPlane.span())));

        // transform the handle to the correct position
        final Transform<Vector3D> t =
                QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, -PlaneAngleRadians.PI_OVER_TWO)
                    .toMatrix()
                    .translate(Vector3D.of(bodyBounds.getMin().getX(), 0, 0));

        handleTree.transform(t);

        debugOutput(handleTree, "handle.obj");

        return handleTree;
    }

    private RegionBSPTree3D buildSpout(final Bounds3D bodyBounds) {
        // create a line path for a circle squashed along the y-axis
        final RegionBSPTree2D pathTree = Circle.from(Vector2D.ZERO, 0.3, precision).toTree(20);
        pathTree.transform(AffineTransformMatrix2D.createScale(1, 0.75));

        final LinePath path = pathTree.getBoundaryPaths().get(0);
        final List<Vector2D> pathSequence = path.getVertexSequence();

        // build a mesh for the spout
        final SimpleTriangleMesh.Builder builder = SimpleTriangleMesh.builder(precision);

        final double height = 1;

        final EmbeddingPlane startPlane = Planes.fromPointAndPlaneVectors(
                Vector3D.of(0, 0, -0.5 * height), Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, precision);
        final List<Vector3D> segmentVertices = startPlane.toSpace(pathSequence.subList(0, pathSequence.size() - 1));

        final int numSegments = 10;
        final int numSegmentVertices = segmentVertices.size();

        // add the curved vertices
        final double taperFactor = 0.9;
        double segmentRelativePosition;
        Transform<Vector3D> vertexTransform;
        for (int i = 0; i <= numSegments; ++i) {
            segmentRelativePosition = i / (double) numSegments;
            vertexTransform = AffineTransformMatrix3D.createScale(1 - (segmentRelativePosition * taperFactor))
                    .translate(0, 0, segmentRelativePosition * height);

            for (int v = 0; v < numSegmentVertices; ++v) {
                builder.addVertex(vertexTransform.apply(segmentVertices.get(v)));
            }
        }

        // build the faces
        int segmentStartIdx;
        int v1;
        int v2;
        int v3;
        int v4;
        for (int i = 0; i < numSegments; ++i) {
            segmentStartIdx = i * numSegmentVertices;

            for (int v = 0; v < numSegmentVertices; ++v) {
                v1 = segmentStartIdx + v;
                v2 = segmentStartIdx + ((v + 1) % numSegmentVertices);
                v3 = v2 + numSegmentVertices;
                v4 = v1 + numSegmentVertices;

                builder.addFace(v1, v2, v3);
                builder.addFace(v1, v3, v4);
            }
        }

        // construct the mesh
        final SimpleTriangleMesh mesh = builder.build();

        // transform to the correct position
        final Transform<Vector3D> t =
                QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, 0.25 * Math.PI)
                    .toMatrix()
                    .translate(bodyBounds.getMax().getX(), 0, 0.5 * bodyBounds.getMax().getZ());

        // transform and convert to a tree
        final RegionBSPTree3D tree = mesh.transform(t).toTree();

        // cap the ends
        final Plane topCap = Planes.fromPointAndNormal(
                Vector3D.of(0, 0, bodyBounds.getMax().getZ() - 0.1), Vector3D.Unit.PLUS_Z, precision);
        final Plane bottomCap = Planes.fromPointAndNormal(
                Vector3D.of(0, 0, bodyBounds.getMin().getZ() + 0.1), Vector3D.Unit.MINUS_Z, precision);

        tree.intersection(RegionBSPTree3D.from(Arrays.asList(topCap.span())));
        tree.intersection(RegionBSPTree3D.from(Arrays.asList(bottomCap.span())));

        debugOutput(tree, "spout.obj");

        return tree;
    }

    /** Write a debug output file containing the given model if a debug directory has
     * been specified. IO errors are rethrown as {@link java.io.UncheckedIOException}s.
     * @param model model to output
     * @param file name of the output file in the debug directory
     * @throws UncheckedIOException if an IO error occurs
     */
    private void debugOutput(final BoundarySource3D model, final String file) {
        if (debugDir != null) {
            try {
                Files.createDirectories(debugDir);

                IO3D.write(model, debugDir.resolve(file));
            } catch (IOException exc) {
                throw new UncheckedIOException(exc);
            }
        }
    }

    private static RegionBSPTree3D unionAll(final RegionBSPTree3D ... trees) {
        RegionBSPTree3D result = RegionBSPTree3D.empty();

        int i = 0;
        if (trees.length > 1) {
            // use the two-argument version first if possible
            result.union(trees[0], trees[1]);
            i = 2;
        }

        for (; i < trees.length; ++i) {
            result.union(trees[i]);
        }

        return result;
    }

    public static void main(final String[] args) throws IOException {
        if (args.length < 1) {
            throw new IllegalArgumentException("Output file argument is required");
        }

        final Path outputFile = Paths.get(args[0]);
        final Path debugDir = args.length > 1 ?
                Paths.get(args[1]) :
                null;

        final TeapotBuilder builder = new TeapotBuilder(new EpsilonDoublePrecisionContext(1e-10));
        builder.setDebugDir(debugDir);

        builder.writeTeapot(outputFile);

        SimpleTriangleMesh mesh = builder.buildUnitCylinder(1, 3, 10, 10);
        IO3D.write(mesh, Paths.get("target/cylinder.obj"));
    }
}

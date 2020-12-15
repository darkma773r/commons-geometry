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
package org.apache.commons.geometry.examples.io.threed;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.mesh.TriangleMesh;
import org.apache.commons.geometry.examples.io.threed.facet.FacetDefinitionReader;

/** Interface for reading 3D models stored in a specific file format.
 */
public interface ModelReadHandler {

    FacetDefinitionReader facetDefinitionReader(InputStream in) throws IOException;

    BoundarySource3D read(InputStream in, DoublePrecisionContext precision) throws IOException;

    TriangleMesh readTriangleMesh(InputStream in, DoublePrecisionContext precision) throws IOException;
}

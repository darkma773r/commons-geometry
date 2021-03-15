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
package org.apache.commons.geometry.io.core.input;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileGeometryInput extends AbstractGeometryInput {

    private final Path file;

    public FileGeometryInput(final Path file) {
        this(file, null);
    }

    public FileGeometryInput(final Path file, final Charset charset) {
        super(file.getFileName().toString(), charset);

        this.file = file;
    }

    public Path getFile() {
        return file;
    }

    /** {@inheritDoc}
     *
     * <p>The returned input stream is buffered.</p>
     */
    @Override
    public InputStream getInputStream() throws IOException {
        return new BufferedInputStream(Files.newInputStream(file));
    }
}

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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GeometryIOExceptionTest {

    @Test
    public void testProperties() {
        // arrange
        Throwable thr = new Throwable();

        // act
        GeometryIOException e1 = new GeometryIOException("e1");
        GeometryIOException e2 = new GeometryIOException("e2", thr);

        // assert
        Assertions.assertEquals("e1", e1.getMessage());
        Assertions.assertNull(e1.getCause());

        Assertions.assertEquals("e2", e2.getMessage());
        Assertions.assertSame(thr, e2.getCause());
    }
}

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
package org.apache.commons.geometry.examples.io.threed.ply;

import java.util.ArrayList;
import java.util.List;

class PLYHeader {

    private final PLYConstants.Format format;

    private final List<Element> elements = new ArrayList<>();

    public PLYHeader(final PLYConstants.Format format) {
        this.format = format;
    }

    public PLYConstants.Format getFormat() {
        return format;
    }

    public List<Element> getElements() {
        return elements;
    }

    public static class Element {

        private final String name;

        private final int count;

        private final List<ElementProperty> properties = new ArrayList<>();

        public Element(final String name, final int count) {
            this.name = name;
            this.count = count;
        }

        public String getName() {
            return name;
        }

        public int getCount() {
            return count;
        }

        public List<ElementProperty> getProperties() {
            return properties;
        }
    }

    public static class ElementProperty {

        private final String name;

        private final PLYConstants.DataType dataType;

        public ElementProperty(final String name, final PLYConstants.DataType dataType) {
            this.name = name;
            this.dataType = dataType;
        }

        public String getName() {
            return name;
        }

        public PLYConstants.DataType getDataType() {
            return dataType;
        }
    }

    public static class ElementListProperty extends ElementProperty {

        private final PLYConstants.DataType countDataType;

        public ElementListProperty(final String name, final PLYConstants.DataType countDataType,
                final PLYConstants.DataType dataType) {
            super(name, dataType);
            this.countDataType = countDataType;
        }

        public PLYConstants.DataType getCountDataType() {
            return countDataType;
        }
    }
}

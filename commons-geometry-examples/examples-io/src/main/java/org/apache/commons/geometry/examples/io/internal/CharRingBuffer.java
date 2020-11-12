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
package org.apache.commons.geometry.examples.io.internal;

import java.util.NoSuchElementException;

class CharRingBuffer {

    private static final int DEFAULT_INITIAL_CAPACITY = 32;

    private static final double LOG2 = Math.log(2);

    private char[] buffer;

    private int head;

    private int numElements;

    public CharRingBuffer() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    public CharRingBuffer(final int initialCapacity) {
        this.buffer = new char[initialCapacity];
    }

    public int size() {
        return numElements;
    }

    public boolean isEmpty() {
        return numElements == 0;
    }

    public void clear() {
        numElements = 0;
    }

    public void add(final char ch) {
        ensureCapacity(numElements + 1);

        final int idx = (head + numElements) % buffer.length;
        ++numElements;

        buffer[idx] = ch;
    }

    public char remove() {
        return get(true);
    }

    public char peek() {
        return get(false);
    }

    public char get(final boolean remove) {
        if (numElements == 0) {
            throw new NoSuchElementException();
        }

        char result = buffer[head];

        if (remove) {
            --numElements;
            head = (head + 1) % buffer.length;
        }

        return result;
    }

    public void ensureCapacity(final int capacity) {
        if (capacity > buffer.length) {
            final double newCapacityPower = Math.ceil(Math.log(capacity) / LOG2);
            final int newCapacity = (int) Math.pow(2, newCapacityPower);

            final char[] newBuffer = new char[newCapacity];

            final int contiguousCount = Math.min(numElements, buffer.length - head);
            System.arraycopy(buffer, head, newBuffer, 0, contiguousCount);

            if (contiguousCount < numElements) {
                System.arraycopy(buffer, 0, newBuffer, contiguousCount, numElements - contiguousCount);
            }

            buffer = newBuffer;
            head = 0;
        }
    }
}

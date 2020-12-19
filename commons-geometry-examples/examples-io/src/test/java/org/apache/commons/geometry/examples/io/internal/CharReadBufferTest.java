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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Random;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.junit.Assert;
import org.junit.Test;

public class CharReadBufferTest {

    @Test
    public void testCtor() {
        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            new CharReadBuffer(null, 1, 1);
        }, NullPointerException.class, "Reader cannot be null");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            new CharReadBuffer(reader("a"), 0, 1);
        }, IllegalArgumentException.class, "Initial buffer capacity must be greater than 0; was 0");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            new CharReadBuffer(reader("a"), 1, 0);
        }, IllegalArgumentException.class, "Min read value must be greater than 0; was 0");
    }

    @Test
    public void testHasMoreCharacters() throws IOException {
        // act/assert
        for (int s = 1; s < 10; s += 2) {
            Assert.assertFalse(new CharReadBuffer(reader("")).hasMoreCharacters());
            Assert.assertFalse(new CharReadBuffer(reader(""), s).hasMoreCharacters());
            Assert.assertFalse(new CharReadBuffer(reader(""), s, s).hasMoreCharacters());
        }

        String str;
        for (int i = 1; i < 10; ++i) {
            str = repeat("a", i);

            for (int s = 1; s < 10; s += 2) {
                Assert.assertTrue(new CharReadBuffer(reader(str)).hasMoreCharacters());
                Assert.assertTrue(new CharReadBuffer(reader(str), s).hasMoreCharacters());
                Assert.assertTrue(new CharReadBuffer(reader(str), s, s).hasMoreCharacters());
            }
        }
    }

    @Test
    public void testPeekRead() throws IOException {
        // arrange
        String str = "abcdefg";
        CharReadBuffer buf = new CharReadBuffer(reader(str), 1);

        StringBuilder peek = new StringBuilder();
        StringBuilder read = new StringBuilder();

        // act
        while (buf.hasMoreCharacters()) {
            peek.append((char) buf.peek());
            read.append((char) buf.read());
        }

        // assert
        Assert.assertEquals(str, peek.toString());
        Assert.assertEquals(str, read.toString());

        Assert.assertEquals(-1, buf.peek());
        Assert.assertEquals(-1, buf.read());
    }

    @Test
    public void testCharAt() throws IOException {
        // arrange
        String str = "abcdefgh";
        CharReadBuffer buf = new CharReadBuffer(reader(str), 3);

        // act/assert
        Assert.assertEquals('a', buf.charAt(0));
        Assert.assertEquals('b', buf.charAt(1));
        Assert.assertEquals('c', buf.charAt(2));
        Assert.assertEquals('d', buf.charAt(3));
        Assert.assertEquals('e', buf.charAt(4));
        Assert.assertEquals('f', buf.charAt(5));
        Assert.assertEquals('g', buf.charAt(6));
        Assert.assertEquals('h', buf.charAt(7));

        Assert.assertEquals(-1, buf.charAt(8));
        Assert.assertEquals(-1, buf.charAt(9));
        Assert.assertEquals(-1, buf.charAt(10));
    }

    @Test
    public void testCharAt_invalidArg() throws IOException {
        // arrange
        String str = "abcdefgh";
        CharReadBuffer buf = new CharReadBuffer(reader(str), 3);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            buf.charAt(-1);
        }, IllegalArgumentException.class, "Character index cannot be negative; was -1");
    }

    @Test
    public void testReadPeek_string() throws IOException {
        // arrange
        String str = "abcdefgh";
        CharReadBuffer buf = new CharReadBuffer(reader(str), 50);

        // act/assert
        Assert.assertEquals("", buf.peekString(0));
        Assert.assertEquals("", buf.readString(0));

        Assert.assertEquals("abc", buf.peekString(3));
        Assert.assertEquals("abc", buf.readString(3));

        Assert.assertEquals("defgh", buf.peekString(100));
        Assert.assertEquals("defgh", buf.readString(100));

        Assert.assertEquals(null, buf.peekString(1));
        Assert.assertEquals(null, buf.readString(1));
    }

    @Test
    public void testReadPeek_tring_zeroLen() throws IOException {
        // act/assert
        Assert.assertNull(new CharReadBuffer(reader("")).peekString(0));
        Assert.assertNull(new CharReadBuffer(reader("")).readString(0));

        Assert.assertEquals("", new CharReadBuffer(reader("a")).peekString(0));
        Assert.assertEquals("", new CharReadBuffer(reader("a")).readString(0));
    }

    @Test
    public void testReadPeek_string_invalidArg() throws IOException {
        // arrange
        CharReadBuffer buf = new CharReadBuffer(reader("a"));
        String msg = "Requested string length cannot be negative; was -1";

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            buf.peekString(-1);
        }, IllegalArgumentException.class, msg);

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            buf.readString(-1);
        }, IllegalArgumentException.class, msg);
    }

    @Test
    public void testSkip() throws IOException {
        // arrange
        CharReadBuffer buf = new CharReadBuffer(reader("abcdefg"), 3);
        buf.peekString(2);

        // act/assert
        buf.skip(0);
        Assert.assertEquals("a", buf.peekString(1));

        buf.skip(1);
        Assert.assertEquals("b", buf.peekString(1));

        buf.skip(4);
        Assert.assertEquals("f", buf.peekString(1));

        buf.skip(1);
        Assert.assertEquals("g", buf.peekString(1));

        buf.skip(100);
        Assert.assertNull(buf.peekString(1));

        buf.skip(100);
        Assert.assertNull(buf.peekString(1));
    }

    @Test
    public void testSkip_invalidArg() throws IOException {
        // arrange
        CharReadBuffer buf = new CharReadBuffer(reader("a"));

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            buf.skip(-1);
        }, IllegalArgumentException.class, "Character skip count cannot be negative; was -1");
    }

    @Test
    public void testPushString_emptyReader() throws IOException {
        // arrange
        String a = "abcd";
        String b = "efgh";
        CharReadBuffer buf = new CharReadBuffer(reader(""), 1);

        // act
        buf.pushString(a);
        buf.pushString(b);

        // assert
        Assert.assertTrue(buf.hasMoreCharacters());
        Assert.assertEquals("efghabcd", buf.readString(8));
    }

    @Test
    public void testPushString_nonEmptyReader() throws IOException {
        // arrange
        String a = "abcd";
        String b = "efgh";
        CharReadBuffer buf = new CharReadBuffer(reader("ABCD"), 1);

        // act
        buf.pushString(a);
        buf.pushString(b);

        // assert
        Assert.assertTrue(buf.hasMoreCharacters());
        Assert.assertEquals("efghabcdABCD", buf.readString(12));
    }

    @Test
    public void testPush_emptyReader() throws IOException {
        // arrange
        CharReadBuffer buf = new CharReadBuffer(reader("ABCD"), 1);

        // act
        buf.push('a');
        buf.push('b');
        buf.push('c');
        buf.push('d');

        // assert
        Assert.assertTrue(buf.hasMoreCharacters());
        Assert.assertEquals("dcbaABCD", buf.readString(8));
    }

    @Test
    public void testAlternatingPushAndRead() throws IOException {
        // arrange
        String str = repeat("abcdefghijlmnopqrstuvwxyz", 10);

        CharReadBuffer buf = new CharReadBuffer(reader(str), 8);

        Random rnd = new Random(1L);

        // act
        StringBuilder result = new StringBuilder();
        String tmp;
        while (buf.hasMoreCharacters()) {
            buf.pushString("ABC");

            tmp = buf.readString(rnd.nextInt(10) + 4);

            result.append(tmp.charAt(3));

            buf.pushString(tmp.substring(4));
        }

        // assert
        Assert.assertEquals(str, result.toString());
    }

    private static Reader reader(final String content) {
        return new StringReader(content);
    }

    private static String repeat(final String str, final int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; ++i) {
            sb.append(str);
        }

        return sb.toString();
    }
}

/*
 * Copyright 2017 Patrick Favre-Bulle
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package at.favre.lib.bytes;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.UUID;

import static org.junit.Assert.*;

public class BytesConstructorTests extends ABytesTest {
    @Rule
    public final TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void wrapTest() {
        Bytes b = Bytes.wrap(example_bytes_seven);
        assertSame(example_bytes_seven, b.array());
        byte[] copy = Arrays.copyOf(example_bytes_seven, example_bytes_seven.length);
        example_bytes_seven[0] = 0;

        assertSame(example_bytes_seven, b.array());
        assertArrayNotEquals(copy, example_bytes_seven);

        Bytes b2 = Bytes.wrap(b);
        assertSame(b.array(), b2.array());

        Bytes bNullSafe = Bytes.wrapNullSafe(example_bytes_seven);
        assertSame(example_bytes_seven, bNullSafe.array());

        Bytes bNullSafe1 = Bytes.wrapNullSafe(null);
        assertEquals(0, bNullSafe1.length());
    }

    @Test(expected = NullPointerException.class)
    public void wrapTestNullExpected() {
        Bytes.wrap((byte[]) null);
    }

    @Test
    public void wrapTestNullSafe() {
        Bytes.wrapNullSafe(null);
    }

    @Test
    public void empty() {
        assertEquals(0, Bytes.empty().length());
        assertEquals(Bytes.allocate(0), Bytes.empty());
        assertArrayEquals(new byte[0], Bytes.empty().array());
        assertSame(Bytes.empty(), Bytes.empty());

        Bytes empty = Bytes.empty();
        empty = empty.byteOrder(ByteOrder.LITTLE_ENDIAN);
        assertNotSame(Bytes.empty(), empty);
    }

    @Test
    public void allocate() {
        assertEquals(0, Bytes.allocate(0).length());
        assertEquals(2, Bytes.allocate(2).length());
        assertEquals(16, Bytes.allocate(16).length());

        checkAllocate(0, (byte) 0);
        checkAllocate(2, (byte) 0);
        checkAllocate(4, (byte) 0);
        checkAllocate(4, (byte) 1);
        checkAllocate(16, (byte) 0xAE);
        checkAllocate(458, (byte) 0x00);
    }

    private void checkAllocate(int length, byte content) {
        Bytes b = Bytes.allocate(length, content);
        assertEquals(length, b.length());
        for (int i = 0; i < b.length(); i++) {
            assertEquals(content, b.array()[i]);
        }
    }

    @Test
    public void fromBitSet() {
        checkBitSet(example_bytes_empty);
        checkBitSet(example_bytes_one);
        checkBitSet(example_bytes_two);
        checkBitSet(example_bytes_seven);
        checkBitSet(example_bytes_eight);
        checkBitSet(example_bytes_twentyfour);
    }

    private void checkBitSet(byte[] array) {
        BitSet bitSet = BitSet.valueOf(array);
        assertArrayEquals(array, Bytes.of(bitSet).array());
        assertEquals(bitSet, Bytes.of(bitSet).toBitSet());
    }

    @Test
    public void fromBigInteger() {
        checkBigInteger(example_bytes_one);
        checkBigInteger(example_bytes_two);
        checkBigInteger(example_bytes_seven);
        checkBigInteger(example_bytes_eight);
        checkBigInteger(example_bytes_twentyfour);
    }

    private void checkBigInteger(byte[] array) {
        BigInteger bigInteger = new BigInteger(array);
        assertArrayEquals(array, Bytes.of(bigInteger).array());
        assertEquals(bigInteger, Bytes.of(bigInteger).toBigInteger());
    }

    @Test
    public void fromBoolean() {
        assertArrayEquals(new byte[]{(byte) 1}, Bytes.of(true).array());
        assertArrayEquals(new byte[]{(byte) 0}, Bytes.of(false).array());
    }

    @Test
    public void fromByte() {
        byte test = 0x4E;
        assertArrayEquals(new byte[]{test}, Bytes.of(test).array());
        assertArrayEquals(new byte[1], Bytes.of((byte) 0).array());
        assertEquals(test, Bytes.of(test).toByte());
    }

    @Test
    public void fromChar() {
        char test = 5821;
        byte[] primitiveArray = ByteBuffer.allocate(2).putChar(test).array();
        assertArrayEquals(primitiveArray, Bytes.of(test).array());
        assertArrayEquals(new byte[2], Bytes.of((char) 0).array());
        assertEquals(test, Bytes.of(test).toChar());
    }

    @Test
    public void fromShort() {
        short test = 12721;
        byte[] primitiveArray = ByteBuffer.allocate(2).putShort(test).array();
        assertArrayEquals(primitiveArray, Bytes.of(test).array());
        assertArrayEquals(new byte[2], Bytes.of((short) 0).array());
        assertEquals(test, Bytes.of(test).toShort());
    }

    @Test
    public void fromInt() {
        int test = 722837193;
        byte[] primitiveArray = ByteBuffer.allocate(4).putInt(test).array();
        assertArrayEquals(primitiveArray, Bytes.of(test).array());
        assertArrayEquals(new byte[4], Bytes.of(0).array());
        assertEquals(test, Bytes.of(test).toInt());
    }

    @Test
    public void fromIntArray() {
        assertArrayEquals(new byte[]{0, 0, 0, 1, 0, 0, 0, 2}, Bytes.of(1, 2).array());
        assertArrayEquals(Bytes.of(Bytes.of(871193), Bytes.of(6761), Bytes.of(-917656)).array(), Bytes.of(871193, 6761, -917656).array());
        assertArrayEquals(Bytes.of(Bytes.of(1678), Bytes.of(-223), Bytes.of(11114)).array(), Bytes.of(1678, -223, 11114).array());
        assertArrayEquals(new byte[]{0, 11, 30, 55, 0, 0, 35, 53, 0, 0, 0, 0, 0, 0, 56, -70}, Bytes.of(728631, 9013, 0, 14522).array());
    }

    @Test
    public void fromIntBuffer() {
        assertArrayEquals(new byte[]{0, 0, 0, 1, 0, 0, 0, 2}, Bytes.of(IntBuffer.wrap(new int[]{1, 2})).array());
        assertArrayEquals(Bytes.of(Bytes.of(871193), Bytes.of(6761), Bytes.of(-917656)).array(), Bytes.of(IntBuffer.wrap(new int[]{871193, 6761, -917656})).array());
        assertArrayEquals(Bytes.empty().array(), Bytes.of(IntBuffer.allocate(0)).array());
    }

    @Test
    public void fromLong() {
        long test = 172283719283L;
        byte[] primitiveArray = ByteBuffer.allocate(8).putLong(test).array();
        assertArrayEquals(primitiveArray, Bytes.of(test).array());
        assertArrayEquals(new byte[8], Bytes.of(0L).array());
        assertEquals(test, Bytes.of(test).toLong());
    }

    @Test
    public void fromLongArray() {
        assertArrayEquals(new byte[]{0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 2}, Bytes.of(new long[]{1, 2}).array());
        assertArrayEquals(Bytes.of(Bytes.of(871193L), Bytes.of(6761L), Bytes.of(-917656L)).array(), Bytes.of(new long[]{871193, 6761, -917656}).array());
        assertArrayEquals(Bytes.of(Bytes.of(1678L), Bytes.of(-223L), Bytes.of(11114L)).array(), Bytes.of(1678L, -223L, 11114L).array());
        assertArrayEquals(Bytes.of(Bytes.of(1273612831678L), Bytes.of(-72639123786223L)).array(), Bytes.of(1273612831678L, -72639123786223L).array());
    }

    @Test
    public void fromFloat() {
        float test = 63278.123f;
        byte[] primitiveArray = ByteBuffer.allocate(4).putFloat(test).array();
        assertArrayEquals(primitiveArray, Bytes.of(test).array());
        assertArrayEquals(new byte[4], Bytes.of(0f).array());
        assertEquals(test, Bytes.of(test).toFloat(), 0.01);
    }

    @Test
    public void fromDouble() {
        double test = 3423423.8923423974123;
        byte[] primitiveArray = ByteBuffer.allocate(8).putDouble(test).array();
        assertArrayEquals(primitiveArray, Bytes.of(test).array());
        assertArrayEquals(new byte[8], Bytes.of(0.0).array());
        assertEquals(test, Bytes.of(test).toDouble(), 0.01);
    }

    @Test
    public void fromByteBuffer() {
        checkByteBuffer(example_bytes_empty);
        checkByteBuffer(example_bytes_one);
        checkByteBuffer(example_bytes_two);
        checkByteBuffer(example_bytes_four);
        checkByteBuffer(example_bytes_seven);
        checkByteBuffer(example_bytes_eight);
    }

    private void checkByteBuffer(byte[] array) {
        Bytes b = Bytes.of(ByteBuffer.wrap(array));
        assertSame(array, b.array());
    }

    @Test
    public void fromString() {
        checkString("", StandardCharsets.UTF_8);
        checkString(" ", StandardCharsets.UTF_8);
        checkString("\t", StandardCharsets.UTF_8);
        checkString("a", StandardCharsets.UTF_8);
        checkString("12345678abcdefjkl", StandardCharsets.UTF_8);
        checkString("asdghasdu72Ahdans", StandardCharsets.UTF_8);
        checkString("asdghasdu72Ahdans", StandardCharsets.ISO_8859_1);
        checkString("7asdh#ö01^^`´dµ@€", StandardCharsets.UTF_8);
        checkString("7asdh#ö01^^`´dµ@€", StandardCharsets.US_ASCII);
        checkString("7asdh#ö01^^`´dµ@€", StandardCharsets.ISO_8859_1);
    }

    @Test
    public void encodeCharsetToBytes() {
        byte[][] testVectors = new byte[][]{example_bytes_seven, example_bytes_one, example_bytes_two, example2_bytes_seven, example_bytes_twentyfour};

        for (byte[] testVector : testVectors) {
            System.out.println(new String(testVector, StandardCharsets.ISO_8859_1));
            System.out.println(new String(Bytes.wrap(testVector).encodeCharsetToBytes(StandardCharsets.ISO_8859_1), StandardCharsets.ISO_8859_1));
            System.out.println(new String(testVector, StandardCharsets.UTF_8));
            System.out.println(new String(Bytes.wrap(testVector).encodeCharsetToBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));

            assertArrayEquals(new String(testVector, StandardCharsets.ISO_8859_1).getBytes(StandardCharsets.ISO_8859_1), Bytes.wrap(testVector).encodeCharsetToBytes(StandardCharsets.ISO_8859_1));
            assertArrayEquals(new String(testVector, StandardCharsets.UTF_8).getBytes(StandardCharsets.UTF_8), Bytes.wrap(testVector).encodeCharsetToBytes(StandardCharsets.UTF_8));
            assertArrayEquals(new String(testVector, StandardCharsets.UTF_8).getBytes(StandardCharsets.UTF_8), Bytes.wrap(testVector).encodeUtf8ToBytes());
        }
    }

    @Test
    public void fromCharArray() {
        checkCharArray("");
        checkCharArray(" ");
        checkCharArray("\t");
        checkCharArray("a");
        checkCharArray("12345678abcdefjkl");
        checkCharArray("é_,,(8áàäöü#+_  ,,mµ");

        String s1 = "oaisdj`ßß__.#äöü_-  *aé";
        assertArrayEquals(String.valueOf(s1.toCharArray()).getBytes(StandardCharsets.ISO_8859_1), Bytes.of(s1.toCharArray(), StandardCharsets.ISO_8859_1).array());
        assertArrayEquals(String.valueOf(s1.toCharArray()).getBytes(StandardCharsets.UTF_16), Bytes.of(s1.toCharArray(), StandardCharsets.UTF_16).array());
        assertArrayEquals(String.valueOf(s1.toCharArray()).getBytes(StandardCharsets.UTF_8), Bytes.of(s1.toCharArray(), StandardCharsets.UTF_8).array());
        assertArrayEquals(String.valueOf(s1.substring(0, 1).toCharArray()).getBytes(StandardCharsets.UTF_8), Bytes.of(s1.toCharArray(), StandardCharsets.UTF_8, 0, 1).array());
        assertArrayEquals(String.valueOf(s1.substring(3, 7).toCharArray()).getBytes(StandardCharsets.UTF_8), Bytes.of(s1.toCharArray(), StandardCharsets.UTF_8, 3, 4).array());
        assertArrayEquals(Bytes.empty().array(), Bytes.of(CharBuffer.allocate(0)).array());
    }

    @Test
    public void toCharArray() {
        String unicodeString = "|µ€@7é8ahslishalsdalöskdḼơᶉëᶆ ȋṕšᶙṁ ḍỡḽǭᵳ ʂǐť ӓṁệẗ, ĉṓɲṩḙċ";
        assertArrayEquals(Bytes.of(unicodeString.toCharArray()).array(), Bytes.of(Bytes.of(unicodeString).toCharArray()).array());

        checkToCharArray(unicodeString, StandardCharsets.UTF_8);
        checkToCharArray(unicodeString, StandardCharsets.UTF_16);
        checkToCharArray(unicodeString, StandardCharsets.UTF_16BE);

        String asciiString = "asciiASCIIString1234$%&";

        checkToCharArray(asciiString, StandardCharsets.UTF_8);
        checkToCharArray(asciiString, StandardCharsets.UTF_16);
        checkToCharArray(asciiString, StandardCharsets.US_ASCII);
        checkToCharArray(asciiString, StandardCharsets.ISO_8859_1);
    }

    private void checkToCharArray(String string, Charset charset) {
        byte[] b0 = String.valueOf(string.toCharArray()).getBytes(charset);
        char[] charArray = Bytes.of(b0).toCharArray(charset);
        assertEquals(string, new String(charArray));
        assertArrayEquals(string.toCharArray(), Bytes.of(string.toCharArray(), charset).toCharArray(charset));
    }

    @Test(expected = NullPointerException.class)
    public void toCharArrayShouldThroughNullPointer() {
        Bytes.allocate(4).toCharArray(null);
    }

    @Test
    public void fromMultipleBytes() {
        assertArrayEquals(new byte[]{0x01, 0x02, 0x03}, Bytes.of(Bytes.of((byte) 0x01), Bytes.of((byte) 0x02), Bytes.of((byte) 0x03)).array());
    }

    private void checkString(String string, Charset charset) {
        Bytes b = Bytes.of(string, charset);
        assertArrayEquals(string.getBytes(charset), b.array());
        assertEquals(new String(string.getBytes(charset), charset), b.encodeCharset(charset));

        if (charset != StandardCharsets.UTF_8) {
            Bytes bUtf8 = Bytes.of(string);
            assertArrayEquals(string.getBytes(StandardCharsets.UTF_8), bUtf8.array());
            assertEquals(new String(string.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8), bUtf8.encodeUtf8());
        } else {
            Bytes bNormalized = Bytes.of(string, Normalizer.Form.NFKD);
            assertArrayEquals(Normalizer.normalize(string, Normalizer.Form.NFKD).getBytes(charset), bNormalized.array());
        }
    }

    private void checkCharArray(String s) {
        Bytes b1 = Bytes.of(s.toCharArray());
        Bytes b2 = Bytes.of(s.toCharArray(), StandardCharsets.UTF_8);
        Bytes b3 = Bytes.of(CharBuffer.wrap(s.toCharArray()));
        assertArrayEquals(String.valueOf(s.toCharArray()).getBytes(StandardCharsets.UTF_8), b1.array());
        assertArrayEquals(String.valueOf(s.toCharArray()).getBytes(StandardCharsets.UTF_8), b2.array());
        assertArrayEquals(String.valueOf(s.toCharArray()).getBytes(StandardCharsets.UTF_8), b3.array());
    }

    @Test
    public void fromInputStream() {
        checkInputStream(example_bytes_one);
        checkInputStream(example_bytes_two);
        checkInputStream(example_bytes_four);
        checkInputStream(example_bytes_seven);
        checkInputStream(example_bytes_eight);
        checkInputStream(example_bytes_sixteen);
        checkInputStream(Bytes.random(32 * 987).array());
        checkInputStream(Bytes.random(1020 * 1104).array());
    }

    private void checkInputStream(byte[] array) {
        assertArrayEquals(array, Bytes.of(new ByteArrayInputStream(array)).array());
    }

    @Test
    public void fromInputStreamLimited() {
        Bytes data = Bytes.random(1090 * 1003);
        assertArrayEquals(data.resize(5123, BytesTransformer.ResizeTransformer.Mode.RESIZE_KEEP_FROM_ZERO_INDEX).array(), Bytes.of(new ByteArrayInputStream(data.array()), 5123).array());

        assertArrayEquals(new byte[0], Bytes.of(new ByteArrayInputStream(example_bytes_sixteen), 0).array());
        assertArrayEquals(new byte[]{0x7E}, Bytes.of(new ByteArrayInputStream(example_bytes_sixteen), 1).array());
        assertArrayEquals(new byte[]{0x7E, (byte) 0xD1}, Bytes.of(new ByteArrayInputStream(example_bytes_sixteen), 2).array());
        assertArrayEquals(new byte[]{0x7E, (byte) 0xD1, (byte) 0xFD}, Bytes.of(new ByteArrayInputStream(example_bytes_sixteen), 3).array());
        assertArrayEquals(new byte[]{0x7E, (byte) 0xD1, (byte) 0xFD, (byte) 0xAA}, Bytes.of(new ByteArrayInputStream(example_bytes_sixteen), 4).array());
        assertArrayEquals(example_bytes_sixteen, Bytes.of(new ByteArrayInputStream(example_bytes_sixteen), 128).array());
    }

    @Test
    public void fromDataInput() {
        checkDataInput(example_bytes_one);
        checkDataInput(example_bytes_two);
        checkDataInput(example_bytes_four);
        checkDataInput(example_bytes_seven);
        checkDataInput(example_bytes_eight);
        checkDataInput(example_bytes_sixteen);
        checkDataInput(Bytes.random(32 * 987).array());
    }

    private void checkDataInput(byte[] array) {
        assertArrayEquals(array, Bytes.of((DataInput) new DataInputStream(new ByteArrayInputStream(array)), array.length).array());
    }

    @Test(expected = IllegalStateException.class)
    public void fromDataInputShouldThrowException() {
        Bytes.of((DataInput) new DataInputStream(new ByteArrayInputStream(example_bytes_one)), 2);
    }

    @Test
    public void fromList() {
        checkList(example_bytes_one);
        checkList(example_bytes_two);
        checkList(example_bytes_four);
        checkList(example_bytes_seven);
        checkList(example_bytes_eight);
        checkList(example_bytes_sixteen);
    }

    private void checkList(byte[] array) {
        Bytes bList = Bytes.of(Util.Converter.toList(array));
        Bytes bLinkedList = Bytes.of(new LinkedList<>(Util.Converter.toList(array)));
        assertArrayEquals(array, bList.array());
        assertArrayEquals(array, bLinkedList.array());
    }

    @Test
    public void fromVariousBytes() {
        assertArrayEquals(example_bytes_one, Bytes.of(example_bytes_one).array());
        assertArrayEquals(example_bytes_two, Bytes.of(example_bytes_two).array());
        assertArrayEquals(example_bytes_seven, Bytes.of(example_bytes_seven).array());
        assertArrayEquals(example_bytes_sixteen, Bytes.of(example_bytes_sixteen).array());
        assertArrayNotEquals(example2_bytes_seven, Bytes.of(example_bytes_seven).array());

        assertArrayEquals(example_bytes_one, Bytes.of(example_bytes_one[0]).array());
        assertArrayEquals(new byte[1], Bytes.of((byte) 0).array());
        assertArrayNotEquals(new byte[0], Bytes.of((byte) 1).array());

        assertArrayEquals(Util.Byte.concat(example_bytes_one, example_bytes_one, example_bytes_one), Bytes.of(example_bytes_one, example_bytes_one, example_bytes_one).array());
        assertArrayEquals(Util.Byte.concat(example_bytes_two, example_bytes_seven), Bytes.of(example_bytes_two, example_bytes_seven).array());
        assertArrayEquals(Util.Byte.concat(example_bytes_one, example_bytes_sixteen), Bytes.of(example_bytes_one, example_bytes_sixteen).array());
        assertArrayNotEquals(Util.Byte.concat(example_bytes_sixteen, example_bytes_one), Bytes.of(example_bytes_one, example_bytes_sixteen).array());

        assertArrayEquals(new byte[]{1, 2, 3}, Bytes.of((byte) 1, (byte) 2, (byte) 3).array());
        assertArrayEquals(new byte[2], Bytes.of((byte) 0, (byte) 0).array());
        assertArrayNotEquals(new byte[2], Bytes.of((byte) 1, (byte) 0).array());

        assertArrayEquals(new byte[0], Bytes.ofNullSafe(null).array());
        assertArrayEquals(example_bytes_two, Bytes.ofNullSafe(example_bytes_two).array());
        assertArrayEquals(example_bytes_sixteen, Bytes.ofNullSafe(example_bytes_sixteen).array());
    }

    @Test
    public void fromPartByte() {
        assertArrayEquals(new byte[]{example_bytes_four[1]}, Bytes.of(example_bytes_four, 1, 1).array());
        assertArrayEquals(new byte[]{example_bytes_eight[4], example_bytes_eight[5], example_bytes_eight[6]}, Bytes.of(example_bytes_eight, 4, 3).array());
    }

    @Test
    public void fromFile() throws Exception {
        File tempFile = testFolder.newFile("out-test.txt");
        Bytes randomBytes = Bytes.random(500);

        try (FileOutputStream stream = new FileOutputStream(tempFile)) {
            stream.write(randomBytes.array());
        }

        assertArrayEquals(randomBytes.array(), Bytes.of(tempFile).array());
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromFileNotExisting() {
        Bytes.of(new File("doesnotexist"));
    }

    @Test
    @Ignore("setting file NOT readable does not seem to work")
    public void fromFileCannotRead() throws Exception {
        File tempFile = testFolder.newFile("out-test2.txt");
        Bytes randomBytes = Bytes.random(500);

        try (FileOutputStream stream = new FileOutputStream(tempFile)) {
            stream.write(randomBytes.array());
        }
        assertTrue(tempFile.setReadable(false));
        try {
            Bytes.of(tempFile);
            fail();
        } catch (IllegalStateException ignored) {
        }
    }

    @Test
    public void fromFileOffset() throws Exception {
        File tempFile = testFolder.newFile("out-test2.txt");
        Bytes bytes = Bytes.wrap(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18});
        try (FileOutputStream stream = new FileOutputStream(tempFile)) {
            stream.write(bytes.array());
        }

        for (int lenI = 1; lenI < bytes.length() + 1; lenI++) {
            for (int offsetI = 0; offsetI < bytes.length(); offsetI++) {
                if (offsetI + lenI > bytes.length()) break;
                assertEquals(bytes.copy(offsetI, lenI), Bytes.of(tempFile, offsetI, lenI));
            }
        }
        assertEquals(Bytes.of(tempFile), Bytes.of(tempFile, 0, (int) tempFile.length()));
    }

    @Test
    public void fromFileOffsetWithIllegalOffsetOrLength() throws Exception {
        File tempFile = testFolder.newFile("fromFileOffsetWithIllegalOffsetOrLength.txt");
        try (FileOutputStream stream = new FileOutputStream(tempFile)) {
            stream.write(new byte[]{0, 1, 2, 3});
        }

        try {
            Bytes.of(tempFile, 0, 5);
            fail();
        } catch (IllegalStateException ignored) {
        }

        try {
            Bytes.of(tempFile, 5, 1);
            fail();
        } catch (IllegalStateException ignored) {
        }
    }

    @Test
    public void fromObjectArray() {
        Byte[] objectArray = new Byte[]{0x01, 0x02, 0x03, 0x04};
        Bytes b = Bytes.of(objectArray);
        assertArrayEquals(new byte[]{0x01, 0x02, 0x03, 0x04}, b.array());
    }

    @Test
    public void fromUUID() {
        testUUID(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        testUUID(UUID.fromString("123e4567-e89b-42d3-a456-556642440000"));
        testUUID(UUID.fromString("e8e3db08-dc39-48ea-a3db-08dc3958eafb"));
        testUUID(UUID.randomUUID());
    }

    private void testUUID(UUID uuid) {
        Bytes b = Bytes.of(uuid);
        assertEquals(16, b.length());
        assertEquals(uuid, b.toUUID());
    }

    @Test(expected = NullPointerException.class)
    public void fromUUIDNullArgument() {
        Bytes.of((UUID) null);
    }

    @Test
    public void createSecureRandom() {
        assertNotEquals(Bytes.random(16), Bytes.random(16));
    }

    @Test
    public void createSecureRandomWithExplicitSecureRandom() {
        assertNotEquals(Bytes.random(16, new SecureRandom()), Bytes.random(16, new SecureRandom()));
    }

    @Test
    public void createUnsecureRandom() {
        assertNotEquals(Bytes.unsecureRandom(128), Bytes.unsecureRandom(128));
    }

    @Test
    public void createUnsecureRandomWithSeed() {
        assertEquals(Bytes.unsecureRandom(128, 4L), Bytes.unsecureRandom(128, 4L));
        assertNotEquals(Bytes.unsecureRandom(4, 3L), Bytes.unsecureRandom(4, 4L));
    }
}

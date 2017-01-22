/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.nio;

import com.jtransc.JTranscArrays;
import com.jtransc.annotation.JTranscKeep;
import libcore.io.Memory;

import java.nio.internal.SizeOf;

public final class ByteBuffer extends Buffer implements Comparable<ByteBuffer> {
	ByteOrder order;
	boolean isNativeOrder;

	public final byte[] backingArray;
	final int arrayOffset;

	private final boolean isReadOnly;
	private boolean isDirect;

	@JTranscKeep
	ByteBuffer(byte[] backingArray) {
		this(backingArray.length, backingArray, 0, false);
		this.isDirect = false;
	}

	@JTranscKeep
	ByteBuffer(byte[] backingArray, boolean isDirect) {
		this(backingArray.length, backingArray, 0, false);
		this.isDirect = isDirect;
	}

	@JTranscKeep
	private ByteBuffer(int capacity, byte[] backingArray, int arrayOffset, boolean isReadOnly) {
		super(0, capacity, null);
		this.backingArray = backingArray;
		this.arrayOffset = arrayOffset;
		this.isReadOnly = isReadOnly;
		order(ByteOrder.BIG_ENDIAN);
		if (arrayOffset + capacity > backingArray.length) {
			throw new IndexOutOfBoundsException("backingArray.length=" + backingArray.length +
				", capacity=" + capacity + ", arrayOffset=" + arrayOffset);
		}
	}


	public static ByteBuffer allocate(int capacity) {
		if (capacity < 0) throw new IllegalArgumentException("capacity < 0: " + capacity);
		return new ByteBuffer(new byte[capacity]);
	}

	public static ByteBuffer allocateDirect(int capacity) {
		if (capacity < 0) throw new IllegalArgumentException("capacity < 0: " + capacity);
		return new ByteBuffer(new byte[capacity], true);
	}

	public static ByteBuffer wrap(byte[] array) {
		return new ByteBuffer(array);
	}

	public static ByteBuffer wrap(byte[] array, int start, int byteCount) {
		JTranscArrays.checkOffsetAndCount(array.length, start, byteCount);
		ByteBuffer buf = new ByteBuffer(array);
		buf.position = start;
		buf.limit = start + byteCount;
		return buf;
	}

	@Override
	public final byte[] array() {
		_checkWritable();
		return backingArray;
	}

	@Override
	public final int arrayOffset() {
		return protectedArrayOffset();
	}

	public final CharBuffer asCharBuffer() {
		return ByteBufferAsCharBuffer.asCharBuffer(this);
	}

	public final DoubleBuffer asDoubleBuffer() {
		return ByteBufferAsDoubleBuffer.asDoubleBuffer(this);
	}

	public final FloatBuffer asFloatBuffer() {
		return ByteBufferAsFloatBuffer.asFloatBuffer(this);
	}

	public final IntBuffer asIntBuffer() {
		return ByteBufferAsIntBuffer.asIntBuffer(this);
	}

	public final LongBuffer asLongBuffer() {
		return ByteBufferAsLongBuffer.asLongBuffer(this);
	}

	public final ShortBuffer asShortBuffer() {
		return ByteBufferAsShortBuffer.asShortBuffer(this);
	}


	@Override
	public int compareTo(ByteBuffer otherBuffer) {
		int compareRemaining = (remaining() < otherBuffer.remaining()) ? remaining()
			: otherBuffer.remaining();
		int thisPos = position;
		int otherPos = otherBuffer.position;
		byte thisByte, otherByte;
		while (compareRemaining > 0) {
			thisByte = get(thisPos);
			otherByte = otherBuffer.get(otherPos);
			if (thisByte != otherByte) return thisByte < otherByte ? -1 : 1;
			thisPos++;
			otherPos++;
			compareRemaining--;
		}
		return remaining() - otherBuffer.remaining();
	}

	public ByteBuffer duplicate() {
		return copy(this, mark, isReadOnly);
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof ByteBuffer)) {
			return false;
		}
		ByteBuffer otherBuffer = (ByteBuffer) other;

		if (remaining() != otherBuffer.remaining()) {
			return false;
		}

		int myPosition = position;
		int otherPosition = otherBuffer.position;
		boolean equalSoFar = true;
		while (equalSoFar && (myPosition < limit)) {
			equalSoFar = get(myPosition++) == otherBuffer.get(otherPosition++);
		}

		return equalSoFar;
	}

	public ByteBuffer get(byte[] dst) {
		return get(dst, 0, dst.length);
	}

	@Override
	public final boolean hasArray() {
		return protectedHasArray();
	}

	@Override
	public int hashCode() {
		int myPosition = position;
		int hash = 0;
		while (myPosition < limit) hash += get(myPosition++);
		return hash;
	}

	public final boolean isDirect() {
		return isDirect;
	}

	public final ByteOrder order() {
		return order;
	}

	public final ByteBuffer order(ByteOrder byteOrder) {
		if (byteOrder == null) byteOrder = ByteOrder.LITTLE_ENDIAN;
		order = byteOrder;
		isNativeOrder = byteOrder == ByteOrder.nativeOrder();
		return this;
	}

	public final ByteBuffer put(byte[] src) {
		return put(src, 0, src.length);
	}

	public ByteBuffer put(ByteBuffer src) {
		_checkWritable();
		if (src == this) throw new IllegalArgumentException("src == this");
		int srcByteCount = src.remaining();
		if (srcByteCount > remaining()) throw new BufferOverflowException();
		while (src.hasRemaining()) this.put(src.get());
		return this;
	}

	public ByteBuffer putChar(int index, char value) {
		_checkWritable();
		checkIndex(index, SizeOf.CHAR);
		Memory.pokeShort(backingArray, arrayOffset + index, (short) value, order);
		return this;
	}

	public ByteBuffer putChar(char value) {
		_checkWritable();
		int newPosition = position + SizeOf.CHAR;
		if (newPosition > limit) throw new BufferOverflowException();
		Memory.pokeShort(backingArray, arrayOffset + position, (short) value, order);
		position = newPosition;
		return this;
	}

	public ByteBuffer putDouble(double value) {
		return putLong(Double.doubleToRawLongBits(value));
	}

	public ByteBuffer putDouble(int index, double value) {
		return putLong(index, Double.doubleToRawLongBits(value));
	}

	public ByteBuffer putFloat(float value) {
		return putInt(Float.floatToRawIntBits(value));
	}

	public ByteBuffer putFloat(int index, float value) {
		return putInt(index, Float.floatToRawIntBits(value));
	}

	public ByteBuffer putInt(int value) {
		_checkWritable();
		int newPosition = position + SizeOf.INT;
		if (newPosition > limit) throw new BufferOverflowException();
		Memory.pokeInt(backingArray, arrayOffset + position, value, order);
		position = newPosition;
		return this;
	}

	public ByteBuffer putInt(int index, int value) {
		_checkWritable();
		checkIndex(index, SizeOf.INT);
		Memory.pokeInt(backingArray, arrayOffset + index, value, order);
		return this;
	}

	public ByteBuffer putLong(int index, long value) {
		_checkWritable();
		checkIndex(index, SizeOf.LONG);
		Memory.pokeLong(backingArray, arrayOffset + index, value, order);
		return this;
	}

	public ByteBuffer putLong(long value) {
		_checkWritable();
		int newPosition = position + SizeOf.LONG;
		if (newPosition > limit) throw new BufferOverflowException();
		Memory.pokeLong(backingArray, arrayOffset + position, value, order);
		position = newPosition;
		return this;
	}

	public ByteBuffer putShort(int index, short value) {
		_checkWritable();
		checkIndex(index, SizeOf.SHORT);
		Memory.pokeShort(backingArray, arrayOffset + index, value, order);
		return this;
	}

	public ByteBuffer putShort(short value) {
		_checkWritable();
		int newPosition = position + SizeOf.SHORT;
		if (newPosition > limit) throw new BufferOverflowException();
		Memory.pokeShort(backingArray, arrayOffset + position, value, order);
		position = newPosition;
		return this;
	}

	public ByteBuffer slice() {
		return new ByteBuffer(remaining(), backingArray, arrayOffset + position, isReadOnly);
	}

	private static ByteBuffer copy(ByteBuffer other, int markOfOther, boolean isReadOnly) {
		ByteBuffer buf = new ByteBuffer(other.capacity(), other.backingArray, other.arrayOffset, isReadOnly);
		buf.limit = other.limit;
		buf.position = other.position();
		buf.mark = markOfOther;
		return buf;
	}

	public ByteBuffer asReadOnlyBuffer() {
		return copy(this, mark, true);
	}

	public ByteBuffer compact() {
		_checkWritable();
		System.arraycopy(backingArray, position + arrayOffset, backingArray, arrayOffset, remaining());
		position = limit - position;
		limit = capacity;
		mark = UNSET_MARK;
		return this;
	}

	public boolean isReadOnly() {
		return isReadOnly;
	}

	private void _checkWritable() {
		if (isReadOnly) throw new ReadOnlyBufferException();
	}

	byte[] protectedArray() {
		_checkWritable();
		return backingArray;
	}

	int protectedArrayOffset() {
		_checkWritable();
		return arrayOffset;
	}

	boolean protectedHasArray() {
		return !isReadOnly;
	}

	//public ByteBuffer get(byte[] dst, int dstOffset, int byteCount) {
	//	JTranscArrays.checkOffsetAndCount(dst.length, dstOffset, byteCount);
	//	if (byteCount > remaining()) throw new BufferUnderflowException();
	//	for (int i = dstOffset; i < dstOffset + byteCount; ++i) dst[i] = get();
	//	return this;
	//}

	public final ByteBuffer get(byte[] dst, int dstOffset, int byteCount) {
		checkGetBounds(1, dst.length, dstOffset, byteCount);
		System.arraycopy(backingArray, arrayOffset + position, dst, dstOffset, byteCount);
		position += byteCount;
		return this;
	}

	final void get(char[] dst, int dstOffset, int charCount) {
		int byteCount = checkGetBounds(SizeOf.CHAR, dst.length, dstOffset, charCount);
		//Memory.unsafeBulkGet(dst, dstOffset, byteCount, backingArray, arrayOffset + position, SizeOf.CHAR, order.needsSwap);
		position += byteCount;
		throw new RuntimeException("Not implemented");
	}

	final void get(double[] dst, int dstOffset, int doubleCount) {
		int byteCount = checkGetBounds(SizeOf.DOUBLE, dst.length, dstOffset, doubleCount);
		//Memory.unsafeBulkGet(dst, dstOffset, byteCount, backingArray, arrayOffset + position, SizeOf.DOUBLE, order.needsSwap);
		position += byteCount;
		throw new RuntimeException("Not implemented");
	}

	final void get(float[] dst, int dstOffset, int floatCount) {
		int byteCount = checkGetBounds(SizeOf.FLOAT, dst.length, dstOffset, floatCount);
		//Memory.unsafeBulkGet(dst, dstOffset, byteCount, backingArray, arrayOffset + position, SizeOf.FLOAT, order.needsSwap);
		position += byteCount;
		throw new RuntimeException("Not implemented");
	}

	final void get(int[] dst, int dstOffset, int intCount) {
		int byteCount = checkGetBounds(SizeOf.INT, dst.length, dstOffset, intCount);
		//Memory.unsafeBulkGet(dst, dstOffset, byteCount, backingArray, arrayOffset + position, SizeOf.INT, order.needsSwap);
		position += byteCount;
		throw new RuntimeException("Not implemented");
	}

	final void get(long[] dst, int dstOffset, int longCount) {
		int byteCount = checkGetBounds(SizeOf.LONG, dst.length, dstOffset, longCount);
		//Memory.unsafeBulkGet(dst, dstOffset, byteCount, backingArray, arrayOffset + position, SizeOf.LONG, order.needsSwap);
		position += byteCount;
		throw new RuntimeException("Not implemented");

	}

	final void get(short[] dst, int dstOffset, int shortCount) {
		int byteCount = checkGetBounds(SizeOf.SHORT, dst.length, dstOffset, shortCount);
		//Memory.unsafeBulkGet(dst, dstOffset, byteCount, backingArray, arrayOffset + position, SizeOf.SHORT, order.needsSwap);
		position += byteCount;
		throw new RuntimeException("Not implemented");
	}

	public final byte get() {
		if (position == limit) throw new BufferUnderflowException();
		return backingArray[arrayOffset + position++];
	}

	public final byte get(int index) {
		checkIndex(index);
		return backingArray[arrayOffset + index];
	}

	public final char getChar() {
		int newPosition = position + SizeOf.CHAR;
		if (newPosition > limit) throw new BufferUnderflowException();
		char result = (char) Memory.peekShort(backingArray, arrayOffset + position, order);
		position = newPosition;
		return result;
	}

	public final char getChar(int index) {
		checkIndex(index, SizeOf.CHAR);
		return (char) Memory.peekShort(backingArray, arrayOffset + index, order);
	}

	public final double getDouble() {
		return Double.longBitsToDouble(getLong());
	}
	public final double getDouble(int index) {
		return Double.longBitsToDouble(getLong(index));
	}
	public final float getFloat() {
		return Float.intBitsToFloat(getInt());
	}
	public final float getFloat(int index) {
		return Float.intBitsToFloat(getInt(index));
	}

	public final int getInt() {
		int newPosition = position + SizeOf.INT;
		if (newPosition > limit) throw new BufferUnderflowException();
		int result = Memory.peekInt(backingArray, arrayOffset + position, order);
		position = newPosition;
		return result;
	}

	public final int getInt(int index) {
		checkIndex(index, SizeOf.INT);
		return Memory.peekInt(backingArray, arrayOffset + index, order);
	}

	public final long getLong() {
		int newPosition = position + SizeOf.LONG;
		if (newPosition > limit) throw new BufferUnderflowException();
		long result = Memory.peekLong(backingArray, arrayOffset + position, order);
		position = newPosition;
		return result;
	}

	public final long getLong(int index) {
		checkIndex(index, SizeOf.LONG);
		return Memory.peekLong(backingArray, arrayOffset + index, order);
	}

	public final short getShort() {
		int newPosition = position + SizeOf.SHORT;
		if (newPosition > limit) throw new BufferUnderflowException();
		short result = Memory.peekShort(backingArray, arrayOffset + position, order);
		position = newPosition;
		return result;
	}

	public final short getShort(int index) {
		checkIndex(index, SizeOf.SHORT);
		return Memory.peekShort(backingArray, arrayOffset + index, order);
	}

	public ByteBuffer put(byte b) {
		_checkWritable();
		if (position == limit) {
			throw new BufferOverflowException();
		}
		backingArray[arrayOffset + position++] = b;
		return this;
	}

	public ByteBuffer put(int index, byte b) {
		_checkWritable();
		checkIndex(index);
		backingArray[arrayOffset + index] = b;
		return this;
	}

	//public ByteBuffer put(byte[] src, int srcOffset, int byteCount) {
	//	JTranscArrays.checkOffsetAndCount(src.length, srcOffset, byteCount);
	//	if (byteCount > remaining()) {
	//		throw new BufferOverflowException();
	//	}
	//	for (int i = srcOffset; i < srcOffset + byteCount; ++i) {
	//		put(src[i]);
	//	}
	//	return this;
	//}

	public ByteBuffer put(byte[] src, int srcOffset, int byteCount) {
		_checkWritable();
		checkPutBounds(1, src.length, srcOffset, byteCount);
		System.arraycopy(src, srcOffset, backingArray, arrayOffset + position, byteCount);
		position += byteCount;
		return this;
	}

}

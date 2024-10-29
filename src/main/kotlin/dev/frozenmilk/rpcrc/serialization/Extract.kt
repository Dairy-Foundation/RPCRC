@file:JvmName("Extract")
package dev.frozenmilk.rpcrc.serialization

import java.io.BufferedInputStream

fun BufferedInputStream.expect(expected: Collection<Int>, orElse: Runnable = Runnable {}) {
	expected.forEach {
		if (it != this.read()) orElse.run()
	}
}

fun BufferedInputStream.expect(expected: Int, orElse: Runnable = Runnable {}) {
	if (this.read() != expected) orElse.run()
}

fun BufferedInputStream.extractLong() : Long = (this.read().toLong() shl 56) or (this.read().toLong() shl 48) or (this.read().toLong() shl 40) or (this.read().toLong() shl 32) or (this.read().toLong() shl 24) or (this.read().toLong() shl 16) or (this.read().toLong() shl 8) or this.read().toLong()
fun BufferedInputStream.extractULong() : ULong = ((this.read().toLong() shl 56) or (this.read().toLong() shl 48) or (this.read().toLong() shl 40) or (this.read().toLong() shl 32) or (this.read().toLong() shl 24) or (this.read().toLong() shl 16) or (this.read().toLong() shl 8) or this.read().toLong()).toULong()
fun BufferedInputStream.extractInt() : Int = (this.read() shl 24) or (this.read() shl 16) or (this.read() shl 8) or this.read()
fun BufferedInputStream.extractUInt() : UInt = ((this.read() shl 24) or (this.read() shl 16) or (this.read() shl 8) or this.read()).toUInt()
fun BufferedInputStream.extractShort() : Short = ((this.read() shl 8) or (this.read())).toShort()
fun BufferedInputStream.extractUShort() : UShort = ((this.read() shl 8) or (this.read())).toUShort()
fun BufferedInputStream.extractByte() : Byte = this.read().toByte()
fun BufferedInputStream.extractUByte() : UByte = this.read().toUByte()

/**
 * collects until [predicate] returns false
 *
 * [predicate] is given `current sequence, newest value, length` as arguments.
 *
 * the first iteration is always `(empty, Promoter.EMPTY, 0u)`
 */
fun BufferedInputStream.collectWhile(predicate: (Sequence<Int>, Int, UInt) -> Boolean) : Sequence<Int> {
	var collector = sequenceOf<Int>()
	var len = 0u
	var next = Promoter.EMPTY
	while (predicate(collector, next, len)) {
		next = read()
		collector += next
		len = len.inc()
	}
	return collector
}
/**
 * collects until [predicate] returns false
 *
 * [predicate] is given `current sequence, newest value, length` as arguments.
 *
 * the first iteration is always `(empty, Promoter.EMPTY, 0u)`
 */
fun BufferedInputStream.collectWhileLong(predicate: (Sequence<Int>, Int, ULong) -> Boolean) : Sequence<Int> {
	var collector = sequenceOf<Int>()
	var len: ULong = 0u
	var next = Promoter.EMPTY
	while (predicate(collector, next, len)) {
		next = read()
		collector += next
		len = len.inc()
	}
	return collector
}
@file:JvmName("Parsers")
package dev.frozenmilk.rpcrc.parse

import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.CharsetDecoder
import java.nio.charset.CharsetEncoder
import java.nio.charset.CodingErrorAction

@FunctionalInterface
@JvmDefaultWithoutCompatibility
fun interface Parser<T> {
	fun ByteBuffer.parse(): Pair<T, ByteBuffer>?
}

fun String.toUTF8() = CharBuffer.wrap(this).toUTF8()
fun CharBuffer.toUTF8(): ByteBuffer = ENCODER.encode(this)

val ENCODER: CharsetEncoder = Charsets.UTF_8.newEncoder()
	.onMalformedInput(CodingErrorAction.REPORT)
	.onUnmappableCharacter(CodingErrorAction.REPORT)
val DECODER: CharsetDecoder = Charsets.UTF_8.newDecoder()
	.onMalformedInput(CodingErrorAction.REPORT)
	.onUnmappableCharacter(CodingErrorAction.REPORT)

fun <T, U> Parser<T>.flatMap(transform: (T) -> Parser<U>) = Parser {
	parse()?.run { transform(first).run { second.parse() } }
}

fun <T, U> Parser<T>.map(transform: (T) -> U) = Parser {
	parse()?.run { transform(first) to second }
}

fun <T> Parser<T>.filter(predicate: (T) -> Boolean) = map {
	if (predicate(it)) it
	else null
}

infix fun <A, B> Parser<A>.then(b: Parser<B>): Parser<Pair<A, B>> = Parser {
	parse()?.run aRes@{
		b.run {
			second.parse()?.run {
				(this@aRes.first to first) to second
			}
		}
	}
}

infix fun <A, B> Parser<A>.thenLeft(b: Parser<B>) = (this then b).map { (l, _) -> l }

infix fun <A, B> Parser<A>.thenRight(b: Parser<B>) = (this then b).map { (_, r) -> r }

infix fun <A, B> Parser<A>.either(b: Parser<B>): Parser<Pair<A?, B?>> = Parser {
	slice().parse()?.run {
		(first to null) to second
	} ?: b.run {
		this@Parser.slice().parse()?.run {
			(null to first) to second
		}
	}
}

infix fun <T> Parser<T>.or(b: Parser<T>): Parser<T> = Parser {
	slice().parse() ?: b.run { slice().parse() }
}

fun <T> firstMatch(parsers: List<Parser<T>>): Parser<T> = Parser {
	parsers.firstOrNull()?.run {
		slice().parse() ?: firstMatch(parsers.drop(1)).run { parse() }
	}
}

fun <T> Parser<T>.optional(): Parser<T?> = Parser {
	slice().parse() ?: (null to this)
}

fun <T> Parser<T>.many(): Parser<List<T>> = Parser {
	val list = mutableListOf<T>()
	var buf = this
	while (true) {
		buf.slice().parse()?.run {
			list.add(first)
			buf = second
		} ?: break
	}
	list to buf
}

fun <T: Any> Parser<T>.many(end: Parser<*>): Parser<List<T>> = Parser {
	val switch = end either this@many
	val list = mutableListOf<T>()
	var buf = this
	var cont = true
	while (cont) {
		switch.run {
			buf.slice().parse()?.run attempt@{
				val (_, con) = first
				if (con != null) {
					list.add(con)
					buf = second
				}
				else {
					return@attempt null
				}
			} ?: run { cont = false }
		}
	}
	list to buf
}

fun <T> Parser<T>.manyMin(min: Int = 1): Parser<List<T>> = Parser {
	many().run {
		parse()?.run {
			if (first.size >= min) this
			else null
		}
	}
}

fun <T: Any> Parser<T>.manyMin(min: Int = 1, end: Parser<*>): Parser<List<T>> = Parser {
	many(end).run {
		parse()?.run {
			if (first.size >= min) this
			else null
		}
	}
}

fun <T> Parser<T>.expectEnd(): Parser<T> = Parser {
	parse()?.run {
		if (second.hasRemaining()) null
		else this
	}
}

fun <T> Parser<T>.repeat(times: Int): Parser<List<T>> = Parser {
	val list = ArrayList<T>(times)
	var buf = this
	for (i in 0 until times) {
		buf.parse()?.run {
			list.add(first)
			buf = second
		} ?: return@Parser null
	}
	list to buf
}

fun match(buf: ByteBuffer): Parser<Unit> = Parser {
	while (buf.hasRemaining()) {
		if (!hasRemaining()) {
			buf.rewind()
			return@Parser null
		}
		if (buf.get() != get()) {
			buf.rewind()
			return@Parser null
		}
	}
	buf.rewind()
	Unit to this
}

fun matchUTF8(string: String) = match(string.toUTF8()).map { string }

@JvmOverloads
fun <T> Parser<T>.throws(
	message: String? = null,
	cause: Throwable? = null
): Parser<T> = Parser { parse() ?: throw ParseException(message, cause) }
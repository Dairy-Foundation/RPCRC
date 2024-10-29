package dev.frozenmilk.rpcrc.routing

import dev.frozenmilk.rpcrc.parse.DECODER
import dev.frozenmilk.rpcrc.parse.ParseException
import dev.frozenmilk.rpcrc.parse.Parser
import dev.frozenmilk.rpcrc.parse.expectEnd
import dev.frozenmilk.rpcrc.parse.manyMin
import dev.frozenmilk.rpcrc.parse.map
import dev.frozenmilk.rpcrc.parse.matchUTF8
import dev.frozenmilk.rpcrc.parse.optional
import dev.frozenmilk.rpcrc.parse.or
import dev.frozenmilk.rpcrc.parse.then
import dev.frozenmilk.rpcrc.parse.thenRight
import dev.frozenmilk.rpcrc.parse.throws
import dev.frozenmilk.rpcrc.parse.toUTF8
import java.nio.ByteBuffer
import java.util.function.Function

fun path(path: String) = pathSequence.run { path.toUTF8().parse()!!.first }

private const val PATH_SEPARATOR: Byte = 0x2F
private val untilEndSegment: Parser<ByteBuffer> = Parser {
	if (!hasRemaining()) return@Parser null
	val decode = slice()
	while (decode.hasRemaining() && decode.get() != PATH_SEPARATOR) {
		get()
	}
	if (decode.hasRemaining()) {
		decode.position(decode.position() - 1)
	}
	decode.limit(decode.position())
	decode.rewind()
	decode to this
}

private val keySegment = matchUTF8("/:") thenRight untilEndSegment
private val wildcardSegment = matchUTF8("/*") thenRight untilEndSegment
private val baseSegment = (matchUTF8("/") thenRight untilEndSegment)
private val segment = baseSegment.map {
	if (it.hasRemaining()) {
		val first = it.get(it.position())
		if (first == 0x3A.toByte()) throw ParseException(cause = IllegalArgumentException("Segment starts with illegal character :"))
		else if (first == 0x2A.toByte()) throw ParseException(cause = IllegalArgumentException("Segment starts with illegal character *"))
	}
	it
}

private val key = keySegment.map { buf1 ->
	val key = DECODER.decode(buf1).toString();
	if (key.isEmpty()) throw IllegalArgumentException("cannot have empty key name");
	{ next: Parser<Function<String, String?>>? ->
		// this is a segment, mapped to return a function
		// the function returns the capture of the segment if it is queried with the name of this capture
		// otherwise it returns null
		val self = baseSegment.map { buf2 ->
			Function { query: String -> if (query == key) DECODER.decode(buf2).toString() else null }
		}
		if (next != null) {
			// if next is not null, then map the two together, so that next is found next
			(self then next).map { (l, r) ->
				Function { query: String -> l.apply(query) ?: r.apply(query) }
			}
		}
		else {
			self
		}
	}
}

private val wildcard = wildcardSegment.map { buf1 ->
	val key = DECODER.decode(buf1).toString();
	if (key.isEmpty()) throw IllegalArgumentException("cannot have empty wildcard name");
	baseSegment.manyMin().map { buf2 ->
		Function { query: String -> if (query == key) buf2.joinToString(separator = "/") { DECODER.decode(it) } else null }
	}
}

private val sequence = segment.manyMin(end = matchUTF8("/:") or matchUTF8("/*")).map { buf2 ->
		val toMatch = buf2.joinToString(prefix = "/", separator = "/") { DECODER.decode(it) };
		{ next: Parser<Function<String, String?>>? ->
			if (next != null) {
				matchUTF8(toMatch) thenRight next
			}
			else {
				// this is an end segment
				matchUTF8(toMatch).map { Function<String, String?> { _: String -> null } }
			}
		}
	}

// yes this is stupid
private fun <L, R> Parser<Pair<L, R?>>.endPathSequence() = Parser {
	parse()?.run {
		if (first.second != null) {
			if (second.hasRemaining()) throw ParseException("cannot have further path segments after a wildcard")
		}
		else {
			if (second.hasRemaining()) throw ParseException("path finishes with an illegal /")
		}
		this
	}
}

private val pathSequence =
	// match actual path
	(key or sequence)
		.manyMin(end = matchUTF8("/*"))
		.throws("path does not start with a root /")
		.then(wildcard.optional())
		.endPathSequence()
		.map { (l, r) ->
			r?.run {
				l.foldRight(r) { function, acc: Parser<Function<String, String?>> ->
					function(acc)
				}
			} ?: l.foldRight(null) { function, acc: Parser<Function<String, String?>>? ->
				function(acc)
			}!!.expectEnd()
		}
		// or root
		.or(matchUTF8("/").expectEnd().map { matchUTF8("/").map { Function<String, String?> { _: String -> null } } })

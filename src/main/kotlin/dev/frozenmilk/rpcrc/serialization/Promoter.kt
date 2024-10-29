package dev.frozenmilk.rpcrc.serialization

import dev.frozenmilk.rpcrc.Connection
import dev.frozenmilk.rpcrc.message.request.PartialRequest
import dev.frozenmilk.rpcrc.message.request.RequestBase
import dev.frozenmilk.rpcrc.message.response.PartialResponse
import dev.frozenmilk.rpcrc.message.response.ResponseBase
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.function.BiFunction

/**
 * part of the [Connection] deserialization pipeline
 *
 * upgrades the input [IN] into [OUT]
 */
@FunctionalInterface
fun interface Promoter<IN, OUT> {
	fun promote(connection: Connection, partial: IN, socket: SocketChannel): OUT
	operator fun <PIPE> plus(promoter: Promoter<OUT, PIPE>) = Promoter<IN, PIPE> { connection, partial, dataStream ->
		promoter.promote(connection, promote(connection, partial, dataStream), dataStream)
	}
	companion object Util {
		fun <DATA, REQUEST: RequestBase<DATA>> requestPromoter(deserializer: Deserializer<DATA>, factory: BiFunction<PartialRequest, DATA, REQUEST>) = Promoter { _, partial: PartialRequest, socket -> factory.apply(partial, deserializer.run { socket.deserialize() }) }
		fun <DATA, REQUEST: ResponseBase<DATA>> responsePromoter(deserializer: Deserializer<DATA>, factory: BiFunction<PartialResponse, DATA, REQUEST>) = Promoter { _, partial: PartialResponse, socket -> factory.apply(partial, deserializer.run { socket.deserialize() }) }
		fun serializeToUTF8(string: String) = CHARSET.encode(string).extract().asList().map { it.toInt() }
		fun deserializeFromUTF8(utf8: Collection<Int>) = CHARSET.decode(ByteBuffer.wrap(utf8.map { it.toByte() }.toByteArray())).toString()
		/**
		 * UTF-8 repr of '\n'
		 */
		const val NEW_LINE = 0xa
		/**
		 * UTF-8 repr of ' '
		 */
		const val SPACE = 0x20
		/**
		 * this returned by the socket while it is empty
		 */
		const val EMPTY = 0xff

		val CHARSET: Charset = StandardCharsets.UTF_8
	}
}

/**
 * doesn't cause [this](https://stackoverflow.com/questions/42912994/null-characters-added-when-a-string-is-encoded-into-utf-8-bytes) issue, by only extracting the occupied array
 *
 * use instead of [ByteBuffer.array]
 */
fun ByteBuffer.extract() : ByteArray {
	val res = ByteArray(limit())
	get(res)
	return res
}

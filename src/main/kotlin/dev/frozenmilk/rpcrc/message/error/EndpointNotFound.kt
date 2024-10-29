package dev.frozenmilk.rpcrc.message.error

import dev.frozenmilk.rpcrc.parse.DECODER
import dev.frozenmilk.rpcrc.parse.toUTF8
import dev.frozenmilk.rpcrc.serialization.Serde
import dev.frozenmilk.rpcrc.serialization.Serializer
import java.nio.ByteBuffer

class EndpointNotFound(
	callbackID: UShort,
	endpoint: ByteBuffer
) : RPCRCError(
	0xFD_u,
	callbackID,
	object : Serializer<Unit> {
		override fun Unit.size() = start.limit() + Serde.VariableLength.RAW.serializer.run { endpoint.size() }

		override fun Unit.serialize(buf: ByteBuffer) {
			Serde.VariableLength.lenSerializer.run {
				size().serialize(buf)
			}
			buf.put(start)
			buf.put(endpoint)
		}
	}
) {
	override val message by lazy {
		try {
			DECODER.decode(endpoint.rewind()).toString()
		}
		catch (e: Throwable) {
			cause = e
			"something went wrong decoding the path"
		}
	}
}

private val start = "Endpoint not found: ".toUTF8()
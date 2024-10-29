package dev.frozenmilk.rpcrc.message.error

import dev.frozenmilk.rpcrc.serialization.Serializable
import dev.frozenmilk.rpcrc.serialization.Serializer
import java.nio.ByteBuffer
import kotlin.experimental.and
import kotlin.experimental.or

open class RPCRCError(var status: UByte = 1u, var callbackID: UShort = 0u, val serializer: Serializer<Unit>) : Throwable(), Serializable {
	final override var cause: Throwable? = null

	// byte of `1` bit + 7 bit status_code + word for callback_id + data size + word for sentinel
	override fun size() = 1 + 2 + serializer.run { Unit.size() } + 2
	override fun serialize(buf: ByteBuffer) {
		serializer.run {
			buf
				.put((status.toByte() and 0x7F) or 0x80.toByte())
				// callback_id
				.putShort(callbackID.toShort())
				// data
				.putT(Unit)
				// sentinel \n newlines in utf8
				.put(0xa)
				.put(0xa)
		}
	}
}
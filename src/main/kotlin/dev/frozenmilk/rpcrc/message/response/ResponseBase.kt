package dev.frozenmilk.rpcrc.message.response

import dev.frozenmilk.rpcrc.serialization.Serde
import java.nio.ByteBuffer
import kotlin.experimental.and
import kotlin.experimental.or

open class ResponseBase<DATA>(status: UByte, callbackID: UShort, final override var data: DATA, val serde: Serde<DATA>) : Response<DATA>, PartialResponse(status, callbackID) {
	// byte of `1` bit + 7 bit status_code + word for callback_id + serde size + word for sentinel
	override fun size() = 1 + 2 + serde.run { data.size() } + 2
	override fun serialize(buf: ByteBuffer) {
		serde.apply {
			buf
				.put((status.toByte() and 0x7F) or 0x80.toByte())
				// callback_id
				.putShort(callbackID.toShort())
				// data
				.putT(data)
				// sentinel \n newlines in utf8
				.put(0x0A)
				.put(0x0A)
		}
	}
}
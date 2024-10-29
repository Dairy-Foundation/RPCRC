package dev.frozenmilk.rpcrc.message.request

import dev.frozenmilk.rpcrc.serialization.Serde
import java.nio.ByteBuffer

open class RequestBase<DATA>(path: ByteBuffer, callbackID: UShort, final override var data: DATA, val serde: Serde<DATA>) : Request<DATA>, PartialRequest(path, callbackID) {
	// word of `0` bit + 15 bit path length + path length + word for callback id + serde size + word for sentinel
	override fun size() = 2 + path.limit() + 2 + serde.run { data.size() } + 2
	override fun serialize(buf: ByteBuffer) {
		serde.apply {
			buf
				// path length, first bit is fixed at 0 to indicate request
				.putShort((path.limit() and 0x7FFF).toShort())
				// path itself
				.put(path.rewind())
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

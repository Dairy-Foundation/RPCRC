package dev.frozenmilk.rpcrc.message.standard

import dev.frozenmilk.rpcrc.message.request.PartialRequest
import dev.frozenmilk.rpcrc.message.request.RequestBase
import dev.frozenmilk.rpcrc.parse.ENCODER
import dev.frozenmilk.rpcrc.serialization.Promoter
import dev.frozenmilk.rpcrc.serialization.Serde
import java.nio.ByteBuffer
import java.nio.CharBuffer

open class Ask(path: ByteBuffer, callbackID: UShort) : RequestBase<Unit>(
	path,
	callbackID,
	Unit,
	Serde.UNIT
) {
	private constructor(partialRequest: PartialRequest) : this(partialRequest.path, partialRequest.callbackID)
	constructor(path: ByteBuffer) : this(path, 0u)
	constructor(path: String) : this(ENCODER.encode(CharBuffer.wrap(path)), 0u)
	companion object {
		val promoter = Promoter.requestPromoter(Serde.UNIT) { partial, _ -> Ask(partial) }
	}
}
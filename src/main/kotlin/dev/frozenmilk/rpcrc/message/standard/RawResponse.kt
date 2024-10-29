package dev.frozenmilk.rpcrc.message.standard

import dev.frozenmilk.rpcrc.message.response.PartialResponse
import dev.frozenmilk.rpcrc.message.response.ResponseBase
import dev.frozenmilk.rpcrc.serialization.Promoter
import dev.frozenmilk.rpcrc.serialization.Serde
import java.nio.ByteBuffer

class RawResponse(
	status: UByte,
	callbackID: UShort,
	data: ByteBuffer
) : ResponseBase<ByteBuffer>(
	status,
	callbackID,
	data,
	serde
) {
	constructor(partial: PartialResponse, data: ByteBuffer) : this(partial.status, partial.callbackID, data)
	companion object {
		val serde = Serde.VariableLength.RAW
		val promoter = Promoter.responsePromoter(serde) { partial, data ->
			RawResponse(partial, data)
		}
	}
}
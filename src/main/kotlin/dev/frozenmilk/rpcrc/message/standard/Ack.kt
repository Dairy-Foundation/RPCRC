package dev.frozenmilk.rpcrc.message.standard

import dev.frozenmilk.rpcrc.message.response.PartialResponse
import dev.frozenmilk.rpcrc.message.response.ResponseBase
import dev.frozenmilk.rpcrc.serialization.Promoter
import dev.frozenmilk.rpcrc.serialization.Serde

open class Ack(
	status: UByte,
	callbackID: UShort
) : ResponseBase<Unit>(
	status,
	callbackID,
	Unit,
	Serde.UNIT
) {
	constructor(partialResponse: PartialResponse) : this(partialResponse.status, partialResponse.callbackID)
	constructor(callbackID: UShort) : this(0u, callbackID)
	companion object {
		val promoter = Promoter.responsePromoter(Serde.UNIT) { partial, _ -> Ack(partial) }
	}
}
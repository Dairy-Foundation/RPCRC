package dev.frozenmilk.rpcrc.reserved.rpc.ping

import dev.frozenmilk.rpcrc.message.request.PartialRequest
import dev.frozenmilk.rpcrc.message.request.RequestBase
import dev.frozenmilk.rpcrc.parse.toUTF8
import dev.frozenmilk.rpcrc.serialization.Promoter
import dev.frozenmilk.rpcrc.serialization.Serde
import java.nio.ByteBuffer

class PingRequest
private constructor(path: ByteBuffer, callbackID: UShort, data: ByteBuffer) : RequestBase<ByteBuffer>(
	path, callbackID, data, serde
) {
	private constructor(partialRequest: PartialRequest, data: ByteBuffer) : this(partialRequest.path, partialRequest.callbackID, data)
	constructor(path: ByteBuffer, data: ByteBuffer) : this(path, 0u, data)
	constructor(data: ByteBuffer) : this("/rpc/ping".toUTF8(), 0u, data)

	companion object {
		val serde = Serde.VariableLength.RAW
		val promoter = Promoter.requestPromoter(serde) { partial, data -> PingRequest( partial, data ) }
	}
}

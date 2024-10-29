package dev.frozenmilk.rpcrc.reserved.rpc.disconnect

import dev.frozenmilk.rpcrc.message.request.PartialRequest
import dev.frozenmilk.rpcrc.message.standard.Ask
import dev.frozenmilk.rpcrc.parse.toUTF8
import java.nio.ByteBuffer

internal class DisconnectRequest private constructor(path: ByteBuffer, callbackID: UShort) : Ask(path, callbackID) {
	constructor(partialRequest: PartialRequest) : this(partialRequest.path, partialRequest.callbackID)
	constructor() : this(PATH, 0u)
	companion object {
		val PATH = "/rpc/disconnect".toUTF8()
	}
}
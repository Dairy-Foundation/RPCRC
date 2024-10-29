package dev.frozenmilk.rpcrc.handling

import dev.frozenmilk.rpcrc.Connection
import dev.frozenmilk.rpcrc.message.error.RPCRCError
import dev.frozenmilk.rpcrc.message.response.PartialResponse
import dev.frozenmilk.rpcrc.message.response.Response
import dev.frozenmilk.rpcrc.serialization.Promoter
import java.util.concurrent.CompletableFuture

interface ResponseHandler <RESPONSE: Response<*>> {
	val responsePromoter: Promoter<PartialResponse, RESPONSE>
	fun handleResponse(connection: Connection, future: CompletableFuture<RESPONSE>, response: RESPONSE)
	fun handleError(connection: Connection, future: CompletableFuture<RESPONSE>, error: RPCRCError)
}
package dev.frozenmilk.rpcrc.reserved.rpc.ping

import dev.frozenmilk.rpcrc.Connection
import dev.frozenmilk.rpcrc.handling.RequestHandler
import dev.frozenmilk.rpcrc.handling.ResponseHandler
import dev.frozenmilk.rpcrc.message.error.RPCRCError
import dev.frozenmilk.rpcrc.message.standard.RawResponse
import dev.frozenmilk.rpcrc.routing.path
import java.util.concurrent.CompletableFuture
import java.util.function.Function

object PingHandler : RequestHandler<PingRequest>, ResponseHandler<RawResponse> {
	override val path = path("/rpc/ping")
	override val requestPromoter = PingRequest.promoter
	override val responsePromoter = RawResponse.promoter

	override fun handleError(connection: Connection, future: CompletableFuture<RawResponse>, error: RPCRCError) {
		future.completeExceptionally(error)
		connection.freeCallbackID(error.callbackID)
	}

	override fun handleResponse(connection: Connection, future: CompletableFuture<RawResponse>, response: RawResponse) {
		response.data.rewind()
		future.complete(response)
		connection.freeCallbackID(response.callbackID)
	}

	override fun handleRequest(
		connection: Connection,
		captures: Function<String, String?>,
		request: PingRequest
	) {
		connection.sendResponse(RawResponse(0u, request.callbackID, request.data))
	}
}

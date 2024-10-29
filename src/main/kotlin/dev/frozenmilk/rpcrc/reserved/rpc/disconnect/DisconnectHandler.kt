package dev.frozenmilk.rpcrc.reserved.rpc.disconnect

import dev.frozenmilk.rpcrc.Connection
import dev.frozenmilk.rpcrc.handling.RequestHandler
import dev.frozenmilk.rpcrc.handling.ResponseHandler
import dev.frozenmilk.rpcrc.message.standard.Ack
import dev.frozenmilk.rpcrc.message.error.RPCRCError
import dev.frozenmilk.rpcrc.routing.path
import dev.frozenmilk.rpcrc.serialization.Promoter
import dev.frozenmilk.rpcrc.serialization.Serde
import java.util.concurrent.CompletableFuture
import java.util.function.Function

internal object DisconnectHandler : RequestHandler<DisconnectRequest>, ResponseHandler<Ack> {
	override val path = path("/rpc/disconnect")
	override val requestPromoter = Promoter.requestPromoter(Serde.UNIT) { partial, _ -> DisconnectRequest( partial ) }
	override val responsePromoter = Ack.promoter

	override fun handleError(connection: Connection, future: CompletableFuture<Ack>, error: RPCRCError) {
		future.completeExceptionally(error)
	}

	override fun handleResponse(connection: Connection, future: CompletableFuture<Ack>, response: Ack) {
		future.complete(response)
	}

	// other end has requested close
	override fun handleRequest(
		connection: Connection,
		captures: Function<String, String?>,
		request: DisconnectRequest
	) {
		// close connection to new operations
		connection.publicClose()
		// send response, we'll use the internal API, as the published one is closed
		connection.send(Ack(request.callbackID))
		// finish internal close
		connection.totalClose()
	}
}
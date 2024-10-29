package dev.frozenmilk.rpcrc.handling

import dev.frozenmilk.rpcrc.Connection
import dev.frozenmilk.rpcrc.message.error.RPCRCError
import dev.frozenmilk.rpcrc.message.request.PartialRequest
import dev.frozenmilk.rpcrc.message.request.Request
import dev.frozenmilk.rpcrc.parse.Parser
import dev.frozenmilk.rpcrc.serialization.Promoter
import java.util.function.Function

interface RequestHandler <REQUEST: Request<*>> {
	val path: Parser<Function<String, String?>>
	val requestPromoter: Promoter<PartialRequest, REQUEST>
	@Throws(RPCRCError::class)
	fun handleRequest(connection: Connection, captures: Function<String, String?>, request: REQUEST)
}
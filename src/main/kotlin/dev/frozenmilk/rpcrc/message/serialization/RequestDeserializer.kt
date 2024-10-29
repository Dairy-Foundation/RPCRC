package dev.frozenmilk.rpcrc.message.serialization

import dev.frozenmilk.rpcrc.Connection
import dev.frozenmilk.rpcrc.handling.RequestHandler
import dev.frozenmilk.rpcrc.message.error.EndpointNotFound
import dev.frozenmilk.rpcrc.message.error.UnterminatedPacket
import dev.frozenmilk.rpcrc.message.request.PartialRequest
import dev.frozenmilk.rpcrc.message.request.Request
import dev.frozenmilk.rpcrc.serialization.Promoter
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.util.function.Function

object RequestDeserializer : Promoter<UShort, Unit> {
	override fun promote(connection: Connection, partial: UShort, socket: SocketChannel) {
		val pathLen = partial.toInt()
		val buf = ByteBuffer.allocateDirect(pathLen + 2) // path length + callback ID word, we'll split these later
		socket.read(buf)

		val endpointPath = buf.rewind().slice().limit(pathLen)
		val callbackID = buf.getShort(pathLen).toUShort()
		val (endpoint, captures) = connection.router[endpointPath] ?: throw EndpointNotFound(callbackID, endpointPath)

		endpoint.handleRequest (
			connection, captures, PartialRequest(endpointPath, callbackID), socket
		)
	}
}

private val buf = ByteBuffer.allocateDirect(2)
private fun <REQUEST: Request<*>> RequestHandler<REQUEST>.handleRequest(connection: Connection, captures: Function<String, String?>, partialRequest: PartialRequest, socket: SocketChannel) {
	val promoted = requestPromoter.promote(connection, partialRequest, socket)
	synchronized(buf) {
		socket.read(buf.rewind())
		if (buf.getShort(0) != 0x0A0A.toShort()) throw UnterminatedPacket(partialRequest.callbackID)
	}
	connection.submit {
		handleRequest(connection, captures, promoted)
	}
}

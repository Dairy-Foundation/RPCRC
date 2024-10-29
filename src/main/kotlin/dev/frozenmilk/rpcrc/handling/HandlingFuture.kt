package dev.frozenmilk.rpcrc.handling

import dev.frozenmilk.rpcrc.Connection
import dev.frozenmilk.rpcrc.message.error.UnterminatedPacket
import dev.frozenmilk.rpcrc.message.response.PartialResponse
import dev.frozenmilk.rpcrc.message.response.Response
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.util.concurrent.CompletableFuture

data class HandlingFuture<RESPONSE: Response<*>>(val handler: ResponseHandler<RESPONSE>, val future: CompletableFuture<RESPONSE>) {
	fun handleResponse(connection: Connection, partialResponse: PartialResponse, socket: SocketChannel) = handler.handleResponse(connection, future, partialResponse, socket)
}

private val buf = ByteBuffer.allocateDirect(2)
private fun <RESPONSE: Response<*>> ResponseHandler<RESPONSE>.handleResponse(connection: Connection, future: CompletableFuture<RESPONSE>, partialResponse: PartialResponse, socket: SocketChannel) {
	val promoted = responsePromoter.promote(connection, partialResponse, socket)
	synchronized(buf) {
		socket.read(buf.rewind())
		if (buf.getShort(0) != 0x0A0A.toShort()) throw UnterminatedPacket(partialResponse.callbackID)
	}
	connection.submit {
		handleResponse(connection, future, promoted)
	}
}

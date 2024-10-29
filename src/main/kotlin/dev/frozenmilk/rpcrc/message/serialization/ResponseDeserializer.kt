package dev.frozenmilk.rpcrc.message.serialization

import dev.frozenmilk.rpcrc.Connection
import dev.frozenmilk.rpcrc.message.response.PartialResponse
import dev.frozenmilk.rpcrc.serialization.Promoter
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

object ResponseDeserializer : Promoter<UByte, Unit> {
	private val buf = ByteBuffer.allocateDirect(2)
	override fun promote(connection: Connection, statusCode: UByte, socket: SocketChannel) {
		val callbackID = synchronized(buf) {
			socket.read(buf.rewind())
			buf.getShort(0).toUShort()
		}

		// todo, handle broadcast errors
		// > take a snapshot of all current (id, response handler) pairs, marking them as 'dangerous' for this broadcast
		// > when each of the ids are returned,
		// 		unmark them as dangerous until the pool is down to only one response handler / reduced as much as possible (i.e. two broad cast errors share the same two response handlers)
		// 		then, once its down to one id ( / the multiple situation described before), recycle it manually, and give the error to the handlers

		if (callbackID == 0xFFFF.toUShort()) {
			//TODO("handle broadcast error, see above")
		}

		connection[callbackID]?.handleResponse(
			connection,
			PartialResponse(statusCode, callbackID),
			socket
		) ?: throw RuntimeException("unregistered callback") // TODO, change to rpcrc error
	}
}

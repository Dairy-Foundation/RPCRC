package dev.frozenmilk.rpcrc.message.serialization

import dev.frozenmilk.rpcrc.Connection
import dev.frozenmilk.rpcrc.serialization.Promoter
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

object MessageDeserializer : Promoter<Nothing?, Unit>  {
	private val buf = ByteBuffer.allocateDirect(2)
	override fun promote(connection: Connection, partial: Nothing?, socket: SocketChannel) {
		synchronized(buf) {
			socket.read(buf.rewind().limit(1))
			val first = buf.get(0).toUByte()
			return if (first and 0x80.toUByte() == 0x00.toUByte()) {
				// request
				buf.limit(2)
				socket.read(buf)
				RequestDeserializer.promote(connection, buf.getShort(0).toUShort() and 0x7FFFu, socket)
			}
			else {
				// response
				ResponseDeserializer.promote(connection, first and 0x7Fu, socket)
			}
		}
	}
}

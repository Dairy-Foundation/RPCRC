package dev.frozenmilk.rpcrc.message.request

import dev.frozenmilk.rpcrc.serialization.Serializable
import java.nio.ByteBuffer

interface Request<DATA> : Serializable {
	val path: ByteBuffer
	var callbackID: UShort
	val data: DATA
}
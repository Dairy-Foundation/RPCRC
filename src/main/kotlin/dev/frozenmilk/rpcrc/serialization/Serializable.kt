package dev.frozenmilk.rpcrc.serialization

import java.nio.ByteBuffer

interface Serializable {
	fun size(): Int
	fun serialize(buf: ByteBuffer)
}
package dev.frozenmilk.rpcrc.serialization

import java.nio.ByteBuffer

@JvmDefaultWithoutCompatibility
interface Serializer<T> {
	fun T.size(): Int
	fun T.serialize(buf: ByteBuffer)
	fun ByteBuffer.putT(t: T): ByteBuffer {
		this@Serializer.run {
			t.serialize(this@putT)
		}
		return this
	}
}
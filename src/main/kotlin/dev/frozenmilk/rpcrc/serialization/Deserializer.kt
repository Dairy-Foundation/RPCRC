package dev.frozenmilk.rpcrc.serialization

import java.nio.channels.SocketChannel

@FunctionalInterface
@JvmDefaultWithoutCompatibility
fun interface Deserializer<T> {
	fun SocketChannel.deserialize(): T
}


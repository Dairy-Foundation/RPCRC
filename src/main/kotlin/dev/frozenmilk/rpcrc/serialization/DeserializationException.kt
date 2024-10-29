package dev.frozenmilk.rpcrc.serialization

class DeserializationException @JvmOverloads constructor(override val message: String? = null, override val cause: Throwable? = null) : RuntimeException()
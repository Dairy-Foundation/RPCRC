package dev.frozenmilk.rpcrc.parse

class ParseException @JvmOverloads constructor(
	override val message: String? = null,
	override val cause: Throwable? = null
) : RuntimeException()
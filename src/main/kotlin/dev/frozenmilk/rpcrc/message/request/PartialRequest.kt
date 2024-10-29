package dev.frozenmilk.rpcrc.message.request

import java.nio.ByteBuffer

open class PartialRequest (
	/**
	 * UTF8 Encoded Path
	 */
	val path: ByteBuffer,
	/**
	 * when sending a request, this value may not be valid until sent
	 */
	var callbackID: UShort
)
package dev.frozenmilk.rpcrc.message.error

import dev.frozenmilk.rpcrc.serialization.Serde

class UnterminatedPacket(
	callbackID: UShort,
) : RPCRCError(
	0xFD_u,
	callbackID,
	Serde.UNIT
) {
	override val message = "callback id $callbackID was unterminated"
}

package dev.frozenmilk.rpcrc.message.response

import dev.frozenmilk.rpcrc.serialization.Serializable

interface Response<DATA> : Serializable {
	var callbackID: UShort
	var status: UByte
	var data: DATA
}
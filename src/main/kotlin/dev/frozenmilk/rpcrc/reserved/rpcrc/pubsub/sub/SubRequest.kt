//package dev.frozenmilk.rpcrc.reserved.rpcrc.pubsub.sub
//
//import dev.frozenmilk.rpcrc.message.getValue
//import dev.frozenmilk.rpcrc.message.request.PartialRequest
//import dev.frozenmilk.rpcrc.message.request.RequestBase
//import dev.frozenmilk.rpcrc.message.setValue
//import dev.frozenmilk.rpcrc.reserved.rpcrc.pubsub.PubSub
//import dev.frozenmilk.rpcrc.serialization.Serializer
//
//class SubRequest (partial: PartialRequest) : RequestBase<SubList>(
//	partial.headers,
//	Serializer { it.serialize() }
//) {
//	/**
//	 * `callback_id` is not set until sent via [dev.frozenmilk.rpcrc.Connection.sendRequest]
//	 */
//	constructor(subList: SubList) : this(PartialRequest(mutableMapOf(
//		"method_id" to PubSub.SubHandler.path,
////		"timestamp" to System.nanoTime(), // todo review if this is the right time to apply it
//		"data" to subList
//	)))
//	var timestamp: Long by "timestamp"
//}
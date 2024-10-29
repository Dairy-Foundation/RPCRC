//package dev.frozenmilk.rpcrc.reserved.rpcrc.pubsub
//
//import dev.frozenmilk.rpcrc.Connection
//import dev.frozenmilk.rpcrc.handling.RequestHandler
//import dev.frozenmilk.rpcrc.message.request.PartialRequest
//import dev.frozenmilk.rpcrc.reserved.rpcrc.pubsub.pub.PubRequest
//import dev.frozenmilk.rpcrc.reserved.rpcrc.pubsub.pub.PublishedValues
//import dev.frozenmilk.rpcrc.reserved.rpcrc.pubsub.sub.SubList
//import dev.frozenmilk.rpcrc.reserved.rpcrc.pubsub.sub.SubRequest
//import dev.frozenmilk.rpcrc.routing.Path
//import dev.frozenmilk.rpcrc.serialization.Promoter
//import java.util.WeakHashMap
//
//object PubSub {
//	private val subscriptionMap = WeakHashMap<Connection, SubTree>()
//	private val publishTree = PublishedValues()
//	object PubHandler : RequestHandler<PubRequest> {
//		override val path = Path("/rpcrc/pub")
//		override val requestPromoter: Promoter<PartialRequest, PubRequest> = TODO()
//
//		override fun handleRequest(connection: Connection, request: PubRequest) {
//			var incoming = request.data
//			var updater = publishTree
//		}
//	}
//
//	object SubHandler : RequestHandler<SubRequest> {
//		override val path = Path("/rpcrc/sub")
//		// todo: add timestamping header to this, so that patches can be applied in timestamped order if sent out of order
//		override val requestPromoter = Promoter.dataRequestPromoter(SubList::deserialize) { SubRequest( it ) }
//
//		override fun handleRequest(connection: Connection, request: SubRequest) {
//			request.data.asList()
//				.forEach { (subState, path) ->
//					if (subState) {
//						subscriptionMap.compute(connection) { _, v ->
//							(v ?: SubTree()).also {
//								it.subscribe(path)
//							}
//						}
//					}
//					else subscriptionMap[connection]?.unsubscribe(path)
//				}
//		}
//	}
//}
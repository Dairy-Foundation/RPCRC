package dev.frozenmilk.rpcrc.reserved.rpc

import dev.frozenmilk.rpcrc.reserved.rpc.disconnect.DisconnectHandler
import dev.frozenmilk.rpcrc.reserved.rpc.ping.PingHandler
import dev.frozenmilk.rpcrc.routing.Router

open class RPCRouter : Router() {
	init {
		route(PingHandler)
		route(DisconnectHandler)
	}
}
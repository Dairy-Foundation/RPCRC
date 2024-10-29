package dev.frozenmilk.rpcrc.routing

import dev.frozenmilk.rpcrc.handling.RequestHandler
import dev.frozenmilk.rpcrc.parse.Parser
import dev.frozenmilk.rpcrc.parse.map
import dev.frozenmilk.rpcrc.parse.or
import dev.frozenmilk.rpcrc.parse.toUTF8
import java.nio.ByteBuffer
import java.util.function.Function

open class Router private constructor(private var parser: Parser<Pair<RequestHandler<*>, Function<String, String?>>>) {
	constructor() : this({ null })
	fun route(handler: RequestHandler<*>): Router {
		parser = handler.path.map { handler to it } or parser
		return this
	}
	operator fun get(buf: ByteBuffer) = parser.run { buf.parse() }?.first

	/**
	 * converts [str] to UTF8 first
	 */
	operator fun get(str: String) = this[str.toUTF8()]
}

//open class Router private constructor(private val methods: RouteMap) {
//	constructor() : this(RouteMap(null))
//
//	/**
//	 * maps [handler]'s [RequestHandler.path] to [handler]
//	 *
//	 * e.g.
//	 * ```kt
//	 * Router()
//	 * 	.route(PingHandler)
//	 * ```
//	 *
//	 * @return self, for chaining
//	 */
//	fun route(handler: RequestHandler<*>): Router {
//		val node = if (handler.path.isEmpty()) methods
//		else { methods.getOrDefault(handler.path, null) }
//
//		if (node.contents != null) throw IllegalStateException("path ${handler.path} already has a handler (${node.contents}) routed")
//
//		node.contents = handler
//		return this
//	}
//
//	fun remove(path: Path): Router {
//		if (path.isEmpty()) methods.contents = null
//		else if (methods.containsKey(path)) methods.getOrDefault(path, null)
//		return this
//	}
//
//	operator fun get(path: Path) = (if (path.isEmpty()) methods.contents else methods[path])
//	operator fun get(path: Collection<String>) = get(Path(path))
//	operator fun get(vararg path: String) = get(Path(*path))
//	operator fun get(path: String) = get(Path(path))
//}
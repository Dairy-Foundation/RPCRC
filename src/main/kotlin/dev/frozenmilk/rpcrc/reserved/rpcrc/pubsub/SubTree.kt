//package dev.frozenmilk.rpcrc.reserved.rpcrc.pubsub
//
//import dev.frozenmilk.rpcrc.routing.Path
//import dev.frozenmilk.rpcrc.routing.PathNode
//import dev.frozenmilk.util.tree.Tree
//
///**
// * a class that represents a hierarchical subscribed / unsubscribed pattern, supporting `*` and `**` glob pattern matching
// */
//open class SubTree {
//	private val targets = Tree<PathNode, Boolean>(false)
//
//	fun subscribe(target: Path): SubTree {
//		targets.getOrDefault(target, false).contents = true
//		return this
//	}
//
//	fun unsubscribe(target: Path): SubTree {
//		targets.remove(target)
//		return this
//	}
//
//	/**
//	 * throws [IllegalArgumentException] if last node of [path] is `*` or `**`
//	 */
//	fun determineSubscription(path: Path) : Boolean {
//		if (arrayOf("*", "**").contains(path.lastOrNull())) throw IllegalArgumentException("$path: last PathNode cannot be * or ** when determining the subscription")
//		var searcher = targets
//		val last = path.size - 1
//		path.forEachIndexed { i, pathNode ->
//			if (i == last && pathNode == "*") return true
//			searcher = searcher.getChild(pathNode) ?: return (i == last && pathNode == "**") // early fail
//		}
//		return searcher.contents
//	}
//
//	// note: probably slower than the other solution
//	private fun determineSubscriptionRecursively(path: Path) : Boolean {
//		if (arrayOf("*", "**").contains(path.lastOrNull())) throw IllegalArgumentException("$path: last PathNode cannot be * or ** when determining the subscription")
//		return targets[path] ?: run {
//			val subPath = Path(path.dropLast(1)) // drop the last item
//			targets[subPath + "*"] ?: determineGlobStarSubscription(subPath) // glob star is allowed to recurse
//		}
//	}
//
//	private tailrec fun determineGlobStarSubscription(path: Path) : Boolean {
//		return targets[path + "**"] ?: determineGlobStarSubscription(Path(path.dropLast(1)))
//	}
//}

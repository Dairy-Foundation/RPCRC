//package dev.frozenmilk.rpcrc.reserved.rpcrc.pubsub.sub
//
//import dev.frozenmilk.rpcrc.routing.Path
//import dev.frozenmilk.rpcrc.serialization.Deserializer
//import dev.frozenmilk.rpcrc.serialization.Promoter
//import dev.frozenmilk.rpcrc.serialization.Serializable
//import java.util.stream.Collectors.toList
//
//@JvmInline
//value class SubList private constructor(private val innerList : MutableList<Pair<Boolean, Path>>) : Serializable {
//	constructor() : this(mutableListOf())
//	override fun serialize() =
//		Deserializer.serializeToUTF8(innerList.fold("") { acc, pair ->
//			"$acc${if (pair.first) '+' else '-'}${pair.second}\n"
//		}).asSequence()
//
//	fun subscribe(path: Path) = this.innerList.add(true to path)
//	fun unsubscribe(path: Path) = this.innerList.add(false to path)
//
//	fun asList(): List<Pair<Boolean, Path>> = innerList
//
//	companion object {
//		fun IntIterator.deserialize() : SubList {
//			return SubList(
//				Promoter.deserializeFromUTF8(this.col)
//					.split('\n')
//					.filter(String::isNotBlank)
//					.map {
//						val first = it.getOrElse(0) { throw TODO() }
//						when (first) {
//							'+' -> true to Path(it.substring(1))
//							'-' -> false to Path(it.substring(1))
//							else -> throw TODO("0x2, invalid line")
//						}
//					}
//					.toCollection(mutableListOf())
//			)
//		}
//	}
//}
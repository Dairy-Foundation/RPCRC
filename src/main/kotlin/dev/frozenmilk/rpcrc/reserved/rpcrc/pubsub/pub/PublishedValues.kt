//package dev.frozenmilk.rpcrc.reserved.rpcrc.pubsub.pub
//
//import dev.frozenmilk.rpcrc.routing.PathNode
//import dev.frozenmilk.rpcrc.serialization.Serializable
//import dev.frozenmilk.util.tree.Tree
//
//// TODO replacing this with a FUSE
//class PublishedValues : Tree<PathNode, PubSubCell<*>?>(null), Serializable {
//	/**
//	 * encodes the update tree to the following per-path format:
//	 *
//	 * `/<path> <type indicator><[additional type indicator data]>[: <data>] `
//	 *
//	 * outside of this encoding the `.` character indicates a step back up the tree
//	 *
//	 * # Type Indicators:
//	 *
//	 * the type indicator takes up a single byte, where the first two bits are reserved to indicate read-write access
//	 *
//	 * `0b1_______` reading is allowed
//	 *
//	 * `0b0_______` reading is not allowed
//	 *
//	 * `0b_1______` writing is allowed
//	 *
//	 * `0b_0______` writing is not allowed
//	 *
//	 * this means that the remainder of the type indicator data must be in the range `[0x00, 0x3f]`
//	 *
//	 * `[: <data>]` this shall be referred to as the 'data-actual', all type indicators specify the length of <data> in some way
//	 *
//	 * some type indicators specify mandatory additional information that should follow the first byte
//	 *
//	 * ## Null
//	 * `0x00` = null, no additional data required, no data-actual
//	 *
//	 * ## Boolean
//	 * `0x01` = boolean, no additional data required, data-actual is a single byte where `0x00` is false, and all other values are true, but true should be written as `0x01`
//	 *
//	 * ## Signed and Unsigned Int
//	 * ```
//	 * 0xAB
//	 *    ^ -- size of data in bits divided by 4
//	 *   ^ -- 1 -> signed, 0 -> unsigned
//	 * ```
//	 *
//	 * `0x02` = unsigned byte / u8, no additional data required, single byte of data-actual
//	 *
//	 * `0x12` = signed byte / i8, no additional data required, single byte of data-actual
//	 *
//	 * `0x04` = unsigned short / u16, no additional data required, 2 bytes of data-actual
//	 *
//	 * `0x14` = signed short / i16, no additional data required, 2 bytes of data-actual
//	 *
//	 * `0x08` = unsigned int / u32, no additional data required, 4 bytes of data-actual
//	 *
//	 * `0x18` = signed int / i32, no additional data required, 4 bytes of data-actual
//	 *
//	 * `0x0f` = unsigned long / u64, no additional data required, 8 bytes of data-actual
//	 *
//	 * `0x1f` = signed long / i64, no additional data required, 8 bytes of data-actual
//	 *
//	 * ## Float and Double
//	 * `0x20` = float, no additional data required, 4 bytes of data-actual
//	 * `0x30` = double, no additional data required, 8 bytes of data-actual
//	 *
//	 * ## Other
//	 * variable length encoded data that must be deserialized on the other end
//	 * `0x22` = additional data is an unsigned byte / u8 representing the length of the serialized data
//	 * `0x24` = additional data is an unsigned short / u16 representing the length of the serialized data
//	 * `0x28` = additional data is an unsigned int / u32 representing the length of the serialized data
//	 * `0x2f` = additional data is an unsigned long / u64 representing the length of the serialized data
//	 *
//	 * ## Array
//	 * variable length encoded data of another type.
//	 * additional data is a number of bytes to specify the length, followed by a second data type definition.
//	 *
//	 * the data-actual must be `(array length * datatype size)`
//	 *
//	 * this works with variable length data, but every array slot must be the same length.
//	 *
//	 * variable length data is better suited if the items are going to be different sizes
//	 *
//	 * `0x32` = length is an unsigned byte / u8 representing the length of the array
//	 * `0x34` = length is an unsigned short / u16 representing the length of the array
//	 * `0x38` = length is an unsigned int / u32 representing the length of the array
//	 * `0x3f` = length is an unsigned long / u64 representing the length of the array
//	 *
//	 * the second datatype definition is used to determine the size of the array contents
//	 *
//	 * ```
//	 * 	/robot {
//	 * 		/constants {
//	 * 			/a = ab
//	 * 			/b = ba
//	 * 		}
//	 * 		/var {
//	 * 			/a = vara {
//	 * 				/b = varab
//	 * 			}
//	 * 			/null = null
//	 * 			/num = 10
//	 * 		}
//	 * 	}
//	 * ```
//	 *
//	 * ~ encodes to:
//	 *
//	 * TODO: REDO
//	 * ```
//	 * /robot/constants/a [0xf2][0x02]: ab
//	 * ./b [0xf2][0x02]: ba
//	 * ../var/a [0xf2][0x04]: vara
//	 * /b [0xf2][0x05]: varab
//	 * ./null [0x00]
//	 * ./num [0x02]: [0x0a]
//	 * ```
//	 *
//	 * note the use of `[...]` to indicate byte encodings in hex form
//	 *
//	 * additionally, new lines were added to separate each separate variable, but these are not included in the actual encoding
//	 */
//	override fun serialize(): Sequence<Int> {
//		TODO()
//	}
//
//	//fun recurseSerialise(sequence: Sequence<Int>): Sequence<Int> {
//	//	var out = sequence
//	//	this.contents?.also {
//	//		out += it.serialize()
//	//	}
//	//	out = this.children.values.fold(out) { acc, tree ->
//	//		acc +
//	//	}
//	//	return out
//	//}
//}

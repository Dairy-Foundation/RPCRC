package dev.frozenmilk.rpcrc.serialization

import dev.frozenmilk.rpcrc.parse.ParseException
import dev.frozenmilk.rpcrc.parse.Parser
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

sealed class Serde<T> : Serializer<T>, Parser<T>, Deserializer<T> {
	enum class TypeID(val byte: Byte) : Deserializer<Unit> {
		UNIT(0x00),
		BOOLEAN(0x01),
		U8(0x02),
		I8(0x12),
		U16(0x04),
		I16(0x14),
		U32(0x08),
		I32(0x18),
		U64(0x0F),
		I64(0x1F),
		VAR8(0x22),
		VAR16(0x24),
		;

		override fun toString() = byte.toString(16)

		override fun SocketChannel.deserialize() {
			synchronized(buf) {
				read(buf.rewind())
				if (buf.get(0) != byte) throw DeserializationException("wrong type, expected $byte, was ${buf.get(0).toString(16)}")
			}
		}

		companion object {
			private val buf = ByteBuffer.allocateDirect(1)
			@JvmStatic
			fun forByte(byte: Byte) = when (byte) {
				0x00.toByte() -> UNIT
				0x01.toByte() -> BOOLEAN
				0x02.toByte() -> U8
				0x13.toByte() -> I8
				0x04.toByte() -> U16
				0x14.toByte() -> I16
				0x08.toByte() -> U32
				0x18.toByte() -> I32
				0x0F.toByte() -> U64
				0x1F.toByte() -> I64
				0x22.toByte() -> VAR8
				0x24.toByte() -> VAR16
				else -> throw IllegalArgumentException("$byte does not have a corresponding TypeID")
			}
			val deserializer = Deserializer {
				synchronized(buf) {
					read(buf.rewind())
					forByte(buf.get(0))
				}
			}
		}
	}
	internal abstract val serializer: Serializer<T>
	internal abstract val parser: Parser<T>

	/**
	 * deserializes the byte stream to a bytebuffer to then be parsed
	 *
	 * note: must not handle the [TypeID] of this serde
	 */
	protected abstract fun SocketChannel.deserialize(typeID: TypeID): ByteBuffer
	final override fun T.size() = serializer.run { this@size.size() }
	final override fun T.serialize(buf: ByteBuffer) = serializer.run { this@serialize.serialize(buf) }
	final override fun ByteBuffer.parse() = parser.run { this@parse.parse() }
	final override fun SocketChannel.deserialize() = parser.run {
		// deserializes the rest of the message to a ByteBuffer
		this@deserialize.deserialize(TypeID.deserializer.run {
			// deserializes the TypeID
			this@deserialize.deserialize()
		}).parse() // parses the result
	}?.first ?: throw ParseException("something went wrong while parsing this message")

	abstract override fun toString(): String

	object UNIT : Serde<Unit>() {
		override val parser = Parser { Unit to this }
		override val serializer = object : Serializer<Unit> {
			override fun Unit.size() = 1
			override fun Unit.serialize(buf: ByteBuffer) { buf.put(TypeID.UNIT.byte) }
		}
		private val empty = ByteBuffer.allocateDirect(0)
		override fun SocketChannel.deserialize(typeID: TypeID): ByteBuffer {
			if (typeID != TypeID.UNIT) throw DeserializationException("Wrong type: expected ${TypeID.UNIT}, found $typeID")
			return empty
		}

		override fun toString() = "UNIT"
	}

	object BOOLEAN : Serde<Boolean>() {
		override val serializer = object : Serializer<Boolean> {
			override fun Boolean.size() = 2
			override fun Boolean.serialize(buf: ByteBuffer) {
				buf
					.put(TypeID.BOOLEAN.byte)
					.put(if (this) 0x01 else 0x00)
			}
		}
		override val parser: Parser<Boolean> = Parser { (get() != 0x0.toByte()) to this }
		override fun SocketChannel.deserialize(typeID: TypeID): ByteBuffer {
			if (typeID != TypeID.BOOLEAN) throw DeserializationException("Wrong type: expected ${TypeID.BOOLEAN}, found $typeID")
			val parseBuf = ByteBuffer.allocateDirect(1)
			read(parseBuf)
			return parseBuf
		}

		override fun toString() = "BOOLEAN"
	}
	object U8 : Serde<UByte>() {
		override val parser = Parser { get().toUByte() to this }
		override val serializer = object : Serializer<UByte> {
			override fun UByte.size() = 2
			override fun UByte.serialize(buf: ByteBuffer) {
				buf
					.put(TypeID.U8.byte)
					.put(this.toByte())
			}
		}
		override fun SocketChannel.deserialize(typeID: TypeID): ByteBuffer {
			if (typeID != TypeID.U8) throw DeserializationException("Wrong type: expected ${TypeID.U8}, found $typeID")
			val parseBuf = ByteBuffer.allocate(1)
			read(parseBuf)
			return parseBuf
		}
		override fun toString() = "U8"
	}
	object I8 : Serde<Byte>() {
		override val parser = Parser { get() to this }
		override val serializer = object : Serializer<Byte> {
			override fun Byte.size() = 2
			override fun Byte.serialize(buf: ByteBuffer) {
				buf
					.put(TypeID.I8.byte)
					.put(this)
			}
		}
		override fun SocketChannel.deserialize(typeID: TypeID): ByteBuffer {
			if (typeID != TypeID.I8) throw DeserializationException("Wrong type: expected ${TypeID.I8}, found $typeID")
			val parseBuf = ByteBuffer.allocate(1)
			read(parseBuf)
			return parseBuf
		}
		override fun toString() = "I8"
	}
	object U16 : Serde<UShort>() {
		override val parser = Parser { getShort().toUShort() to this }
		override val serializer = object : Serializer<UShort> {
			override fun UShort.size() = 2
			override fun UShort.serialize(buf: ByteBuffer) {
				buf
					.put(TypeID.U16.byte)
					.putShort(this.toShort())
			}
		}
		override fun SocketChannel.deserialize(typeID: TypeID): ByteBuffer {
			if (typeID != TypeID.U16) throw DeserializationException("Wrong type: expected ${TypeID.U16}, found $typeID")
			val parseBuf = ByteBuffer.allocate(2)
			read(parseBuf)
			return parseBuf
		}
		override fun toString() = "U16"
	}
	object I16 : Serde<Short>() {
		override val parser = Parser { getShort() to this }
		override val serializer = object : Serializer<Short> {
			override fun Short.size() = 2
			override fun Short.serialize(buf: ByteBuffer) {
				buf
					.put(TypeID.I16.byte)
					.putShort(this)
			}
		}
		override fun SocketChannel.deserialize(typeID: TypeID): ByteBuffer {
			if (typeID != TypeID.I16) throw DeserializationException("Wrong type: expected ${TypeID.I16}, found $typeID")
			val parseBuf = ByteBuffer.allocate(2)
			read(parseBuf)
			return parseBuf
		}
		override fun toString() = "I16"
	}
	object U32 : Serde<UInt>() {
		override val parser = Parser { getInt().toUInt() to this }
		override val serializer = object : Serializer<UInt> {
			override fun UInt.size() = 4
			override fun UInt.serialize(buf: ByteBuffer) {
				buf
					.put(TypeID.U32.byte)
					.putInt(this.toInt())
			}
		}
		override fun SocketChannel.deserialize(typeID: TypeID): ByteBuffer {
			if (typeID != TypeID.U32) throw DeserializationException("Wrong type: expected ${TypeID.U32}, found $typeID")
			val parseBuf = ByteBuffer.allocate(4)
			read(parseBuf)
			return parseBuf
		}
		override fun toString() = "U32"
	}
	object I32 : Serde<Int>() {
		override val parser = Parser { getInt() to this }
		override val serializer = object : Serializer<Int> {
			override fun Int.size() = 4
			override fun Int.serialize(buf: ByteBuffer) {
				buf
					.put(TypeID.I32.byte)
					.putInt(this)
			}
		}
		override fun SocketChannel.deserialize(typeID: TypeID): ByteBuffer {
			if (typeID != TypeID.I32) throw DeserializationException("Wrong type: expected ${TypeID.I32}, found $typeID")
			val parseBuf = ByteBuffer.allocate(4)
			read(parseBuf)
			return parseBuf
		}
		override fun toString() = "I32"
	}
	object U64 : Serde<ULong>() {
		override val parser = Parser { getLong().toULong() to this }
		override val serializer = object : Serializer<ULong> {
			override fun ULong.size() = 8
			override fun ULong.serialize(buf: ByteBuffer) {
				buf
					.put(TypeID.U64.byte)
					.putLong(this.toLong())
			}
		}
		override fun SocketChannel.deserialize(typeID: TypeID): ByteBuffer {
			if (typeID != TypeID.U64) throw DeserializationException("Wrong type: expected ${TypeID.U64}, found $typeID")
			val parseBuf = ByteBuffer.allocate(8)
			read(parseBuf)
			return parseBuf
		}
		override fun toString() = "U64"
	}
	object I64 : Serde<Long>() {
		override val parser = Parser { getLong() to this }
		override val serializer = object : Serializer<Long> {
			override fun Long.size() = 8
			override fun Long.serialize(buf: ByteBuffer) {
				buf
					.put(TypeID.I64.byte)
					.putLong(this)
			}
		}
		override fun SocketChannel.deserialize(typeID: TypeID): ByteBuffer {
			if (typeID != TypeID.I64) throw DeserializationException("Wrong type: expected ${TypeID.I64}, found $typeID")
			val parseBuf = ByteBuffer.allocate(8)
			read(parseBuf)
			return parseBuf
		}
		override fun toString() = "I64"
	}

	open class VariableLength<T>(serializer: Serializer<T>, override val parser: Parser<T>) : Serde<T>() {
		override fun toString() = "VAR"

		private val buf = ByteBuffer.allocateDirect(2)
		override fun SocketChannel.deserialize(typeID: TypeID): ByteBuffer {
			val bufLen = when (typeID) {
				TypeID.VAR8 ->
					synchronized(buf) {
						read(buf.rewind().limit(1))
						buf.get(0).toInt()
					}
				TypeID.VAR16 ->
					synchronized(buf) {
						read(buf.rewind().limit(2))
						buf.getShort(0).toInt()
					}
				else -> throw DeserializationException("Wrong type: expected either ${TypeID.VAR8} or ${TypeID.VAR16}, found $typeID")
			}
			val parseBuf = ByteBuffer.allocate(bufLen)
			read(parseBuf)
			return parseBuf
		}
		final override val serializer = object : Serializer<T> {
			override fun T.size(): Int {
				val dataSize = serializer.run { this@size.size() }
				return dataSize + lenSerializer.run { dataSize.size() }
			}
			override fun T.serialize(buf: ByteBuffer) {
				lenSerializer.run { serializer.run { this@serialize.size() }.serialize(buf) }
				serializer.run { this@serialize.serialize(buf) }
			}
		}

		companion object {
			val lenSerializer = object : Serializer<Int> {
				override fun Int.size(): Int {
					return if (this <= UByte.MAX_VALUE.toInt()) 2
					else if (this <= UShort.MAX_VALUE.toInt()) 3
					else throw DeserializationException("data of length $this is too long to be serialized")
				}
				override fun Int.serialize(buf: ByteBuffer) {
					if (this <= UByte.MAX_VALUE.toInt()) buf.put(TypeID.VAR8.byte).put(this.toByte())
					else if (this <= UShort.MAX_VALUE.toInt()) buf.put(TypeID.VAR16.byte).putShort(this.toShort())
					else throw DeserializationException("data of length $this is too long to be serialized")
				}
			}
			val RAW = VariableLength(
				// no-op
				serializer = object : Serializer<ByteBuffer> {
					override fun ByteBuffer.size() = limit()
					override fun ByteBuffer.serialize(buf: ByteBuffer) { buf.put(rewind()) }
				},
				// no-op
				parser = { this to this },
			)
		}
	}
}
package dev.frozenmilk.rpcrc.serialization

import dev.frozenmilk.rpcrc.parse.Parser

interface SerializerParser<T> : Serializer<T>, Parser<T>
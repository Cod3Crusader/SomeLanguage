package com.archvin.data.type

sealed class BuiltinType<out T>(override val id: String) : Type.ObjectType(id) {
    override val className = "${id}T"

    object CharType : BuiltinType<Char>("char")
    object I32Type : BuiltinType<Int>("i32")
    object StrType : BuiltinType<String>("str")
    object VoidType : BuiltinType<Unit>("void")

    object AnyType : BuiltinType<Any>("debug") // TODO: remove
}


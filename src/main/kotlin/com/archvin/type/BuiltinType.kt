package com.archvin.type

sealed class BuiltinType(override val id: String) : Type(), HasId {
    override val className = "${id}T"

    object CharType : BuiltinType("char")
    object I32Type : BuiltinType("i32")
    object StrType : BuiltinType("str")
    object VoidType : BuiltinType("void")

    object DebugType : Type() // TODO: remove
}


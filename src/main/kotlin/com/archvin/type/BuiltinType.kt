package com.archvin.type

sealed class BuiltinType(override val id: String) : Type(), HasId {
    data object CharType : BuiltinType("char")
    data object I32Type : BuiltinType("i32")
    data object StrType : BuiltinType("str")
    data object VoidType : BuiltinType("void")

    data object AnyType : Type()
}


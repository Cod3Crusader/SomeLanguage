package com.archvin.type

enum class BuiltinType(override val id: String) : Type, HasId {
    CharType("char"),
    I32Type("i32"),
    StrType("str"),
    VoidType("void"),
    NullType("null")
}
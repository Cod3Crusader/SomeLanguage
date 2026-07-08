package com.archvin.data.type

import com.archvin.exceptions.CompileError

sealed class BuiltinType<out T>(override val id: String) : Type.ObjectType(id) {
    override val className = "${id}T"

    object CharType : BuiltinType<Char>("char")
    object I32Type : BuiltinType<Int>("i32")
    object StrType : BuiltinType<String>("str")
    object VoidType : BuiltinType<Unit>("void")

    object AnyType : BuiltinType<Any>("debug") // TODO: remove

    // TODO: reconsider if this should be here
    companion object Resolver {
        fun resolveType(id: String) = when(id) {
            "char" -> CharType
            "i32" -> I32Type
            "str" -> StrType
            "void" -> VoidType
            else -> throw CompileError.UnresolvedIdentifier(id)
        } // i have no idea why but kotlin doesnt accept BuiltinType<*> sometimes
    }
}


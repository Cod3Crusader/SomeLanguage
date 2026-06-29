package com.archvin.data.type

sealed class BuiltinType<out T>(override val id: String) : Type.ObjectType(id) {
    override val className = "${id}T"

    init { map[id] = this }

    object CharType : BuiltinType<Char>("char")
    object I32Type : BuiltinType<Int>("i32")
    object StrType : BuiltinType<String>("str")
    object VoidType : BuiltinType<Unit>("void")

    object AnyType : BuiltinType<Any>("debug") // TODO: remove

    // TODO: reconsider if this should be here
    companion object Resolver {
        private val map = mutableMapOf<String, BuiltinType<*>>()

        fun resolveType(id: String) = map[id] as Type // i have no idea why but kotlin doesnt accept BuiltinType<*> sometimes
    }
}


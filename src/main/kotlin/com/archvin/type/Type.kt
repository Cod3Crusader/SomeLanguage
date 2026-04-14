package com.archvin.type

open class Type(override val id: String) : HasId {
    class FunctionType(paramTypes: Array<Type>, returnType: Type) :
        Type("(${paramTypes.joinToString(", ") { it.id }}):(${returnType.id})")
}

package com.archvin.expression

import com.archvin.token.LiteralToken
import com.archvin.type.BuiltinType
import com.archvin.type.BuiltinType.VoidType
import com.archvin.type.HasType
import com.archvin.type.Type
import com.archvin.variable.Variable

sealed class Expression(override val type: Type, val paramTypes: List<Type>) : HasType {
    data class Literal<out T>(val lit: LiteralToken<T>) : Expression(lit.type, emptyList()) {
        val value = lit.value
    }
    data class Declare(val id: String, override val type: Type) : Expression(type, listOf(type)) {}
    data class Read(val variable: Variable) : Expression(variable.type, emptyList()) {}

    data object Println : Expression(VoidType, listOf(BuiltinType.AnyType)) {} // TODO: replace
}
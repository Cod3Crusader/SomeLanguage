package com.archvin.instruction

import com.archvin.token.LiteralToken
import com.archvin.type.BuiltinType
import com.archvin.type.BuiltinType.VoidType
import com.archvin.type.HasType
import com.archvin.type.Type
import com.archvin.variable.Variable

sealed class Instruction(override val type: Type, val paramTypes: List<Type>) : HasType {
    data class Literal<out T>(val lit: LiteralToken<T>) : Instruction(lit.type, emptyList()) {
        val value = lit.value
    }
    data class Declare(val id: String, override val type: Type) : Instruction(type, listOf(type)) {}
    data class Read(val variable: Variable) : Instruction(variable.type, emptyList()) {}

    data object Debug : Instruction(VoidType, listOf(BuiltinType.DebugType)) {} // TODO: replace
}
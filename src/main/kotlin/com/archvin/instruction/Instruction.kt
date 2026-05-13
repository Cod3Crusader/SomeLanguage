package com.archvin.instruction

import com.archvin.token.LiteralToken
import com.archvin.type.BuiltinType
import com.archvin.type.BuiltinType.VoidType
import com.archvin.type.HasType
import com.archvin.type.Type
import com.archvin.variable.Variable
import com.debug.DebugString

sealed class Instruction(override val type: Type, val paramTypes: List<Type>) : DebugString() , HasType {
    class Literal<out T>(val lit: LiteralToken<T>) : Instruction(lit.type, emptyList()) {
        val value = lit.value
        override val className: String = "Lit"
    }
    class Assign(val variable: Variable, override val type: Type) : Instruction(type, listOf(type)) {}
    class Read(val variable: Variable) : Instruction(variable.type, emptyList()) {}

    object Debug : Instruction(VoidType, listOf(BuiltinType.DebugType)) {} // TODO: replace
}
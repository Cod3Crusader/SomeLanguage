package com.archvin.instruction

import com.archvin.debug.Debug
import com.archvin.token.LiteralToken
import com.archvin.type.BuiltinType
import com.archvin.type.BuiltinType.VoidType
import com.archvin.type.HasType
import com.archvin.type.Type
import com.archvin.variable.AbstractVariable

sealed class Instruction(override val type: Type, val paramTypes: List<Type>) : Debug(), HasType {
    class Literal<out T>(val lit: LiteralToken<T>) : Instruction(lit.type, emptyList()) {
        override val className: String = "Lit"
    }

    class Read(val variable: AbstractVariable) : Instruction(variable.type, emptyList()) {}
    class Assign(val variable: AbstractVariable) : Instruction(variable.type, listOf(variable.type))

    object Println : Instruction(VoidType, listOf(BuiltinType.DebugType)) {} // TODO: replace
    object Pass : Instruction(VoidType, listOf())
}
package com.archvin.pipeline.finalizing

import com.archvin.data.HasType
import com.archvin.data.Literal
import com.archvin.data.type.BuiltinType.VoidType
import com.archvin.data.type.Type
import com.archvin.data.variable.Symbol
import com.archvin.data.variable.Symbol.Variable
import com.archvin.utils.Debug

sealed class Instruction(override val type: Type, val paramTypes: List<Type>) : Debug(), HasType {
    class LitInstr<out T>(val lit: Literal<T>) : Instruction(lit.type, emptyList()) {
        override val className: String = "Lit"
    }

    class ReadInstr(val variable: Variable) : Instruction(variable.type, emptyList()) {}
    class AssignInstr(val variable: Variable) : Instruction(variable.type, listOf(variable.type))

    class CallInstr(val function: Symbol.Function) : Instruction(function.type.retType, function.type.paramTypes)

    object PassInstr : Instruction(VoidType, listOf())
}
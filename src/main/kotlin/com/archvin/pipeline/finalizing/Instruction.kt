package com.archvin.pipeline.finalizing

import com.archvin.type.BuiltinType.VoidType
import com.archvin.type.HasType
import com.archvin.type.Type
import com.archvin.utils.Debug
import com.archvin.variable.Literal
import com.archvin.variable.VariableLike
import com.archvin.variable.VariableLike.Variable

sealed class Instruction(override val type: Type, val paramTypes: List<Type>) : Debug(), HasType {
    class LitInstr<out T>(val lit: Literal<T>) : Instruction(lit.type, emptyList()) {
        override val className: String = "Lit"
    }

    class ReadInstr(val variable: Variable) : Instruction(variable.type, emptyList()) {}
    class AssignInstr(val variable: Variable) : Instruction(variable.type, listOf(variable.type))

    class CallInstr(val function: VariableLike.Function) : Instruction(function.type.retType, function.type.paramTypes)

    object PassInstr : Instruction(VoidType, listOf())
}
package com.archvin.pipeline.finalizing

import com.archvin.data.HasType
import com.archvin.data.type.BuiltinType.VoidType
import com.archvin.data.type.Type
import com.archvin.data.value.Value
import com.archvin.data.variable.Symbol
import com.archvin.utils.Debug

sealed class Instruction(override val type: Type, val paramTypes: List<Type>) : Debug(), HasType {
    class LitInstr(val value: Value, type: Type) : Instruction(type, emptyList()) {
        override val className: String = "Lit"
    }

    class ReadInstr(val variable: Symbol) : Instruction(variable.type, emptyList()) {}
    class AssignInstr(val variable: Symbol) : Instruction(variable.type, listOf(variable.type))

    class CallInstr(val function: Symbol.Function) : Instruction(function.type.retType, function.type.paramTypes)

    object PassInstr : Instruction(VoidType, listOf())
}
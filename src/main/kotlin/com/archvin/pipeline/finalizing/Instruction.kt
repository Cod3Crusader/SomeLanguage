package com.archvin.pipeline.finalizing

import com.archvin.data.value.Value
import com.archvin.data.variable.Symbol
import com.archvin.utils.Debug

sealed class Instruction(val paramNum: Int) : Debug() {
    class LitInstr(val value: Value) : Instruction(0) {
        override val className: String = "Lit"
    }

    class ReadInstr(val variable: Symbol) : Instruction(0) {}
    class AssignInstr(val variable: Symbol) : Instruction(1)

    class CallInstr(val function: Symbol.Function) : Instruction(function.type.paramTypes.size)

    object PassInstr : Instruction(0)
}
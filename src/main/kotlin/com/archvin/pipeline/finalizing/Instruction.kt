package com.archvin.pipeline.finalizing

import com.archvin.data.value.Value
import com.archvin.data.symbol.Symbol
import com.archvin.utils.Debug

sealed class Instruction(val paramNum: Int) : Debug() {
    class LitInstr(val value: Value) : Instruction(0) {
        override val className: String = "Lit"
    }

    class ReadInstr(val level: Int, val index: Int) : Instruction(0)
    class AssignInstr(val level: Int, val index: Int) : Instruction(1)

    class CallInstr(paramNum: Int) : Instruction(paramNum)
}
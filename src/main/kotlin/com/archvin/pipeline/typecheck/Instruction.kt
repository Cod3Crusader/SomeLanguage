package com.archvin.pipeline.typecheck

import com.archvin.data.value.Value
import com.archvin.utils.Debug

sealed class Instruction() : Debug() {
    class LitInstr(val value: Value) : Instruction() {
        override val className: String = "Lit"
    }

    class ReadInstr(val level: Int, val index: Int) : Instruction()
    class AssignInstr(val level: Int, val index: Int) : Instruction()

    class ReadStatic(val static: Value.StaticValue) : Instruction()
    class AssignStatic(val static: Value.StaticValue) : Instruction()

    class CallInstr(val paramNum: Int) : Instruction()
}
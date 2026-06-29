package com.archvin.pipeline.typecheck

import com.archvin.data.scope.Scope
import com.archvin.data.value.Value
import com.archvin.utils.Debug

sealed class Instruction : Debug() {
    class LitInstr(val value: Value) : Instruction() {
        override val className: String = "Lit"
    }

    class ReadInstr(val scope: Scope, val index: Int) : Instruction()
    class AssignInstr(val scope: Scope, val index: Int) : Instruction()

    class CallInstr(val paramNum: Int) : Instruction()
}
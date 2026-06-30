package com.archvin.pipeline.typecheck

import com.archvin.pipeline.execution.RuntimeScope
import com.archvin.pipeline.execution.Value
import com.archvin.utils.Debug

sealed class Instruction : Debug() {
    class LoadValue(val value: Value) : Instruction()

    class ReadInstr(val scope: RuntimeScope, val index: Int) : Instruction()
    class AssignInstr(val scope: RuntimeScope, val index: Int) : Instruction()

    class CallInstr(val paramNum: Int) : Instruction()
}
package com.archvin.pipeline.typecheck

import com.archvin.pipeline.execution.LambdaVal
import com.archvin.pipeline.execution.RuntimeScope
import com.archvin.pipeline.execution.Value
import com.archvin.utils.Debug

sealed class Instruction : Debug() {
    class LoadValue(val value: Value) : Instruction()

    class ReadInstr(val scope: RuntimeScope, val index: Int) : Instruction()
    class AssignInstr(val scope: RuntimeScope, val index: Int, val newValue: Instruction) : Instruction()

    class CallInstr(val func: Instruction, val params: List<Instruction>) : Instruction()
    class LambdaInstr(val body: LambdaVal.Composite) : Instruction()

    class ReturnInstr(val returns: Instruction?, val returnFrom: RuntimeScope) : Instruction()
    class ConditionalInstr(val condition: Instruction, val body: Instruction, val elseBranch: Instruction?) : Instruction()
}
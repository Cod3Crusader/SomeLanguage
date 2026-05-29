package com.archvin.data.value

import com.archvin.pipeline.finalizing.Instruction

sealed class FunctionValue : Value() {
    class CustomFunction(val instructions: List<Instruction>) : FunctionValue()
}
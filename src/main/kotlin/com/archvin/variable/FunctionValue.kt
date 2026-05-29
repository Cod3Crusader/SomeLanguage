package com.archvin.variable

import com.archvin.pipeline.finalizing.Instruction

abstract class FunctionValue : Value() {
    class CustomFunction(val instructions: List<Instruction>) : FunctionValue()
}
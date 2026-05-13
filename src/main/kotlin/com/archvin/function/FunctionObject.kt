package com.archvin.function

import com.archvin.instruction.Instruction
import com.archvin.type.HasId
import com.archvin.type.HasType
import com.archvin.type.Type

class FunctionObject(
        override val id: String,
        val params: List<HasType>,
        val returnType: Type,
        val instructions: List<Instruction>) : HasId, HasType {

    override val type: Type.FunctionType =
        Type.FunctionType(params.map { it.type }.toList(), returnType)

    fun call(): HasType {
        TODO()
    }
}
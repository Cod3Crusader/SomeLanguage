package com.archvin.function

import com.archvin.expression.Expression
import com.archvin.type.HasId
import com.archvin.type.HasType
import com.archvin.type.Type

class FunctionObject(
        override val id: String,
        val params: List<HasType>,
        val returnType: Type,
        val expressions: List<Expression>) : HasId, HasType {

    override val type: Type.FunctionType =
        Type.FunctionType(params.map { it.type }.toList(), returnType)

    fun call(): HasType {
        TODO()
    }
}
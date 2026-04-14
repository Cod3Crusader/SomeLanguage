package com.archvin.function

import com.archvin.expression.Expression
import com.archvin.type.HasId
import com.archvin.type.HasType
import com.archvin.type.Type

class DeclaredFunction(
    override val id: String,
    val params: Array<HasType>,
    val returnType: Type,
    val expressions: Array<Expression>,
        ) : HasId, HasType {
    override val type: Type.FunctionType =
        Type.FunctionType(params.map { it.type }.toTypedArray(), returnType)

    fun call(): HasType {
        TODO()
    }
}
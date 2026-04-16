package com.archvin.expression

import com.archvin.function.FunctionObject
import com.archvin.type.HasType
import com.archvin.type.Type
import com.archvin.type.VoidType

sealed class Expression(val returnType: Type) : HasType {
    override val type = returnType

    class Call(val function: FunctionObject, val arguments: List<HasType>) : Expression(function.returnType)
    class Assignment : Expression(VoidType)
    class Declaration : Expression(VoidType)
    class Operation(val operandType: Type) : Expression(operandType)
    object PassExpression : Expression(VoidType)
}
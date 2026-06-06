package com.archvin.pipeline.finalizing

import com.archvin.data.HasType
import com.archvin.data.Literal
import com.archvin.data.type.BuiltinType.VoidType
import com.archvin.data.type.Type
import com.archvin.data.variable.Symbol

sealed class CheckedExpr(override val type: Type) : HasType {
    class Read(val read: Symbol) : CheckedExpr(read.type)
    class Lit<out T>(val lit: Literal<T>) : CheckedExpr(lit.type)
    class Assign(val variable: Symbol, val assigned: CheckedExpr) : CheckedExpr(variable.type)

    //class OpExpr(val operationFun: LambdaVal) : CheckedExpr()

    class Call(val function: Symbol.Function, val params: List<CheckedExpr>) : CheckedExpr(function.type)
    class Lambda(val expressions: List<CheckedExpr>) : CheckedExpr(Type.FunctionType(VoidType, emptyList()))
}
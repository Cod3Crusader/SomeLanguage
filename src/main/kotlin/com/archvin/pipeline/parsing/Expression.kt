package com.archvin.pipeline.parsing

import com.archvin.data.HasId
import com.archvin.data.Literal
import com.archvin.pipeline.execution.LambdaVal
import com.archvin.utils.Debug

sealed class Expression : Debug() {
    class ReadExpr(val id: String) : Expression()
    class LitExpr<out T>(val lit: Literal<T>) : Expression()
    class AssignExpr(val id: String, val assigned: Expression) : Expression()

    class OpExpr(val operationFun: LambdaVal) : Expression()

    class CallExpr(val functionId: String, val params: List<Expression>) : Expression()

    sealed class Declaration(val id: String, open val init: Expression) : Expression() {
        class VarDeclare(id: String, val typeId: String, override val init: Expression) : Declaration(id, init)
        class FunDeclare(id: String, val retType: String, val params: List<Param>, override val init: LambdaExpr)
            : Declaration(id, init) {
            class Param(override val id: String, val typeId: String) : Debug(), HasId
        }
    }

    class LambdaExpr(val expressions: MutableList<Expression> = ArrayList()) : Expression()
}
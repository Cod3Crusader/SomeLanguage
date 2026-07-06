package com.archvin.pipeline.parsing

import com.archvin.data.Literal
import com.archvin.pipeline.execution.LambdaVal

sealed class Expression : AstNode() {
    class ReadExpr(val id: String) : Expression()
    class LitExpr<out T>(val lit: Literal<T>) : Expression()
    class AssignExpr(val id: String, val assigned: Expression) : Expression()

    class OpExpr(val operationFun: LambdaVal) : Expression()

    class CallExpr(val functionId: String, val params: List<Expression>) : Expression()

    class LambdaExpr(val expressions: List<Expression>, val declarations: List<Declaration>) : Expression()
}
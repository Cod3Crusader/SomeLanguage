package com.archvin.parser

import com.archvin.utils.Debug
import com.archvin.variable.FunctionValue
import com.archvin.variable.Literal

sealed class Expression : Debug() {
    class ReadExpr(val id: String) : Expression()
    class LitExpr<out T>(val lit: Literal<T>) : Expression()

    class AssignExpr(val variableId: String) : Expression()
    class OpExpr(val operationFun: FunctionValue) : Expression()
    class CallExpr(val functionId: String) : Expression() {
        var paramNum = 0

        override fun toString(): String {
            return "${super.toString()} $paramNum"
        }
    }

    object PassExpr : Expression()
}
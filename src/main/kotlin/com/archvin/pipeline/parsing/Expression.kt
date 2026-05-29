package com.archvin.pipeline.parsing

import com.archvin.data.Literal
import com.archvin.data.value.FunctionValue
import com.archvin.utils.Debug
import com.archvin.utils.funSignature

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

    open class DeclareExpr(val id: String, val typeId: String, val isMutable: Boolean) : Expression() {
        class FunDeclare(id: String, val retType: String, val paramTypes: List<String>)
            : DeclareExpr(id, funSignature(retType, paramTypes), false)
    }

    object PassExpr : Expression()
}
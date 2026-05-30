package com.archvin.pipeline.parsing

import com.archvin.data.Literal
import com.archvin.data.value.FunctionBody
import com.archvin.utils.Debug
import com.archvin.utils.funSignature

sealed class Expression : Debug() {
    class ReadExpr(val id: String) : Expression()
    class LitExpr<out T>(val lit: Literal<T>) : Expression()
    class AssignExpr(val variableId: String) : Expression()

    class OpExpr(val operationFun: FunctionBody) : Expression()

    // TODO: use val
    class CallExpr(val functionId: String, var paramNum: Int = 0) : Expression()
    class LambdaExpr(var exprNum: Int = 0) : Expression()

    open class DeclareExpr(val id: String, val typeId: String, val isMutable: Boolean) : Expression() {
        class FunDeclare(id: String, val retType: String, val paramTypes: List<String>)
            : DeclareExpr(id, funSignature(retType, paramTypes), false)
    }

    object PassExpr : Expression()
}
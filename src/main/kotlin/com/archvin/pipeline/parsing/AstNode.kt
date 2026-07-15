package com.archvin.pipeline.parsing

import com.archvin.utils.Debug

sealed class AstNode : Debug() {
    sealed class Declaration(val id: String) : AstNode() {
        sealed interface UncheckedType {
            @JvmInline
            value class TypeId(val id: String) : UncheckedType

            class LambdaType(val retType: UncheckedType, val paramTypes: List<UncheckedType>) : UncheckedType
        }

        class VarDeclare(id: String, val typeId: UncheckedType, val init: Expression? = null) : Declaration(id)
        class FunDeclare(id: String, val retType: UncheckedType, val params: List<VarDeclare>, val init: Expression.LambdaExpr) : Declaration(id)
    }
}
package com.archvin.pipeline.parsing

import com.archvin.data.HasId
import com.archvin.utils.Debug

sealed class AstNode : Debug() {
    sealed class Declaration(val id: String, open val init: Expression) : AstNode() {
        sealed interface UncheckedType {
            @JvmInline
            value class TypeId(val id: String) : UncheckedType

            class LambdaType(val retType: UncheckedType, val paramTypes: List<UncheckedType>) : UncheckedType
        }

        class VarDeclare(id: String, val typeId: UncheckedType, override val init: Expression) : Declaration(id, init)
        class FunDeclare(id: String, val retType: UncheckedType, val params: List<Param>, override val init: Expression.LambdaExpr)
            : Declaration(id, init) {
            class Param(override val id: String, val typeId: UncheckedType) : Debug(), HasId
        }
    }
}
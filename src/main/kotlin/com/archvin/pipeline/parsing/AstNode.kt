package com.archvin.pipeline.parsing

import com.archvin.data.HasId
import com.archvin.utils.Debug

sealed class AstNode : Debug() {
    sealed class Declaration(val id: String, open val init: Expression) : AstNode() {
        class VarDeclare(id: String, val typeId: String, override val init: Expression) : Declaration(id, init)
        class FunDeclare(id: String, val retType: String, val params: List<Param>, override val init: Expression.LambdaExpr)
            : Declaration(id, init) {
            class Param(override val id: String, val typeId: String) : Debug(), HasId
        }
    }
}
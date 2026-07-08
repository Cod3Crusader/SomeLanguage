package com.archvin.pipeline.typecheck

import com.archvin.data.HasType
import com.archvin.data.symbol.BuiltinFunction.Companion.builtins
import com.archvin.data.symbol.Symbol
import com.archvin.data.type.BuiltinType
import com.archvin.data.type.BuiltinType.AnyType
import com.archvin.data.type.BuiltinType.VoidType
import com.archvin.data.type.Type
import com.archvin.exceptions.CompileError
import com.archvin.pipeline.execution.LambdaVal.Composite
import com.archvin.pipeline.execution.RuntimeScope
import com.archvin.pipeline.execution.Value
import com.archvin.pipeline.parsing.AstNode
import com.archvin.pipeline.parsing.AstNode.Declaration.FunDeclare
import com.archvin.pipeline.parsing.AstNode.Declaration.VarDeclare
import com.archvin.pipeline.parsing.Expression
import com.archvin.pipeline.parsing.Expression.*
import com.archvin.pipeline.typecheck.Instruction.*

object TypeChecker {
    val topResolver: NameResolver
    val topScope: RuntimeScope

    init {
        val topBuilder = ScopeBuilder()

        builtins.forEach { topBuilder.addSymbol(it) }

        val (topResolver, topScope) = topBuilder.build(null)
        builtins.forEachIndexed { index, function ->
            topScope.changeStart(index, function.lambda)
        }

        this.topResolver = topResolver
        this.topScope = topScope
    }

    private fun declare(dec: AstNode.Declaration, builder: ScopeBuilder) {
        val symbol = when (dec) {
            is FunDeclare -> {
                val retType = BuiltinType.resolveType(dec.retType)
                val paramTypes = dec.params.map { BuiltinType.resolveType(it.typeId) }
                Symbol.Function(dec.id, retType, paramTypes)
            }

            is VarDeclare -> {
                val type = BuiltinType.resolveType(dec.typeId)
                Symbol(dec.id, type)
            }
        }

        builder.addSymbol(symbol)
    }

    private fun checkType(expr: Expression, expectType: Type, resolver: NameResolver): Instruction {
        val instr: TypedInstruction = when (expr) {
            is AssignExpr -> {
                val (variable, scope, index) = resolver.resolveVar(expr.id)

                AssignInstr(scope, index,
                    checkType(expr.assigned, variable.type, resolver)
                ) + VoidType
            }

            is CallExpr -> {
                val (func, scope, index) = resolver.resolveFunc(expr.functionId)


                CallInstr(ReadInstr(scope, index), expr.params.mapIndexed { i, arg ->
                    checkType(arg, func.type.paramTypes[i], resolver)
                }) + func.type.retType
            }

            is LitExpr<*> -> LoadValue(Value.Primitive(expr.lit.value)) + expr.lit.type
            is OpExpr -> TODO()

            is ReadExpr -> {
                val (symbol, scope, index) = resolver.resolveVar(expr.id)
                ReadInstr(scope, index) + symbol.type
            }

            is LambdaExpr -> LoadValue(processFunc(expr, emptyList(), resolver)) + VoidType

            is ReturnExpr -> {
                val returns = expr.returns?.let {checkType(it, AnyType, resolver)} // TODO: check type of return statement
                ReturnInstr(returns, resolver.scope) + VoidType
            }
        }

        if (instr.type != expectType && expectType !is AnyType)
            throw CompileError.TypeMismatchError(expectType, instr.type)

        return instr.instr
    }

    private fun processFunc(lambdaExpr: LambdaExpr, params: List<FunDeclare.Param>, parent: NameResolver): Composite {
        val builder = ScopeBuilder()

        params.forEach { builder.addSymbol(Symbol(it.id, BuiltinType.resolveType(it.typeId))) }

        return processLambda(lambdaExpr, parent, builder)
    }

    private fun processLambda(lambdaExpr: LambdaExpr, parent: NameResolver, builder: ScopeBuilder? = null): Composite {
        val builder = builder ?: ScopeBuilder()

        val pendingFunctions = mutableListOf<FunDeclare>()

        // parse declarations
        for (expr in lambdaExpr.declarations) {
            declare(expr, builder)
            if (expr is FunDeclare) {
                pendingFunctions.add(expr)
            }
        }

        val (resolver, scope) = builder.build(parent)


        val instructions = ArrayList<Instruction>()

        // go back to function declarations and parse their values
        for (funcDecl in pendingFunctions) {
            val (_, funcScope, index) = resolver.resolveFunc(funcDecl.id)
            scope.changeStart(index, processFunc(funcDecl.init, funcDecl.params, resolver))
        }

        // parse instructions
        for (expr in lambdaExpr.expressions) {
            instructions.add(checkType(expr, AnyType, resolver))
        }

        return Composite(scope, instructions)
    }

    fun process(r: LambdaExpr): Composite {
        // builds a composite from the code and adds the top level builtins to its parent scope

        val main = processLambda(r, topResolver)

        return main
    }


    private class TypedInstruction(val instr: Instruction, override val type: Type) : HasType
    private operator fun Instruction.plus(type: Type) = TypedInstruction(this, type)
}
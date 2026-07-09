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
import com.archvin.pipeline.typecheck.Context.ContextType
import com.archvin.pipeline.typecheck.Context.ContextType.FUNCTION
import com.archvin.pipeline.typecheck.Context.ContextType.LAMBDA
import com.archvin.pipeline.typecheck.Instruction.*

object TypeChecker {
    val topResolver: Context
    val topScope: RuntimeScope

    init {
        val topBuilder = ScopeBuilder()

        builtins.forEach { topBuilder.addSymbol(it) }

        val (topResolver, topScope) = topBuilder.build(VoidType, LAMBDA, null)
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

    private fun checkType(expr: Expression, expectType: Type, context: Context): Instruction {
        val instr: TypedInstruction = when (expr) {
            is AssignExpr -> {
                val (variable, scope, index) = context.resolveVar(expr.id)

                AssignInstr(scope, index,
                    checkType(expr.assigned, variable.type, context)
                ) + VoidType
            }

            is CallExpr -> {
                val (func, scope, index) = context.resolveFunc(expr.functionId)


                CallInstr(ReadInstr(scope, index), expr.params.mapIndexed { i, arg ->
                    checkType(arg, func.type.paramTypes[i], context)
                }) + func.type.retType
            }

            is LitExpr<*> -> LoadValue(Value.Primitive(expr.lit.value)) + expr.lit.type
            is OpExpr -> TODO()

            is ReadExpr -> {
                val (symbol, scope, index) = context.resolveVar(expr.id)
                ReadInstr(scope, index) + symbol.type
            }

            is LambdaExpr -> LoadValue(processScope(expr, VoidType, LAMBDA, context)) + VoidType

            is ReturnExpr -> {
                val from = context.getClosestFunction() ?: error("return expressions can only appear inside functions")

                val returns =
                    if (from.retType != VoidType)
                        expr.returns?.let { checkType(it, from.retType, context) }
                    else LoadValue(Value.Uninitialized)

                ReturnInstr(returns, from.scope) + VoidType
            }

            is ConditionalExpr -> {
                val condition = checkType(expr.condition, BuiltinType.BoolType, context)
                val body = processScope(expr.body, VoidType, LAMBDA, context)
                val elseBranch = expr.elseBranch?.let { processScope(it, VoidType, LAMBDA, context) }

                ConditionalInstr(condition, CallInstr(LoadValue(body), emptyList()),
                    elseBranch?.let { CallInstr(LoadValue(it), emptyList()) }) + VoidType
            }
        }

        if (instr.type != expectType && expectType !is AnyType)
            throw CompileError.TypeMismatchError(expectType, instr.type)

        return instr.instr
    }

    private fun processFunc(funDecl: FunDeclare, declared: Symbol.Function, parent: Context): Composite {
        val builder = ScopeBuilder()

        funDecl.params.forEach { builder.addSymbol(Symbol(it.id, BuiltinType.resolveType(it.typeId))) }

        return processScope(funDecl.init, declared.type.retType, FUNCTION, parent, builder)
    }

    private fun processScope(lambdaExpr: LambdaExpr, retType: Type, ctxType: ContextType, parent: Context, builder: ScopeBuilder? = null): Composite {
        val builder = builder ?: ScopeBuilder()

        val pendingFunctions = mutableListOf<FunDeclare>()

        // parse declarations
        for (expr in lambdaExpr.declarations) {
            declare(expr, builder)
            if (expr is FunDeclare) {
                pendingFunctions.add(expr)
            }
        }

        val (resolver, scope) = builder.build(retType, ctxType, parent)


        val instructions = ArrayList<Instruction>()

        // go back to function declarations and parse their values
        for (funcDecl in pendingFunctions) {
            val (declared, _, index) = resolver.resolveFunc(funcDecl.id)
            scope.changeStart(index, processFunc(funcDecl, declared, resolver))
        }

        // parse instructions
        for (expr in lambdaExpr.expressions) {
            instructions.add(checkType(expr, AnyType, resolver))
        }

        return Composite(scope, instructions)
    }

    fun process(r: LambdaExpr): Composite {
        // builds a composite from the code and adds the top level builtins to its parent scope

        val main = processScope(r, VoidType, LAMBDA, topResolver)

        return main
    }


    private class TypedInstruction(val instr: Instruction, override val type: Type) : HasType
    private operator fun Instruction.plus(type: Type) = TypedInstruction(this, type)
}
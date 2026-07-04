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
import com.archvin.pipeline.parsing.Expression
import com.archvin.pipeline.parsing.Expression.*
import com.archvin.pipeline.parsing.Expression.Declaration.FunDeclare
import com.archvin.pipeline.parsing.Expression.Declaration.VarDeclare
import com.archvin.pipeline.typecheck.Instruction.*


// TODO: separate scope builder from resolver, move instructionstack to local

object TypeChecker {

    private val instructionStack = ArrayDeque<ArrayList<Instruction>>()

    fun yield(add: Instruction) {
        instructionStack.last().add(add)
    }

    private fun declare(dec: Declaration, builder: ScopeBuilder) {
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

    private fun checkType(expr: Expression, expectType: Type, resolver: NameResolver): TypedInstruction {
        val instr: TypedInstruction = when (expr) {
            is AssignExpr -> {
                val (variable, scope, index) = resolver.resolveVar(expr.id)
                checkType(expr.assigned, variable.type, resolver)

                AssignInstr(scope, index) + VoidType
            }

            is CallExpr -> {
                val (func, scope, index) = resolver.resolveFunc(expr.functionId)
                expr.params.forEachIndexed { i, arg ->
                    checkType(arg, func.type.paramTypes[i], resolver)
                }
                yield(ReadInstr(scope, index))

                CallInstr(expr.params.size) + func.type.retType
            }

            is LitExpr<*> -> LoadValue(Value.Primitive(expr.lit.value)) + expr.lit.type
            is OpExpr -> TODO()

            is ReadExpr -> {
                val (symbol, scope, index) = resolver.resolveVar(expr.id)
                ReadInstr(scope, index) + symbol.type
            }

            is LambdaExpr -> LoadValue(processFunc(expr, emptyList(), resolver)) + VoidType

            is VarDeclare -> {
                val (variable, scope, index) = resolver.resolveVar(expr.id)
                checkType(expr.init, variable.type, resolver)
                AssignInstr(scope, index) + VoidType
            }

            is FunDeclare -> TypedInstruction(null, VoidType) // Already handled
        }

        if (instr.type != expectType && expectType != AnyType)
            throw CompileError.TypeMismatchError(expectType, instr.type)

        instr.instr?.let { yield(it) }
        return instr
    }

    private fun processFunc(lambdaExpr: LambdaExpr, params: List<FunDeclare.Param>, parent: NameResolver): Composite {
        val builder = ScopeBuilder()
        instructionStack.add(ArrayList())

        params.forEach { builder.addSymbol(Symbol(it.id, BuiltinType.resolveType(it.typeId))) }

        val scope = processLambda(lambdaExpr, parent, builder)

        return Composite(scope, instructionStack.removeLast())
    }

    private fun processLambda(lambdaExpr: LambdaExpr, parent: NameResolver, builder: ScopeBuilder? = null): RuntimeScope {
        val builder = builder ?: ScopeBuilder()

        val pendingFunctions = mutableListOf<FunDeclare>()

        // parse declarations
        for (expr in lambdaExpr.expressions) {
            if (expr !is Declaration) continue
            declare(expr, builder)
            if (expr is FunDeclare) {
                pendingFunctions.add(expr)
            }

        }

        val (resolver, scope) = builder.build(parent)

        // go back to function declarations and parse their values
        for (funcDecl in pendingFunctions) {
            // TODO: make it constant immediately
            val (_, funcScope, index) = resolver.resolveFunc(funcDecl.id)
            yield(LoadValue(processFunc(funcDecl.init, funcDecl.params, resolver)))
            yield(AssignInstr(funcScope, index))
        }

        // parse instructions
        for (expr in lambdaExpr.expressions) {
            checkType(expr, AnyType, resolver)
        }

        return scope
    }

    fun process(r: List<Expression>): Composite {
        val topBuilder = ScopeBuilder()

        instructionStack.add(ArrayList())

        builtins.forEach { topBuilder.addSymbol(it) }

        val (topResolver, topScope) = topBuilder.build(null)
        val scope = processLambda(LambdaExpr(r.toMutableList()), topResolver)


        // TODO
        val setBase = ArrayList<Instruction>()
        builtins.forEach {
            setBase.add(LoadValue(it.lambda))
            val (_, scope, index) = topResolver.resolveFunc(it.id)
            setBase.add(AssignInstr(scope, index))
        }


        val main = instructionStack.removeLast()
        return Composite(scope, main)
    }

    private class TypedInstruction(val instr: Instruction?, override val type: Type) : HasType
    private operator fun Instruction.plus(type: Type) = TypedInstruction(this, type)
}
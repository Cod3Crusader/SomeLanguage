package com.archvin.pipeline.typecheck

import com.archvin.data.HasType
import com.archvin.data.scope.Scope
import com.archvin.data.symbol.BuiltinFunction.Companion.builtins
import com.archvin.data.symbol.Symbol
import com.archvin.data.type.BuiltinType
import com.archvin.data.type.BuiltinType.AnyType
import com.archvin.data.type.BuiltinType.VoidType
import com.archvin.data.type.Type
import com.archvin.data.value.LambdaVal.Composite
import com.archvin.data.value.Value
import com.archvin.exceptions.CompileError
import com.archvin.pipeline.parsing.Expression
import com.archvin.pipeline.parsing.Expression.*
import com.archvin.pipeline.parsing.Expression.Declaration.FunDeclare
import com.archvin.pipeline.parsing.Expression.Declaration.VarDeclare
import com.archvin.pipeline.typecheck.Instruction.*

object TypeChecker {

    private val instructionStack = ArrayDeque<ArrayList<Instruction>>()

    private var resolver: ScopeBuilder = ScopeBuilder()

    fun yield(add: Instruction) {
        instructionStack.last().add(add)
    }

    private fun declare(dec: Declaration) {
        val symbol = when (dec) {
            is FunDeclare -> {
                val retType = BuiltinType.resolveType(dec.retType)
                val paramTypes = dec.params.map { BuiltinType.resolveType(it.typeId) }
                Symbol.createFun(dec.id, retType, paramTypes)
            }

            is VarDeclare -> {
                val type = BuiltinType.resolveType(dec.typeId)
                Symbol.create(dec.id, type)
            }
        }

        resolver.addSymbol(symbol)
    }

    private fun checkType(expr: Expression, expectType: Type): TypedInstruction {
        val instr: TypedInstruction = when (expr) {
            is AssignExpr -> {
                val (variable, level, index) = resolver.resolveVar(expr.id)
                checkType(expr.assigned, variable.type)

                AssignInstr(level, index) + VoidType
            }

            is CallExpr -> {
                val (func, scope, index) = resolver.resolveFunc(expr.functionId)
                expr.params.forEachIndexed { i, arg ->
                    checkType(arg, func.type.paramTypes[i])
                }
                yield(ReadInstr(scope, index))

                CallInstr(expr.params.size) + func.type.retType
            }

            is LitExpr<*> -> LitInstr(Value.Primitive(expr.lit.value)) + expr.lit.type
            is OpExpr -> TODO()

            is ReadExpr -> {
                val (symbol, scope, index) = resolver.resolveVar(expr.id)
                ReadInstr(scope, index) + symbol.type
            }

            is LambdaExpr -> LitInstr(processScope(expr, emptyList())) + VoidType

            is VarDeclare -> {
                val (variable, level, index) = resolver.resolveVar(expr.id)
                checkType(expr.init, variable.type)
                AssignInstr(level, index) + VoidType
            }

            is FunDeclare -> TypedInstruction(null, VoidType) // Already handled
        }

        if (instr.type != expectType && expectType != AnyType)
            throw CompileError.TypeMismatchError(expectType, instr.type)

        instr.instr?.let { yield(it) }
        return instr
    }

    private fun processScope(lambdaExpr: LambdaExpr, params: List<FunDeclare.Param>): Composite {
        resolver = ScopeBuilder(resolver)
        instructionStack.add(ArrayList())

        params.forEach { resolver.addSymbol(Symbol.create(it.id, BuiltinType.resolveType(it.typeId))) }

        val varNum = processLambda(lambdaExpr) + params.size

        resolver = resolver.parent!!
        return Composite(Scope(ArrayList(), varNum), instructionStack.removeLast(), instructionStack.size)
    }

    private fun processLambda(lambdaExpr: LambdaExpr): Int {
        var varNum = 0
        val pendingFunctions = mutableListOf<FunDeclare>()

        // parse declarations
        for (expr in lambdaExpr.expressions) {
            if (expr !is Declaration) continue
            declare(expr)
            varNum++
            if (expr is FunDeclare) {
                pendingFunctions.add(expr)
            }

        }

        resolver.finish()

        for (funcDecl in pendingFunctions) {
            // TODO: make it constant immediately
            val (_, scope, index) = resolver.resolveFunc(funcDecl.id)
            yield(LitInstr(processScope(funcDecl.init, funcDecl.params)))
            yield(AssignInstr(scope, index))
        }

        // parse instructions
        for (expr in lambdaExpr.expressions) {
            checkType(expr, AnyType)
        }

        return varNum
    }

    fun process(r: List<Expression>): Composite {
        instructionStack.add(ArrayList())

        builtins.forEach { resolver.addSymbol(it) }

        val varNum = processLambda(LambdaExpr(r.toMutableList()))

        val setBase = ArrayList<Instruction>()
        builtins.forEach {
            setBase.add(LitInstr(it.lambda))
            val (_, scope, index) = resolver.resolveFunc(it.id)
            setBase.add(AssignInstr(scope, index))
        }
        val main = instructionStack.removeLast()
        return Composite(Scope(ArrayList(), varNum + builtins.size), main, 0)
    }

    private data class PendingFunc(val symbol: Symbol, val decl: FunDeclare)
    private class TypedInstruction(val instr: Instruction?, override val type: Type) : HasType
    private operator fun Instruction.plus(type: Type) = TypedInstruction(this, type)
}
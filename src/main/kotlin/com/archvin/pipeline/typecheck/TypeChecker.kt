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

object TypeChecker {

    private val instructionStack = ArrayDeque<ArrayList<Instruction>>()

    private var scopeBuilder: ScopeBuilder = ScopeBuilder()

    fun yield(add: Instruction) {
        instructionStack.last().add(add)
    }

    private fun declare(dec: Declaration) {
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

        scopeBuilder.addSymbol(symbol)
    }

    private fun checkType(expr: Expression, expectType: Type): TypedInstruction {
        val instr: TypedInstruction = when (expr) {
            is AssignExpr -> {
                val (variable, scope, index) = scopeBuilder.resolveVar(expr.id)
                checkType(expr.assigned, variable.type)

                AssignInstr(scope, index) + VoidType
            }

            is CallExpr -> {
                val (func, scope, index) = scopeBuilder.resolveFunc(expr.functionId)
                expr.params.forEachIndexed { i, arg ->
                    checkType(arg, func.type.paramTypes[i])
                }
                yield(ReadInstr(scope, index))

                CallInstr(expr.params.size) + func.type.retType
            }

            is LitExpr<*> -> LoadValue(Value.Primitive(expr.lit.value)) + expr.lit.type
            is OpExpr -> TODO()

            is ReadExpr -> {
                val (symbol, scope, index) = scopeBuilder.resolveVar(expr.id)
                ReadInstr(scope, index) + symbol.type
            }

            is LambdaExpr -> LoadValue(processScope(expr, emptyList())) + VoidType

            is VarDeclare -> {
                val (variable, scope, index) = scopeBuilder.resolveVar(expr.id)
                checkType(expr.init, variable.type)
                AssignInstr(scope, index) + VoidType
            }

            is FunDeclare -> TypedInstruction(null, VoidType) // Already handled
        }

        if (instr.type != expectType && expectType != AnyType)
            throw CompileError.TypeMismatchError(expectType, instr.type)

        instr.instr?.let { yield(it) }
        return instr
    }

    private fun processScope(lambdaExpr: LambdaExpr, params: List<FunDeclare.Param>): Composite {
        scopeBuilder = ScopeBuilder(scopeBuilder)
        instructionStack.add(ArrayList())

        params.forEach { scopeBuilder.addSymbol(Symbol(it.id, BuiltinType.resolveType(it.typeId))) }

        val scope = processLambda(lambdaExpr)

        scopeBuilder = scopeBuilder.parent!!
        return Composite(scope, instructionStack.removeLast())
    }

    private fun processLambda(lambdaExpr: LambdaExpr): RuntimeScope {
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

        scopeBuilder.build()

        for (funcDecl in pendingFunctions) {
            // TODO: make it constant immediately
            val (_, rtScope, index) = scopeBuilder.resolveFunc(funcDecl.id)
            yield(LoadValue(processScope(funcDecl.init, funcDecl.params)))
            yield(AssignInstr(rtScope, index))
        }

        // parse instructions
        for (expr in lambdaExpr.expressions) {
            checkType(expr, AnyType)
        }

        return scopeBuilder.scope
    }

    fun process(r: List<Expression>): Composite {
        instructionStack.add(ArrayList())

        builtins.forEach { scopeBuilder.addSymbol(it) }

        val scope = processLambda(LambdaExpr(r.toMutableList()))


        // TODO
        val setBase = ArrayList<Instruction>()
        builtins.forEach {
            setBase.add(LoadValue(it.lambda))
            val (_, scope, index) = scopeBuilder.resolveFunc(it.id)
            setBase.add(AssignInstr(scope, index))
        }


        val main = instructionStack.removeLast()
        return Composite(scope, main)
    }

    private data class PendingFunc(val symbol: Symbol, val decl: FunDeclare)
    private class TypedInstruction(val instr: Instruction?, override val type: Type) : HasType
    private operator fun Instruction.plus(type: Type) = TypedInstruction(this, type)
}
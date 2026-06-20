package com.archvin.pipeline.typecheck

import com.archvin.data.HasType
import com.archvin.data.symbol.BuiltinFunction.Companion.builtins
import com.archvin.data.symbol.Symbol
import com.archvin.data.type.BuiltinType
import com.archvin.data.type.BuiltinType.AnyType
import com.archvin.data.type.BuiltinType.VoidType
import com.archvin.data.type.Type
import com.archvin.data.value.LambdaVal.Composite
import com.archvin.data.value.Value
import com.archvin.exceptions.CompileError
import com.archvin.pipeline.IStage
import com.archvin.pipeline.parsing.Expression
import com.archvin.pipeline.parsing.Expression.*
import com.archvin.pipeline.parsing.Expression.Declaration.FunDeclare
import com.archvin.pipeline.parsing.Expression.Declaration.VarDeclare
import com.archvin.pipeline.typecheck.Instruction.*

object TypeChecker : IStage<Composite, List<Expression>> {

    private val instructionStack = ArrayDeque<ArrayList<Instruction>>()

    private var resolver: NameResolver = NameResolver()

    fun yield(add: Instruction) {
        instructionStack.last().add(add)
    }

    private fun declare(dec: Declaration) {
        val symbol = when (dec) {
            is FunDeclare -> {
                val retType = resolver.resolveType(dec.retType).res
                val paramTypes = dec.params.map { resolver.resolveType(it.typeId).res }
                Symbol.createFun(dec.id, retType, paramTypes)
            }

            is VarDeclare -> {
                val type = resolver.resolveType(dec.typeId).res
                Symbol.createVar(dec.id, type)
            }
        }

        resolver.add(symbol)
    }

    private fun checkType(expr: Expression, expectType: Type): TypedInstruction {
        val instr: TypedInstruction = when (expr) {
            is AssignExpr -> {
                val (variable, level, index) = resolver.resolveVar(expr.id)
                checkType(expr.assigned, variable.type)
                if (variable is Symbol.StaticSymbol) AssignStatic(variable.value) + VoidType
                else AssignInstr(level, index) + VoidType
            }
            is CallExpr -> {
                val resolved = resolver.resolveFunc(expr.functionId)
                val func = resolved.res
                expr.params.forEachIndexed { i, arg ->
                    checkType(arg, func.type.paramTypes[i])
                }
                yield(ReadStatic((func.symbol as Symbol.StaticSymbol).value))
                CallInstr(expr.params.size) + func.type.retType
            }
            is LitExpr<*> -> LitInstr(Value.Primitive(expr.lit.value)) + expr.lit.type
            is OpExpr -> TODO()
            is ReadExpr -> {
                val (symbol, level, index) = resolver.resolveVar(expr.id)
                if (symbol is Symbol.StaticSymbol) ReadStatic(symbol.value) + symbol.type
                else ReadInstr(level, index) + symbol.type
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
        resolver = NameResolver(resolver)
        instructionStack.add(ArrayList())

        params.forEach { resolver.add(Symbol.createVar(it.id, resolver.resolveType(it.typeId).res)) }

        val varNum = processLambda(lambdaExpr)

        resolver = resolver.parent!!
        return Composite(params.size + varNum, instructionStack.removeLast(), instructionStack.size)
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

        for (funcDecl in pendingFunctions) {
            val func = resolver.resolveFunc(funcDecl.id).res.symbol as Symbol.StaticSymbol
            func.load(processScope(funcDecl.init, funcDecl.params))
            println()
        }

        // parse instructions
        for (expr in lambdaExpr.expressions) {
            checkType(expr, AnyType)
        }

        return varNum
    }

    override fun process(r: List<Expression>): Composite {
        instructionStack.add(ArrayList())

        builtins.forEach { resolver.add(it) }

        resolver.add(BuiltinType.I32Type)
        resolver.add(BuiltinType.CharType)
        resolver.add(BuiltinType.StrType)
        resolver.add(BuiltinType.VoidType)

        val varNum = processLambda(LambdaExpr(r.toMutableList()))

        val main = instructionStack.removeLast()
        return Composite(varNum + builtins.size, main, 0)
    }

    private data class PendingFunc(val symbol: Symbol.StaticSymbol, val decl: FunDeclare)
    private class TypedInstruction(val instr: Instruction?, override val type: Type) : HasType
    private operator fun Instruction.plus(type: Type) = TypedInstruction(this, type)
}
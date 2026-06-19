package com.archvin.pipeline.finalizing

import com.archvin.data.HasType
import com.archvin.data.symbol.Symbol
import com.archvin.data.type.BuiltinType.AnyType
import com.archvin.data.type.BuiltinType.VoidType
import com.archvin.data.type.Type
import com.archvin.data.value.LambdaVal.Composite
import com.archvin.data.value.Value
import com.archvin.exceptions.CompileError
import com.archvin.pipeline.IStage
import com.archvin.pipeline.finalizing.Instruction.*
import com.archvin.pipeline.parsing.AstNode.Declaration
import com.archvin.pipeline.parsing.AstNode.Declaration.FunDeclare
import com.archvin.pipeline.parsing.AstNode.Declaration.VarDeclare
import com.archvin.pipeline.parsing.Expression
import com.archvin.pipeline.parsing.Expression.*
import com.archvin.pipeline.parsing.LambdaExpr
import com.archvin.utils.pop

object TypeChecker : IStage<Composite, LambdaExpr> {

    private val instructionStack = ArrayDeque<ArrayList<Instruction>>()

    private var resolver: NameResolver = NameResolver.TopResolver()

    fun yield(add: Instruction) {
        instructionStack.last().add(add)
    }

    private fun declare(dec: Declaration): Symbol {
        val symbol = when (dec) {
            is FunDeclare -> {
                val retType = resolver.resolveType(dec.retType).res
                val paramTypes = dec.paramTypes.map { resolver.resolveType(it).res }
                Symbol.createFun(dec.id, retType, paramTypes)
            }

            is VarDeclare -> {
                val type = resolver.resolveType(dec.typeId).res
                Symbol.createVar(dec.id, type)
            }
        }

        resolver.add(symbol)

        return symbol
    }

    private fun checkType(expr: Expression, expectType: Type): TypedInstruction {
        val instr: TypedInstruction = when (expr) {
            is AssignExpr -> {
                val (variable, level, index) = resolver.resolveVar(expr.id)
                checkType(expr.assigned, variable.type)
                AssignInstr(level, index) + VoidType
            }
            is CallExpr -> {
                val func = resolver.resolveFunc(expr.functionId).res
                expr.params.forEachIndexed { i, arg ->
                    checkType(arg, func.type.paramTypes[i])
                }
                CallInstr(expr.params.size) + func.type.retType
            }
            is LitExpr<*> -> LitInstr(Value.Primitive(expr.lit.value)) + expr.lit.type
            is OpExpr -> TODO()
            is ReadExpr -> {
                val (symbol, level, index) = resolver.resolveVar(expr.id)
                ReadInstr(level, index) + symbol.type
            }
            is LambdaExpr -> LitInstr(processLambda(expr)) + VoidType
        }

        if (instr.type != expectType && expectType != AnyType)
            throw CompileError.TypeMismatchError(expectType, instr.type)

        instr.instr?.let { yield(it) }
        return instr
    }

    private fun processLambda(lambdaExpr: LambdaExpr): Composite {
        resolver = NameResolver(resolver)

        instructionStack.add(ArrayList())

        val pendingFunctions = mutableListOf<FunDeclare>()

        for (decl in lambdaExpr.declares) {
            declare(decl)
            if (decl is FunDeclare) {
                pendingFunctions.add(decl)
            }
        }

        for (expr in lambdaExpr.expressions) {
            checkType(expr, AnyType)
        }

        for (funcDecl in pendingFunctions) {
            processLambda(funcDecl.init)
        }

        resolver = resolver.parent!!

        return Composite(lambdaExpr.declares.size, instructionStack.pop(), instructionStack.size)
    }

    override fun process(r: LambdaExpr) = processLambda(r)

    private class TypedInstruction(val instr: Instruction?, override val type: Type) : HasType
    private operator fun Instruction.plus(type: Type) = TypedInstruction(this, type)
}
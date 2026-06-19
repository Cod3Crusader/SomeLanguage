package com.archvin.pipeline.finalizing

import com.archvin.data.HasType
import com.archvin.data.symbol.BuiltinFunction
import com.archvin.data.symbol.Symbol
import com.archvin.data.type.BuiltinType
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

object TypeChecker : IStage<Composite, LambdaExpr> {

    private val instructionStack = ArrayDeque<ArrayList<Instruction>>()

    private var resolver: NameResolver = NameResolver()

    fun yield(add: Instruction) {
        instructionStack.last().add(add)
    }

    private fun declare(dec: Declaration) {
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
    }

    private fun checkType(expr: Expression, expectType: Type): TypedInstruction {
        val instr: TypedInstruction = when (expr) {
            is AssignExpr -> {
                val (variable, level, index) = resolver.resolveVar(expr.id)
                checkType(expr.assigned, variable.type)
                AssignInstr(level, index) + VoidType
            }
            is CallExpr -> {
                val resolved = resolver.resolveFunc(expr.functionId)
                val func = resolved.res
                expr.params.forEachIndexed { i, arg ->
                    checkType(arg, func.type.paramTypes[i])
                }
                yield(ReadInstr(resolved.level, resolved.index))
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

        processScope(lambdaExpr)

        resolver = resolver.parent!!

        return Composite(lambdaExpr.declares.size, instructionStack.removeLast(), instructionStack.size)
    }

    private fun processScope(lambdaExpr: LambdaExpr) {
        val pendingFunctions = mutableListOf<FunDeclare>()

        for (decl in lambdaExpr.declares) {
            declare(decl)
            if (decl is FunDeclare) {
                pendingFunctions.add(decl)
            }
        }

        for (funcDecl in pendingFunctions) {
            yield(LitInstr(processLambda(funcDecl.init)))
            val (_, level, index) = resolver.resolveFunc(funcDecl.id)
            yield(AssignInstr(level, index))
        }

        for (expr in lambdaExpr.expressions) {
            checkType(expr, AnyType)
        }
    }

    override fun process(r: LambdaExpr): Composite {
        instructionStack.add(ArrayList())

        BuiltinFunction.builtins.forEachIndexed { i, it ->
            resolver.add(it)
            instructionStack.last().add(LitInstr(it.lambda))
            instructionStack.last().add(AssignInstr(0, i))
        }

        resolver.add(BuiltinType.I32Type)
        resolver.add(BuiltinType.CharType)
        resolver.add(BuiltinType.StrType)
        resolver.add(BuiltinType.VoidType)

        processScope(r)

        val main = instructionStack.removeLast()
        return Composite(BuiltinFunction.builtins.size + r.declares.size, main, 0)
    }

    private class TypedInstruction(val instr: Instruction?, override val type: Type) : HasType
    private operator fun Instruction.plus(type: Type) = TypedInstruction(this, type)
}
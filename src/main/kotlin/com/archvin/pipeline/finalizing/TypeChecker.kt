package com.archvin.pipeline.finalizing

import com.archvin.data.HasType
import com.archvin.data.type.BuiltinType.AnyType
import com.archvin.data.type.BuiltinType.VoidType
import com.archvin.data.type.Type
import com.archvin.data.value.LambdaVal
import com.archvin.data.value.Value
import com.archvin.data.symbol.Symbol
import com.archvin.exceptions.CompileError
import com.archvin.pipeline.IStage
import com.archvin.pipeline.finalizing.Instruction.*
import com.archvin.pipeline.parsing.AstNode.Declaration
import com.archvin.pipeline.parsing.AstNode.Declaration.FunDeclare
import com.archvin.pipeline.parsing.AstNode.Declaration.VarDeclare
import com.archvin.pipeline.parsing.Expression
import com.archvin.pipeline.parsing.Expression.*
import com.archvin.pipeline.parsing.Scope

object TypeChecker : IStage.IProvider<Instruction, Scope> {
    override val ret = mutableListOf<Instruction>()

    private val instructionStack = ArrayDeque<MutableList<Instruction>>()

    private var resolver: NameResolver = NameResolver.TopResolver()

    override fun yield(add: Instruction) {
        if (instructionStack.isNotEmpty())
            instructionStack.last().add(add)
        else
            ret.add(add)
    }

    private fun declareDeclaration(dec: Declaration): Symbol {
        return when (dec) {
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
    }

    private fun checkType(expr: Expression, expectType: Type): TypedInstruction {
        val instr: TypedInstruction = when (expr) {
            is AssignExpr -> {
                val variable = resolver.resolveVar(expr.id).res
                checkType(expr.assigned, variable.type)
                AssignInstr(variable) + VoidType
            }
            is CallExpr -> {
                val func = resolver.resolveFunc(expr.functionId).res
                expr.params.forEachIndexed { i, arg ->
                    checkType(arg, func.type.paramTypes[i])
                }
                CallInstr(func) + func.type.retType
            }
            is LambdaExpr -> {
                val bodyInstructions = mutableListOf<Instruction>()
                instructionStack.add(bodyInstructions)
                expr.expressions.forEach { checkType(it, AnyType) }
                instructionStack.removeLast()
                LitInstr(LambdaVal.Composite(bodyInstructions)) +
                        Type.FunctionType(VoidType, emptyList()) // TODO: proper lambda type
            }
            is LitExpr<*> -> LitInstr(Value.Primitive(expr.lit.value)) + expr.lit.type
            is OpExpr -> TODO()
            is ReadExpr -> {
                val symbol = resolver.resolveVar(expr.id).res
                ReadInstr(symbol) + symbol.type
            }
        }

        if (instr.type != expectType && expectType != AnyType)
            throw CompileError.TypeMismatchError(expectType, instr.type)

        instr.instr?.let { yield(it) }
        return instr
    }

    private fun processScope(scope: Scope, ownerLambda: LambdaVal.Composite? = null) {
        resolver = (NameResolver(resolver))

        if (ownerLambda != null) instructionStack.add(ownerLambda.instructions)

        val pendingFunctions = mutableListOf<PendingFunction>()

        for (decl in scope.varDeclares) {
            val symbol = declareDeclaration(decl)
            val index = resolver.add(symbol).index
            if (decl is FunDeclare) {
                pendingFunctions.add(PendingFunction(decl, symbol))
            }
        }

        for (expr in scope.expressions) {
            checkType(expr, AnyType)
        }

        for ((funcDecl, lambda) in pendingFunctions) {
            processScope(funcDecl.scope, lambda)
        }

        if (ownerLambda != null) instructionStack.removeLast()

        resolver = resolver.parent!!
    }

    override fun process(r: Scope): List<Instruction> {
        processScope(r, ownerLambda = null)
        return ret
    }

    private data class PendingFunction(val decl: FunDeclare, val lambda: LambdaVal.Composite)
    private class TypedInstruction(val instr: Instruction?, override val type: Type) : HasType
    private operator fun Instruction.plus(type: Type) = TypedInstruction(this, type)
}
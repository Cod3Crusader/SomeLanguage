package com.archvin.pipeline.finalizing

import com.archvin.data.HasType
import com.archvin.data.type.BuiltinType.AnyType
import com.archvin.data.type.BuiltinType.VoidType
import com.archvin.data.type.Type
import com.archvin.data.value.LambdaVal
import com.archvin.data.value.Value
import com.archvin.data.variable.Symbol
import com.archvin.data.variable.Symbol.Function.CustomFunction
import com.archvin.data.variable.Symbol.Variable
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
    private val resolverStack = ArrayDeque<NameResolver>(listOf(NameResolver.TopResolver()))

    private fun resolver() = resolverStack.last()

    override fun yield(add: Instruction) {
        if (instructionStack.isNotEmpty())
            instructionStack.last().add(add)
        else
            ret.add(add)
    }

    private fun declareDeclaration(dec: Declaration): Symbol {
        return when (dec) {
            is FunDeclare -> {
                val retType = resolver().resolveType(dec.retType)
                val paramTypes = dec.paramTypes.map { resolver().resolveType(it) }
                CustomFunction(dec.id, Type.FunctionType(retType, paramTypes))
            }

            is VarDeclare -> {
                val type = resolver().resolveType(dec.typeId)
                Variable(dec.id, type)
            }
        }
    }

    private fun checkType(expr: Expression, expectType: Type): TypedInstruction {
        val instr: TypedInstruction = when (expr) {
            is AssignExpr -> {
                val variable = resolver().resolveVar(expr.id)
                checkType(expr.assigned, variable.type)
                AssignInstr(variable) + VoidType
            }
            is CallExpr -> {
                val func = resolver().resolveFunc(expr.functionId)
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
                val symbol = resolver().resolveVar(expr.id)
                ReadInstr(symbol) + symbol.type
            }
        }

        if (instr.type != expectType && expectType != AnyType)
            throw CompileError.TypeMismatchError(expectType, instr.type)

        instr.instr?.let { yield(it) }
        return instr
    }

    private data class PendingFunction(val decl: FunDeclare, val symbol: CustomFunction)

    private fun processScope(scope: Scope, ownerFunction: CustomFunction? = null) {
        resolverStack.add(NameResolver(resolver()))

        if (ownerFunction != null) {
            instructionStack.add(mutableListOf())
        }

        val pendingFunctions = mutableListOf<PendingFunction>()

        for (decl in scope.varDeclares) {
            val symbol = declareDeclaration(decl)
            resolver().add(symbol)
            if (decl is FunDeclare) {
                pendingFunctions.add(PendingFunction(decl, symbol as CustomFunction))
            }
        }

        for (expr in scope.expressions) {
            checkType(expr, AnyType)
        }

        for ((funcDecl, funcSym) in pendingFunctions) {
            processScope(funcDecl.scope, funcSym)
        }

        if (ownerFunction != null) {
            val bodyInstructions = instructionStack.removeLast()
            ownerFunction.setValue(LambdaVal.Composite(bodyInstructions))
        }

        resolverStack.removeLast()
    }

    override fun process(r: Scope): List<Instruction> {
        processScope(r, ownerFunction = null)
        return ret
    }

    private class TypedInstruction(val instr: Instruction?, override val type: Type) : HasType
    private operator fun Instruction.plus(type: Type) = TypedInstruction(this, type)
}
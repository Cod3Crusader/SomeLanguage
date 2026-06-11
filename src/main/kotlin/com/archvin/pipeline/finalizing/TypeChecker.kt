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
import com.archvin.pipeline.parsing.Expression
import com.archvin.pipeline.parsing.Expression.*
import com.archvin.pipeline.parsing.Scope


object TypeChecker : IStage.IProvider<Instruction, Scope> {
    override val ret = mutableListOf<Instruction>()

    private val scopeStack = ArrayDeque<ArrayList<Instruction>>()
    private val resolverStack = ArrayDeque<NameResolver>(listOf(NameResolver.TopResolver()))

    private fun resolver() = resolverStack.last()

    override fun yield(add: Instruction) {
        if (scopeStack.isNotEmpty()) scopeStack.last().add(add)
        else super.yield(add)
    }

    fun checkDeclaration(dec: Declaration): Symbol {
        return if (dec is FunDeclare) {
            val retType = resolver().resolveType(dec.retType)
            val paramTypes = dec.paramTypes.map { resolver().resolveType(it) }
            val func = CustomFunction(dec.id, Type.FunctionType(retType, paramTypes))
            func
        }
        else {
            val type = resolver().resolveType(dec.typeId)
            val variable = Variable(dec.id, type)
            variable
        }
    }

    fun checkType(expr: Expression, expectType: Type) {
        val instr: TypedInstruction = when (expr) {
            is AssignExpr -> {
                val variable = resolver().resolveVar(expr.id)

                checkType(expr.assigned, variable.type)

                AssignInstr(variable) + VoidType
            }
            is CallExpr -> {
                val func = resolver().resolveFunc(expr.functionId)

                expr.params.mapIndexed { i, it -> checkType(it, func.type.paramTypes[i]) }

                CallInstr(func) + func.type.retType
            }
            is LambdaExpr -> {
                val params = ArrayList<Instruction>()

                scopeStack.add(params)
                expr.expressions.forEach { checkType(it, AnyType) }
                scopeStack.removeLast()

                LitInstr(LambdaVal.Composite(params.toList())) +
                        Type.FunctionType(VoidType, emptyList()) // TODO
            }

            is LitExpr<*> -> LitInstr(Value.Primitive(expr.lit.value)) + expr.lit.type
            is OpExpr -> TODO()
            is ReadExpr -> {
                val symbol = resolver().resolveVar(expr.id)
                ReadInstr(symbol) + symbol.type
            }
        }

        if (instr.type != expectType) throw CompileError.TypeMismatchError(expectType, instr.type)


        instr.instr?.let { yield(it) }
    }

    private fun checkScope(s: Scope) {
        resolverStack.add(NameResolver(resolver()))
        val scopes = mutableListOf<Scope>()
        s.declarations.forEach {
            val symbol = checkDeclaration(it)
            resolver().add(symbol)
            if (it is FunDeclare) scopes.add(it.scope)
        }
        s.expressions.forEach { checkType(it, AnyType) }
        resolverStack.removeLast()
    }

    override fun process(r: Scope): List<Instruction> {
        checkScope(r)

        return ret
    }

    private class TypedInstruction(val instr: Instruction?, override val type: Type) : HasType
    private operator fun Instruction.plus(type: Type) = TypedInstruction(this, type)
}


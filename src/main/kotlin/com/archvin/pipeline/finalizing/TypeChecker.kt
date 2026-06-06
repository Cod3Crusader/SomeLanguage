package com.archvin.pipeline.finalizing


import com.archvin.data.type.BuiltinType.AnyType
import com.archvin.data.type.BuiltinType.VoidType
import com.archvin.data.type.Type
import com.archvin.data.HasType
import com.archvin.data.value.LambdaVal
import com.archvin.data.value.Value
import com.archvin.data.variable.Symbol.Function.CustomFunction
import com.archvin.data.variable.Symbol.Variable
import com.archvin.exceptions.CompileError
import com.archvin.pipeline.Stage
import com.archvin.pipeline.finalizing.Instruction.*
import com.archvin.pipeline.parsing.Expression
import com.archvin.pipeline.parsing.Expression.DeclareExpr.FunDeclare


class TypeChecker : Stage<Instruction, Expression>() {
    val resolver = NameResolver()

    private val scopeStack = ArrayDeque<ArrayList<Instruction>>()

    override fun yield(add: Instruction) {
        if (scopeStack.isNotEmpty()) scopeStack.last().add(add)
        else super.yield(add)
    }

    fun checkType(expr: Expression, expectType: Type) {
        val instr: TypedInstruction = when (expr) {
            is Expression.AssignExpr -> {
                val variable = resolver.resolveVar(expr.id)

                checkType(expr.assigned, variable.type)

                AssignInstr(variable) + VoidType
            }
            is Expression.DeclareExpr -> {
                val symbol = if (expr is FunDeclare) {
                    val retType = resolver.resolveType(expr.retType)
                    val paramTypes = expr.paramTypes.map { resolver.resolveType(it) }
                    val func = CustomFunction(expr.id, Type.FunctionType(retType, paramTypes))
                    resolver.add(func)
                    func
                }
                else {
                    val type = resolver.resolveType(expr.typeId)
                    val variable = Variable(expr.id, type)
                    resolver.add(variable)
                    variable
                }

                checkType(r.step(), symbol.type)
                yield(AssignInstr(symbol))

                TypedInstruction(null, VoidType)
            }
            is Expression.CallExpr -> {
                val func = resolver.resolveFunc(expr.functionId)

                expr.params.mapIndexed { i, it -> checkType(it, func.type.paramTypes[i]) }

                CallInstr(func) + func.type.retType
            }
            is Expression.LambdaExpr -> {
                val params = ArrayList<Instruction>()

                scopeStack.add(params)
                expr.expressions.forEach { checkType(it, AnyType) }
                scopeStack.removeLast()

                LitInstr(LambdaVal.Composite(params.toList())) +
                        Type.FunctionType(VoidType, emptyList()) // TODO
            }

            is Expression.LitExpr<*> -> LitInstr(Value.Primitive(expr.lit.value)) + expr.lit.type
            is Expression.OpExpr -> TODO()
            is Expression.ReadExpr -> {
                val symbol = resolver.resolveVar(expr.id)
                ReadInstr(symbol) + symbol.type
            }
        }

        if (instr.type != expectType) throw CompileError.TypeMismatchError(expectType, instr.type)


        instr.instr?.let { yield(it) }
    }

    override fun step(c: Expression) { checkType(c, AnyType) }

    private class TypedInstruction(val instr: Instruction?, override val type: Type) : HasType
    private operator fun Instruction.plus(type: Type) = TypedInstruction(this, type)
}


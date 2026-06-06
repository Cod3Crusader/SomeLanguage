package com.archvin.pipeline.finalizing


import com.archvin.data.type.BuiltinType.AnyType
import com.archvin.data.type.BuiltinType.VoidType
import com.archvin.data.type.Type
import com.archvin.data.HasType
import com.archvin.data.value.Value
import com.archvin.data.variable.Symbol.Function.CustomFunction
import com.archvin.data.variable.Symbol.Variable
import com.archvin.exceptions.CompileError
import com.archvin.pipeline.Stage
import com.archvin.pipeline.finalizing.Instruction.*
import com.archvin.pipeline.finalizing.Instruction
import com.archvin.pipeline.parsing.Expression


class TypeChecker : Stage<Instruction, Expression>() {
    val resolver = NameResolver()


    fun checkType(expr: Expression, expectType: Type) {
        val instr: TypedInstruction = when (expr) {
            is Expression.AssignExpr -> {
                val variable = resolver.resolveVar(expr.id)


                checkType(expr.assigned, variable.type)


                AssignInstr(variable) retType VoidType
            }
            is Expression.DeclareExpr -> {
                val symbolType = resolver.resolveType(expr.typeId)
                resolver.add(
                    if (symbolType is Type.FunctionType) CustomFunction(expr.id, symbolType)
                    else Variable(expr.id, symbolType)
                )


                TypedInstruction(null, VoidType)
            }
            is Expression.CallExpr -> {
                val func = resolver.resolveFunc(expr.functionId)


                expr.params.mapIndexed { i, it -> checkType(it, func.type.paramTypes[i]) }


                CallInstr(func) retType func.type.retType
            }
            is Expression.LambdaExpr -> {
                TODO()
                //Lit(LambdaVal.Composite(expr.expressions.map { checkType(it, AnyType)!! }))
            }
            is Expression.LitExpr<*> -> LitInstr(Value.Primitive(expr.lit.value)) retType expr.lit.type
            is Expression.OpExpr -> TODO()
            is Expression.ReadExpr -> {
                val symbol = resolver.resolveVar(expr.id)
                ReadInstr(symbol) retType symbol.type
            }
        }


        if (instr.type != expectType) throw CompileError.TypeMismatchError(expectType, instr.type)


        instr.instr?.let { yield(it) }
    }


    override fun step(c: Expression) { checkType(c, AnyType) }


    private class TypedInstruction(val instr: Instruction?, override val type: Type) : HasType
    private infix fun Instruction.retType(type: Type) = TypedInstruction(this, type)
}


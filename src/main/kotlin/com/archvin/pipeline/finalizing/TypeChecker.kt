package com.archvin.pipeline.finalizing

import com.archvin.data.type.BuiltinType.AnyType
import com.archvin.data.type.Type
import com.archvin.data.variable.Symbol.Function.CustomFunction
import com.archvin.data.variable.Symbol.Variable
import com.archvin.exceptions.CompileError
import com.archvin.pipeline.Stage
import com.archvin.pipeline.finalizing.CheckedExpr.*
import com.archvin.pipeline.parsing.Expression

class TypeChecker : Stage.ConsumerStage<CheckedExpr, Expression>() {
    val resolver = NameResolver()

    fun checkType(expr: Expression, expectType: Type): CheckedExpr? {
        val expr: CheckedExpr? = when (expr) {
            is Expression.AssignExpr -> {
                val variable = resolver.resolveVar(expr.id)


                Assign(variable, checkType(expr.assigned, variable.type)!!)
            }
            is Expression.DeclareExpr -> {
                val symbolType = resolver.resolveType(expr.typeId)
                resolver.add(
                    if (symbolType is Type.FunctionType) CustomFunction(expr.id, symbolType)
                    else Variable(expr.id, symbolType)
                )

                null // TODO
            }
            is Expression.CallExpr -> {
                val func = resolver.resolveFunc(expr.functionId)

                Call(func, expr.params
                    .mapIndexed { i, it -> checkType(it, func.type.paramTypes[i])!! })
            }
            is Expression.LambdaExpr -> {
                TODO()
                //Lit(LambdaVal.Composite(expr.expressions.map { checkType(it, AnyType)!! }))
            }
            is Expression.LitExpr<*> -> Lit(expr.lit)
            is Expression.OpExpr -> TODO()
            is Expression.ReadExpr -> Read(resolver.resolveVar(expr.id))
        }

        if (expr?.type != expectType) throw CompileError.TypeMismatchError(expectType, expr!!.type)

        return expr
    }

    override fun consume(c: Expression): CheckedExpr? {
        return checkType(c, AnyType)
    }
}
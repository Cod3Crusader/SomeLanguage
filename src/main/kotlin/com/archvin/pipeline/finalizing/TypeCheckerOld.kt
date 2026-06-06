package com.archvin.pipeline.finalizing

/*
class TypeChecker : ConsumerStage<Instruction, Expression>() {
    private val resolver = NameResolver()
    private val topManager = InstructionManager()

    private val scopeStack = ArrayDeque<PendingScope>()

    private fun manager() = scopeStack.lastOrNull()?.manager ?: topManager

    fun declare(c: DeclareExpr) {
        if (c !is DeclareExpr.FunDeclare) {

            val type = resolver.resolveType(c.typeId)
            val variable = Variable(c.id, type, c.isMutable)
            resolver.add(variable)
            manager().addPending(AssignInstr(variable), listOf(variable.type))
        } else {
            val type =
                 Type.FunctionType(
                    resolver.resolveType(c.retType),
                    c.paramTypes.map { resolver.resolveType(it) }
                )

            if (r.peek() !is LambdaExpr) throw CompileError.UninitializedError(c.id)

            val variable = Symbol.Function.CustomFunction(c.id, type)
            resolver.add(variable)
            manager().addPending(AssignInstr(variable), listOf(variable.type))
        }
    }

    fun yield(add: Instruction) {
        if (scopeStack.isEmpty()) super.yield(add)
        else scopeStack.last().instructions.add(add)
    }

    override fun consume(c: Expression): Instruction {
        when (c) {
            is ReadExpr -> {

                val resolved = resolver.resolveVar(c.id)
                manager().addComplete(ReadInstr(resolved), resolved.type)
            }
            is LitExpr<*> -> manager().addComplete(LitInstr(Value.Primitive(c.lit.value)), c.lit.type)
            is Assign -> {
                val variable = resolver.resolveVar(c.variableId)
                manager().addPending(AssignInstr(variable), listOf(variable.type))
                if (!variable.isMutable) throw CompileError.CannotReassign(variable)
            }
            is DeclareExpr -> declare(c)
            is CallExpr -> {
                val resolved = resolver.resolveFunc(c.functionId)
                val paramNum = resolved.type.paramTypes.size
                if (c.paramNum != paramNum) throw CompileError.InvalidArgumentCount(c.functionId, paramNum, c.paramNum)

                manager().addPending(CallInstr(resolved), resolved.type.paramTypes, resolved.type.retType)
            }

            is LambdaExpr -> {
                scopeStack.add(PendingScope())

                val top = manager().peek()

                for (i in 1 .. c.exprNum) {
                    consume(r.step())
                }
                if (manager().peek() != top) error("useful message")

                val lambda = LambdaVal.Composite(scopeStack.removeLast().instructions)
                manager().addComplete(LitInstr(lambda), Type.FunctionType(VoidType, emptyList())) // TODO
            }

            is OpExpr -> TODO()
            is PassExpr -> TODO("remove this sometime")
        }
    }

    override fun process(r: Reader<Expression>): List<Instruction> {
        val ret = super.process(r)

        if (manager().hasPending()) error("unfinished scope or instruction")

        return ret
    }

    private inner class InstructionManager {
        inner class PendingInstruction(val instr: Instruction, val type: Type, val paramTypes: List<Type>) : Debug() {
            var counter = paramTypes.size
        }

        private val instrStack = ArrayDeque<PendingInstruction>()

        fun peek() = instrStack.lastOrNull()
        fun hasPending() = instrStack.isNotEmpty()

        private fun checkType(type: Type) {
            val top = peek() ?: return

            val types = top.paramTypes
            val expected = types[types.size - top.counter]
            if (expected != type) throw CompileError.TypeMismatchError(expected, type)
        }

        private fun tryPop() {
            if (peek()?.counter != 0) return

            val instr = instrStack.removeLastOrNull()!!.instr
            yield(instr)

            peek()?.counter--
            tryPop()
        }

        fun addPending(instr: Instruction, paramTypes: List<Type>, retType: Type = VoidType) {
            checkType(retType)

            instrStack.add(PendingInstruction(instr, retType, paramTypes))

           tryPop()
        }

        fun addComplete(instr: Instruction, type: Type) {
            assert(instr.paramNum == 0)

            checkType(type)

            yield(instr)

            peek()?.counter--
            tryPop()
        }
    }

    private inner class PendingScope {
        val manager = InstructionManager()
        val instructions = arrayListOf<Instruction>()
    }
}
 */
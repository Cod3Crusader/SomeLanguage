package com.archvin.pipeline.execution

import com.archvin.utils.Debug

open class RuntimeScope(val varNum: Int, val startValues: List<Value> = List(varNum) { Value.Uninitialized }) : Debug() {
    private val stack = ArrayDeque<MutableList<Value>>()

    fun incDepth() { stack.addLast(startValues.toMutableList()) }
    fun decDepth() { stack.removeLast() }

    operator fun get(index: Int): Value {
        if (index < varNum) return stack.last()[index]
        else throw IndexOutOfBoundsException(index)
    }

    operator fun set(index: Int, newVal: Value) {
        if (index < varNum) stack.last()[index] = newVal
        else throw IndexOutOfBoundsException(index)
    }
}
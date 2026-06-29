package com.archvin.data.scope

import com.archvin.data.value.Value

open class Scope(val statics: MutableList<Value>, val varNum: Int) {
    private val staticCount = statics.size
    private val stack = ArrayDeque<MutableList<Value>>()

    fun incDepth() { stack.addLast(MutableList(varNum) { Value.Uninitialized }) }
    fun decDepth() { stack.removeLast() }

    operator fun get(index: Int): Value {
        if (index < staticCount) return statics[index]
        if (index < staticCount + varNum) return stack.last()[index]
        else throw IndexOutOfBoundsException(index)
    }

    operator fun set(index: Int, newVal: Value) {
        if (index < staticCount) statics[index] = newVal
        if (index < staticCount + varNum) stack.last()[index] = newVal
        else throw IndexOutOfBoundsException(index)
    }
}
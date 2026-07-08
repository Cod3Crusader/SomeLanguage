package com.archvin.pipeline.execution

import com.archvin.utils.Debug

open class RuntimeScope(val varNum: Int) : Debug() {
    private val startValues = MutableList<Value>(varNum) { Value.Uninitialized }
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

    fun forceChangeStart(index: Int, newValue: Value) { startValues[index] = newValue }

    fun changeStart(index: Int, newValue: Value) {
        if (index >= varNum) throw IndexOutOfBoundsException(index)
        if (newValue == Value.Uninitialized) error("start value should not be changed to Uninitialized")
        if (startValues[index] != Value.Uninitialized) error("start value for $index changed already")

        forceChangeStart(index, newValue)
    }
}
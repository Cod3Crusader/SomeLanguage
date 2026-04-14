package com.archvin.reader

sealed class ProcessorInputReader<out T> {
    var index : Int = 0
        protected set(value) { field = value.coerceIn(0, length()) }

    abstract fun length(): Int
    abstract fun get(i: Int): T
    abstract fun getAll(): Array<out T>

    fun current(): T = get(index)

    fun reset() { index = 0 }

    fun step(): T = get(++index)

    fun peek(i: Int = 1): T = get(index + i)
    fun isEof(): Boolean = index >= length()

    fun isEmpty(): Boolean = length() == 0
}
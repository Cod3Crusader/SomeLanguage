package com.archvin.reader

sealed class Reader<out T>(val length: Int) {
    var index : Int = 0
        protected set(value) { field = value.coerceIn(0, length) }

    init { if (length == 0) error("Reader is empty") }

    fun get(i: Int) = if (i in 0..<length) forceGet(i) else null

    abstract fun forceGet(i: Int): T
    abstract fun getAll(): List<T>

    fun current(): T = get(index) ?: forceGet(index-1)

    fun reset() { index = 0 }

    fun step(amount: Int = 1): T? {
        index += amount
        return get(index)
    }
    fun back() { index-- }

    fun peek(i: Int = 1): T? = get(index + i)
    fun isEof(): Boolean = index >= length
}
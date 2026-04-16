package com.archvin.reader

class SimpleReader<out T>(private val content: Array<out T>, defaultValue: T) : Reader<T>(defaultValue) {
    override fun length(): Int = content.size
    override fun get(i: Int): T = if (i in 0 until length()) content[i] else defaultValue
    override fun getAll(): Array<out T> = content.clone()
}
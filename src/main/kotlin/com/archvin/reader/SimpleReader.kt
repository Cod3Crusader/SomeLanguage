package com.archvin.reader

class SimpleReader<out T>(private val content: List<T>, defaultValue: T) : Reader<T>(defaultValue) {
    override fun length(): Int = content.size
    override fun get(i: Int): T = if (i in 0 until length()) content[i] else defaultValue
    override fun getAll(): List<T> = content
}
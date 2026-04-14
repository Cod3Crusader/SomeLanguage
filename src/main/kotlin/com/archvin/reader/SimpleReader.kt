package com.archvin.reader

class SimpleReader<out T>(private val content: Array<out T>, defaultValue: T) : ProcessorInputReader<T>(defaultValue) {
    override fun length(): Int = content.size
    override fun get(i: Int): T = if (i < length()) content[i] else defaultValue
    override fun getAll(): Array<out T> = content.clone()
}
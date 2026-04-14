package com.archvin.reader

class SimpleReader<out T>(private val content: Array<out T>) : ProcessorInputReader<T>() {
    override fun length(): Int = content.size
    override fun get(i: Int): T = content[i]
    override fun getAll(): Array<out T> = content.clone()
}
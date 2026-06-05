package com.archvin.reader

class SimpleReader<out T>(private val content: List<T>) : Reader<T>() {
    override fun length(): Int = content.size
    override fun get(i: Int): T = content[i]
    override fun getAll(): List<T> = content
}
package com.archvin.reader

class SimpleReader<out T>(private val content: List<T>) : Reader<T>(content.size) {
    override fun forceGet(i: Int): T = content[i]
    override fun getAll(): List<T> = content
}
package com.archvin.process

import com.archvin.reader.Reader

sealed class Processor<T, R> {
    lateinit var r: Reader<R>
    val ret = mutableListOf<T>()

    abstract fun step(c: R)

    fun process(r: Reader<R>): List<T> {
        this.r = r

        r.reset()
        while (!r.isEof()) {
            step(r.current())
            r.step()
        }

        return ret.toList()
    }
}
package com.archvin.process

import com.archvin.reader.Reader

sealed interface Processor<T, R> {
    fun step(c: R, r: Reader<R>): T?

    fun process(r: Reader<R>): List<T> {
        val ret = mutableListOf<T>()

        r.reset()
        while (!r.isEof()) {
            step(r.current(), r)?.let { ret.add(it) }
            r.step()
        }

        return ret.toList()
    }
}
package com.archvin

import com.archvin.reader.Reader

abstract class Processor<T, R> {
    protected lateinit var r: Reader<R>
    private val ret = mutableListOf<T>()

    protected abstract fun step(c: R)

    protected open fun yield(add : T) {
        ret.add(add)
    }

    open fun process(r: Reader<R>): List<T> {
        this.r = r

        r.reset()
        while (!r.isEof()) {
            step(r.current())
            r.step()
        }

        return ret.toList()
    }
}
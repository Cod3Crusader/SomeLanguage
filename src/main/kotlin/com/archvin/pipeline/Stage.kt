package com.archvin.pipeline

import com.archvin.reader.Reader

abstract class Stage<T, R> {
    protected lateinit var r: Reader<R>
    protected val ret = mutableListOf<T>()

    protected abstract fun consume(c: R): T?

    protected open fun next(): T? = if (r.isEof()) consume(r.step()) else null

    open fun process(r: Reader<R>): List<T> {
        this.r = r

        r.reset()
        while (!r.isEof()) {
            consume(r.current())?.let { ret.add(it) }
            if (!r.isEof()) r.step()
        }

        return ret.toList()
    }
}
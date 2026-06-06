package com.archvin.pipeline

import com.archvin.reader.Reader

abstract class Stage<T, R> {
    protected lateinit var r: Reader<R>
    protected val ret = mutableListOf<T>()

    protected abstract fun step(c: R)

    protected open fun yield(add: T) { ret.add(add) }

    open fun process(r: Reader<R>): List<T> {
        this.r = r

        if (r.isEof()) return emptyList()

        ret.clear()
        r.reset()
        while (true) {
            step(r.current())
            if (r.isEof()) break
            r.step()
        }

        return ret.toList()
    }

    abstract class ConsumerStage<T, R> : Stage<T, R>() {
        override fun step(c: R) { consume(c)?.let { yield(it) } }

        protected abstract fun consume(c: R): T?
        protected open fun next(): T? = if (!r.isEof()) consume(r.step()) else null
    }
}

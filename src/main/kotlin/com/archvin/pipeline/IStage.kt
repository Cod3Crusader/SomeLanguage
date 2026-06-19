package com.archvin.pipeline

import com.archvin.reader.Reader

interface IStage<E, R> {
    fun process(r: R): E

    interface IConsumer<T, E, R> : IStage<E, Reader<R>> {
        var r: Reader<R>

        fun ret(): E

        fun read(): R? = if (!r.isEof()) r.step() else null

        fun until(u: R, lambda: (R) -> Unit) {
            var next = read()
            while (next != null && next != u) {
                lambda(next)
                next = read()
            }
        }

        override fun process(r: Reader<R>): E {
            this.r = r

            if (r.isEmpty()) return ret()

            r.reset()
            step(r.current())
            while (true) step(read() ?: break)
            return ret()
        }

        fun step(c: R) { consume(c) }
        fun consume(c: R): T?
        fun next(): T? = if (!r.isEof()) consume(r.step()) else null
    }

    interface IProvider<T, R>: IStage<List<T>, R> {
        val ret:  MutableList<T>

        fun yield(add: T) { ret.add(add) }
    }

    abstract class ProvideConsume<T, R> : IProvider<T, Reader<R>>, IConsumer<T, List<T>, R> {
        override val ret = mutableListOf<T>()
        override lateinit var r: Reader<R>

        override fun ret() = ret

        override fun step(c: R) { consume(c)?.let { yield(it) } }
    }
}

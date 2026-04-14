package com.archvin.process

import com.archvin.reader.ProcessorInputReader

sealed class Processor<R, T>(val r: ProcessorInputReader<R>) {
    protected abstract fun step(c: R): T?

    fun process(): Array<T> {
        val ret = arrayListOf<T>()

        r.reset()
        while (!r.isEof()) {
            step(r.current())?.let { ret.add(it) }
            r.step()
        }

        return ret.toArray() as Array<T>
    }
}
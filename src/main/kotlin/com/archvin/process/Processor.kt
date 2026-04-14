package com.archvin.process

import com.archvin.reader.ProcessorInputReader

sealed class Processor<R, T>(val r: ProcessorInputReader<R>) {
    abstract fun process(): Array<T>
}
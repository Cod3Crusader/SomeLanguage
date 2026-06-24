package com.archvin.reader

fun <T> Reader<T>.until(u: T, lambda: (T) -> Unit) {
    var next = step()
    while (next != null && next != u) {
        lambda(next)
        next = step()
    }
}

fun <T> Reader<T>.readAll(lambda: (T) -> Unit) {
    reset()
    lambda(current())
    while (true) lambda(step() ?: break)
}
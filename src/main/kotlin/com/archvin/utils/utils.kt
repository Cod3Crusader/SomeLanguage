package com.archvin.utils

fun funSignature(retType: String, paramTypes: List<String>) = "(${paramTypes.joinToString(", ")}): $retType"
fun <E> ArrayDeque<E>.pop() = this.removeLast()

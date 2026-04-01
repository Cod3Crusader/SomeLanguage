package com.archvin.type

interface HasType {
    val type: Type
    fun matches(t2: HasType): Boolean {return type == t2.type}
}
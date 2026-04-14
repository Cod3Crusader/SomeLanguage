package com.archvin.type

interface HasType {
    val type: Type
    fun matches(t2: HasType): Boolean = type.id == t2.type.id
}
package com.archvin.type

interface HasId {
    val id: String

    class TODO(override val id: String) : HasId
}
package com.archvin.utils

import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

// Created with Claude AI

abstract class Debug {
    open val className = this::class.simpleName ?: "unnamed"

    override fun toString(): String {
        if (this::class.objectInstance != null) return className

        val props = this::class.primaryConstructor
            ?.parameters
            ?.mapNotNull { param ->
                this::class.memberProperties
                    .find { it.name == param.name }
                    ?.let { "${it.getter.call(this)}" }
            }
            ?.joinToString(", ")
            ?: ""

        return "${className}($props)"
    }
}
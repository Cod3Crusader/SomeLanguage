package com.archvin.exceptions

sealed class RuntimeError(errorMessage: String) : Exception(errorMessage) {
}
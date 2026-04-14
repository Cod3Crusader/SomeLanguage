package com.archvin.exceptions

sealed class RuntimeError(errorMessage: String) : Exception(errorMessage) {
    class UnresolvedIdentifier(id: String) : RuntimeError("Unresolved identifier $id")
    class Redeclaration(id: String) : RuntimeError("Redeclaration for $id")
}
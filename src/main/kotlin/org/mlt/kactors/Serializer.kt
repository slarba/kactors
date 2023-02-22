package org.mlt.kactors

abstract class Serializer<T> {
    private var name: String? = null
    private var args: Array<out Any>? = null
    fun method(name: String) { this.name = name }
    fun args(vararg args: Any) { this.args = args }

    abstract fun real(): T

    fun serialize(): String = "serialized $name(${args?.joinToString(",") ?: ""})"
}

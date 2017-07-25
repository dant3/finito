package com.github.dant3.finito.impl

interface Monitor {
    fun <T> run(operation: () -> T): T

    companion object {
        fun synchronized() = object : Monitor {
            private val lock = Any()

            override fun <T> run(operation: () -> T): T = synchronized(lock) { operation() }
        }
    }
}
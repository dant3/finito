package com.github.dant3.finito.tests

val <T> List<T>.head: T
    get() = first()
val <T> List<T>.tail: List<T>
    get() = subList(1, size)
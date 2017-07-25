package com.github.dant3.finito

import com.github.dant3.finito.impl.FSMBuilderImpl

interface FSM<State, Event> {
    val currentState: State
    val currentStates: Set<State>

    fun receive(event: Event)

    companion object {
        fun <State, Event> define(builder: FSMBuilder<State, Event>.() -> State): FSM<State, Event> = FSMBuilderImpl.define(builder)
    }
}
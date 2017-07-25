package com.github.dant3.finito.impl

import com.github.dant3.finito.FSM

class ParallelFSM<State, Event>(val rootState: State, val childs: List<FSM<State, Event>>) : FSM<State, Event> {
    override val currentState: State = rootState
    override val currentStates: Set<State>
        get() = childs.fold(mutableSetOf<State>()) { accumulator, fsm ->
            accumulator.addAll(fsm.currentStates)
            accumulator
        }.toSet()

    override fun receive(event: Event) {
        childs.forEach { it.receive(event) }
    }
}
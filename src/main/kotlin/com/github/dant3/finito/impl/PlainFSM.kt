package com.github.dant3.finito.impl

import com.github.dant3.finito.FSM
import com.github.dant3.finito.TransitionListener

data class TransitionSource<State, Event>(val fromState: State, val onEvent: Event)
data class Transition<State, Event>(val source: TransitionSource<State, Event>, val targetState: State)

class PlainFSM<State, Event> internal constructor(startState: State,
                                                  transitions: MutableSet<Transition<State, Event>>,
                                                  val parallelStatesFactory: Map<State, () -> List<FSM<State, Event>>>,
                                                  val transitionListener: TransitionListener<State, Event> = TransitionListener.noop()): FSM<State, Event> {
    private val transitionMapping: Map<TransitionSource<State, Event>, State> = transitions.associate {
        it.source to it.targetState
    }
    private var currentParallelStates: List<FSM<State, Event>> = emptyList()

    override var currentState: State = startState
        private set
    override val currentStates: Set<State>
        get() = setOf(currentState) + currentParallelStates.flatMap{ it.currentStates }.toSet()

    override fun receive(event: Event) {
        synchronized(this) {
            val targetState = transitionMapping[currentState on event]
            if (targetState != null) {
                val previousState = currentState
                currentState = targetState
                currentParallelStates = parallelStatesFactory[targetState]?.invoke() ?: emptyList()
                transitionListener.onTransition(previousState, currentState, event)
            } else {
                // perhaps a child transition exists
                currentParallelStates.forEach { it.receive(event) }
            }
        }
    }

    private infix fun State.on(event: Event) = TransitionSource(this, event)
}
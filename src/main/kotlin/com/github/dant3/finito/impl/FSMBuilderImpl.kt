package com.github.dant3.finito.impl

import com.github.dant3.finito.*

class FSMBuilderImpl<State, Event> internal constructor(): FSMBuilder<State, Event> {
    private val onEnterActions: MutableMap<State, MutableList<OnEntryAction>> = mutableMapOf()
    private val onExitActions: MutableMap<State, MutableList<OnExitAction>> = mutableMapOf()
    private val transitions: MutableSet<Transition<State, Event>> = mutableSetOf()
    private val onTransitionAction: MutableMap<Triple<State, State, Event>, OnTransitionAction> = mutableMapOf()

    private val parallelStates: MutableMap<State, MutableList<FSMBuilder<State, Event>.() -> State>> = mutableMapOf()

    override fun onEnterState(state: State, action: OnEntryAction) {
        onEnterActions.computeIfAbsent(state, { _ -> mutableListOf() }).add(action)
    }

    override fun onExitState(state: State, action: OnExitAction) {
        onExitActions.computeIfAbsent(state, { _ -> mutableListOf() }).add(action)
    }

    override fun transition(fromState: State, toState: State, onEvent: Event, action: OnTransitionAction?) {
        val transition = Transition(TransitionSource(fromState = fromState, onEvent = onEvent), targetState = toState)
        transitions.add(transition)
        if (action != null) {
            onTransitionAction.put(Triple(fromState, toState, onEvent), action)
        }
    }

    override var onTransition: TransitionListener<State, Event>? = null

    override fun parallelStatesOf(parent: State, vararg builder: FSMBuilder<State, Event>.() -> State) {
        parallelStates.computeIfAbsent(parent, { _ -> mutableListOf() } ).addAll(builder)
    }

    internal fun build(initialState: State): FSM<State, Event> {
        val transitionListener = TransitionListener { from: State, to: State, event: Event ->
            onExitActions[from]?.forEach { it() }
            onEnterActions[to]?.forEach { it() }
            onTransition?.onTransition(from, to, event)
            onTransitionAction[Triple(from, to, event)]?.invoke()
        }
        return PlainFSM(initialState, transitions, parallelStates.mapValues { (_, builders) -> {
            builders.map { builder ->
                define<State, Event> {
                    val childInitialState = builder(this)
                    onTransition = transitionListener
                    childInitialState
                }
            }
        }}, transitionListener)
    }


    companion object {
        fun <State, Event> define(builder: FSMBuilder<State, Event>.() -> State): FSM<State, Event> {
            val fsmBuilder = FSMBuilderImpl<State, Event>()
            val initialState = builder(fsmBuilder)
            return fsmBuilder.build(initialState)
        }
    }
}
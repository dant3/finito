package com.github.dant3.finito

typealias Transition<State, Event> = Triple<State, Event, State>
typealias OnEntryAction = () -> Any
typealias OnExitAction = () -> Any
typealias OnTransitionAction = () -> Any

interface TransitionListener<in State, in Event> {
    fun onTransition(fromState: State, toState: State, event: Event)

    companion object {
        operator fun <State, Event> invoke(impl: (State, State, Event) -> Unit) = object : TransitionListener<State, Event> {
            override fun onTransition(fromState: State, toState: State, event: Event) { impl(fromState, toState, event) }
        }
        fun <State, Event> noop() = invoke<State, Event> { _, _, _ ->  }
    }
}


interface FSMBuilder<State, Event> {
    fun onEnterState(state: State, action: OnEntryAction)
    fun onExitState(state: State, action: OnExitAction)

    fun transition(fromState: State, toState: State, onEvent: Event, action: OnTransitionAction? = null)
    fun transition(transition: Transition<State, Event>, action: OnTransitionAction? = null) = transition(transition.first, transition.third, transition.second, action)

    var onTransition: TransitionListener<State, Event>?

    fun State.onEntry(action: OnEntryAction) = onEnterState(this, action)
    fun State.onExit(action: OnExitAction) = onExitState(this, action)

    fun parallelStatesOf(parent: State, builder: FSMBuilder<State, Event>.() -> State) = parallelStatesOf(parent, *arrayOf(builder))
    fun parallelStatesOf(parent: State, vararg builder: FSMBuilder<State, Event>.() -> State)

    fun State.parallelStates(vararg builder: FSMBuilder<State, Event>.() -> State) = parallelStatesOf(this, *builder)
    infix fun Pair<State, State>.on(event: Event): Transition<State, Event> = Triple(first, event, second)
}

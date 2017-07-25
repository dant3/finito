package com.github.dant3.finito.tests.button

import com.github.dant3.finito.FSM
import com.github.dant3.finito.TransitionListener

class Button {
    internal val fsm: FSM<State, Event> = FSM.define<State, Event> {
        State.Idle.parallelStates({
            transition(State.Disabled to State.Enabled on Event.Enabled)
            transition(State.Enabled to State.Disabled on Event.Disabled)
            State.Enabled
        }, {
            transition(State.NoHover to State.Hovered on Event.MouseOn)
            transition(State.Hovered to State.NoHover on Event.MouseOut)
            State.NoHover
        }, {
            transition(State.Released to State.Pressed on Event.MousePressed)
            transition(State.Pressed to State.Released on Event.MouseReleased)

            onExitState(State.Pressed) {
                handleClick()
            }

            State.Released
        })

        onTransition = TransitionListener { from, to, event ->
            println("Transition from $from to $to on event $event")
        }

        State.Idle
    }

    // the hierarchy is:
    // Idle
    // +- Disabled/Enabled
    // +- Hovered/NoHover
    // +- Clicked/NotClicked
    enum class State {
        Idle,
        Disabled,
        Enabled,
        Hovered,
        NoHover,
        Pressed,
        Released
    }

    enum class Event {
        MouseOn,
        MouseOut,
        MousePressed,
        MouseReleased,
        Disabled,
        Enabled
    }

    var isEnabled: Boolean
        get() = fsm.currentStates.contains(State.Enabled)
        set(value) = fsm.receive(if (value) Event.Enabled else Event.Disabled)

    var isHovered: Boolean
        get() = fsm.currentStates.contains(State.Hovered)
        set(value) = fsm.receive(if (value) Event.MouseOn else Event.MouseOut)

    var isPressed: Boolean
        get() = fsm.currentStates.contains(State.Pressed)
        set(value) = fsm.receive(if (value) Event.MousePressed else Event.MouseReleased)

    private var onClickListener: (() -> Any)? = null

    fun onClick(onClick: (() -> Any)?) {
        onClickListener = onClick
    }

    private fun handleClick() {
        if (fsm.currentStates.contains(State.Hovered)) {
            onClickListener?.invoke()
        }
    }

    fun draw(): String = when {
        !isEnabled -> "disabled"
        isHovered -> "hovered"
        isPressed -> "pressed"
        else -> "button"
    }
}
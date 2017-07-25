package com.github.dant3.finito.tests.trafficlights

import com.github.dant3.finito.FSM
import org.amshove.kluent.shouldBe

object Main {
    @JvmStatic fun main(args: Array<String>) {
        val fsm = FSM.define<Light, Command> {
            transition(Light.red to Light.yellow on Command.ready)
            transition(Light.yellow to Light.green on Command.go)
            transition(Light.green to Light.yellow on Command.ready)
            transition(Light.yellow to Light.red on Command.stop)

            Light.red
        }

        fsm.currentState shouldBe Light.red
        fsm.receive(Command.ready)
        fsm.currentState shouldBe Light.yellow
        fsm.receive(Command.ready)
        fsm.currentState shouldBe Light.yellow
        fsm.receive(Command.stop)
        fsm.currentState shouldBe Light.red
        fsm.receive(Command.ready)
        fsm.currentState shouldBe Light.yellow
        fsm.receive(Command.go)
        fsm.currentState shouldBe Light.green
        fsm.receive(Command.stop)
        fsm.currentState shouldBe Light.green
        fsm.receive(Command.ready)
        fsm.currentState shouldBe Light.yellow
        fsm.receive(Command.stop)
        fsm.currentState shouldBe Light.red
    }
}
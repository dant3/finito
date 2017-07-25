package com.github.dant3.finito.tests.snake

import com.github.dant3.finito.FSM
import java.util.*

class SnakeGame(val fieldWidth: Int = 40, val fieldHeight: Int = 40) {
    private val random = Random()

    var snake = Snake(spawnPoint = Point(fieldWidth / 2, fieldHeight / 2))
        private set
    var fruitLocation: Point = spawnFruit()
        private set

    private fun spawnFruit(): Point {
        var spawnPoint = randomFieldPoint()
        while (snake.isLocatedAt(spawnPoint)) {
            spawnPoint = randomFieldPoint()
        }
        return spawnPoint
    }


    @Throws(Snake.CollapsedException::class)
    fun step(direction: Snake.Direction) {
        val nextSnakePosition = snake.nextHeadPosition(direction)

        val willGrow = nextSnakePosition == fruitLocation
        if (willGrow) {
            fruitLocation = spawnFruit()
        }
        snake.move(direction, willGrow)

        val snakeHead = snake.headPosition
        if (snakeHead.x < 0 || snakeHead.x > fieldWidth) throw Snake.CollapsedException(snakeHead)
        if (snakeHead.y < 0 || snakeHead.y > fieldHeight) throw Snake.CollapsedException(snakeHead)
    }

    private fun randomFieldPoint(): Point = Point(x = random.nextInt(fieldWidth), y = random.nextInt(fieldHeight))


    enum class State {
        NEW, UP, LEFT, RIGHT, DOWN, MOVE, PAUSE, GAMEOVER
    }

    enum class Event {
        PRESS_START, TURN_UP, TURN_LEFT, TURN_RIGHT, TURN_DOWN, MOVE_AHEAD, DIED, PRESS_PAUSE
    }

    interface Controller {
        fun startNewGame()
        fun gameOver()
        fun pause()
        fun resume()

        fun advance(direction: Snake.Direction)
    }

    companion object {
        fun fsm(controller: Controller): FSM<State, Event> = FSM.define {
            transition(fromState = State.NEW, toState = State.MOVE, onEvent = Event.PRESS_START, action = controller::startNewGame)
            transition(fromState = State.GAMEOVER, toState = State.MOVE, onEvent = Event.PRESS_START, action = controller::startNewGame)
            transition(fromState = State.MOVE, toState = State.GAMEOVER, onEvent = Event.DIED, action = controller::gameOver)
            transition(fromState = State.MOVE, toState = State.PAUSE, onEvent = Event.PRESS_PAUSE, action = controller::pause)
            transition(fromState = State.PAUSE, toState = State.MOVE, onEvent = Event.PRESS_PAUSE, action = controller::resume)

            val directionalStates = setOf(State.UP, State.DOWN, State.LEFT, State.RIGHT)

            fun advanceAction(state: State) = { controller.advance(Snake.Direction.forState(state)) }
            fun turnAction(state: State) = { controller.advance(Snake.Direction.forState(state)) }

            parallelStatesOf(State.MOVE) {
                for (direction in directionalStates) { transition(direction, direction, Event.MOVE_AHEAD, action = advanceAction(direction)) }
                transition(State.UP, State.LEFT, Event.TURN_LEFT, action = turnAction(State.LEFT))
                transition(State.UP, State.RIGHT, Event.TURN_RIGHT, action = turnAction(State.RIGHT))
                transition(State.DOWN, State.LEFT, Event.TURN_LEFT, action = turnAction(State.LEFT))
                transition(State.DOWN, State.RIGHT, Event.TURN_RIGHT, action = turnAction(State.RIGHT))
                transition(State.LEFT, State.UP, Event.TURN_UP, action = turnAction(State.UP))
                transition(State.LEFT, State.DOWN, Event.TURN_DOWN, action = turnAction(State.DOWN))
                transition(State.RIGHT, State.UP, Event.TURN_UP, action = turnAction(State.UP))
                transition(State.RIGHT, State.DOWN, Event.TURN_DOWN, action = turnAction(State.DOWN))

                State.UP
            }

            State.NEW
        }
    }
}
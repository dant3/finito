package com.github.dant3.finito.tests.snake

import com.github.dant3.finito.FSM
import java.time.Duration


class SnakeGameEngine(val renderer: Renderer) : SnakeGame.Controller {
    val gameTickDuration: Duration = Duration.ofMillis(200)

    private var gameTimer: Timer? = null
    private var snakeGame: SnakeGame? = null
    private val snakeFsm: FSM<SnakeGame.State, SnakeGame.Event> = SnakeGame.fsm(this)

    init {
        renderer.drawNewGame()
    }

    fun receiveEvent(event: SnakeGame.Event) {
        snakeFsm.receive(event)
    }

    override fun startNewGame() {
        snakeGame = SnakeGame(renderer.gameZoneSize.first, renderer.gameZoneSize.second).also {
            renderer.drawGameState(it)
        }
        gameTimer = Timer(gameTickDuration) {
            snakeFsm.receive(SnakeGame.Event.MOVE_AHEAD)
        }.also {
            it.start()
        }
    }

    override fun gameOver() {
        gameTimer?.stop()
        renderer.drawGameOver()
    }

    override fun pause() {
        gameTimer?.isPaused = true
        renderer.drawPaused()
    }

    override fun resume() {
        gameTimer?.isPaused = false
        snakeGame?.let { renderer.drawGameState(it)  }
    }

    override fun advance(direction: Snake.Direction) {
        snakeGame?.let { game ->
            try {
                game.step(direction)
                renderer.drawGameState(game)
            } catch (ex: Snake.CollapsedException) {
                renderer.drawGameState(game)
                snakeFsm.receive(SnakeGame.Event.DIED)
            }
        }
    }

    interface Renderer {
        fun drawNewGame()
        fun drawGameState(game: SnakeGame)
        fun drawPaused()
        fun drawGameOver()
        val gameZoneSize: Pair<Int, Int>
    }
}
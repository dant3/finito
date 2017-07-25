package com.github.dant3.finito.tests.snake

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.Terminal
import java.nio.charset.Charset


object Main {
    @JvmStatic fun main(args: Array<String>) {
        val terminal = DefaultTerminalFactory(System.out, System.`in`, Charset.forName("UTF8")).createTerminal()
        terminal.enterPrivateMode();
        try {
            run(terminal)
        } finally {
            terminal.exitPrivateMode()
        }
    }

    private fun run(terminal: Terminal) {
        val renderer = TerminalSnakeRenderer(terminal)
        val gameController = SnakeGameEngine(renderer)

//        Thread {
            inputHandlingLoop(terminal, gameController)
//        }.apply {
//            isDaemon = false
//            name = "User input reader"
//        }.start()
    }

    private fun inputHandlingLoop(terminal: Terminal, gameController: SnakeGameEngine) {
        while (!Thread.interrupted()) {
            val gameEvent = terminal.readInput()?.let(Main::interpretUserInput)
            if (gameEvent != null) {
                gameController.receiveEvent(gameEvent)
            }
        }
    }

    private fun interpretUserInput(input: KeyStroke): SnakeGame.Event? = when (input.keyType) {
        KeyType.ArrowUp -> SnakeGame.Event.TURN_UP
        KeyType.ArrowDown -> SnakeGame.Event.TURN_DOWN
        KeyType.ArrowLeft -> SnakeGame.Event.TURN_LEFT
        KeyType.ArrowRight -> SnakeGame.Event.TURN_RIGHT
        KeyType.Enter -> SnakeGame.Event.PRESS_START
        KeyType.Escape -> { System.exit(0); null }
        KeyType.Character -> when (input.character) {
            ' ' -> SnakeGame.Event.PRESS_PAUSE
            else -> null
        }
        else -> null
    }

    class TerminalSnakeRenderer(val terminal: Terminal) : SnakeGameEngine.Renderer {
        private val terminalSize by lazy { terminal.terminalSize }
        private val terminalCenter by lazy { TerminalSize(terminalSize.columns / 2, terminalSize.rows / 2) }
        override val gameZoneSize by lazy { terminalSize.columns - 2 to terminalSize.rows - 4 }

        override fun drawNewGame() {
            terminal.clearScreen()
            drawGameFrame()
            val title = "| Snake |"
            val text = "Press 'Enter' to start new game"
            drawText(terminalCenter.columns - (title.length / 2), terminalCenter.rows - 1, title)
            drawText(terminalCenter.columns - (text.length / 2), terminalCenter.rows + 1, text)
            terminal.flush()
        }

        override fun drawGameOver() {
            terminal.clearScreen()
            drawGameFrame()
            val title = "!!! GAME OVER !!!"
            val text = "Press 'Enter' to start new game"
            drawText(terminalCenter.columns - (title.length / 2), terminalCenter.rows - 1, title)
            drawText(terminalCenter.columns - (text.length / 2), terminalCenter.rows + 1, text)
            terminal.flush()
        }

        private fun displayInfo(infoText: String) {
            val lineSize = terminalSize.columns - 4
            val drawnText = infoText.substring(0, minOf(infoText.length, lineSize)).padEnd(lineSize, ' ')
            drawText(2, terminalSize.rows - 2, drawnText)
            terminal.flush()
        }

        private fun drawText(x: Int, y: Int, text: String) {
            terminal.setCursorPosition(x, y)
            text.forEach(terminal::putCharacter)
        }

        private fun drawCrossedLine() {
            terminal.putCharacter('+')
            for (i in 1 .. terminalSize.columns - 2) {
                terminal.putCharacter('-')
            }
            terminal.putCharacter('+')
        }

        private fun drawGameFrame() {
            terminal.setCursorPosition(0, 0)
            drawCrossedLine()

            for (j in 1..terminalSize.rows - 1) {
                terminal.setCursorPosition(0, j)
                terminal.putCharacter('|')
                terminal.setCursorPosition(terminalSize.columns - 1, j)
                terminal.putCharacter('|')
            }

            terminal.setCursorPosition(0, terminalSize.rows - 1)
            drawCrossedLine()

            terminal.setCursorPosition(0, terminalSize.rows - 3)
            drawCrossedLine()
        }

        override fun drawGameState(game: SnakeGame) {
            fun Terminal.setCursorPosition(gamePoint: Point) {
                setCursorPosition(gamePoint.x + 1, gamePoint.y + 1)
            }
            fun snakeHeadChar(snakeDirection: Snake.Direction) = when (snakeDirection) {
                Snake.Direction.UP -> 'Λ'
                Snake.Direction.DOWN -> 'V'
                Snake.Direction.LEFT -> '<'
                Snake.Direction.RIGHT -> '>'
            }
            val snakeBodyChar = 'H'

            // clear game screen
            terminal.clearScreen()
            drawGameFrame()

            // draw fruit:
            terminal.setCursorPosition(game.fruitLocation)
            terminal.putCharacter('%')

            // draw snake:
            val snakeBody = game.snake.body
            val snakeDirection = game.snake.snakeDirection
            val snakeHead = game.snake.headPosition
            for (snakePiece in snakeBody) {
                val isHead = snakePiece == snakeHead
                terminal.setCursorPosition(snakePiece)
                val snakePieceChar = if (isHead) snakeHeadChar(snakeDirection) else snakeBodyChar
                terminal.putCharacter(snakePieceChar)
            }
            terminal.flush()
        }

        override fun drawPaused() {
            displayInfo("Game Paused")
        }
    }
}
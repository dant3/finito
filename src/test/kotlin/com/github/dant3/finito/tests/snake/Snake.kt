package com.github.dant3.finito.tests.snake

import com.github.dant3.finito.tests.head
import com.github.dant3.finito.tests.tail
import org.amshove.kluent.shouldBeGreaterOrEqualTo

class Snake(spawnPoint: Point) {
    var snakeDirection = Direction.UP

    var body: List<Point> = listOf(
            spawnPoint,
            spawnPoint.copy(y = spawnPoint.y+1),
            spawnPoint.copy(y = spawnPoint.y+2)
    )
    private val oldDirections: MutableMap<Point, Direction> = mutableMapOf(body.head to snakeDirection)

    fun move(direction: Direction, grow: Boolean = false) {
        if (direction == snakeDirection.opposite) throw IllegalArgumentException("Can't start moving in opposite direction $direction. Snake direction is $snakeDirection")
        if (direction != snakeDirection) {
            oldDirections[headPosition] = snakeDirection
            snakeDirection = direction
        }

        val oldBody = body
        val newBody = advanceBody(direction, grow)
        newBody.size shouldBeGreaterOrEqualTo oldBody.size

        body = newBody
        if (oldBody.subList(0, body.size - 1).contains(body.head)) {
            throw CollapsedException(body.head)
        }
    }

    fun nextHeadPosition(direction: Direction) = direction.movePoint(headPosition)

    fun isLocatedAt(point: Point) = body.contains(point)

    private fun advanceBody(direction: Direction, grow: Boolean = false): List<Point> {
        val newBody = mutableListOf(direction.movePoint(body.head))
        var lastOldPiece = body.head
        var lastDirection: Direction = direction
        for (piece in body.tail) {
            val turnAtThisPoint: Direction? = oldDirections[lastOldPiece]
            if (turnAtThisPoint != null) {
                lastDirection = turnAtThisPoint
            }
            val newPiecePosition = lastDirection.movePoint(piece)
            newBody.add(newPiecePosition)
            lastOldPiece = piece
        }
        if (grow) {
            newBody.add(lastOldPiece)
        } else {
            oldDirections.remove(lastOldPiece)
        }
        return newBody
    }

    val headPosition: Point
        get() = body.head


    enum class Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT;

        fun movePoint(point: Point): Point = when (this) {
            UP -> point.copy(y = point.y - 1)
            DOWN -> point.copy(y = point.y + 1)
            LEFT -> point.copy(x = point.x - 1)
            RIGHT -> point.copy(x = point.x + 1)
        }

        val opposite: Direction
            get() = when (this) {
                UP -> DOWN
                DOWN -> UP
                LEFT -> RIGHT
                RIGHT -> LEFT
            }

        companion object {
            @JvmStatic fun forState(state: SnakeGame.State) = when (state) {
                SnakeGame.State.LEFT -> LEFT
                SnakeGame.State.RIGHT -> RIGHT
                SnakeGame.State.UP -> UP
                SnakeGame.State.DOWN -> DOWN
                else -> throw IllegalArgumentException("Cant convert state $state to direction")
            }
        }
    }

    class CollapsedException(head: Point) : Exception()
}
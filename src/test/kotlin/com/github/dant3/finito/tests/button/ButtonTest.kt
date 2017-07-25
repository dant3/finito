package com.github.dant3.finito.tests.button

import com.nhaarman.mockito_kotlin.times
import org.amshove.kluent.mock
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldEqual
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.mockito.Mockito.verify

class ButtonTest : Spek({
    describe("button") {
        val button = Button()
        it("should be enabled by default") {
            button.isEnabled shouldBe true
        }
        it("should not be hovered by default") {
            button.isHovered shouldBe false
        }
        it("should be hovered if we hover it") {
            button.isHovered = true
            button.isHovered shouldBe true
        }
        it("should not be hovered once mouse leaves it") {
            button.isHovered = false
            button.isHovered shouldBe false
        }
        it("should be clicked once we click it") {
            button.isPressed = true
            button.isPressed shouldBe true
        }
        it("should call onClick listener once released") {
            val onClickListener = mock<() -> Any>()
            button.onClick(onClickListener)
            button.isHovered = true // required to trigger release
            button.isPressed = false // release the button
            verify(onClickListener, times(1)).invoke()
        }
    }
}) {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            val onClickListener = mock<() -> Any>()
            val button = Button()
            button.onClick(onClickListener)
            button.isHovered = true // required to trigger release
            button.isPressed = true
            button.isPressed = false // release the button
            verify(onClickListener, times(1)).invoke()

            button.isHovered shouldBe true
            button.draw() shouldBe "hovered"
            button.fsm.currentStates shouldEqual setOf(Button.State.Idle, Button.State.Enabled, Button.State.Hovered, Button.State.Released)
        }
    }
}
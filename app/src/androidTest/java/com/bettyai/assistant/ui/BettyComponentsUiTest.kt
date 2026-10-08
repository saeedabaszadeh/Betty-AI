package com.bettyai.assistant.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.bettyai.assistant.core.state.BettyState
import com.bettyai.assistant.ui.components.StatusIndicator
import com.bettyai.assistant.ui.components.TalkButton
import com.bettyai.assistant.ui.theme.BettyTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for Betty's core, stateless composables. They verify that the state
 * machine is reflected in the UI (Persian labels + button affordances) and that
 * the primary interaction invokes its callback only when enabled.
 *
 * This is an instrumentation test; it requires a connected device or emulator.
 */
class BettyComponentsUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun statusIndicator_showsIdleLabel() {
        composeRule.setContent {
            BettyTheme { StatusIndicator(state = BettyState.IDLE) }
        }
        composeRule.onNodeWithText("آماده").assertIsDisplayed()
    }

    @Test
    fun statusIndicator_showsListeningLabel() {
        composeRule.setContent {
            BettyTheme { StatusIndicator(state = BettyState.LISTENING) }
        }
        composeRule.onNodeWithText("در حال شنیدن…").assertIsDisplayed()
    }

    @Test
    fun talkButton_idleShowsStartAffordance_andInvokesCallback() {
        var clicked = false
        composeRule.setContent {
            BettyTheme {
                TalkButton(state = BettyState.IDLE, enabled = true, onClick = { clicked = true })
            }
        }
        composeRule.onNodeWithContentDescription("شروع گفتگو").assertIsDisplayed()
        composeRule.onNode(hasClickAction()).performClick()
        assertTrue(clicked)
    }

    @Test
    fun talkButton_busyShowsStopAffordance() {
        composeRule.setContent {
            BettyTheme {
                TalkButton(state = BettyState.LISTENING, enabled = true, onClick = {})
            }
        }
        composeRule.onNodeWithContentDescription("توقف").assertIsDisplayed()
    }

    @Test
    fun talkButton_disabledDoesNotInvokeCallback() {
        var clicked = false
        composeRule.setContent {
            BettyTheme {
                TalkButton(state = BettyState.IDLE, enabled = false, onClick = { clicked = true })
            }
        }
        composeRule.onNode(hasClickAction()).performClick()
        assertFalse(clicked)
    }
}

package com.bettyai.assistant.ui.components

import androidx.compose.ui.graphics.Color
import com.bettyai.assistant.core.state.BettyState
import com.bettyai.assistant.ui.theme.BettyDisabled
import com.bettyai.assistant.ui.theme.BettyError
import com.bettyai.assistant.ui.theme.BettyIdle
import com.bettyai.assistant.ui.theme.BettyListening
import com.bettyai.assistant.ui.theme.BettyProcessing
import com.bettyai.assistant.ui.theme.BettySpeaking

/** Visual + textual presentation of a [BettyState], shared by every screen. */
data class BettyStateVisual(
    val labelFa: String,
    val color: Color
)

fun BettyState.visual(): BettyStateVisual = when (this) {
    BettyState.IDLE -> BettyStateVisual("آماده", BettyIdle)
    BettyState.LISTENING -> BettyStateVisual("در حال شنیدن…", BettyListening)
    BettyState.PROCESSING -> BettyStateVisual("در حال فکر کردن…", BettyProcessing)
    BettyState.SPEAKING -> BettyStateVisual("در حال صحبت…", BettySpeaking)
    BettyState.ERROR -> BettyStateVisual("خطا", BettyError)
    BettyState.DISABLED -> BettyStateVisual("غیرفعال", BettyDisabled)
}

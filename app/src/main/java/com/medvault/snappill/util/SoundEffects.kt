package com.medvault.snappill.util

import android.media.AudioManager
import android.media.ToneGenerator

object SoundEffects {

    private var toneGen: ToneGenerator? = null

    init {
        try {
            toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 85)
        } catch (ignored: Exception) {}
    }

    fun playSuccessChime() {
        try {
            toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP2, 200)
        } catch (ignored: Exception) {}
    }

    fun playDoubleDoseWarning() {
        try {
            toneGen?.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 450)
        } catch (ignored: Exception) {}
    }
}

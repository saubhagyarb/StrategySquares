package com.saubh.strategysquares.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.annotation.RawRes
import com.saubh.strategysquares.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundEffects @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private var symbolPlaceSound: Int = 0
    private var symbolSelectSound: Int = 0
    private var winSound: Int = 0
    private var drawSound: Int = 0

    init {
        symbolPlaceSound = soundPool.load(context, R.raw.symbol_place, 1)
        symbolSelectSound = soundPool.load(context, R.raw.symbol_select, 1)
        winSound = soundPool.load(context, R.raw.win, 1)
        drawSound = soundPool.load(context, R.raw.draw, 1)
    }

    fun playSymbolPlace() {
        soundPool.play(symbolPlaceSound, 1f, 1f, 1, 0, 1f)
    }

    fun playSymbolSelect() {
        soundPool.play(symbolSelectSound, 0.5f, 0.5f, 1, 0, 1f)
    }

    fun playWin() {
        soundPool.play(winSound, 1f, 1f, 1, 0, 1f)
    }

    fun playDraw() {
        soundPool.play(drawSound, 1f, 1f, 1, 0, 1f)
    }

    fun release() {
        soundPool.release()
    }
}

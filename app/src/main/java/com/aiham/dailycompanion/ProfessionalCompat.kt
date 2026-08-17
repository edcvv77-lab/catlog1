package com.aiham.dailycompanion

import androidx.compose.ui.graphics.Color
import java.time.LocalDate

/** Small typed helpers used by the professional layer without leaking implementation details. */
operator fun Set<String>.plus(date: LocalDate): Set<String> = this + date.toString()

fun Color.luminance(): Float = (0.2126f * red + 0.7152f * green + 0.0722f * blue)

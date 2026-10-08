package com.bettyai.assistant

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point for Betty AI.
 *
 * Annotated with [HiltAndroidApp] so that Hilt can generate the application-level
 * dependency graph. All singletons (engines, managers, repositories) live here.
 */
@HiltAndroidApp
class BettyApplication : Application()

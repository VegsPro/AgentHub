package com.vegspro.agenthub

import android.app.Application

class AgentHubApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: AgentHubApp
            private set
    }
}

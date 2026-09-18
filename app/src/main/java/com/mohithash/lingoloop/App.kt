package com.mohithash.lingoloop

import android.app.Application
import androidx.room.Room
import com.mohithash.lingoloop.ai.AiClient
import com.mohithash.lingoloop.ai.TutorAi
import com.mohithash.lingoloop.data.AppDb
import com.mohithash.lingoloop.data.JsonStore

class App : Application() {
    lateinit var db: AppDb
    lateinit var store: JsonStore
    val client = AiClient()
    val tutor by lazy { TutorAi(client) }
    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(this, AppDb::class.java, "lingoloop.db").build()
        store = JsonStore(this)
    }
}

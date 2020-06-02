package com.infinty8.flashcall.db

import com.infinty8.flashcall.model.Meeting
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Meeting::class], version = 1)
abstract class MeetlyDB : RoomDatabase() {

    abstract fun meetingDao(): MeetingDao

}
package net.primal.data.local.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

internal fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<PrimalDatabase> {
    val appContext = context.applicationContext
    val dbFile = context.getDatabasePath("primal_database.db")
    return Room.databaseBuilder<PrimalDatabase>(
        context = appContext,
        name = dbFile.absolutePath,
    )
}

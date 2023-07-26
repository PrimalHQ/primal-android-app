package net.primal.android.db

import android.content.Context
import androidx.room.Room
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PrimalDatabaseBuilder @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun build(): PrimalDatabase {
        return Room.databaseBuilder(
            context,
            PrimalDatabase::class.java,
            "${context.dataDir.path}/databases/primal.db",
        )
            .enableMultiInstanceInvalidation()
            .fallbackToDestructiveMigration()
            .build()
    }
}


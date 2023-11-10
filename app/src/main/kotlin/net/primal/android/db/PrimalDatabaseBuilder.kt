package net.primal.android.db

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteOpenHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PrimalDatabaseBuilder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val openHelperFactory: SupportSQLiteOpenHelper.Factory,
) {

    init {
        System.loadLibrary("sqlcipher")
        context.deleteDatabase("primal.db")
    }

    fun build(): PrimalDatabase {
        return Room.databaseBuilder(
            context,
            PrimalDatabase::class.java,
            "${context.dataDir.path}/databases/primal_v2.db",
        )
            .openHelperFactory(openHelperFactory)
            .enableMultiInstanceInvalidation()
            .fallbackToDestructiveMigration()
            .build()
    }
}

package net.primal.android.user.db

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteOpenHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class UsersDatabaseBuilder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val openHelperFactory: SupportSQLiteOpenHelper.Factory,
) {

    init {
        System.loadLibrary("sqlcipher")
    }

    fun build(): UsersDatabase {
        return Room.databaseBuilder(
            context,
            UsersDatabase::class.java,
            "${context.dataDir.path}/databases/users_database.db",
        )
            .openHelperFactory(openHelperFactory)
            .enableMultiInstanceInvalidation()
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }
}

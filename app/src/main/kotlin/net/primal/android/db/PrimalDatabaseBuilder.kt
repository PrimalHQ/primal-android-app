package net.primal.android.db

import android.content.Context
import androidx.room.Room
import dagger.hilt.android.qualifiers.ApplicationContext
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Inject

class PrimalDatabaseBuilder @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    init {
        System.loadLibrary("sqlcipher")
    }

    fun build(): PrimalDatabase {
        return Room.databaseBuilder(
            context,
            PrimalDatabase::class.java,
            "${context.dataDir.path}/databases/primal_v2.db",
        )
            .openHelperFactory(SupportOpenHelperFactory("testingPurposesOnlyPassword".toByteArray()))
            .enableMultiInstanceInvalidation()
            .fallbackToDestructiveMigration()
            .build()
    }
}

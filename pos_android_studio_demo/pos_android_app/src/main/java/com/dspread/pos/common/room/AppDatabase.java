package com.dspread.pos.common.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities =
        {TransactionRecord.class},
        version = 3,
        exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract TransactionRecordDao transactionRecordDao();

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            try {
                database.execSQL(
                        "ALTER TABLE transaction_record ADD COLUMN merchant_name TEXT DEFAULT ''"
                );
            } catch (Exception e) {
                // 处理迁移异常
                e.printStackTrace();
            }
        }
    };


    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "pos_transaction.db")
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries()
                            .addMigrations(MIGRATION_2_3)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
package com.example.myapplication.data;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import android.content.Context;

@Database(entities = {Playlist.class, Song.class}, version = 4)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract PlaylistDao playlistDao();
    public abstract SongDao songDao();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "app_database")
                            .addMigrations(MIGRATION_1_2, MIGRATION_3_4)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    static final Migration MIGRATION_1_2 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Migration code for version 2 to 3
            database.execSQL("CREATE TABLE playlists_new (" +
                    "id INTEGER PRIMARY KEY NOT NULL, " +
                    "name TEXT, " +
                    "creation_date TEXT DEFAULT '1970-01-01 00:00:00', " +
                    "size INTEGER NOT NULL)");

            database.execSQL("INSERT INTO playlists_new (id, name, creation_date, size) " +
                    "SELECT id, name, " +
                    "CASE WHEN creation_date IS NULL THEN '1970-01-01 00:00:00' ELSE creation_date END, " +
                    "size FROM playlists");

            database.execSQL("DROP TABLE playlists");

            database.execSQL("ALTER TABLE playlists_new RENAME TO playlists");
        }
    };

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Adding new columns with default values
            database.execSQL("ALTER TABLE songs ADD COLUMN size INTEGER NOT NULL DEFAULT 'undefined'");
            database.execSQL("ALTER TABLE songs ADD COLUMN date TEXT DEFAULT 'undefined'");
        }
    };


}



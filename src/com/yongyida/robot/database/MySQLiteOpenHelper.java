package com.yongyida.robot.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2016/7/9 0009.
 */
public class MySQLiteOpenHelper extends SQLiteOpenHelper {
    private static final String end = "db"; //数据库名称

    private static final int version = 1;

    private String name;

    public MySQLiteOpenHelper(Context context, String name) {
        super(context, end, null, version);
        this.name = name;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db);
    }

    private void createTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS "
                + name
                + " ("
                + SavingMessage.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + SavingMessage.FROM + " TEXT, "
                + SavingMessage.TO + " TEXT, "
                + SavingMessage.TIME + " TEXT, "
                + SavingMessage.CONTENT + " TEXT "
                + ")";
        db.execSQL(sql);
    }

    public class SavingMessage {
        public static final String ID = "id";
        public static final String FROM = "from";
        public static final String TO = "to";
        public static final String TIME = "time";
        public static final String CONTENT = "content";
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

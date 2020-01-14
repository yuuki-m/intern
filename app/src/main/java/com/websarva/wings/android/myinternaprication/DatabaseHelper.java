package com.websarva.wings.android.myinternaprication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "save_cite.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //記事の保管場所
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE save_cite (");
        sb.append("_id INTEGER PRIMARY KEY,");
        sb.append("title TEXT,");
        sb.append("url TEXT,");
        sb.append("photo BLOB");//保存はできるが取得の時にエラーが発生する
        sb.append(");");
        String sql = sb.toString();

        db.execSQL(sql);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS save_cite");
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion){
        onUpgrade(db, oldVersion, newVersion);
    }
}

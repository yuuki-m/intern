package com.websarva.wings.android.myinternaprication;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.ArrayMap;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.sql.Blob;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SaveActivity extends AppCompatActivity {

    private ListView _saveArticle;
    private List<Map<String, Object>> _saveList;
    private static final String[] FROM = {"title","URL","img"};
    private static final int[] TO = {R.id.tvSaveTitle,R.id.tvSaveURL,R.id.imSaveImg};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.save_activity);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        DatabaseHelper helper = new DatabaseHelper(SaveActivity.this);
        SQLiteDatabase db = helper.getReadableDatabase();

        List<Map<String, Object>> lvSave = new ArrayList<>();
        //一つの記事しか表示できない
        try{
            String sql = "SELECT * FROM save_cite WHERE _id = 1";
            Cursor cursor = db.rawQuery(sql,null);
            String title = "";
            String url = "";
            byte[] img;
            Bitmap btp;

            while(cursor.moveToNext()){
                int idxTitle = cursor.getColumnIndex("title");
                int idxUrl = cursor.getColumnIndex("url");
                int idxImg = cursor.getColumnIndex("photo");
                Map<String, Object> save = new HashMap<>();

                title = cursor.getString(idxTitle);
                Log.i("Save",title);
                url = cursor.getString(idxUrl);
                Log.i("Save",url);
                save.put("title",title);
                save.put("URL",url);
                img = cursor.getBlob(idxImg);
                Log.i("Save","img");
                if(img != null){
                    btp = BitmapFactory.decodeByteArray(img,0,img.length);
                    save.put("img",btp);
                }

                lvSave.add(save);
            }

        }finally {
            db.close();
        }
        _saveArticle = findViewById(R.id.lvsave);
        _saveList = lvSave;
        SimpleAdapter adapter = new SimpleAdapter(SaveActivity.this, _saveList,
                R.layout.save_lv_layout, FROM, TO);
        adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if(view instanceof ImageView){
                    if(data != null) {
                        ((ImageView) view).setImageBitmap((Bitmap) data);
                        return true;
                    }else{
                        return true;
                    }
                }
                return false;
            }
        });
        _saveArticle.setAdapter(adapter);
        registerForContextMenu(_saveArticle);
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo
            menuInfo){
        super.onCreateContextMenu(menu, view, menuInfo);
        menu.add(0,0,0,R.string.con_web);
        menu.add(0,1,0,R.string.con_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.
                getMenuInfo();
        int listPosition = info.position;
        int itemId = item.getItemId();
        Map<String, Object> menu = _saveList.get(listPosition);

        switch (itemId){
            case 0:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse((String) menu.get("URL")));
                startActivity(intent);
                break;
            case 1:
                DatabaseHelper helper = new DatabaseHelper(SaveActivity.this);
                SQLiteDatabase db = helper.getWritableDatabase();
                try{

                    String sqlDelete = "DELETE FROM save_cite WHERE _id =?";

                    SQLiteStatement stmtDel = db.compileStatement(sqlDelete);

                    stmtDel.bindLong(1,1);
                    stmtDel.executeUpdateDelete();
                }finally {
                    db.close();
                }
                setContentView (R.layout.save_activity);
                break;
        }
        return super.onContextItemSelected(item);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int itemId = item.getItemId();
        if(itemId == android.R.id.home){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}

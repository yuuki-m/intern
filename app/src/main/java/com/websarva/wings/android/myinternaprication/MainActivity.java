package com.websarva.wings.android.myinternaprication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ListView _lvArticle;
    private List<Map<String, Object>> _articleList;
    private EditText editText;
    private InputMethodManager inputMethodManager;
    private static final String[] FROM = {"title","URL","img"};
    private static final int[] TO = {R.id.tvArticleTitle,R.id.tvArticleURL,R.id.imArticleImg};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("MyInternAprication","Main onCreate() called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btClick = findViewById(R.id.button);
        ListAdd add = new ListAdd();
        btClick.setOnClickListener(add);

        editText = (EditText) findViewById(R.id.editText);
        inputMethodManager =  (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        editText.setOnKeyListener(new View.OnKeyListener() {

            //コールバックとしてonKey()メソッドを定義
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //イベントを取得するタイミングには、ボタンが押されてなおかつエンターキーだったときを指定
                if((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
                    //キーボードを閉じる
                    inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(),
                            InputMethodManager.RESULT_UNCHANGED_SHOWN);

                    return true;
                }

                return false;
            }
        });

    }

    //optionmenuの追加
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //optionmenuの動作
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int itemId = item.getItemId();

        switch(itemId){
            case R.id.optionSavePage:
                Intent intent = new Intent(MainActivity.this,SaveActivity.class);
                startActivity(intent);
                break;
            case R.id.optionFinish:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //contextmenuの追加
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo
     menuInfo){
        super.onCreateContextMenu(menu, view, menuInfo);
        menu.add(0,0,0,R.string.con_save);
        menu.add(0,1,0,R.string.con_web);
        menu.add(0, 2, 0, R.string.con_share);
    }


    @Override
    public boolean onContextItemSelected(MenuItem item){

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.
                getMenuInfo();

        int listPosition = info.position;
        Map<String, Object> menu = _articleList.get(listPosition);

        int itemId = item.getItemId();

        switch (itemId){
            case 0:
                String title = menu.get("title").toString();
                String url = menu.get("URL").toString();
                Bitmap btmImg = (Bitmap) menu.get("img");

                DatabaseHelper helper = new DatabaseHelper(MainActivity.this);

                SQLiteDatabase db = helper.getWritableDatabase();
                //一つの記事しか保存できない
                try{

                    String sqlDelete = "DELETE FROM save_cite WHERE _id =?";

                    SQLiteStatement stmtDel = db.compileStatement(sqlDelete);

                    stmtDel.bindLong(1,1);
                    stmtDel.executeUpdateDelete();
                    //普通にSQLiteを使って書いたほうがいいかも
                    String sqlInsert = "INSERT INTO save_cite (_id,title, url, photo) " +
                            "VALUES (?,?, ?, ?)";
                    SQLiteStatement stmt = db.compileStatement(sqlInsert);
                    stmt.bindLong(1,1);
                    stmt.bindString(2,title);
                    Log.i("Main",title);
                    stmt.bindString(3,url);
                    Log.i("Main",url);
                    if(btmImg != null) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        btmImg.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] jpgarr = baos.toByteArray();
                        stmt.bindBlob(4,jpgarr);
                        Log.i("Main","img");

                    }

                    stmt.executeInsert();

                }
                finally {
                    db.close();
                }

                break;
            case 1:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse((String) menu.get("URL")));
                startActivity(intent);
                break;
            case 2:
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/html");
                shareIntent.putExtra(Intent.EXTRA_TEXT, menu.get("URL").toString());
                startActivity(shareIntent);
                break;
        }

        return super.onContextItemSelected(item);
    }

    private class ListItemClickListener implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id){
            Map<String, Object> menu = _articleList.get(position);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse((String) menu.get("URL")));
            startActivity(intent);
        }
    }

    public class ListAdd implements View.OnClickListener{
        ProgressDialog progressDialog;
        @Override
        public void onClick(View view){
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setTitle("検索中");
            progressDialog.setMessage("お待ちください");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
            ArticleSearch articlesearch = new ArticleSearch(progressDialog);
            EditText input = findViewById(R.id.editText);
            inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(),
                    InputMethodManager.RESULT_UNCHANGED_SHOWN);

            articlesearch.setOnCallBack(new ArticleSearch.CallBackTask(){

                @Override
                public void CallBack(List<Map<String, Object>> result){
                    super.CallBack(result);
                    _lvArticle = findViewById(R.id.lvArticle);
                    _articleList = result;
                    SimpleAdapter adapter = new SimpleAdapter(MainActivity.this, _articleList,
                    R.layout.lv_layout, FROM, TO);
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
                _lvArticle.setAdapter(adapter);
                _lvArticle.setOnItemClickListener(new ListItemClickListener());
                registerForContextMenu(_lvArticle);
                }


            });

            articlesearch.execute(input.getText().toString());

        }
    }
}

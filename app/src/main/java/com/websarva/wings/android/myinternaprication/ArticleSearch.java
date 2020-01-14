package com.websarva.wings.android.myinternaprication;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArticleSearch extends AsyncTask<String,String, List<Map<String, Object>>> {
    private CallBackTask callbacktask;
    private ProgressDialog progressDialog;

     public ArticleSearch(ProgressDialog progressDialog) {
        super();
        this.progressDialog  = progressDialog;
    }
    @Override
    public List<Map<String, Object>> doInBackground(String... params){
        //このarticle変数の場所に注目してみてください loop文の中に入れる必要があった
        Log.i("ArticleSearch","info1");
        List<Map<String, Object>> articleList = new ArrayList<>();
        String  seek =  params[0];
        List<Map<String, Object>> result = new ArrayList<>();

        try{
            Log.i("ArticleSearch","info2");
            Connection conn = Jsoup.connect("https://www.google.com/search?");
            Document doc = conn.data("q",seek).get();
            Elements elements = doc.getElementsByClass("r");
            Elements links = elements.select("a");
            //Thread.sleep(1000);
            for(Element link : links){
                Map<String, Object> article = new HashMap<>();
                String cite = link.attr("href");
                if(cite.equals("#"))continue;
                String title = link.select("h3").text();
                if(title.equals(""))continue;
                Log.i("ArticleSearch",title);
                article.put("URL", cite);
                article.put("title",title);
                //Thread.sleep(1000);
                try{
                    Document imgSite = Jsoup.connect(cite).get();
                    Element imgElement = imgSite.select("img").get(0);
                    String imgStr = imgElement.attr("abs:src");
                    Log.i("ArticleSearch",imgStr);
                    try {
                        Log.i("ArticleSearch","info3");
                        URL url = new URL(imgStr);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setDoInput(true);
                        connection.connect();
                        InputStream input = connection.getInputStream();
                        Bitmap myBitmap = BitmapFactory.decodeStream(input);
                        article.put("img", myBitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                        continue;
                    }

                    articleList.add(article);
                    result = articleList;
                }catch(MalformedURLException e){
                    e.printStackTrace();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public void onPostExecute(List<Map<String, Object>> result){
        progressDialog.dismiss();
        callbacktask.CallBack(result);
    }

    public void setOnCallBack(CallBackTask _cbj){
        callbacktask = _cbj;
    }

    public static class CallBackTask{
        public void CallBack(List<Map<String, Object>> result) {

        }
    }
}
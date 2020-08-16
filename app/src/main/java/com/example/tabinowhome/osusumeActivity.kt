package com.example.tabinowhome

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.activity_osusume.*
import kotlinx.android.synthetic.main.list_textview_tap.view.*
import org.json.JSONArray
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.net.URLEncoder

class osusumeActivity : AppCompatActivity() {
    var data1: String = ""
    var photo_map=ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_osusume)

        data1 = intent.getStringExtra("selected")
        //textView3.text = data1
        //val pic = intent.getStringExtra("pic")//picはurl
        val osusumespot=intent.getStringArrayListExtra("places")//20こくらい
        //val images=intent.getStringArrayExtra("images")
        //val photo_map = intent.getStringArrayExtra("photo_map")//URL

       val osusumelist = findViewById<ListView>(R.id.osusumelist)
        //setContentView(listView)
        //val adapter:ArrayAdapter<String> = ArrayAdapter(applicationContext, android.R.layout.simple_list_item_1, osusumespot)
        osusumelist.adapter=ArrayAdapter(applicationContext, android.R.layout.simple_list_item_1, osusumespot)//adapter
        //TODO list表示する


        //imageを取得
        //val image = imageView2.findViewById(R.id.imageView2) as ImageView
        //画像取得スレッド起動
       // val task = ImageGetTask(image)
       // task.execute(pic)

        //val text = findViewById<TextView>(R.id.textView5)
        //val wikitask = WikiTask(text)
       // wikitask.execute(data1)

        //button7.setOnClickListener{onMap(data1)}

    }

    fun onMap(place:String) {
        val searchword = URLEncoder.encode(place,"UTF-8")
        val urlstr="geo:0,0?q=${searchword}"
        val url = Uri.parse(urlstr)
        val intent = Intent(Intent.ACTION_VIEW, url)
        startActivity(intent)
    }

    private inner class ListItemClickListener: AdapterView.OnItemClickListener{
        override fun onItemClick(parent: AdapterView<*>, view: View, position:Int, id:Long){
            val link1id =view.findViewById<TextView>(R.id.link1text)
            val link2id =view.findViewById<TextView>(R.id.link2text)
            link1id.setOnClickListener{
                val url =photo_map[position]
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }
            link2id.setOnClickListener{
                val url =photo_map[position]
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }
        }
    }
}

internal class ImageGetTask(private val image: ImageView) : AsyncTask<String, Void, Bitmap>() {   //unknownsourceでた
    override fun doInBackground(vararg params: String): Bitmap? {
        val image: Bitmap
        try {
            val imageUrl = URL(params[0])
            val imageIs: InputStream
            imageIs = imageUrl.openStream()
            image = BitmapFactory.decodeStream(imageIs)
            return image
        } catch (e: MalformedURLException) {
            return null
        } catch (e: IOException) {
            return null
        }
    }

    override fun onPostExecute(result: Bitmap) {
        // 取得した画像をImageViewに設定します。
        image.setImageBitmap(result)
    }
}

private class WikiTask (private val text: TextView): AsyncTask<String, String, String>() {
    override fun doInBackground(vararg params: String): String {

        var Wikitext: String = ""
        val word = "鈴鹿サーキット"//params[0]
        //接続URL文字列を作成。
        val urlStr =
            "http://wikipedia.simpleapi.net/api?keyword=${word}&output=json"
        //URLオブジェクトを生成。
        val url = URL(urlStr)
        //URLオブジェクトからHttpURLConnectionオブジェクトを取得。
        val con = url.openConnection() as HttpURLConnection
        //http接続メソッドを設定。
        con.requestMethod = "GET"

        //以下タイムアウトを設定する場合のコード。
        con.connectTimeout = 1000
        con.readTimeout = 1000
        //接続。
        try {
            con.connect()
            //HttpURLConnectionオブジェクトからレスポンスデータを取得。天気情報が格納されている。
            val stream = con.inputStream
            //レスポンスデータであるInputStreamオブジェクトを文字列(JSON文字列)に変換。
            val result = is2String(stream)
            //HttpURLConnectionオブジェクトを解放。
            con.disconnect()
            //InputStreamオブジェクトを解放。
            stream.close()

            Wikitext = json2Wiki(result)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            con.disconnect()
        }
        return Wikitext
    }

    override fun onPostExecute(result: String) {
        text.setText(result)
    }

    private fun json2Wiki(json: String): String {
        //JSON文字列からJSONObjectオブジェクトを生成。これをルートJSONオブジェクトとする。
        val rootJSON = JSONArray(json)
        val query = rootJSON.getJSONObject(0)
        val url = query.getString("url")
        //var name = feature.getString("Name")
        Log.d("tag",url)

        return url
    }

    private fun is2String(stream: InputStream): String {
        val sb = StringBuilder()
        val reader = BufferedReader(InputStreamReader(stream, "UTF-8"))
        var line = reader.readLine()
        while (line != null) {
            sb.append(line)
            line = reader.readLine()
        }
        reader.close()
        return sb.toString()
    }
}
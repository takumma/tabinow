package com.example.tabinowhome

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import com.example.tflitesample.ImageClassifier
import kotlinx.android.synthetic.main.activity_photo.*
import kotlinx.android.synthetic.main.activity_tenki_hyouji.*
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class PhotoActivity : AppCompatActivity() {

    val weatherlist = listOf("全てOK", "晴れ", "雨", "くもり")
    val wikilink = ArrayList<String>()
    val piclink = ArrayList<String>()
    var lon: String = ""
    var lat: String = ""
    var photos = ArrayList<String?>()
    var spotNames = ArrayList<String>()
    var name_map = ArrayList<String>()

    private lateinit var classifier: ImageClassifier

    var image01 = ""
    var photo_map = ArrayList<String>()
    var spotPic = ""
    var weather: String = ""
    var hiradata = ""
    var wiki_map = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)

        progressBar2.setVisibility(ProgressBar.INVISIBLE)

        classifier = ImageClassifier(assets)

        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, weatherlist)
        // ドロップダウンのレイアウトを指定
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // ListViewにAdapterを関連付ける
        val spinner = findViewById<Spinner>(R.id.spinner2)
        spinner.adapter = adapter

        button8.setOnClickListener { jugde() }

    }

    fun jugde() {
        val editdata = editText.text.toString()
        weather = spinner2.selectedItem as String
        if (editdata == "") {
            Toast.makeText(applicationContext, "目的地が入力されていません", Toast.LENGTH_LONG).show()
        } else {
            progressBar2.setVisibility(ProgressBar.VISIBLE)
            searchspot(editdata,weather)
        }
    }

    fun searchspot(spotdata: String, weather: String) {

        //TODO:ここにplaceAPIの処理を入れる
        val hira = Hiragana()
        hira.execute(spotdata)
//        val receiver = LongLatiInfoReceiver(hiradata)
//        receiver.execute("")
//        val place = PlaceAPI(weather)
//        place.execute("")
//        val google = GoogleSearchReceiver(weather)
//        val dummy = "dummy"
//        name_map.clear()
//        google.execute(dummy)

//        onTugiheButtonTapped(name_map)
    }

    fun onTugiheButtonTapped(data: ArrayList<String>) {
        val intent = Intent(this, KensakuHyoujiActivity::class.java)
        //TODO そのスポットの写真と名前を渡す
        intent.putExtra("place",editText.text.toString())
        intent.putExtra("pic",spotPic)
        intent.putExtra("placelist", data)//こっからしたが周辺のスポットのやつ
        intent.putExtra("wikilink", wikilink)
        intent.putExtra("piclink", photo_map)
        progressBar2.setVisibility(ProgressBar.INVISIBLE)
        startActivity(intent)
    }

    private inner class GoogleSearchReceiver(w: String) : AsyncTask<String, String, String>() {
        var weather = w
        var placeName = String()
        var placeI = 0

        override fun doInBackground(vararg params: String): String {
            photo_map.clear()

            val key = BuildConfig.GOOGLE_API_KEY
            val cx = BuildConfig.GOOGLECUSTOMSEARCH_API_CX
            while (name_map.size < 4) { // なぜか５だと怒られる...
                val name = spotNames[placeI]
                placeName = spotNames[placeI]
                var imageurlarray = ArrayList<String>()
                val urlStr =
                    "https://www.googleapis.com/customsearch/v1?key=${key}&cx=${cx}&q=${name}&searchType=image&num=6"

                Log.d("TAG", urlStr)
                //URLオブジェクトを生成。
                val url = URL(urlStr)
                //URLオブジェクトからHttpURLConnectionオブジェクトを取得。
                val con = url.openConnection() as HttpURLConnection
                //http接続メソッドを設定。
                con.requestMethod = "GET"
                //以下タイムアウトを設定する場合のコード。
                //con.connectTimeout = 1000
                //con.readTimeout = 1000

                try {
                    con.connect()

                    //HttpURLConnectionオブジェクトからレスポンスデータを取得。天気情報が格納されている。
                    val stream = con.inputStream
                    //レスポンスデータであるInputStreamオブジェクトを文字列(JSON文字列)に変換。
                    val result = is2String2(stream)
                    //HttpURLConnectionオブジェクトを解放。
                    con.disconnect()
                    //InputStreamオブジェクトを解放。
                    stream.close()
                    Log.d("TAG", result)

                    val sample: ArrayList<String> =
                        json2Google(result)  //url:arrylist<string>が渡される(6つのurl)
                    for (i in 0..5) {
                        imageurlarray.add(sample[i])//一つずつ追加してい

                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    con.disconnect()
                }

                var bitmapimage = ArrayList<Bitmap>()
                //var num = 0
                var m = 0
                var imageUrl: URL
                var imageIs: InputStream
                var image: Bitmap
                var photoURL: String

                for (i in 0..5) {
                    try {
                        imageUrl = URL(imageurlarray[i])
                        Log.d("url", imageurlarray[i])
                        imageIs = imageUrl.openStream()
                        image = BitmapFactory.decodeStream(imageIs)
                        bitmapimage.add(image)
                        // picture=image
                    } catch (e: MalformedURLException) {
                        return ""//null
                    } catch (e: IOException) {
                        return ""//null
                    }

                    //追加したやつ
                    val res = classifier.recognizeImage(image)
                    when (res) {
                        "outdoor" -> m++
                        "outside" -> m--
                        "inside" -> m--
                    }
                    //おすすめのやつ
                    //ok->name_map.add(name)num++
                    //no->nune
                    Log.d("res", res)
                    Log.d("osusume", m.toString())
                }

                if (photos[placeI] == null) {
                    photoURL = "https://upload.wikimedia.org/wikipedia/ja/b/b5/Noimage_image.png"
                } else {
                    photoURL =
                        "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=${photos[placeI]}&key=${key}"//placephotosURL
                }

                weather = "rainy"
                when (weather) {
                    "sunny" -> {
                        name_map.add(name)
                        photo_map.add(photoURL)
                        //images.add(image01)
                    }
                    "cloudy" -> {
                        name_map.add(name)
                        photo_map.add(photoURL)
                    }
                    "rainy" -> if (m < 0) {
                        name_map.add(name)
                        photo_map.add(photoURL)
                    }
                }
                placeI++
            }
            return ""
        }

        override fun onPostExecute(result: String) {
            //name_map.add(placeName)
            //num++
            Log.d("num", name_map.size.toString())
            val wiki = WikiTask()
            wiki.execute("")
            //next(spotText,spotPic,name_map,images,photo_map,wiki_map)

        }

        fun next(
            i: String,
            p: Bitmap,
            namemap: ArrayList<String>,
            images: ArrayList<String>,
            photo_map: ArrayList<String>,
            wiki_map: ArrayList<String>
        ) {
            Log.d("TAG", "next()")
            val intent = Intent(applicationContext, osusumeActivity::class.java)
            intent.putExtra("selected", i)
            intent.putExtra("pic", p)
            intent.putExtra("places", namemap)
            intent.putExtra("images", images)
            intent.putExtra("photo_map", photo_map)
            intent.putExtra("wiki_map", wiki_map)
            progressBar3.setVisibility(ProgressBar.INVISIBLE)
            startActivity(intent)
        }

    }


    private fun json2Google(json: String): ArrayList<String> {
        //JSON文字列からJSONObjectオブジェクトを生成。これをルートJSONオブジェクトとする。
        val rootJSON = JSONObject(json)
        val items = rootJSON.getJSONArray("items")
        var item: JSONObject
        var imageJSON: JSONObject
        var imageLink: String

        val imageurlArray = ArrayList<String>()
        for (i in 0..5) {
            item = items.getJSONObject(i)
            imageJSON = item.getJSONObject("image")
            imageLink = imageJSON.getString("thumbnailLink")
            imageurlArray.add(imageLink)
        }
        return imageurlArray
    }

    private fun is2String2(stream: InputStream): String {
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


    private inner class WikiTask : AsyncTask<String, String, String>() {
        override fun doInBackground(vararg params: String): String {

            var Wikitext: String = ""
            for (i in 0 until name_map.size) {
                val word = name_map[i]//params[0]
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
                    wiki_map.add(Wikitext)
                } catch (e: Exception) {
                    Wikitext = ""
                    wiki_map.add(Wikitext)
                    e.printStackTrace()
                } finally {
                    con.disconnect()
                }
            }

            return ""
        }

        override fun onPostExecute(result: String) {
            Log.d("spotText", editText.text.toString())
            Log.d("spotPic", spotPic)
            for (i in 0 until name_map.size) {
                Log.d("name_map", name_map[i])
            }
            for (i in 0 until photo_map.size) {
                Log.d("photo_map", photo_map[i])
            }
            for (i in 0 until wiki_map.size) {
                Log.d("wiki_map", wiki_map[i])
            }
            onTugiheButtonTapped(name_map)
        }

        private fun json2Wiki(json: String?): String {
            //JSON文字列からJSONObjectオブジェクトを生成。これをルートJSONオブジェクトとする。
            val rootJSON = JSONArray(json)
            var url: String = ""
            try {
                val query = rootJSON.getJSONObject(0)
                url = query.getString("url")
                //var name = feature.getString("Name")
                Log.d("tag", url)
                return url
            } catch (e: Exception) {
                return ""
            }
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


        private inner class LongLatiInfoReceiver : AsyncTask<String, String, String>() {
            override fun doInBackground(vararg params: String): String {
                    val appid = BuildConfig.YAHOOSEARCH_APP_ID
                    //接続URL文字列を作成。
                    val urlStr =
                        "https://map.yahooapis.jp/search/local/V1/localSearch?appid=${appid}&results=1&detail=simple&output=json&query=${hiradata}"

                    //URLオブジェクトを生成。
                    val url = URL(urlStr)
                    //URLオブジェクトからHttpURLConnectionオブジェクトを取得。
                    val con = url.openConnection() as HttpURLConnection
                    //http接続メソッドを設定。
                    con.requestMethod = "GET"

                    //以下タイムアウトを設定する場合のコード。
                    //con.connectTimeout = 1000
                    //con.readTimeout = 1000

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


                        val longlati = json2LongLati(result)
                        lon=longlati.first
                        lat=longlati.second
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        con.disconnect()
                    }
                return ""
            }

            override fun onPostExecute(result: String) {
                val test = PlaceAPI(lon, lat, weather)
                test.execute("")
            }

            private fun json2LongLati(json: String): Pair<String, String> {
                //JSON文字列からJSONObjectオブジェクトを生成。これをルートJSONオブジェクトとする。
                val rootJSON = JSONObject(json)
                val featureArray = rootJSON.getJSONArray("Feature")
                val feature = featureArray.getJSONObject(0)
                //var name = feature.getString("Name")
                var geometryObj = feature.getJSONObject("Geometry")
                var coordinates = geometryObj.getString("Coordinates")

                var lonlat = coordinates.split(",")

                return Pair(lonlat[0], lonlat[1])
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

            private inner class Hiragana() : AsyncTask<String, String, String>() {
                override fun doInBackground(vararg params: String): String {
                    var result = ""
                    var MIMEType: MediaType?
                    var body: RequestBody?
                    var request: Request
                    var call: Call
                        val client = OkHttpClient()
                        val url = "https://labs.goo.ne.jp/api/hiragana"
                        val Jsontext = """{
"app_id":
"${BuildConfig.GOOHIRAGANA_APP_ID}",
"sentence": "${params[0]}",
"output_type": "hiragana"}
""".trimIndent()
                        MIMEType = MediaType.parse("application/json; charset=utf-8")
                        body = RequestBody.create(MIMEType, Jsontext)
                        request = Request.Builder().url(url).post(body).build()

                        call = client.newCall(request)
                        try {
                            var response = call.execute()
                            var body = response.body()
                            if (body != null) {
                                result = body.string()
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                        Log.d("ひらがなJSON", result)
                        var rootJSON = JSONObject(result)
                        var hira = rootJSON.getString("converted")
                        hiradata = hira
                        Log.d("ひらがな結果", hira)
                    //可変長引数の1個目(インデックス0)を取得。これが都市ID

                    return result
                }

                override fun onPostExecute(result: String) {
                    val receiver = LongLatiInfoReceiver()
                    receiver.execute("")
                }
            }

    private inner class PlaceAPI(lon:String,lat:String,w:String): AsyncTask<String, ArrayList<String>, String>() {
        val weather=w
        var lon1=lon
        var lat1=lat
        override fun doInBackground(vararg params: String): String {

            val key = BuildConfig.GOOGLE_API_KEY
            val urlStr = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?parameters&key=${key}&location=${lat1},${lon1}&radius=2000&language=ja"
            //            https://maps.googleapis.com/maps/api/place/nearbysearch/json?parameters&key=${key}&location=34.8499629,136.5411905&radius=3000
            Log.d("TAG", urlStr)
            val url = URL(urlStr)
            val con = url.openConnection() as HttpURLConnection
            con.requestMethod = "GET"
            // con.connectTimeout = 1000
            // con.readTimeout = 1000
            con.connect()
            val stream = con.inputStream
            val result = is2String(stream)
            con.disconnect()
            stream.close()

            Log.d("TAG", result)

            //JSON文字列を返す。
            return result
        }
        /*
                override fun onPostExecute(result: String) {

                    val rootJSON = JSONObject(result)
                    val items = rootJSON.getJSONArray("results")
                    var item :JSONObject
                    var types: JSONArray
                    var type: String
                    var name: String


                    for (i in 0..19) {
                        item = items.getJSONObject(i)
                        types = item.getJSONArray("types")
                        type = types.getString(0)
                        if (type != "locality" && name_map.size<6) {
                            name = item.getString("name")
                            Log.d("spot",name)
                            val Google = GoogleSearchReceiver(weather)
                            Google.execute(name)
                            // googleのpostexecuteにいどう
                            //name_map.add(name)
                            //num++
                        }
                        if(name_map.size >= 5){
                            break
                        }
                    }//TODO:okが5個なかった時の処理
                }
        */
        override fun onPostExecute(result: String) {
            val rootJSON = JSONObject(result)
            val items = rootJSON.getJSONArray("results")
            var item :JSONObject
            var types: JSONArray
            var type: String
            var name: String=""
            var photoArray:JSONArray
            var photoNow:JSONObject
            var photoString:String = ""

            spotNames.clear()
            for (i in 0 until items.length()){
                item = items.getJSONObject(i)
                types = item.getJSONArray("types")
                type = types.getString(0)

                if (type != "locality") {
                    if (item.isNull("photos")) {
                        photoString = ""
                        photos.add(null)
                    }else{
                        photoArray = item.getJSONArray("photos")
                        photoNow = photoArray.getJSONObject(0)
                        photoString = photoNow.getString("photo_reference")
                        photos.add(photoString)
                    }
                    name = item.getString("name")

                    spotNames.add(name)
                    //TODO"photo_reference"1を一度とってきて(addして)、のちに五個取り出す処理で"photo_reference"もaddする。
                }
                if (name == editText.text.toString()) {
                    if(photoString=="") {
                        spotPic = "https://upload.wikimedia.org/wikipedia/ja/b/b5/Noimage_image.png"
                    }else{
                        spotPic = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=${photoString}&key=${BuildConfig.GOOGLE_API_KEY}"//placephotosURL
                    }
                }
            }
            val google = GoogleSearchReceiver(weather)
            val dummy = "dummy"
            name_map.clear()
            google.execute(dummy)
        }

        private fun is2String(stream: InputStream): String {
            val sb = StringBuilder()
            val reader = BufferedReader(InputStreamReader(stream, "UTF-8"))
            var line = reader.readLine()
            while(line != null) {
                sb.append(line)
                line = reader.readLine()
            }
            reader.close()
            return sb.toString()
        }

    }
}

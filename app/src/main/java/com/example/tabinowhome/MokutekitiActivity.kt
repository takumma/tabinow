package com.example.tabinowhome

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import com.example.tflitesample.ImageClassifier
import kotlinx.android.synthetic.main.activity_mokutekiti.*
import okhttp3.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL



class MokutekitiActivity : AppCompatActivity() {
    var places = ArrayList<String>()            //editTextからとったやつを入れるマップ
    var hira_map = ArrayList<String>()
    var lon_map = ArrayList<String>()     //経度と緯度
    var lat_map = ArrayList<String>()
    var weather_map = ArrayList<String>()            //icon
    var temp_map = ArrayList<String>()   //気温


    //画像
    var image01 = ArrayList<String>()//次画面にわたすやつ
    var sent = ArrayList<String>()

    //機械学習
    private lateinit var classifier: ImageClassifier
    var result = ArrayList<String>()  //追加したやつ





    var data1 = ""   //placesにつっ込む兼次のビューに渡す役割
    var data2 = ""
    var data3 = ""
    var data4 = ""
    var data5 = ""
    var data6 = ""
    var hour: Int = 6  //時間をいじってる
    var day = ""   //先のビューから日付受け取って、JSON叩くときに使う




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mokutekiti)

        val text = intent.getStringExtra("date")
        textView9.text = text
        day = text
        classifier = ImageClassifier(assets)        //追加したやつ

        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        progressBar.setVisibility(ProgressBar.INVISIBLE)


        button10.setOnClickListener { judge() }   //judge()はデータが入っているかを確認する関数、データがなければトーストを表示して、APIの処理ができないようにしている
    }


    fun judge() {


        val editdata1 = editText2.text.toString()
        val editdata2 = editText5.text.toString()
        val editdata3 = editText6.text.toString()
        val editdata4 = editText7.text.toString()
        val editdata5 = editText8.text.toString()
        val editdata6 = editText9.text.toString()

        data1 = ""   //placesにつっ込む兼次のビューに渡す役割
        data2 = ""
        data3 = ""
        data4 = ""
        data5 = ""
        data6 = ""


        if (editdata1 != "") {
            data1 = editdata1
        }

        data2 = if (editdata2 != "") {
            editdata2
        } else {
            data1
        }

        data3 = if (editdata3 != "") {
            editdata3
        } else {
            data2
        }

        data4 = if (editdata4 != "") {
            editdata4
        } else {
            data3
        }

        data5 = if (editdata5 != "") {
            editdata5
        } else {
            data4
        }

        data6 = if (editdata6 != "") {
            editdata6
        } else {
            data5
        }

        if (data5 == "") {
            data5 = data6
        }
        if (data4 == "") {
            data4 = data5
        }
        if (data3 == "") {
            data3 = data4
        }
        if (data2 == "") {
            data2 = data3
        }
        if (data1 == "") {
            data1 = data2
        }  //ここまでの処理で一つでも値が入っていれば全部の変数に入るようになっている

        if (data1 == "") {
            Toast.makeText(applicationContext, "目的地が入力されていません", Toast.LENGTH_LONG).show()
        } else {
            progressBar.setVisibility(ProgressBar.VISIBLE)
            places.clear()
            image01.clear()
            temp_map.clear()
            weather_map.clear()
            result.clear()
            lon_map.clear()
            lat_map.clear()
            hira_map.clear()
            hour=6
            adddata()
        }

    }

    fun adddata() {


        places.add(data1)
        places.add(data2)
        places.add(data3)
        places.add(data4)
        places.add(data5)
        places.add(data6)


        //Google画像検索
        val Googlereceiver = GoogleSearchReceiver()
        Googlereceiver.execute("")

        val hiraganare = Hiragana()
        hiraganare.execute()

        //longLatiInfoReceiverインスタンスを生成
//        val receiver = LongLatiInfoReceiver()
//        receiver.execute("")
    }


    private inner class LongLatiInfoReceiver : AsyncTask<String, String, String>() {
        override fun doInBackground(vararg params: String): String {
            for(i in 0..5) {
                val appid = BuildConfig.YAHOOSEARCH_APP_ID
                //接続URL文字列を作成。
                val urlStr =
                    "https://map.yahooapis.jp/search/local/V1/localSearch?appid=${appid}&results=1&detail=simple&output=json&query=${hira_map[i]}"

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
                    lon_map.add(longlati.first)
                    lat_map.add(longlati.second)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    con.disconnect()
                }
            }
            return ""
        }

        override fun onPostExecute(result: String) {
            val receiver2 = WeatherInfoReceiver()
            receiver2.execute("")
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


        // 非同期で、お天気データを取得するクラス。
        private inner class WeatherInfoReceiver : AsyncTask<String, String, String>() {
            override fun doInBackground(vararg params: String): String {
                var num = 0
                for (i in 0..5) {
                    val appid = BuildConfig.OPENWEATHERMAP_APP_ID

                    val lon = lon_map[i]
                    val lat = lat_map[i]


                    val urlStr =
                        "http://api.openweathermap.org/data/2.5/forecast?lat=${lat}&lon=${lon}&appid=${appid}&units=metric"

                    Log.d("TAG", urlStr)
                    //URLオブジェクトを生成。
                    val url = URL(urlStr)
                    //URLオブジェクトからHttpURLConnectionオブジェクトを取得。
                    val con = url.openConnection() as HttpURLConnection
                    //http接続メソッドを設定。
                    con.requestMethod = "GET"

                    //以下タイムアウトを設定する場合のコード。
                    con.connectTimeout = 1000
                    con.readTimeout = 1000

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

                        val sample = json2Weather(result)
                        val tempicon = sample.split(",")
                        val icon = tempicon[0]
                        val temp = tempicon[1]
                        val splittemp = temp.split(".")
                        val temp1 = splittemp[0]
                        val temp2 = splittemp[1]
                        val inttemp2 = Integer.parseInt(temp2)
                        var casttemp = ""
                        if (inttemp2 >= 50) {
                            var inttemp1 = Integer.parseInt(temp1)
                            inttemp1 += 1
                            casttemp = "${inttemp1}℃"
                        } else {
                            casttemp = "${temp1}℃"
                        }
                        weather_map.add(icon)
                        temp_map.add(casttemp)
                        //Log.d("weather_map",weather_map[i])
                        //Log.d("temp_map",temp_map[i])

                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        con.disconnect()
                    }
                    num++
                }
                return ""
            }

            override fun onPostExecute(result: String) {
                next()
            }

            private fun json2Weather(json: String): String {
                //JSON文字列からJSONObjectオブジェクトを生成。これをルートJSONオブジェクトとする。
                val rootJSON = JSONObject(json)
                val lists = rootJSON.getJSONArray("list")

                var sthour=""
                if(hour==6){
                    sthour="06"
                }else if(hour==9){
                    sthour="09"
                }else{
                    sthour="${hour}"
                }

                val time = "${day} ${sthour}:00:00"
                var listJSON = lists.getJSONObject(0)
                var dt_txt = "none"

                for (i in 0..35) {
                    listJSON = lists.getJSONObject(i)
                    dt_txt = listJSON.getString("dt_txt")
                    if (dt_txt == time) {
                        break
                    }
                }
                val weather = listJSON.getJSONArray("weather")
                val weatherNOW = weather.getJSONObject(0)
                val icon = weatherNOW.getString("icon")
                val main = listJSON.getJSONObject("main")
                val temp = main.getString("temp")


                val result = "${icon},${temp}"
                hour += 3
                return result
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

        }


    }

    private inner class GoogleSearchReceiver : AsyncTask<String, String, String>() {
        override fun doInBackground(vararg params: String): String {
            val key = BuildConfig.GOOGLE_API_KEY
            val cx = BuildConfig.GOOGLECUSTOMSEARCH_API_CX
            //var num = 0
            for (plc in places) {

                val urlStr =
                    "https://www.googleapis.com/customsearch/v1?key=${key}&cx=${cx}&q=${plc}&searchType=image&num=6"

                Log.d("TAG", urlStr)
                //URLオブジェクトを生成。
                val url = URL(urlStr)
                //URLオブジェクトからHttpURLConnectionオブジェクトを取得。
                val con = url.openConnection() as HttpURLConnection
                //http接続メソッドを設定。
                con.requestMethod = "GET"

                //以下タイムアウトを設定する場合のコード。
                con.connectTimeout = 1000
                con.readTimeout = 1000

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
                    Log.d("TAG", plc)
                    Log.d("TAG", result)

                    val sample =
                        json2Google(result)  //url:arrylist<string>が渡される(6つのurl)

                    Log.d("TAG", plc)

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
                //var imageurlarray: ArrayList<String>

                //imageurlarray = sent


            }
            return ""
        }

    }



    private fun json2Google(json: String): String{
        //JSON文字列からJSONObjectオブジェクトを生成。これをルートJSONオブジェクトとする。
        val rootJSON = JSONObject(json)
        val items = rootJSON.getJSONArray("items")
        var item: JSONObject
        var imageJSON: JSONObject
        var imageLink: String
        for (i in 0..5) {
            item = items.getJSONObject(i)
            imageJSON = item.getJSONObject("image")
            imageLink = imageJSON.getString("thumbnailLink")
            sent.add(imageLink)
            Log.d("imagelink",sent[i])
            if (i == 0) {
                image01.add(imageLink)
            }
        }
        var bitmapimage = ArrayList<Bitmap>()
        //var num = 0
        var m = 0
        var imageUrl: URL
        var imageIs: InputStream
        var image: Bitmap

        for (i in 0..5) {
            try {
                imageUrl = URL(sent[i])//ここの中身がないよ
                Log.d("url","${imageUrl}")
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
                "inside" -> m--
            }
            Log.d("res", res)
            Log.d("osusume", m.toString())


            var osusume = ""
            if (i == 5) {
                if (m > 0) {
                    osusume = "100"
                    result.add(osusume)
                    //sent.clear()
                    m = 0
                } else if (m == 0) {
                    osusume = "100"
                    result.add(osusume)
                    //sent.clear()
                    m = 0
                } else {
                    osusume = "0"
                    result.add(osusume)
                    //sent.clear()
                    m = 0
                }
                Log.d("osusume", osusume)
            }

        }
        return ""
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


    //ひらがなAPI
    private inner class Hiragana(): AsyncTask<String, String, String>() {
        override fun doInBackground(vararg params: String): String {
            var result =""
            var MIMEType: MediaType?
            var body: RequestBody?
            var request: Request
            var call: Call
            for (i in 0..5){
                val client = OkHttpClient()
                val url = "https://labs.goo.ne.jp/api/hiragana"
                val Jsontext = """{
"app_id":
"${BuildConfig.GOOHIRAGANA_APP_ID}",
"sentence": "${places[i]}",
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
                Log.d("ひらがなJSON",result)
                var rootJSON = JSONObject(result)
                var hira = rootJSON.getString("converted")
                hira_map.add(hira)
                Log.d("ひらがな結果",hira)
            }
            //可変長引数の1個目(インデックス0)を取得。これが都市ID

            return result
        }

        override fun onPostExecute(result: String) {
            val receiver = LongLatiInfoReceiver()
            receiver.execute("")
        }
    }


    fun next() {
        val intent = Intent(this, TenkiHyoujiActivity::class.java)
        intent.putExtra("date",day)
        intent.putExtra("lon",lon_map)
        intent.putExtra("lat",lat_map)
        intent.putExtra("icon", weather_map)  //weathermap だけど　iconたち
        intent.putExtra("temp", temp_map)    //気温たち
        intent.putExtra("picture", image01) //画像たち
        intent.putExtra("result", result)   //機械学習結果
        intent.putExtra("am6", data1)
        intent.putExtra("am9", data2)
        intent.putExtra("pm0", data3)
        intent.putExtra("pm3", data4)
        intent.putExtra("pm6", data5)
        intent.putExtra("pm9", data6)
        progressBar.setVisibility(ProgressBar.INVISIBLE)
        startActivity(intent)
    }
}


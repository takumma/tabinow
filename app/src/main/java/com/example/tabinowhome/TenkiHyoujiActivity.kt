@file:Suppress("UnusedImport")

package com.example.tabinowhome

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.activity_tenki_hyouji.*
import kotlinx.android.synthetic.main.list_item.view.*
import kotlinx.android.synthetic.main.list_item.view.spot
import kotlinx.android.synthetic.main.list_item.view.temp
import kotlinx.android.synthetic.main.list_item.view.time
import kotlinx.android.synthetic.main.list_item.view.weathericon
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import com.example.tflitesample.ImageClassifier
import kotlinx.android.synthetic.main.activity_mokutekiti.*



class TenkiHyoujiActivity : AppCompatActivity() {
    var icons= ArrayList<Int>()
    var spot = ArrayList<String>()
    var results=ArrayList<Int>()
    var icon=ArrayList<String>()
    //var bitmap=ArrayList<Int>()
    var bitmap=ArrayList<String>()//url
    var lonlist=ArrayList<String>()
    var latlist=ArrayList<String>()
    var Wresult =ArrayList<String>()
    var name_map=ArrayList<String>()
    var image:String=""//とりあえず保持

    var spotText=String()
    var spotPic = String()

    var wiki_map=ArrayList<String>()

    val photos = ArrayList<String?>()
    val photo_map = ArrayList<String>()
    val spotNames = ArrayList<String>()
    private lateinit var classifier: ImageClassifier




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tenki_hyouji)


        spot.clear()
        icons.clear()
        results.clear()
        bitmap.clear()
        name_map.clear()
        //icon.clear()
        lonlist.clear()
        latlist.clear()
        wiki_map.clear()
        photo_map.clear()
        photos.clear()
        //val progressBar = findViewById<ProgressBar>(R.id.progressBar3)
        progressBar3.setVisibility(ProgressBar.INVISIBLE)

        val day=intent.getStringExtra("date")
        textView.text=day

        val data1 = intent.getStringExtra("am6")
        val data2 = intent.getStringExtra("am9")
        val data3 = intent.getStringExtra("pm0")
        val data4 = intent.getStringExtra("pm3")
        val data5 = intent.getStringExtra("pm6")
        val data6 = intent.getStringExtra("pm9")


        spot.add(data1)
        spot.add(data2)
        spot.add(data3)
        spot.add(data4)
        spot.add(data5)
        spot.add(data6)


        icon = intent.getStringArrayListExtra("icon")
        val temps=intent.getStringArrayListExtra("temp")
        val result = intent.getStringArrayListExtra("result")
        lonlist=intent.getStringArrayListExtra("lon")
        latlist=intent.getStringArrayListExtra("lat")

        //val b = intent.extras
       // bitmap = b.get("picture") as ArrayList<Bitmap>
        bitmap = intent.getStringArrayListExtra("picture")

        classifier = ImageClassifier(assets)


        for(pic in icon){
            when (pic) {
                "01d" -> icons.add(R.drawable.n01)
                "01n" -> icons.add(R.drawable.n01)
                "02d" -> icons.add(R.drawable.n01)
                "02n" -> icons.add(R.drawable.n01)
                "03d" -> icons.add(R.drawable.n03)
                "03n" -> icons.add(R.drawable.n03)
                "50d" -> icons.add(R.drawable.n03)
                "50n" -> icons.add(R.drawable.n03)
                "04d" -> icons.add(R.drawable.n03)
                "04n" -> icons.add(R.drawable.n03)
                "09d" -> icons.add(R.drawable.n09)
                "09n" -> icons.add(R.drawable.n09)
                "10d" -> icons.add(R.drawable.n10)
                "10n" -> icons.add(R.drawable.n10)
                "11d" -> icons.add(R.drawable.n11)
                "11n" -> icons.add(R.drawable.n11)
                "13d" -> icons.add(R.drawable.n13)
                "13n" -> icons.add(R.drawable.n13)
            }
        }





        /*val Wresult =ArrayList<String>()

        for(pic in icon){
            if((pic=="01d")||(pic=="01n")||(pic=="02d")||(pic=="02n")||(pic=="03d")||(pic=="03n")||
                (pic=="50d")||(pic=="50n")||(pic=="04d")||(pic=="04n")){
                Wresult.add("wgood")
            }else{
                Wresult.add("wbad")
            }
        }*/
        //Wresult =ArrayList<String>()

        for(i in 0 until icon.size){
            if((icon[i]=="01d")||(icon[i]=="01n")||(icon[i]=="02d")||(icon[i]=="02n")) {
                results.add(R.drawable.icon100)
                Wresult.add("sunny")
            }else if((icon[i]=="03d")||(icon[i]=="03n")||(icon[i]=="50d")||(icon[i]=="50n")||(icon[i]=="04d")||(icon[i]=="04n")){
                if(result[i]=="0"){
                    results.add(R.drawable.icon100)
                }else{
                    results.add(R.drawable.icon100)
                }
                Wresult.add("cloudy")
            }else{
                if(result[i]=="0"){
                    results.add(R.drawable.icon100)
                }else{
                    results.add(R.drawable.icon0)
                }
                Wresult.add("rainy")
            }
        }

       /*for(i in 0..5){
            if(Wresult[i]=="wgood"){                     //天気が晴
                results.add(R.drawable.icon100)
            }else{
                when(result[i]){                        //天気が雨
                    "100"->results.add(R.drawable.icon0)
                    "50"->results.add(R.drawable.icon0)
                    "0"->results.add(R.drawable.icon100)
                }
            }
        }*/


        val time = listOf("06:00", "09:00", "12:00", "15:00", "18:00", "21:00")   //時間なのでこのままでok
        val spot = spot//目的地(前画面でユーザが入力したものを持ってくる)
        val temp = temps //気温
        val weathericon =icons
        val osusumeicon =results


        val datas = List(time.size) { i -> mixdata(time[i], spot[i], temp[i], weathericon[i],osusumeicon[i]) }
        val adapter = spotsListAdapter(this, datas)
        lvmenu.adapter = adapter

        //val lvmemu=findViewById<ListView>(R.id.lvmenu)
        lvmenu.onItemClickListener= ListItemClickListener()   //画面遷移のところ



    }

    data class mixdata(val time: String, val spot: String, val temp: String, val imageId: Int, val osusumeId:Int)
    data class ViewHolder(
        val timeTextView: TextView,
        val spotTextView: TextView,
        val tempTextView: TextView,
        val weatherImgView: ImageView,
        val osusumeImgView:ImageView
    )


    class spotsListAdapter(context: Context, spots: List<mixdata>) : ArrayAdapter<mixdata>(context, 0, spots) {
        private val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var view = convertView
            val holder: ViewHolder

            if (view == null) {
                view = layoutInflater.inflate(R.layout.list_item, parent, false)
                holder = ViewHolder(
                    view.time,
                    view.spot,
                    view.temp,
                    view.weathericon,
                    view.osusumeicon
                )
                view.tag = holder
            } else {
                holder = view.tag as ViewHolder
            }

            val spot = getItem(position) as mixdata
            holder.timeTextView.text = spot.time
            holder.spotTextView.text = spot.spot
            holder.tempTextView.text = spot.temp
            holder.weatherImgView.setImageBitmap(BitmapFactory.decodeResource(context.resources, spot.imageId))
            holder.osusumeImgView.setImageBitmap(BitmapFactory.decodeResource(context.resources,spot.osusumeId))

            return view!!
        }
    }

    fun place(lon: String,lat: String,weather:String){
        val test=PlaceAPI(lon,lat,weather)
        test.execute("")

    }




    private inner class ListItemClickListener:AdapterView.OnItemClickListener{


        override fun onItemClick(parent:AdapterView<*>,view:View,position:Int,id:Long){
            progressBar3.setVisibility(ProgressBar.VISIBLE)
            progressBar3.bringToFront()
            spotText=spot[position]
            spotPic = bitmap[position]
            Log.d("debug",spotText)
            Log.d("debug",spotPic)
            val lon =lonlist[position]
            val lat= latlist[position]
            val weather =Wresult[position]
            place(lon,lat,weather)
            //PlaceAPI(lon,lat,weather)//returnはArrayList

            //next(spotText,SpotPic,name_map,images)
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
                if (name == spotText) {
                    if(photoString=="") {//spotPicからかえた
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

    private inner class GoogleSearchReceiver (w: String): AsyncTask<String, String, String>() {
        var weather = w
        var placeName = String()
        var placeI = 0

        override fun doInBackground(vararg params: String): String {
            photo_map.clear()

            val key = BuildConfig.GOOGLE_API_KEY
            val cx = BuildConfig.GOOGLECUSTOMSEARCH_API_CX
            while (name_map.size<4) { // なぜか５だと怒られる...
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

                if(photos[placeI]==null) {
                    photoURL = "https://upload.wikimedia.org/wikipedia/ja/b/b5/Noimage_image.png"
                }else{
                    photoURL = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=${photos[placeI]}&key=${BuildConfig.GOOGLE_API_KEY}"//placephotosURL
                }

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

        fun next(i:String,p:Bitmap,namemap:ArrayList<String>,images:ArrayList<String>,photo_map:ArrayList<String>,wiki_map:ArrayList<String>){
            Log.d("TAG","next()")
            val intent =Intent(applicationContext,osusumeActivity::class.java)
            intent.putExtra("selected",i)
            intent.putExtra("pic",p)
            intent.putExtra("places",namemap)
            intent.putExtra("images",images)
            intent.putExtra("photo_map" ,photo_map)
            intent.putExtra("wiki_map",wiki_map)
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


    private inner class WikiTask: AsyncTask<String, String, String>() {
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
            Log.d("spotText",spotText)
            Log.d("spotPic",spotPic)
            for (i in 0 until name_map.size){
                Log.d("name_map",name_map[i])
            }
            for (i in 0 until photo_map.size){
                Log.d("photo_map",photo_map[i])
            }
            for (i in 0 until wiki_map.size){
                Log.d("wiki_map",wiki_map[i])
            }
            next(spotText,spotPic,name_map,photo_map,wiki_map)
        }

        private fun json2Wiki(json: String?): String {
            //JSON文字列からJSONObjectオブジェクトを生成。これをルートJSONオブジェクトとする。
            val rootJSON = JSONArray(json)
            var url:String = ""
            try {
                val query = rootJSON.getJSONObject(0)
                url = query.getString("url")
                //var name = feature.getString("Name")
                Log.d("tag", url)
                return url
            }catch (e: Exception){
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

        fun next(i:String,p:String,namemap:ArrayList<String>,photo_map:ArrayList<String>,wiki_map:ArrayList<String>){
            Log.d("TAG","next()")
            val intent =Intent(applicationContext,KensakuHyoujiActivity::class.java)
            intent.putExtra("place",i)
            intent.putExtra("pic",p)
            intent.putExtra("placelist",namemap)
            intent.putExtra("piclink" ,photo_map)
            intent.putExtra("wikilink",wiki_map)
            progressBar3.setVisibility(ProgressBar.INVISIBLE)
            startActivity(intent)
        }
    }

}

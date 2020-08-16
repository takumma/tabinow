package com.example.tabinowhome

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.activity_kensaku_hyouji.*
import kotlinx.android.synthetic.main.list_textview_tap.view.*
import retrofit2.http.Url
import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL


 class KensakuHyoujiActivity : AppCompatActivity() {
    var piclist = ArrayList<String>()//画面に表示するやつ
    var wikilist=ArrayList<String>()//画面に表示するやつ

    //var wikilink = ArrayList<String>()//wikiのリンク
    //var piclink = ArrayList<String>()//picのリンク

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kensaku_hyouji)

        piclist.clear()
        wikilist.clear()

        val spotname = intent.getStringExtra("place")
        val picture = intent.getStringExtra("pic")
        //TODO 写真のURL受け取る
        val spotlist = intent.getStringArrayListExtra("placelist")
        val wikilink = intent.getStringArrayListExtra("wikilink")
        val piclink = intent.getStringArrayListExtra("piclink")
//        val task = ImageGetTask(imageView2)
//        task.execute(picture)
        /*val image : Bitmap
        val imageUrl = URL(picture)
        val imageIs: InputStream
        imageIs = imageUrl.openStream()
        image = BitmapFactory.decodeStream(imageIs)
        imageView2.setImageBitmap(image)*/
        button2.setOnClickListener{picture(picture)}
        textView3.text = spotname
        textView5.text = "${spotname}周辺のおすすめスポット"
        for(i in 0 until spotlist.size){
            piclist.add("  写真  ")
            wikilist.add("  wiki  ")
        }

        val datas = List(spotlist.size) { i -> mixdata(spotlist[i], piclist[i], wikilist[i]) }
        val adapter = spotsListAdapter(this, datas)
        lvmenu.adapter = adapter

        lvmenu.onItemClickListener= ListItemClickListener(wikilink,piclink)

    }

    data class mixdata(val place: String, val wiki: String, val pic: String)
    data class ViewHolder(
        val placeTextView: TextView,
        val link1TextView: TextView,
        val link2TextView: TextView
    )

    class spotsListAdapter(context: Context, spots: List<mixdata>) : ArrayAdapter<mixdata>(context, 0, spots) {
        private val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var view = convertView
            val holder: ViewHolder

            if (view == null) {
                view = layoutInflater.inflate(R.layout.list_textview_tap, parent, false)
                holder = ViewHolder(view.place, view.link1text, view.link2text)
                view.tag = holder
            } else {
                holder = view.tag as ViewHolder
            }

            val spot = getItem(position) as mixdata
            holder.placeTextView.text = spot.place
            holder.link1TextView.text = spot.pic
            holder.link2TextView.text = spot.wiki

            return view!!
        }
    }

    private inner class ListItemClickListener(wikilink:ArrayList<String>,piclink:ArrayList<String>): AdapterView.OnItemClickListener{

        val wiki = wikilink
        val pic = piclink
        override fun onItemClick(parent: AdapterView<*>, view:View, position:Int, id:Long){
            val link1id =view.findViewById<TextView>(R.id.link1text)
            val link2id =view.findViewById<TextView>(R.id.link2text)
            link1id.setOnClickListener{
                var url:String=""
                url =wiki[position]
                if(url!="") {
                    intent(url)
                }else{
                    Toast.makeText(applicationContext, "ページが見つかりませんでした", Toast.LENGTH_LONG).show()
                }
            }
            link2id.setOnClickListener{
                var url=""
                url =pic[position]
                if(url!="") {
                    intent(url)
                }else{
                    Toast.makeText(applicationContext, "ページが見つかりませんでした", Toast.LENGTH_LONG).show()
                }
            }
        }

        fun intent(url:String){
            val URL =url
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(URL))
            startActivity(intent)
        }
    }

     fun picture(picture:String){
         val intent=Intent(Intent.ACTION_VIEW, Uri.parse(picture))
         startActivity(intent)
     }



    /*internal inner class ImageGetTask(private val image: ImageView?) :
        AsyncTask<String, Void, Bitmap>() {
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
            image?.setImageBitmap(result)
        }
    }*/



    /*fun onMap(view: View?) {
        val searchword = URLEncoder.encode(place,"UTF-8")
        val urlstr="geo:0,0?q=${searchword}"
        val url = Uri.parse(urlstr)
        val intent = Intent(Intent.ACTION_VIEW, url)
        startActivity(intent)
    }*/
}



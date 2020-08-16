package com.example.tabinowhome

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import kotlinx.android.synthetic.main.activity_newdata.*
import java.text.SimpleDateFormat
import java.util.*



class newdataActivity : AppCompatActivity() {
    var dates= ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_newdata)

        val data=getNowDate()      //今日の日付を受け取る処理


        var times=data.split("/")
        val year=times[0]
        val month=times[1]
        var times2=times[2].split(" ")
        val day=times2[0]


        val iyear : Int = Integer.parseInt(year)      //型変換
        val imonth:Int=Integer.parseInt(month)
        val iday:Int=Integer.parseInt(day)

        selectdate(iyear,imonth,iday)           //その日から四日分の日付をdatesにぶち込む関数


        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, dates)
        // ドロップダウンのレイアウトを指定
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // ListViewにAdapterを関連付ける
        val spinner = findViewById<Spinner>(R.id.spinner)
        spinner.adapter = adapter

        button4.setOnClickListener{toNextView(it)}
    }

    fun getNowDate(): String {
        val df = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        val date = Date(System.currentTimeMillis())
        return df.format(date)
    }

    fun toNextView(view: View?){
        val intent = Intent(this, MokutekitiActivity::class.java)
        //val date = spinner.toString()
        val date = spinner.selectedItem as String
        intent.putExtra("date", date)
        startActivity(intent)
    }


    fun selectdate(year:Int,month:Int,day:Int) {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)

        val dataf="%04d-%02d-%02d"
        val data=dataf.format(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE))

        //val data =calendar.get(Calendar.YEAR).toString() +
        //        "-" +calendar.get(Calendar.MONTH).toString() + "-"+ calendar.get(Calendar.DATE).toString()
        dates.add(data)
        //Log.i("date1", calendar.get(Calendar.YEAR).toString() +
        //        "-" +calendar.get(Calendar.MONTH).toString() + "-"+ calendar.get(Calendar.DATE).toString() )

        for(i in 0..2){
            calendar.add(Calendar.DATE, 1)
            val data=dataf.format(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE))
            //val data =calendar.get(Calendar.YEAR).toString() +
            //        "-" +calendar.get(Calendar.MONTH).toString() + "-"+ calendar.get(Calendar.DATE).toString()
            dates.add(data)
        }
    }
}

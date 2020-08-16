package com.example.tabinowhome

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    var data=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)




        button.setOnClickListener{onSakuseiButtonTapped()}
        button3.setOnClickListener{onPhotoButtonTapped()}
    }



    fun onSakuseiButtonTapped(){
        val intent = Intent(this,newdataActivity::class.java)
        startActivity(intent)
    }


    fun onPhotoButtonTapped(){
        val intent = Intent(this,PhotoActivity::class.java)
        startActivity(intent)
    }
}

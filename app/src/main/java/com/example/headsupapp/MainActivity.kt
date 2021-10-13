package com.example.headsupapp

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Surface
import android.widget.*
import androidx.core.view.isVisible
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import java.net.URL
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    private lateinit var llTime: LinearLayout
    private lateinit var llStart: LinearLayout
    private lateinit var llCeleb: LinearLayout
    private lateinit var tvTime: TextView
    private lateinit var tvName: TextView
    private lateinit var tvT1: TextView
    private lateinit var tvT2: TextView
    private lateinit var tvT3: TextView
    private lateinit var tvHead: TextView
    private lateinit var btnStart: Button
    private lateinit var celebrities: ArrayList<JSONObject>

    private var celeb = 0
    private var Activeplay = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        llTime = findViewById(R.id.llTime)
        llStart = findViewById(R.id.llStart)
        llCeleb = findViewById(R.id.llCeleb)
        tvTime = findViewById(R.id.tvTime)
        tvName = findViewById(R.id.tvName)
        tvT1 = findViewById(R.id.tvT1)
        tvT2 = findViewById(R.id.tvT2)
        tvT3 = findViewById(R.id.tvT3)
        tvHead = findViewById(R.id.tvHead)
        btnStart = findViewById(R.id.btnStart)
        btnStart.setOnClickListener { requestAPI() }
        celebrities = arrayListOf()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val rotation = windowManager.defaultDisplay.rotation
        if(rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180){
            if(Activeplay){
                celeb++
                newCelebrity(celeb)
                updateStatus(false)
            }else{
                updateStatus(false)
            }
        }else{
            if(Activeplay){
                updateStatus(true)
            }else{
                updateStatus(false)
            }
        }
    }

    private fun newTimer(){
        if(!Activeplay){
            Activeplay = true
            tvHead.text = "Please Rotate Device"
            btnStart.isVisible = false
            val rotation = windowManager.defaultDisplay.rotation
            if(rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180){
                updateStatus(false)
            }else{
                updateStatus(true)
            }

            object : CountDownTimer(60000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    tvTime.text = "Time: ${millisUntilFinished / 1000}"
                }

                override fun onFinish() {
                    Activeplay = false
                    tvTime.text = "Time: --"
                    tvHead.text = "Heads Up!"
                    btnStart.isVisible = true
                    updateStatus(false)
                }
            }.start()
        }
    }

    private fun newCelebrity(id: Int){
        if(id < celebrities.size){
            tvName.text = celebrities[id].getString("name")
            tvT1.text = celebrities[id].getString("taboo1")
            tvT2.text = celebrities[id].getString("taboo2")
            tvT3.text = celebrities[id].getString("taboo3")
        }
    }

    private fun requestAPI(){
        CoroutineScope(Dispatchers.IO).launch {
            val data = async {
                getCelebrities()
            }.await()
            if(data.isNotEmpty()){
                withContext(Main){
                    parseJSON(data)
                    celebrities.shuffle()
                    newCelebrity(0)
                    newTimer()
                }
            }else{

            }
        }
    }

    private suspend fun parseJSON(result: String){
        withContext(Dispatchers.Main){
            celebrities.clear()
            val jsonArray = JSONArray(result)
            for(i in 0 until jsonArray.length()){
                celebrities.add(jsonArray.getJSONObject(i))
            }
        }
    }

    private fun getCelebrities(): String{
        var response = ""
        try {
            response = URL("https://dojo-recipes.herokuapp.com/celebrities/")
                .readText(Charsets.UTF_8)
        }catch (e: Exception){
            println("Error: $e")
        }
        return response
    }

    private fun updateStatus(showCelebrity: Boolean){
        if(showCelebrity){
            llCeleb.isVisible = true
            llStart.isVisible = false
        }else{
            llCeleb.isVisible = false
            llStart.isVisible = true
        }
    }
}
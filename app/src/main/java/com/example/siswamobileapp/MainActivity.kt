package com.example.siswamobileapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.siswamobileapp.databinding.ActivityMainBinding
import com.example.siswamobileapp.databinding.CardLayoutBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var bind: ActivityMainBinding
    val startResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->
            showAllSiswa()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)

        bind.fabHome.setOnClickListener {
            startResult.launch(Intent(this@MainActivity, CreateActivity::class.java))
        }

        showAllSiswa()
    }

    private fun showAllSiswa() {
        try {
            GlobalScope.launch(Dispatchers.IO) {
                val url = "${Connection.url}/Siswa"
                val conn = URL(url).openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("Content-Type", "application/json")

                val responseCode = conn.responseCode

                if (responseCode in 200..299) {
                    val getData = JSONArray(conn.inputStream.bufferedReader().readText())

                    runOnUiThread {
                        bind.mainRv.adapter = object : RecyclerView.Adapter<HolderCard>() {
                            override fun onCreateViewHolder(
                                parent: ViewGroup,
                                viewType: Int
                            ): HolderCard {
                                val inflate =
                                    CardLayoutBinding.inflate(layoutInflater, parent, false)
                                return HolderCard(inflate)
                            }

                            override fun getItemCount(): Int = getData.length()

                            override fun onBindViewHolder(holder: HolderCard, position: Int) {
                                val data = getData.getJSONObject(position)
                                holder.binding.namaSiswa.text = data.getString("name")
                                holder.binding.sexSiswa.text = data.getString("sex")

                                holder.binding.btnDetail.setOnClickListener {
                                    startResult.launch(Intent(
                                        this@MainActivity,
                                        DetailActivity::class.java
                                    ).apply {
                                        putExtra("idSiswa", data.getString("id"))
                                    })
                                }
                            }
                        }
                        bind.mainRv.layoutManager = LinearLayoutManager(this@MainActivity)
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Error", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this@MainActivity, "Error", Toast.LENGTH_SHORT).show()
        }
    }

    class HolderCard(val binding: CardLayoutBinding) : RecyclerView.ViewHolder(binding.root)
}
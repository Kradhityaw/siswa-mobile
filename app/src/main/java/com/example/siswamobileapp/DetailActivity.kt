package com.example.siswamobileapp

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.siswamobileapp.databinding.ActivityDetailBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class DetailActivity : AppCompatActivity() {
    private lateinit var bind: ActivityDetailBinding

    val startResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->
            getSiswa()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(bind.root)

        bind.toolbarDetail.setNavigationOnClickListener {
            finish()
        }

        bind.ubahBtn.setOnClickListener {
            startResult.launch(Intent(this@DetailActivity, EditActivity::class.java).apply {
                putExtra("idSiswa", intent.getStringExtra("idSiswa"))
            })
        }

        bind.hapusBtn.setOnClickListener {
            val alertMaterial = MaterialAlertDialogBuilder(this@DetailActivity)
                .setTitle("Hapus Siswa")
                .setBackground(resources.getDrawable(R.drawable.custom_material_alert))
                .setMessage("Apakah kamu ingin menghapusnya?")
                .setPositiveButton(
                    "Ya"
                ) { dialog, which -> deleteSiswa() }
                .setNegativeButton("Tidak", { dialog, which -> dialog.dismiss() })
            alertMaterial.create()
            alertMaterial.show()
        }

        getSiswa()
    }

    private fun getSiswa() {
        val idSiswa = intent.getStringExtra("idSiswa")

        GlobalScope.launch(Dispatchers.IO) {
            val url = "${Connection.url}/Siswa/${idSiswa}"
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Content-Type", "application/json")

            val responseCode = conn.responseCode

            if (responseCode in 200..299) {
                val inputStream = conn.inputStream.bufferedReader().readText()
                val JsonObjectData = JSONObject(inputStream)

                runOnUiThread {
                    bind.namaSiswa.text = JsonObjectData.getString("name")
                    bind.jenisKelamin.text =
                        if (JsonObjectData.getString("sex") == "L") "Laki - Laki" else "Perempuan"
                    bind.asalSekolah.text =
                        JsonObjectData.getJSONObject("asalSekolah").getString("name")
                }
            } else {
            }
        }
    }

    fun deleteSiswa() {
        val idSiswa = intent.getStringExtra("idSiswa")

        GlobalScope.launch(Dispatchers.IO) {
            val conn =
                URL("${Connection.url}/Siswa/${idSiswa}").openConnection() as HttpURLConnection
            conn.requestMethod = "DELETE"
            conn.setRequestProperty("Content-Type", "application/json")

            val responseCode = conn.responseCode

            if (responseCode in 200..299) {
                runOnUiThread {
                    Toast.makeText(
                        this@DetailActivity,
                        "Berhasil Menghapus Siswa!",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            } else {
                runOnUiThread {
                    Toast.makeText(
                        this@DetailActivity,
                        "Gagal Menghapus Siswa!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
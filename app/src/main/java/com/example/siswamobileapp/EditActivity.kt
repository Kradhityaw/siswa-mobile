package com.example.siswamobileapp

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.siswamobileapp.databinding.ActivityEditBinding
import com.example.siswamobileapp.databinding.SpinnerLayoutBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class EditActivity : AppCompatActivity() {
    lateinit var bind: ActivityEditBinding
    var sekolahId = 0
    var gender = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityEditBinding.inflate(layoutInflater)
        setContentView(bind.root)

        bind.toolbarCreate.setNavigationOnClickListener {
            finish()
        }

        getSiswa()
        spinnerInit()

        bind.genderRadios.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->
            val radio: RadioButton = findViewById(checkedId)
            gender = if (radio.text.toString() == "Laki - Laki") "L" else "P"
        })

        bind.createBtn.setOnClickListener {
            if (gender == "" || bind.createNama.text.isNullOrBlank() || sekolahId == 0) {
                Toast.makeText(this@EditActivity, "Harap Mengisi Semua Data!", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            addSiswa()
        }
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
                    bind.createNama.setText(JsonObjectData.getString("name"))
                    if (JsonObjectData.getString("sex") == "L") bind.L.isChecked =
                        true else bind.P.isChecked = true
                    sekolahId =
                        JsonObjectData.getJSONObject("asalSekolah").getInt("id")

                }
            } else {
            }
        }
    }

    fun spinnerInit() {
        GlobalScope.launch(Dispatchers.IO) {
            val conn = URL("${Connection.url}/AsalSekolah").openStream().bufferedReader().readText()
            val arrayData = JSONArray(conn)

            runOnUiThread {
                val adapter = object : ArrayAdapter<JSONObject>(
                    this@EditActivity,
                    android.R.layout.simple_spinner_item
                ) {
                    override fun getCount(): Int = arrayData.length()

                    override fun getItem(position: Int): JSONObject? =
                        arrayData.getJSONObject(position)

                    override fun getView(
                        position: Int,
                        convertView: View?,
                        parent: ViewGroup
                    ): View {
                        val binding: SpinnerLayoutBinding =
                            if (convertView != null) SpinnerLayoutBinding.bind(convertView) else SpinnerLayoutBinding.inflate(
                                layoutInflater, parent, false
                            )

                        val it = getItem(position)
                        binding.spinnerNamaSekolah.text = it?.getString("name")

                        return binding.root
                    }

                    override fun getDropDownView(
                        position: Int,
                        convertView: View?,
                        parent: ViewGroup
                    ): View {
                        val binding: SpinnerLayoutBinding =
                            if (convertView != null) SpinnerLayoutBinding.bind(convertView) else SpinnerLayoutBinding.inflate(
                                layoutInflater, parent, false
                            )

                        val it = getItem(position)
                        binding.spinnerNamaSekolah.text = it?.getString("name")

                        return binding.root
                    }
                }
                bind.spinnerSekolah.adapter = adapter
                bind.spinnerSekolah.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            val item: JSONObject = adapter.getItem(position) as JSONObject
                            sekolahId = item.getInt("id")
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {
                            Toast.makeText(this@EditActivity, "Gagal", Toast.LENGTH_SHORT).show()
                        }
                    }
//                Toast.makeText(this@EditActivity, sekolahId.toString(), Toast.LENGTH_SHORT).show()
                var index = 0
                for (e in 0 until arrayData.length()) {
                    val data = arrayData.getJSONObject(e)

                    if (data.getInt("id") == sekolahId) {
                        index = e
                    }
                }
                bind.spinnerSekolah.setSelection(index)
            }
        }
    }

    fun addSiswa() {
        val idSiswa = intent.getStringExtra("idSiswa")

        GlobalScope.launch(Dispatchers.IO) {
            val conn =
                URL("${Connection.url}/Siswa/${idSiswa}").openConnection() as HttpURLConnection
            conn.requestMethod = "PUT"
            conn.setRequestProperty("Content-Type", "application/json")

            val data = JSONObject().apply {
                put("name", bind.createNama.text.toString())
                put("sex", gender)
                put("asalSekolahId", sekolahId)
            }

            conn.outputStream.write(data.toString().toByteArray())

            val responseCode = conn.responseCode

            if (responseCode in 200..201) {
                runOnUiThread {
                    Toast.makeText(
                        this@EditActivity,
                        "Berhasil Menyimpan Perubahan",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            } else {
                runOnUiThread {
                    Toast.makeText(
                        this@EditActivity,
                        "Gagal Menyimpan Perubahan",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

}
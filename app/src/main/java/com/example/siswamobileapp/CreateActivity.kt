package com.example.siswamobileapp

import android.os.Bundle
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
import com.example.siswamobileapp.databinding.ActivityCreateBinding
import com.example.siswamobileapp.databinding.SpinnerLayoutBinding
import com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class CreateActivity : AppCompatActivity() {
    lateinit var bind: ActivityCreateBinding
    var sekolahId = 0
    var gender = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityCreateBinding.inflate(layoutInflater)
        setContentView(bind.root)

        bind.toolbarCreate.setNavigationOnClickListener {
            finish()
        }

        spinnerInit()

        bind.genderRadios.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->
            val radio: RadioButton = findViewById(checkedId)
            gender = if (radio.text.toString() == "Laki - Laki") "L" else "P"
        })


        bind.createBtn.setOnClickListener {
            if (gender == "" || bind.createNama.text.isNullOrBlank() || sekolahId == 0) {
                Toast.makeText(this@CreateActivity, "Harap Mengisi Semua Data!", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            addSiswa()
        }
    }

    fun spinnerInit() {
        GlobalScope.launch(Dispatchers.IO) {
            val conn = URL("${Connection.url}/AsalSekolah").openStream().bufferedReader().readText()
            val arrayData = JSONArray(conn)

            runOnUiThread {
                val adapter = object : ArrayAdapter<JSONObject>(
                    this@CreateActivity,
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

                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }
            }
        }
    }

    fun addSiswa() {
        GlobalScope.launch(Dispatchers.IO) {
            val conn = URL("${Connection.url}/Siswa").openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
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
                        this@CreateActivity,
                        "Berhasil Menambahkan Siswa",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            } else {
                runOnUiThread {
                    Toast.makeText(
                        this@CreateActivity,
                        "Gagal Menambahkan Siswa",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

}
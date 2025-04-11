package com.example.fm4

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.fm4.R
import java.io.File

class FileManagerActivity : AppCompatActivity() {

    companion object {
        private const val PERMISSION_REQUEST_CODE = 101
    }

    private lateinit var listViewFiles: ListView
    private val fileNames = mutableListOf<String>()
    private val filePaths = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>

    private var currentDirectory: File? = null // 🧭 para mantener la ruta actual

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_manager)

        listViewFiles = findViewById(R.id.listViewFiles)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, fileNames)
        listViewFiles.adapter = adapter

        verificarYSolicitarPermiso()

        listViewFiles.setOnItemClickListener { _, _, position, _ ->
            val selectedFile = File(filePaths[position])
            if (selectedFile.isDirectory) {
                // Navegar dentro del directorio
                listFilesInDirectory(selectedFile)
            } else {
                // Seleccionar archivo
                val resultIntent = Intent()
                resultIntent.putExtra("rutaArchivo", selectedFile.absolutePath)
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }

        // Volver atrás al directorio padre con botón de volver
        listViewFiles.setOnItemLongClickListener { _, _, position, _ ->
            if (currentDirectory != null && currentDirectory?.parentFile != null) {
                listFilesInDirectory(currentDirectory!!.parentFile!!) // Usar !! con precaución, pero aquí es seguro después de la comprobación
                return@setOnItemLongClickListener true
            }
            return@setOnItemLongClickListener false
        }
    }

    private fun verificarYSolicitarPermiso() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivityForResult(intent, PERMISSION_REQUEST_CODE)
            } else {
                listFilesInDirectory(Environment.getExternalStorageDirectory())
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            } else {
                listFilesInDirectory(Environment.getExternalStorageDirectory())
            }
        }
    }

    private fun listFilesInDirectory(directory: File) {
        fileNames.clear()
        filePaths.clear()
        currentDirectory = directory

        val files = directory.listFiles()
        if (files != null) {
            for (file in files) {
                fileNames.add((if (file.isDirectory) "\uD83D\uDCC1 " else "") + file.name)
                filePaths.add(file.absolutePath)
            }
        }

        adapter.notifyDataSetChanged()
        title = directory.absolutePath // opcional: muestra la ruta actual en la barra superior
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    listFilesInDirectory(Environment.getExternalStorageDirectory())
                } else {
                    Toast.makeText(this, "Permiso de almacenamiento no concedido", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                listFilesInDirectory(Environment.getExternalStorageDirectory())
            } else {
                Toast.makeText(this, "Permiso de lectura de almacenamiento denegado.", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }
    }
}
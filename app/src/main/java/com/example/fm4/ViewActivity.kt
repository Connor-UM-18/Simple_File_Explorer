package com.example.fm4

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.graphics.Insets

class ViewActivity : AppCompatActivity() {

    private lateinit var btnAccederFileManager: Button
    private lateinit var btnVerArchivoSeleccionado: Button
    private var rutaArchivoSeleccionado: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Cargar el tema almacenado en SharedPreferences
        cargarTema()
        setContentView(R.layout.activity_view)

        btnAccederFileManager = findViewById(R.id.btnAccederFileManager)
        btnVerArchivoSeleccionado = findViewById(R.id.btnVerArchivoSeleccionado)

        btnAccederFileManager.setOnClickListener {
            val intent = Intent(this, FileManagerActivity::class.java)
            startActivityForResult(intent, 1) // Usamos un código de solicitud
        }

        btnVerArchivoSeleccionado.setOnClickListener {
            if (rutaArchivoSeleccionado != null && rutaArchivoSeleccionado!!.isNotEmpty()) {
                val intent = Intent(this, FileViewerActivity::class.java)
                intent.putExtra("rutaArchivo", rutaArchivoSeleccionado)
                startActivity(intent)
            } else {
                // Puedes agregar aquí un mensaje para el usuario indicando que debe seleccionar un archivo primero.
                // Por ejemplo:
                // Toast.makeText(this, "Por favor, selecciona un archivo primero", Toast.LENGTH_SHORT).show()
            }
        }

        // Inicialmente el botón de ver archivo está deshabilitado
        btnVerArchivoSeleccionado.isEnabled = false

        // Manteniendo la configuración de EdgeToEdge y OnApplyWindowInsetsListener
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }
    }
    private fun cargarTema() {
        val sharedPref = getSharedPreferences("temas", MODE_PRIVATE)
        val tema = sharedPref.getString("temaSeleccionado", "Azul")  // Default es azul
        when (tema) {
            "Azul" -> setTheme(R.style.Theme_TuExploradorDeArchivos_Azul)
            "Verde" -> setTheme(R.style.Theme_TuExploradorDeArchivos_Verde)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data != null && data.getStringExtra("rutaArchivo") != null) {
                rutaArchivoSeleccionado = data.getStringExtra("rutaArchivo")
                btnVerArchivoSeleccionado.isEnabled = true
                // Opcional: Puedes mostrar un mensaje indicando el archivo seleccionado
                // Toast.makeText(this, "Archivo seleccionado: " + rutaArchivoSeleccionado, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
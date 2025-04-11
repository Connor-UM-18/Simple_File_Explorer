package com.example.fm4

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Cargar el tema almacenado en SharedPreferences
        cargarTema()
        setContentView(R.layout.activity_main)

        // Acción para el botón Tema Azul
        findViewById<Button>(R.id.btnTemaAzul).setOnClickListener {
            // Guardar el tema azul y recrear la actividad
            setTheme(R.style.Theme_TuExploradorDeArchivos_Azul)
            guardarTema("Azul")
            recreate()  // Recrear la actividad para aplicar el nuevo tema
            iniciarViewActivity() // Redirigir a ViewActivity después de aplicar el tema
        }

        // Acción para el botón Tema Verde
        findViewById<Button>(R.id.btnTemaVerde).setOnClickListener {
            // Guardar el tema verde y recrear la actividad
            setTheme(R.style.Theme_TuExploradorDeArchivos_Verde)
            guardarTema("Verde")
            recreate()  // Recrear la actividad para aplicar el nuevo tema
            iniciarViewActivity() // Redirigir a ViewActivity después de aplicar el tema
        }
    }

    // Función para guardar el tema seleccionado en SharedPreferences
    private fun guardarTema(tema: String) {
        val sharedPref = getSharedPreferences("temas", MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("temaSeleccionado", tema)
        editor.apply()
    }

    // Función para cargar el tema almacenado de SharedPreferences
    private fun cargarTema() {
        val sharedPref = getSharedPreferences("temas", MODE_PRIVATE)
        val tema = sharedPref.getString("temaSeleccionado", "Azul")  // Default es azul
        when (tema) {
            "Azul" -> setTheme(R.style.Theme_TuExploradorDeArchivos_Azul)
            "Verde" -> setTheme(R.style.Theme_TuExploradorDeArchivos_Verde)
        }
    }

    // Función para redirigir a ViewActivity
    private fun iniciarViewActivity() {
        val intent = Intent(this, ViewActivity::class.java)
        startActivity(intent)
    }
}

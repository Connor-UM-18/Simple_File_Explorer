package com.example.fm4

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.View
import android.webkit.WebView
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileReader
import java.io.IOException
import java.io.StringWriter
import java.util.Locale

class FileViewerActivity : AppCompatActivity() {

    private lateinit var textViewContenido: TextView
    private lateinit var imageViewContenido: ImageView
    private lateinit var webViewContenido: WebView

    private var pdfUri: Uri? = null
    private lateinit var choosePdfLauncher: ActivityResultLauncher<String>

    private val pdfBitmapConverter = PdfBitmapConverter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_viewer)

        textViewContenido = findViewById(R.id.textViewContenido)
        imageViewContenido = findViewById(R.id.imageViewContenido)
        webViewContenido = findViewById(R.id.webViewContenido)

        // Inicializar el ActivityResultLauncher
        choosePdfLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            pdfUri = uri
            if (uri != null) {
                renderPdf()
            }
        }

        val rutaArchivo = intent.getStringExtra("rutaArchivo")
        if (rutaArchivo != null && rutaArchivo.isNotEmpty()) {
            mostrarContenidoArchivo(rutaArchivo)
        } else {
            textViewContenido.text = "No se proporcionó ninguna ruta de archivo."
        }
    }

    private fun mostrarContenidoArchivo(rutaArchivo: String) {
        val archivo = File(rutaArchivo)

        when (archivo.extension.lowercase(Locale.getDefault())) {
            "jpg", "jpeg", "png" -> {
                mostrarImagen(archivo)
            }
            "txt", "html", "css", "js", "sh" -> {
                mostrarTexto(archivo)
            }
            "pdf" -> {
                //  Solicitar al usuario que elija el archivo PDF usando el launcher
                //  Esto es necesario porque es posible que no siempre tengas la URI directa
                choosePdfLauncher.launch("application/pdf")
            }
            "doc" -> {
                mostrarDocx(archivo)
            }
            else -> {
                // Manejar otros tipos de archivos o mostrar un mensaje de error
                textViewContenido.text = "Tipo de archivo no compatible para visualización directa."
                imageViewContenido.visibility = View.GONE
                webViewContenido.visibility = View.GONE
                textViewContenido.visibility = View.VISIBLE
            }
        }
    }

    private fun mostrarTexto(archivo: File) {
        imageViewContenido.visibility = View.GONE
        webViewContenido.visibility = View.GONE
        textViewContenido.visibility = View.VISIBLE

        val contenido = StringBuilder()
        try {
            val br = BufferedReader(FileReader(archivo))
            var linea = br.readLine()
            while (linea != null) {
                contenido.append(linea).append("\n")
                linea = br.readLine()
            }
            textViewContenido.text = contenido.toString()
        } catch (e: IOException) {
            textViewContenido.text = "Error al leer el archivo: ${e.message}"
        }
    }

    private fun mostrarImagen(archivo: File) {
        textViewContenido.visibility = View.GONE
        webViewContenido.visibility = View.GONE
        imageViewContenido.visibility = View.VISIBLE

        val bitmap = BitmapFactory.decodeFile(archivo.absolutePath)
        imageViewContenido.setImageBitmap(bitmap)
    }

    private fun renderPdf() {
        pdfUri?.let { uri ->
            lifecycleScope.launch(Dispatchers.Main) {
                val bitmaps = withContext(Dispatchers.IO) {
                    pdfBitmapConverter.pdfToBitmaps(this@FileViewerActivity, uri)
                }
                if (bitmaps.isNotEmpty()) {
                    // Display the first page for simplicity
                    imageViewContenido.visibility = View.VISIBLE
                    webViewContenido.visibility = View.GONE
                    textViewContenido.visibility = View.GONE
                    imageViewContenido.setImageBitmap(bitmaps[0])

                    //  TODO:  Implementar un visor de PDF más completo
                    //  para mostrar todas las páginas (por ejemplo, usando un ViewPager o RecyclerView)
                } else {
                    textViewContenido.text = "Error al renderizar el PDF."
                    imageViewContenido.visibility = View.GONE
                    webViewContenido.visibility = View.GONE
                    textViewContenido.visibility = View.VISIBLE
                }
            }
        } ?: run {
            textViewContenido.text = "No se proporcionó ninguna URI de PDF."
            imageViewContenido.visibility = View.GONE
            webViewContenido.visibility = View.GONE
            textViewContenido.visibility = View.VISIBLE
        }
    }

    private fun mostrarDocx(archivo: File) {
        textViewContenido.visibility = View.GONE
        imageViewContenido.visibility = View.GONE
        webViewContenido.visibility = View.VISIBLE

        try {
            lifecycleScope.launch(Dispatchers.IO) {
                val htmlContent = convertDocxToHtml(archivo)
                withContext(Dispatchers.Main) {
                    webViewContenido.loadDataWithBaseURL(
                        null,
                        htmlContent,
                        "text/html",
                        "UTF-8",
                        null
                    )
                }
            }
        } catch (e: Exception) {
            textViewContenido.text = "Error al leer el archivo DOCX: ${e.message}"
            webViewContenido.visibility = View.GONE
            textViewContenido.visibility = View.VISIBLE
        }
    }

    private fun convertDocxToHtml(archivo: File): String {
        return try {
            val fis = FileInputStream(archivo)
            if (archivo.extension.equals("docx", ignoreCase = true)) {
                val document = XWPFDocument(fis)
                val stringWriter = StringWriter()
                val writer = java.io.PrintWriter(stringWriter)

                // Encabezado HTML con estilos CSS básicos (mejorados)
                writer.println("""
            <html>
            <head>
                <style>
                    body {
                        font-family: sans-serif;
                        padding: 20px;
                        min-height: 100vh;
                        background-color: #ffffff;
                        color: #333333;
                    }
                    p {
                        font-size: 16px;
                        line-height: 1.6;
                        margin-bottom: 12px;
                    }
                    h1 {
                        font-size: 24px;
                        margin-bottom: 16px;
                    }
                    h2 {
                        font-size: 20px;
                        margin-bottom: 14px;
                    }
                    /* ... Agrega más estilos según sea necesario ... */
                </style>
            </head>
            <body>
        """.trimIndent())

                // Itera a través de los párrafos y procesa el contenido
                document.paragraphs.forEach { paragraph ->
                    val text = paragraph.text.trim()
                    if (text.isNotEmpty()) {
                        // Determina el estilo del párrafo (título, normal, etc.)
                        val style = paragraph.style
                        when {
                            style != null && style.contains("Heading 1") -> {
                                writer.println("<h1>${text.replace("\n", "<br>")}</h1>")
                            }
                            style != null && style.contains("Heading 2") -> {
                                writer.println("<h2>${text.replace("\n", "<br>")}</h2>")
                            }
                            else -> {
                                writer.println("<p>${text.replace("\n", "<br>")}</p>")
                            }
                        }
                    }
                }

                // Cerramos el HTML
                writer.println("</body></html>")
                return stringWriter.toString()
            } else {
                //  Esta rama ya no debería alcanzarse si se usa la función mostrarContenidoArchivo modificada
                return "<p>Error: Se esperaba un archivo .docx.</p>"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return "<p>Error al convertir el archivo DOCX a HTML.</p>"
        }
    }

}

class PdfBitmapConverter {

    fun pdfToBitmaps(context: android.content.Context, pdfUri: Uri): List<Bitmap> {
        val bitmaps = mutableListOf<Bitmap>()
        var parcelFileDescriptor: ParcelFileDescriptor? = null
        try {
            parcelFileDescriptor = context.contentResolver.openFileDescriptor(pdfUri, "r")
            parcelFileDescriptor?.let {
                val pdfRenderer = PdfRenderer(it)
                for (i in 0 until pdfRenderer.pageCount) {
                    val page = pdfRenderer.openPage(i)
                    val bitmap = Bitmap.createBitmap(
                        page.width,
                        page.height,
                        Bitmap.Config.ARGB_8888
                    )
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmaps.add(bitmap)
                    page.close()
                }
                pdfRenderer.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                parcelFileDescriptor?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return bitmaps
    }
}
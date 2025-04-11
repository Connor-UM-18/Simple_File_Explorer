#Explorador de Archivos Android

Este es un explorador de archivos básico para Android desarrollado en Kotlin. Permite a los usuarios navegar a través de los directorios de su dispositivo y seleccionar archivos para ver su contenido.

## Características

*     Navegación por el sistema de archivos.
*     Visualización de imágenes (JPG, JPEG, PNG).
*     Visualización de archivos de texto (TXT, HTML, CSS, JS, SH).
*     Renderizado de archivos PDF.
*     Soporte básico para visualizar archivos DOCX (requiere conversión a HTML).
*     Selección de archivos para ver su contenido.
*     Soporte de temas (Azul y Verde).

## Cómo usar

1.      La aplicación solicita permisos de almacenamiento.
2.      El usuario puede navegar a través de los directorios.
3.      Los archivos y directorios se muestran en una lista.
4.      Al hacer clic en un directorio, se navega dentro de él.
5.      Al hacer clic en un archivo, se selecciona.
6.      Hay un botón para ver el archivo seleccionado.

## Consideraciones sobre DOCX

*     La visualización de DOCX se realiza convirtiendo el contenido a HTML.
*     La conversión puede no ser perfecta y puede que no se muestre todo el formato.
*     Se recomienda convertir los archivos DOCX a PDF para una mejor visualización.

## Limitaciones

*     El soporte para DOCX es básico.
*     La aplicación no maneja todos los tipos de archivos.
*     Faltan funciones avanzadas de un explorador de archivos completo (creación de carpetas, eliminación, etc.).

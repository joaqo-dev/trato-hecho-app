package cl.duoc.tratohecho.utils

import cl.duoc.tratohecho.domain.model.Moneda
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Formatos de montos y fechas con configuración chilena
object Formatos {
    private val localeChile = Locale.forLanguageTag("es-CL")

    fun monto(valor: Double, moneda: Moneda = Moneda.CLP): String {
        val formato = NumberFormat.getNumberInstance(localeChile).apply {
            minimumFractionDigits = moneda.decimales
            maximumFractionDigits = moneda.decimales
        }
        return "${moneda.simbolo} ${formato.format(valor)}"
    }

    fun numero(valor: Double, decimales: Int = 2): String {
        val formato = NumberFormat.getNumberInstance(localeChile).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = decimales
        }
        return formato.format(valor)
    }

    fun fecha(millis: Long?): String =
        if (millis == null) "-" else SimpleDateFormat("dd-MM-yyyy", localeChile).format(Date(millis))

    fun fechaHora(millis: Long?): String =
        if (millis == null) "-" else SimpleDateFormat("dd-MM-yyyy HH:mm", localeChile).format(Date(millis))

    fun porcentaje(valor: Double): String = "${(valor * 100).toInt()}%"

    // Para mostrar un Double en un campo de texto sin ".0" sobrante
    fun paraCampo(valor: Double): String =
        if (valor % 1.0 == 0.0) valor.toLong().toString() else valor.toString()
}

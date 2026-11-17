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

    // "12 oct"
    fun fechaCorta(millis: Long?): String =
        if (millis == null) "-" else SimpleDateFormat("d MMM", localeChile).format(Date(millis)).replace(".", "")

    // "10:30"
    fun hora(millis: Long?): String =
        if (millis == null) "" else SimpleDateFormat("HH:mm", localeChile).format(Date(millis))

    // Días de calendario desde hoy hasta la fecha (0 = hoy, 1 = mañana, negativo = ya pasó)
    fun diasHasta(millis: Long, ahora: Long = System.currentTimeMillis()): Int {
        fun inicioDelDia(t: Long) = java.util.Calendar.getInstance().apply {
            timeInMillis = t
            set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
        return Math.round((inicioDelDia(millis) - inicioDelDia(ahora)) / 86_400_000.0).toInt()
    }

    // "martes 17 de noviembre"
    fun fechaLarga(millis: Long): String =
        SimpleDateFormat("EEEE d 'de' MMMM", localeChile).format(Date(millis))

    fun porcentaje(valor: Double): String = "${(valor * 100).toInt()}%"

    // Para mostrar un Double en un campo de texto sin ".0" sobrante
    fun paraCampo(valor: Double): String =
        if (valor % 1.0 == 0.0) valor.toLong().toString() else valor.toString()
}

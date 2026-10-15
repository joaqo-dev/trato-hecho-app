package cl.duoc.tratohecho.utils

// Validación y formato del RUT chileno (módulo 11)
object ValidadorRut {

    fun limpiar(rut: String): String = rut.replace(".", "").replace("-", "").replace(" ", "").uppercase()

    fun calcularDigitoVerificador(cuerpo: String): String {
        var suma = 0
        var multiplicador = 2
        for (digito in cuerpo.reversed()) {
            suma += digito.digitToInt() * multiplicador
            multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
        }
        return when (val resto = 11 - (suma % 11)) {
            11 -> "0"
            10 -> "K"
            else -> resto.toString()
        }
    }

    fun esValido(rut: String): Boolean {
        val limpio = limpiar(rut)
        if (limpio.length < 2) return false
        val cuerpo = limpio.dropLast(1)
        val dv = limpio.takeLast(1)
        if (!cuerpo.all { it.isDigit() } || cuerpo.length !in 7..8) return false
        return calcularDigitoVerificador(cuerpo) == dv
    }

    // 761234560 -> 76.123.456-0
    fun formatear(rut: String): String {
        val limpio = limpiar(rut)
        if (limpio.length < 2) return rut
        val cuerpo = limpio.dropLast(1)
        val dv = limpio.takeLast(1)
        val conPuntos = cuerpo.reversed().chunked(3).joinToString(".").reversed()
        return "$conPuntos-$dv"
    }
}

package cl.duoc.tratohecho.utils

import android.util.Patterns

// Reglas de validación de los formularios. Devuelven el mensaje de error o null si el valor es válido.
// Se usan desde los ViewModel, así la lógica no queda dentro de los componentes visuales.
object Validaciones {

    // "La descripción es obligatoria" / "El nombre es obligatorio"
    private fun obligatorio(campo: String): String =
        if (campo.startsWith("La ")) "$campo es obligatoria" else obligatorio(campo)

    fun requerido(valor: String, campo: String): String? =
        if (valor.isBlank()) obligatorio(campo) else null

    fun largo(valor: String, campo: String, min: Int, max: Int): String? = when {
        valor.isBlank() -> obligatorio(campo)
        valor.trim().length < min -> "$campo debe tener al menos $min caracteres"
        valor.trim().length > max -> "$campo no puede superar $max caracteres"
        else -> null
    }

    fun correo(valor: String): String? = when {
        valor.isBlank() -> "El correo es obligatorio"
        !Patterns.EMAIL_ADDRESS.matcher(valor.trim()).matches() -> "Ingresa un correo válido (ej: nombre@empresa.cl)"
        else -> null
    }

    fun clave(valor: String): String? = when {
        valor.isBlank() -> "La clave es obligatoria"
        valor.length < 6 -> "La clave debe tener al menos 6 caracteres"
        else -> null
    }

    fun claveNueva(valor: String): String? = when {
        valor.isBlank() -> "La clave es obligatoria"
        valor.length < 8 -> "Debe tener al menos 8 caracteres"
        !valor.any { it.isDigit() } -> "Debe incluir al menos un número"
        !valor.any { it.isLetter() } -> "Debe incluir al menos una letra"
        else -> null
    }

    fun rut(valor: String): String? = when {
        valor.isBlank() -> "El RUT es obligatorio"
        !ValidadorRut.esValido(valor) -> "RUT inválido, revisa el dígito verificador"
        else -> null
    }

    // Teléfono chileno: +56 y 9 dígitos (se aceptan espacios)
    fun telefono(valor: String): String? {
        val digitos = valor.replace(" ", "").replace("+", "")
        return when {
            valor.isBlank() -> "El teléfono es obligatorio"
            !digitos.all { it.isDigit() } -> "Solo números (ej: +56 9 1234 5678)"
            digitos.length != 11 || !digitos.startsWith("56") -> "Usa el formato +56 9 1234 5678"
            else -> null
        }
    }

    fun numeroPositivo(valor: String, campo: String): String? {
        val numero = valor.replace(",", ".").toDoubleOrNull()
        return when {
            valor.isBlank() -> obligatorio(campo)
            numero == null -> "$campo debe ser un número"
            numero <= 0 -> "$campo debe ser mayor a 0"
            else -> null
        }
    }

    fun rango(valor: String, campo: String, min: Double, max: Double): String? {
        val numero = valor.replace(",", ".").toDoubleOrNull()
        return when {
            valor.isBlank() -> obligatorio(campo)
            numero == null -> "$campo debe ser un número"
            numero < min || numero > max -> "$campo debe estar entre ${min.toInt()} y ${max.toInt()}"
            else -> null
        }
    }

    // Descuento: en % entre 0 y 100; en monto entre 0 y el monto sobre el que se aplica
    fun descuento(valor: String, esPorcentaje: Boolean, montoBase: Double): String? {
        if (valor.isBlank()) return null
        val numero = valor.replace(",", ".").toDoubleOrNull() ?: return "El descuento debe ser un número"
        return when {
            numero < 0 -> "El descuento no puede ser negativo"
            esPorcentaje && numero > 100 -> "El descuento no puede superar el 100%"
            !esPorcentaje && numero > montoBase -> "El descuento no puede superar el monto"
            else -> null
        }
    }

    fun aNumero(valor: String): Double = valor.replace(",", ".").toDoubleOrNull() ?: 0.0
}

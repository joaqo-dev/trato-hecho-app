package cl.duoc.tratohecho.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

// Muestra el RUT con formato 99.999.999-9 mientras se escribe.
// El campo guarda solo dígitos y K ("761234560"); los puntos y el guion son solo visuales,
// así el cursor no salta al insertar los separadores.
class RutVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val crudo = text.text
        val formateado = if (crudo.length < 2) crudo else ValidadorRut.formatear(crudo)

        // posición en el texto crudo -> posición en el texto con formato
        val crudoAFormato = IntArray(crudo.length + 1)
        var j = 0
        for (i in crudo.indices) {
            while (j < formateado.length && (formateado[j] == '.' || formateado[j] == '-')) j++
            crudoAFormato[i] = j
            j++
        }
        crudoAFormato[crudo.length] = formateado.length

        // posición en el texto con formato -> cuántos caracteres crudos hay antes
        val formatoACrudo = IntArray(formateado.length + 1)
        var contador = 0
        for (k in 0..formateado.length) {
            formatoACrudo[k] = contador
            if (k < formateado.length && formateado[k] != '.' && formateado[k] != '-') contador++
        }

        val mapeo = object : OffsetMapping {
            override fun originalToTransformed(offset: Int) = crudoAFormato[offset.coerceIn(0, crudo.length)]
            override fun transformedToOriginal(offset: Int) = formatoACrudo[offset.coerceIn(0, formateado.length)]
        }
        return TransformedText(AnnotatedString(formateado), mapeo)
    }

    companion object {
        // Deja solo dígitos y K (máximo 9 caracteres: 8 del cuerpo + dígito verificador)
        fun limpiarEntrada(valor: String): String =
            valor.uppercase().filter { it.isDigit() || it == 'K' }.take(9)
    }
}

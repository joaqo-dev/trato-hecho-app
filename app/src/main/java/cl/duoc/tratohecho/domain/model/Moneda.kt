package cl.duoc.tratohecho.domain.model

enum class Moneda(val etiqueta: String, val simbolo: String, val decimales: Int) {
    CLP("Peso chileno", "$", 0),
    USD("Dólar", "US$", 2),
    UF("Unidad de Fomento", "UF", 2)
}

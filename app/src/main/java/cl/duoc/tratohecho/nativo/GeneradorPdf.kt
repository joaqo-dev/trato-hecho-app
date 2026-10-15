package cl.duoc.tratohecho.nativo

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import cl.duoc.tratohecho.data.local.entity.ClienteEntity
import cl.duoc.tratohecho.data.local.entity.ContactoEntity
import cl.duoc.tratohecho.data.local.entity.CotizacionConItems
import cl.duoc.tratohecho.data.local.entity.EmpresaEntity
import cl.duoc.tratohecho.domain.CalculadoraCotizacion
import cl.duoc.tratohecho.domain.model.Moneda
import cl.duoc.tratohecho.domain.model.TipoDescuento
import cl.duoc.tratohecho.utils.Formatos
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

// Genera el documento PDF de la cotización usando PdfDocument (API nativa de Android)
@Singleton
class GeneradorPdf @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val ancho = 595   // A4 en puntos
    private val alto = 842
    private val margen = 40f

    private val negro = Color.rgb(10, 10, 10)
    private val gris = Color.rgb(107, 107, 114)

    private val titulo = Paint().apply { color = negro; textSize = 20f; typeface = Typeface.DEFAULT_BOLD; isAntiAlias = true }
    private val subtitulo = Paint().apply { color = negro; textSize = 12f; typeface = Typeface.DEFAULT_BOLD; isAntiAlias = true }
    private val normal = Paint().apply { color = Color.BLACK; textSize = 10f; isAntiAlias = true }
    private val negrita = Paint().apply { color = Color.BLACK; textSize = 10f; typeface = Typeface.DEFAULT_BOLD; isAntiAlias = true }
    private val pequeno = Paint().apply { color = gris; textSize = 9f; isAntiAlias = true }
    private val derecha = Paint(normal).apply { textAlign = Paint.Align.RIGHT }
    private val derechaNegrita = Paint(negrita).apply { textAlign = Paint.Align.RIGHT }
    private val linea = Paint().apply { color = Color.rgb(228, 228, 231); strokeWidth = 1f }
    private val fondoTabla = Paint().apply { color = Color.rgb(244, 244, 245) }

    fun generar(
        datos: CotizacionConItems,
        empresa: EmpresaEntity?,
        cliente: ClienteEntity?,
        contacto: ContactoEntity?
    ): File {
        val cot = datos.cotizacion
        val moneda = cot.moneda
        val documento = PdfDocument()
        var numeroPagina = 1
        var pagina = documento.startPage(PdfDocument.PageInfo.Builder(ancho, alto, numeroPagina).create())
        var canvas = pagina.canvas
        var y = margen + 10

        // ---------- Encabezado: emisor ----------
        canvas.drawText(empresa?.razonSocial ?: "Empresa", margen, y, titulo)
        canvas.drawText("COTIZACIÓN", ancho - margen, y, Paint(titulo).apply { textAlign = Paint.Align.RIGHT })
        y += 16
        canvas.drawText("RUT ${empresa?.rut ?: "-"} · ${empresa?.giro ?: ""}", margen, y, pequeno)
        canvas.drawText(cot.numeroCompleto, ancho - margen, y, derechaNegrita)
        y += 12
        canvas.drawText("${empresa?.direccion ?: ""} · ${empresa?.telefono ?: ""} · ${empresa?.correo ?: ""}", margen, y, pequeno)
        canvas.drawText("Fecha: ${Formatos.fecha(cot.fechaEnvio ?: System.currentTimeMillis())}", ancho - margen, y, derecha)
        y += 12
        canvas.drawText("Válida por ${cot.diasValidez} días", ancho - margen, y, derecha)
        y += 14
        canvas.drawLine(margen, y, ancho - margen, y, linea)
        y += 20

        // ---------- Cliente ----------
        canvas.drawText("CLIENTE", margen, y, subtitulo)
        y += 16
        canvas.drawText(cliente?.razonSocial ?: cot.clienteNombre, margen, y, negrita)
        y += 13
        canvas.drawText("RUT ${cliente?.rut ?: "-"} · ${cliente?.giro ?: ""}", margen, y, normal)
        y += 13
        canvas.drawText(cliente?.direccion ?: "", margen, y, normal)
        y += 13
        if (contacto != null) {
            canvas.drawText("Atención: ${contacto.nombre} (${contacto.cargo}) · ${contacto.correo}", margen, y, normal)
            y += 13
        }
        canvas.drawText("Vendedor: ${cot.vendedorNombre}", margen, y, pequeno)
        y += 22

        // ---------- Tabla de ítems ----------
        val colCant = 300f
        val colPrecio = 385f
        val colDesc = 455f
        val colTotal = ancho - margen

        fun encabezadoTabla() {
            canvas.drawRect(margen, y - 12, ancho - margen, y + 6, fondoTabla)
            canvas.drawText("Descripción", margen + 4, y, negrita)
            canvas.drawText("Cant.", colCant, y, derechaNegrita)
            canvas.drawText("Precio unit.", colPrecio, y, derechaNegrita)
            canvas.drawText("Desc.", colDesc, y, derechaNegrita)
            canvas.drawText("Subtotal", colTotal - 4, y, derechaNegrita)
            y += 20
        }
        encabezadoTabla()

        datos.items.sortedBy { it.orden }.forEach { item ->
            // Si no cabe en la página, se continúa en una nueva
            if (y > alto - 200) {
                documento.finishPage(pagina)
                numeroPagina++
                pagina = documento.startPage(PdfDocument.PageInfo.Builder(ancho, alto, numeroPagina).create())
                canvas = pagina.canvas
                y = margen + 10
                encabezadoTabla()
            }
            val descripcion = if (item.descripcion.length > 42) item.descripcion.take(40) + "…" else item.descripcion
            canvas.drawText(descripcion, margen + 4, y, normal)
            canvas.drawText("${Formatos.numero(item.cantidad)} ${item.unidad}", colCant, y, derecha)
            canvas.drawText(Formatos.monto(item.precioUnitario, moneda), colPrecio, y, derecha)
            val textoDescuento = when {
                item.descuentoValor <= 0 -> "-"
                item.descuentoTipo == TipoDescuento.PORCENTAJE -> "${Formatos.numero(item.descuentoValor)}%"
                else -> Formatos.monto(item.descuentoValor, moneda)
            }
            canvas.drawText(textoDescuento, colDesc, y, derecha)
            canvas.drawText(Formatos.monto(CalculadoraCotizacion.subtotalItem(item), moneda), colTotal - 4, y, derecha)
            y += 12
            canvas.drawText("${item.impuestoNombre} ${Formatos.numero(item.impuestoPorcentaje)}%", margen + 4, y, pequeno)
            y += 6
            canvas.drawLine(margen, y, ancho - margen, y, linea)
            y += 14
        }

        // ---------- Totales ----------
        val totales = CalculadoraCotizacion.calcular(datos.items, cot.descuentoGlobalTipo, cot.descuentoGlobalValor, moneda)
        y += 6
        fun filaTotal(etiqueta: String, valor: String, destacado: Boolean = false) {
            canvas.drawText(etiqueta, colDesc - 40, y, if (destacado) derechaNegrita else derecha)
            canvas.drawText(valor, colTotal - 4, y, if (destacado) derechaNegrita else derecha)
            y += 15
        }
        filaTotal("Subtotal", Formatos.monto(totales.subtotal, moneda))
        if (totales.descuentoGlobal > 0) filaTotal("Descuento", "- ${Formatos.monto(totales.descuentoGlobal, moneda)}")
        filaTotal("Neto", Formatos.monto(totales.neto, moneda))
        filaTotal("Impuestos", Formatos.monto(totales.impuestos, moneda))
        filaTotal("TOTAL", Formatos.monto(totales.total, moneda), destacado = true)

        if (moneda != Moneda.CLP) {
            y += 4
            canvas.drawText(
                "Valor ${moneda.name}: ${Formatos.monto(cot.tipoCambio)} (${cot.fechaTipoCambio ?: "-"}) · " +
                    "Equivalente aprox.: ${Formatos.monto(totales.total * cot.tipoCambio)}",
                margen, y, pequeno
            )
            y += 14
        }

        // ---------- Notas ----------
        if (cot.notas.isNotBlank()) {
            y += 10
            canvas.drawText("NOTAS", margen, y, subtitulo)
            y += 15
            cot.notas.chunked(95).take(8).forEach { trozo ->
                canvas.drawText(trozo, margen, y, normal)
                y += 13
            }
        }

        canvas.drawText(
            "Documento generado con Trato Hecho · Prismatec Ltda.",
            ancho / 2f, alto - 24f, Paint(pequeno).apply { textAlign = Paint.Align.CENTER }
        )
        documento.finishPage(pagina)

        val carpeta = File(context.cacheDir, "pdfs").apply { mkdirs() }
        val archivo = File(carpeta, "Cotizacion_${cot.numeroCompleto.replace(" ", "_")}.pdf")
        FileOutputStream(archivo).use { documento.writeTo(it) }
        documento.close()
        return archivo
    }
}

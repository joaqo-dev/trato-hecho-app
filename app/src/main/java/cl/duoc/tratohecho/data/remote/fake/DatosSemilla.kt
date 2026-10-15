package cl.duoc.tratohecho.data.remote.fake

import cl.duoc.tratohecho.data.local.entity.ClienteConContactos
import cl.duoc.tratohecho.data.local.entity.ClienteEntity
import cl.duoc.tratohecho.data.local.entity.ContactoEntity
import cl.duoc.tratohecho.data.local.entity.CotizacionConItems
import cl.duoc.tratohecho.data.local.entity.CotizacionEntity
import cl.duoc.tratohecho.data.local.entity.EmpresaEntity
import cl.duoc.tratohecho.data.local.entity.ImpuestoEntity
import cl.duoc.tratohecho.data.local.entity.ItemCotizacionEntity
import cl.duoc.tratohecho.data.local.entity.ProductoEntity
import cl.duoc.tratohecho.data.local.entity.UnidadEntity
import cl.duoc.tratohecho.data.local.entity.UsuarioEntity
import cl.duoc.tratohecho.domain.CalculadoraCotizacion
import cl.duoc.tratohecho.domain.model.EstadoCotizacion
import cl.duoc.tratohecho.domain.model.Moneda
import cl.duoc.tratohecho.domain.model.Rol
import cl.duoc.tratohecho.domain.model.TipoDescuento
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Datos de prueba: dos empresas (tenants) con sus usuarios, catálogo, clientes y cotizaciones.
// Todos los usuarios usan la clave CLAVE_DEMO (ver README).
object DatosSemilla {

    const val CLAVE_DEMO = "Trato123"
    private const val DIA = 24L * 60 * 60 * 1000

    fun crear(ahora: Long): BaseDatosServidor {
        val bd = BaseDatosServidor(fechaSemilla = ahora)
        cargarConstructoraAndes(bd, ahora)
        cargarClimatizaSur(bd, ahora)
        return bd
    }

    // ------------------------------------------------------------------
    // Empresa 1: Constructora Andes SpA (remodelaciones)
    // ------------------------------------------------------------------
    private fun cargarConstructoraAndes(bd: BaseDatosServidor, ahora: Long) {
        val emp = "emp-andes"
        bd.empresas += EmpresaEntity(
            id = emp,
            razonSocial = "Constructora Andes SpA",
            rut = "76.123.456-0",
            giro = "Construcción y remodelaciones",
            direccion = "Av. O'Higgins 1250, Concepción",
            correo = "contacto.andes@example.com",
            telefono = "+56 41 234 5678"
        )
        bd.usuarios += usuario("usr-andes-admin", emp, "Carolina Fuentes", "admin@andes.cl", Rol.ADMINISTRADOR)
        bd.usuarios += usuario("usr-andes-sup", emp, "Pablo Herrera", "supervisor@andes.cl", Rol.SUPERVISOR)
        bd.usuarios += usuario("usr-andes-v1", emp, "Camila Rojas", "camila@andes.cl", Rol.VENDEDOR)
        bd.usuarios += usuario("usr-andes-v2", emp, "Diego Muñoz", "diego@andes.cl", Rol.VENDEDOR)

        bd.unidades += UnidadEntity("und-andes-un", emp, "Unidad", "un")
        bd.unidades += UnidadEntity("und-andes-m2", emp, "Metro cuadrado", "m²")
        bd.unidades += UnidadEntity("und-andes-ml", emp, "Metro lineal", "ml")
        bd.unidades += UnidadEntity("und-andes-hr", emp, "Hora", "hr")
        bd.unidades += UnidadEntity("und-andes-srv", emp, "Servicio", "srv")

        val iva = ImpuestoEntity("imp-andes-iva", emp, "IVA", 19.0)
        val exento = ImpuestoEntity("imp-andes-exento", emp, "Exento", 0.0)
        bd.impuestos += listOf(iva, exento)

        val pintura = ProductoEntity("prd-andes-1", emp, "Pintura muros interiores", "Dos manos, incluye materiales", 6500.0, "und-andes-m2", iva.id)
        val ceramica = ProductoEntity("prd-andes-2", emp, "Instalación de cerámica piso", "Mano de obra y fragüe", 14900.0, "und-andes-m2", iva.id)
        val demolicion = ProductoEntity("prd-andes-3", emp, "Demolición de tabique", "Incluye retiro de escombros", 9000.0, "und-andes-m2", iva.id)
        val visita = ProductoEntity("prd-andes-4", emp, "Visita técnica", "Evaluación en terreno", 35000.0, "und-andes-srv", exento.id)
        val maestro = ProductoEntity("prd-andes-5", emp, "Hora maestro especialista", "Gasfitería o electricidad", 18000.0, "und-andes-hr", iva.id)
        val guardapolvo = ProductoEntity("prd-andes-6", emp, "Guardapolvo MDF instalado", "MDF 70 mm pintado", 4200.0, "und-andes-ml", iva.id)
        bd.productos += listOf(pintura, ceramica, demolicion, visita, maestro, guardapolvo)

        bd.clientes += cliente(
            ClienteEntity("cli-andes-1", emp, "usr-andes-v1", "96.512.340-7", "Inmobiliaria Los Robles SpA",
                "Inmobiliaria", "Los Carrera 455, Concepción", "losrobles@example.com", "+56 9 8765 4321"),
            ContactoEntity("con-andes-1", "cli-andes-1", "Andrea Silva", "Jefa de proyectos", "andrea.silva@example.com", "+56 9 1111 2222")
        )
        bd.clientes += cliente(
            ClienteEntity("cli-andes-2", emp, "usr-andes-v2", "78.345.120-4", "Comercial Biobío Ltda.",
                "Comercio al por menor", "Barros Arana 980, Concepción", "biobio@example.com", "+56 41 222 3344"),
            ContactoEntity("con-andes-2", "cli-andes-2", "Marcelo Torres", "Administrador", "marcelo.torres@example.com", "+56 9 3333 4444")
        )
        bd.clientes += cliente(
            ClienteEntity("cli-andes-3", emp, "usr-andes-v1", "12.345.678-5", "Juan Pérez González",
                "Particular", "Pasaje Los Aromos 12, Talcahuano", "juan.perez@example.com", "+56 9 5555 6666")
        )

        val camila = "usr-andes-v1" to "Camila Rojas"
        val diego = "usr-andes-v2" to "Diego Muñoz"
        bd.cotizaciones += cotizacion(
            "cot-andes-1", emp, "COT-0001", "cli-andes-1", "Inmobiliaria Los Robles SpA", "con-andes-1", camila,
            EstadoCotizacion.FACTURADA, enviadaHace = 40, ahora = ahora,
            items = listOf(item(pintura, 120.0), item(guardapolvo, 60.0, descuento = 10.0))
        )
        bd.cotizaciones += cotizacion(
            "cot-andes-2", emp, "COT-0002", "cli-andes-2", "Comercial Biobío Ltda.", "con-andes-2", diego,
            EstadoCotizacion.ACEPTADA, enviadaHace = 9, ahora = ahora,
            items = listOf(item(ceramica, 45.0), item(demolicion, 12.0), item(visita, 1.0))
        )
        bd.cotizaciones += cotizacion(
            "cot-andes-3", emp, "COT-0003", "cli-andes-3", "Juan Pérez González", null, camila,
            EstadoCotizacion.RECHAZADA, enviadaHace = 12, ahora = ahora,
            items = listOf(item(maestro, 6.0), item(visita, 1.0))
        )
        bd.cotizaciones += cotizacion(
            "cot-andes-4", emp, "COT-0004", "cli-andes-1", "Inmobiliaria Los Robles SpA", "con-andes-1", camila,
            EstadoCotizacion.ENVIADA, enviadaHace = 2, ahora = ahora,
            items = listOf(item(pintura, 300.0, descuento = 5.0), item(ceramica, 80.0)),
            descuentoGlobal = 3.0
        )
        bd.cotizaciones += cotizacion(
            "cot-andes-5", emp, "COT-0005", "cli-andes-2", "Comercial Biobío Ltda.", "con-andes-2", diego,
            EstadoCotizacion.ENVIADA, enviadaHace = 4, ahora = ahora,
            items = listOf(item(ceramica, 150.0)),
            moneda = Moneda.UF, tipoCambio = 39485.65
        )
        bd.cotizaciones += cotizacion(
            "cot-andes-6", emp, "COT-0006", "cli-andes-3", "Juan Pérez González", null, diego,
            EstadoCotizacion.VENCIDA, enviadaHace = 25, ahora = ahora,
            items = listOf(item(demolicion, 8.0), item(pintura, 30.0))
        )
        bd.cotizaciones += cotizacion(
            "cot-andes-7", emp, "COT-0007", "cli-andes-1", "Inmobiliaria Los Robles SpA", "con-andes-1", camila,
            EstadoCotizacion.ACEPTADA, enviadaHace = 3, ahora = ahora,
            items = listOf(item(guardapolvo, 220.0), item(maestro, 4.0))
        )
        bd.correlativos[emp] = 7
    }

    // ------------------------------------------------------------------
    // Empresa 2: Climatiza Sur Ltda. (climatización)
    // ------------------------------------------------------------------
    private fun cargarClimatizaSur(bd: BaseDatosServidor, ahora: Long) {
        val emp = "emp-climatiza"
        bd.empresas += EmpresaEntity(
            id = emp,
            razonSocial = "Climatiza Sur Ltda.",
            rut = "77.654.321-7",
            giro = "Venta e instalación de climatización",
            direccion = "Colón 3200, Talcahuano",
            correo = "ventas.climatiza@example.com",
            telefono = "+56 41 298 7766"
        )
        bd.usuarios += usuario("usr-clima-admin", emp, "Fernanda Lagos", "admin@climatiza.cl", Rol.ADMINISTRADOR)
        bd.usuarios += usuario("usr-clima-sup", emp, "Ignacio Vera", "supervisor@climatiza.cl", Rol.SUPERVISOR)
        bd.usuarios += usuario("usr-clima-v1", emp, "Tomás Navarro", "tomas@climatiza.cl", Rol.VENDEDOR)
        bd.usuarios += usuario("usr-clima-v2", emp, "Valentina Castro", "valentina@climatiza.cl", Rol.VENDEDOR)

        bd.unidades += UnidadEntity("und-clima-un", emp, "Unidad", "un")
        bd.unidades += UnidadEntity("und-clima-ml", emp, "Metro lineal", "ml")
        bd.unidades += UnidadEntity("und-clima-hr", emp, "Hora", "hr")
        bd.unidades += UnidadEntity("und-clima-srv", emp, "Servicio", "srv")

        val iva = ImpuestoEntity("imp-clima-iva", emp, "IVA", 19.0)
        val exento = ImpuestoEntity("imp-clima-exento", emp, "Exento", 0.0)
        bd.impuestos += listOf(iva, exento)

        val split12 = ProductoEntity("prd-clima-1", emp, "Split inverter 12.000 BTU", "Equipo frío/calor", 389990.0, "und-clima-un", iva.id)
        val split18 = ProductoEntity("prd-clima-2", emp, "Split inverter 18.000 BTU", "Equipo frío/calor", 529990.0, "und-clima-un", iva.id)
        val instalacion = ProductoEntity("prd-clima-3", emp, "Instalación estándar split", "Hasta 3 m de cañería", 120000.0, "und-clima-srv", iva.id)
        val mantencion = ProductoEntity("prd-clima-4", emp, "Mantención preventiva", "Limpieza y revisión de gas", 45000.0, "und-clima-srv", iva.id)
        val caneria = ProductoEntity("prd-clima-5", emp, "Cañería de cobre adicional", "Por metro sobre lo estándar", 15000.0, "und-clima-ml", iva.id)
        val tecnico = ProductoEntity("prd-clima-6", emp, "Hora técnico", "Trabajos adicionales", 25000.0, "und-clima-hr", iva.id)
        bd.productos += listOf(split12, split18, instalacion, mantencion, caneria, tecnico)

        bd.clientes += cliente(
            ClienteEntity("cli-clima-1", emp, "usr-clima-v1", "79.111.222-2", "Agrícola Santa Rosa SpA",
                "Agricultura", "Camino a Florida km 8, Concepción", "santarosa@example.com", "+56 9 7777 8888"),
            ContactoEntity("con-clima-1", "cli-clima-1", "Rodrigo Espinoza", "Gerente de operaciones", "rodrigo.espinoza@example.com", "+56 9 1212 3434")
        )
        bd.clientes += cliente(
            ClienteEntity("cli-clima-2", emp, "usr-clima-v2", "76.987.654-5", "Hotel Pastor del Sur SpA",
                "Hotelería", "Caupolicán 510, Concepción", "reservas.pastor@example.com", "+56 41 244 5566"),
            ContactoEntity("con-clima-2", "cli-clima-2", "Sofía Riquelme", "Jefa de mantención", "sofia.riquelme@example.com", "+56 9 5656 7878")
        )
        bd.clientes += cliente(
            ClienteEntity("cli-clima-3", emp, "usr-clima-v1", "77.222.333-1", "Colegio San Andrés Ltda.",
                "Educación", "Lincoyán 1020, Concepción", "administracion.sanandres@example.com", "+56 41 255 6677")
        )
        bd.clientes += cliente(
            ClienteEntity("cli-clima-4", emp, "usr-clima-v2", "15.333.444-7", "María Soto Paredes",
                "Particular", "Los Olmos 77, San Pedro de la Paz", "maria.soto@example.com", "+56 9 9090 1010")
        )

        val tomas = "usr-clima-v1" to "Tomás Navarro"
        val valentina = "usr-clima-v2" to "Valentina Castro"
        bd.cotizaciones += cotizacion(
            "cot-clima-1", emp, "COT-0001", "cli-clima-2", "Hotel Pastor del Sur SpA", "con-clima-2", valentina,
            EstadoCotizacion.FACTURADA, enviadaHace = 30, ahora = ahora,
            items = listOf(item(split18, 6.0, descuento = 8.0), item(instalacion, 6.0))
        )
        bd.cotizaciones += cotizacion(
            "cot-clima-2", emp, "COT-0002", "cli-clima-1", "Agrícola Santa Rosa SpA", "con-clima-1", tomas,
            EstadoCotizacion.ACEPTADA, enviadaHace = 6, ahora = ahora,
            items = listOf(item(split12, 3.0), item(instalacion, 3.0), item(caneria, 10.0)),
            moneda = Moneda.USD, tipoCambio = 948.35
        )
        bd.cotizaciones += cotizacion(
            "cot-clima-3", emp, "COT-0003", "cli-clima-4", "María Soto Paredes", null, valentina,
            EstadoCotizacion.RECHAZADA, enviadaHace = 5, ahora = ahora,
            items = listOf(item(split12, 1.0), item(instalacion, 1.0))
        )
        bd.cotizaciones += cotizacion(
            "cot-clima-4", emp, "COT-0004", "cli-clima-3", "Colegio San Andrés Ltda.", null, tomas,
            EstadoCotizacion.ENVIADA, enviadaHace = 1, ahora = ahora,
            items = listOf(item(mantencion, 14.0, descuento = 10.0))
        )
        bd.cotizaciones += cotizacion(
            "cot-clima-5", emp, "COT-0005", "cli-clima-2", "Hotel Pastor del Sur SpA", "con-clima-2", valentina,
            EstadoCotizacion.ENVIADA, enviadaHace = 3, ahora = ahora,
            items = listOf(item(mantencion, 20.0), item(tecnico, 5.0)),
            descuentoGlobal = 5.0
        )
        bd.correlativos[emp] = 5
    }

    // ------------------------------------------------------------------
    // Funciones de apoyo
    // ------------------------------------------------------------------
    private fun usuario(id: String, empresaId: String, nombre: String, correo: String, rol: Rol) =
        UsuarioServidor(UsuarioEntity(id, empresaId, nombre, correo, rol, activo = true), CLAVE_DEMO)

    private fun cliente(cliente: ClienteEntity, vararg contactos: ContactoEntity) =
        ClienteConContactos(cliente, contactos.toList())

    // Ítem desde un producto del catálogo (precio en CLP, se convierte después si la moneda no es CLP)
    private fun item(producto: ProductoEntity, cantidad: Double, descuento: Double = 0.0) =
        ItemCotizacionEntity(
            id = "",
            cotizacionId = "",
            productoId = producto.id,
            descripcion = producto.nombre,
            unidad = "",
            cantidad = cantidad,
            precioUnitario = producto.precio,
            descuentoTipo = TipoDescuento.PORCENTAJE,
            descuentoValor = descuento,
            impuestoNombre = if (producto.impuestoId.endsWith("exento")) "Exento" else "IVA",
            impuestoPorcentaje = if (producto.impuestoId.endsWith("exento")) 0.0 else 19.0
        )

    private fun cotizacion(
        id: String,
        empresaId: String,
        numero: String,
        clienteId: String,
        clienteNombre: String,
        contactoId: String?,
        vendedor: Pair<String, String>,
        estado: EstadoCotizacion,
        enviadaHace: Int,
        ahora: Long,
        items: List<ItemCotizacionEntity>,
        descuentoGlobal: Double = 0.0,
        moneda: Moneda = Moneda.CLP,
        tipoCambio: Double = 1.0
    ): CotizacionConItems {
        val fechaEnvio = ahora - enviadaHace * DIA
        val itemsFinales = items.mapIndexed { i, it ->
            it.copy(
                id = "$id-item-$i",
                cotizacionId = id,
                unidad = unidadDeProducto(it.productoId),
                precioUnitario = CalculadoraCotizacion.convertirDesdeClp(it.precioUnitario, moneda, tipoCambio),
                orden = i
            )
        }
        val totales = CalculadoraCotizacion.calcular(itemsFinales, TipoDescuento.PORCENTAJE, descuentoGlobal, moneda)
        val respondida = estado in listOf(EstadoCotizacion.ACEPTADA, EstadoCotizacion.RECHAZADA, EstadoCotizacion.FACTURADA)
        return CotizacionConItems(
            CotizacionEntity(
                id = id,
                empresaId = empresaId,
                numero = numero,
                clienteId = clienteId,
                clienteNombre = clienteNombre,
                contactoId = contactoId,
                vendedorId = vendedor.first,
                vendedorNombre = vendedor.second,
                moneda = moneda,
                tipoCambio = tipoCambio,
                fechaTipoCambio = if (moneda == Moneda.CLP) null else SimpleDateFormat("dd-MM-yyyy", Locale("es", "CL")).format(Date(fechaEnvio)),
                diasValidez = 15,
                descuentoGlobalTipo = TipoDescuento.PORCENTAJE,
                descuentoGlobalValor = descuentoGlobal,
                estado = estado,
                total = totales.total,
                fechaCreacion = fechaEnvio - DIA / 2,
                fechaEnvio = fechaEnvio,
                fechaVencimiento = fechaEnvio + 15 * DIA,
                fechaRespuesta = if (respondida) fechaEnvio + 2 * DIA else null
            ),
            itemsFinales
        )
    }

    private fun unidadDeProducto(productoId: String?): String = when {
        productoId == null -> "un"
        productoId in listOf("prd-andes-1", "prd-andes-2", "prd-andes-3") -> "m²"
        productoId in listOf("prd-andes-6", "prd-clima-5") -> "ml"
        productoId in listOf("prd-andes-5", "prd-clima-6") -> "hr"
        productoId in listOf("prd-andes-4", "prd-clima-3", "prd-clima-4") -> "srv"
        else -> "un"
    }
}

package cl.duoc.tratohecho.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import cl.duoc.tratohecho.domain.model.IndicadorEconomico
import cl.duoc.tratohecho.domain.model.Moneda
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.indicadoresDataStore: DataStore<Preferences> by preferencesDataStore(name = "indicadores")

// Último valor conocido del dólar y la UF, para usarlo cuando no hay conexión
@Singleton
class IndicadoresDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private fun claveValor(moneda: Moneda) = doublePreferencesKey("valor_${moneda.name}")
    private fun claveFecha(moneda: Moneda) = stringPreferencesKey("fecha_${moneda.name}")

    suspend fun guardar(indicador: IndicadorEconomico) {
        context.indicadoresDataStore.edit { prefs ->
            prefs[claveValor(indicador.moneda)] = indicador.valor
            prefs[claveFecha(indicador.moneda)] = indicador.fecha
        }
    }

    suspend fun obtener(moneda: Moneda): IndicadorEconomico? {
        val prefs = context.indicadoresDataStore.data.first()
        val valor = prefs[claveValor(moneda)] ?: return null
        val fecha = prefs[claveFecha(moneda)] ?: return null
        return IndicadorEconomico(moneda, valor, fecha, desdeCache = true)
    }
}

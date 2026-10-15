package cl.duoc.tratohecho.di

import cl.duoc.tratohecho.data.local.SesionDataStore
import cl.duoc.tratohecho.data.remote.AuthInterceptor
import cl.duoc.tratohecho.data.remote.MindicadorApi
import cl.duoc.tratohecho.data.remote.TratoHechoApi
import cl.duoc.tratohecho.data.remote.fake.FakeApiInterceptor
import cl.duoc.tratohecho.data.remote.fake.ServidorSimulado
import cl.duoc.tratohecho.nativo.MonitorRed
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // URL de la API de Trato Hecho. Las peticiones las responde FakeApiInterceptor (API simulada).
    private const val URL_TRATO_HECHO = "https://api.tratohecho.cl/v1/"
    private const val URL_MINDICADOR = "https://mindicador.cl/"

    @Provides
    @Singleton
    fun provideTratoHechoApi(
        sesionDataStore: SesionDataStore,
        servidor: ServidorSimulado,
        monitorRed: MonitorRed
    ): TratoHechoApi {
        val cliente = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sesionDataStore))
            .addInterceptor(FakeApiInterceptor(servidor, monitorRed))
            .build()
        return Retrofit.Builder()
            .baseUrl(URL_TRATO_HECHO)
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TratoHechoApi::class.java)
    }

    @Provides
    @Singleton
    fun provideMindicadorApi(): MindicadorApi {
        val cliente = OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder()
            .baseUrl(URL_MINDICADOR)
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MindicadorApi::class.java)
    }
}

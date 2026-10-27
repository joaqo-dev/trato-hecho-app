package cl.duoc.tratohecho.di

import cl.duoc.tratohecho.BuildConfig
import cl.duoc.tratohecho.data.local.SesionDataStore
import cl.duoc.tratohecho.data.remote.AuthInterceptor
import cl.duoc.tratohecho.data.remote.IndicadoresApi
import cl.duoc.tratohecho.data.remote.TratoHechoApi
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

    // URL del servidor de Trato Hecho (carpeta backend/). Se configura en local.properties
    // con tratohecho.api.url (ej: la URL de ngrok); ver app/build.gradle.kts.
    private const val URL_INDICADORES = "https://findic.cl/"

    @Provides
    @Singleton
    fun provideTratoHechoApi(sesionDataStore: SesionDataStore): TratoHechoApi {
        val cliente = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor(sesionDataStore))
            // ngrok muestra una página de advertencia a los navegadores; este encabezado la omite
            .addInterceptor { chain ->
                chain.proceed(chain.request().newBuilder().header("ngrok-skip-browser-warning", "true").build())
            }
            .build()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.URL_API)
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TratoHechoApi::class.java)
    }

    @Provides
    @Singleton
    fun provideIndicadoresApi(): IndicadoresApi {
        val cliente = OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder()
            .baseUrl(URL_INDICADORES)
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IndicadoresApi::class.java)
    }
}

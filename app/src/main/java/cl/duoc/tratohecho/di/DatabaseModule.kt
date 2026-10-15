package cl.duoc.tratohecho.di

import android.content.Context
import androidx.room.Room
import cl.duoc.tratohecho.data.local.AppDatabase
import cl.duoc.tratohecho.data.local.dao.CatalogoDao
import cl.duoc.tratohecho.data.local.dao.ClienteDao
import cl.duoc.tratohecho.data.local.dao.CotizacionDao
import cl.duoc.tratohecho.data.local.dao.EmpresaDao
import cl.duoc.tratohecho.data.local.dao.UsuarioDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "trato_hecho.db")
            // La base local es una copia de la API: si cambia el esquema se vuelve a descargar
            .fallbackToDestructiveMigration(true)
            .build()

    @Provides fun provideEmpresaDao(db: AppDatabase): EmpresaDao = db.empresaDao()
    @Provides fun provideUsuarioDao(db: AppDatabase): UsuarioDao = db.usuarioDao()
    @Provides fun provideClienteDao(db: AppDatabase): ClienteDao = db.clienteDao()
    @Provides fun provideCatalogoDao(db: AppDatabase): CatalogoDao = db.catalogoDao()
    @Provides fun provideCotizacionDao(db: AppDatabase): CotizacionDao = db.cotizacionDao()
}

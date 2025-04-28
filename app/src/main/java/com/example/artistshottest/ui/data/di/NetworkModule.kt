package com.example.artistshottest.ui.data.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.artistshottest.ui.data.database.AppDatabase
import com.example.artistshottest.ui.data.database.NoteDao
import com.example.artistshottest.ui.data.remote.NotesRemoteDataSource
import com.example.artistshottest.ui.data.repositoty.NoteRepository
import com.example.artistshottest.ui.data.repositoty.NoteRepositoryImpl
import com.example.artistshottest.ui.data.service.NotesApiService
import com.example.artistshottest.ui.domain.usecases.FilterNotesByDateUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // Network dependencies
    private const val BASE_URL = "https://jsonplaceholder.typicode.com/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY // Add this for full request/response logging
            })
            .addInterceptor(Interceptor { chain ->
                val request = chain.request()
                Log.d("NETWORK", "Request: ${request.url}")
                chain.proceed(request)
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideNotesApiService(retrofit: Retrofit): NotesApiService {
        return retrofit.create(NotesApiService::class.java)
    }

    // Database dependencies
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "notes-db"
        ).build()
    }

    @Provides
    fun provideNoteDao(db: AppDatabase): NoteDao = db.noteDao()



    // Repository
    @Provides
    fun provideNoteRepository(
        localDataSource: NoteDao,
        remoteDataSource: NotesRemoteDataSource
    ): NoteRepository {
        return NoteRepositoryImpl(localDataSource, remoteDataSource)
    }

    @Provides
    fun provideFilterNotesByDateUseCase(): FilterNotesByDateUseCase {
        return FilterNotesByDateUseCase()
    }
}
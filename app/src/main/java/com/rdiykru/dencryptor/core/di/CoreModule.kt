package com.rdiykru.dencryptor.core.di

import android.content.Context
import com.rdiykru.dencryptor.core.file.FileOperationsManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {
	@Provides
	@Singleton
	fun provideFileOperationsManager(@ApplicationContext context: Context): FileOperationsManager {
		return FileOperationsManager(context)
	}
}
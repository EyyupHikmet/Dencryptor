package com.rdiykru.dencryptor.core.encryption.di

import com.rdiykru.dencryptor.core.encryption.rsa.RSA
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RSAKeyModule {

    @Singleton
    @Provides
    fun provideRSAKeyPair(): RSA.KeyPair {
        // Generates a 2048-bit RSA KeyPair for encryption/decryption
        return RSA.generateKeyPair(2048)
    }
}

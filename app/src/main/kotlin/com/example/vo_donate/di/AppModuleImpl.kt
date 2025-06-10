package com.example.vo_donate.di

import android.content.Context
import dev.kevin.core.network.data.NetworkDataSource
import dev.kevin.core.network.data.NetworkDataSourceImpl
import dev.kevin.core.network.di.NetworkModule
import dev.kevin.data.repository.AuthRepository
import dev.kevin.data.repository.AuthRepositoryImpl
import dev.kevin.data.repository.ProposalRepository
import dev.kevin.data.repository.ProposalRepositoryImpl
import kotlinx.coroutines.CoroutineDispatcher

class AppModuleImpl(
    private val appContext: Context,
    private val onTokenExpired: () -> Unit,
    private val ioDispatcher: CoroutineDispatcher
) : AppModule {

    // NetworkModule is already lazy, which is good for a potentially heavy setup.
    override val networkModule: NetworkModule by lazy {
        NetworkModule(appContext, onTokenExpired)
    }

    // Making other dependencies lazy if they should be singletons
    // within the scope of AppModuleImpl and if their creation is non-trivial.

    override val networkDataSource: NetworkDataSource by lazy {
        // Assuming NetworkDataSourceImpl constructor takes specific dependencies from networkModule
        // e.g., networkModule.provideNetworkApi(), networkModule.provideTokenManager()
        // For this example, let's assume it has been refactored to take these:
        NetworkDataSourceImpl(networkModule, ioDispatcher)
        // If NetworkDataSourceImpl still takes NetworkModule directly:
        // NetworkDataSourceImpl(networkModule)
    }

    override val authRepository: AuthRepository by lazy {
        // Assuming AuthRepositoryImpl takes NetworkDataSource (or specific parts of it)
        AuthRepositoryImpl(networkDataSource)
        // Or if it needs more specific parts:
        // AuthRepositoryImpl(networkModule.provideNetworkApi(), networkModule.provideTokenManager())
    }

    override val proposalRepository: ProposalRepository by lazy {
        // Assuming ProposalRepositoryImpl takes NetworkDataSource
        ProposalRepositoryImpl(networkDataSource)
    }
}
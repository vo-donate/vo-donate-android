package com.example.vo_donate.di

import dev.kevin.core.network.data.NetworkDataSource
import dev.kevin.core.network.di.NetworkModule
import dev.kevin.data.repository.AuthRepository
import dev.kevin.data.repository.ProposalRepository

interface AppModule {
    val networkModule: NetworkModule
    val networkDataSource: NetworkDataSource
    val authRepository: AuthRepository?
    val proposalRepository: ProposalRepository?
}
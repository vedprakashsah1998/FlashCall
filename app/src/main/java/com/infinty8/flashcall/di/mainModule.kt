package com.infinty8.flashcall.di

import com.infinty8.flashcall.repository.MainRepository
import com.infinty8.flashcall.viewmodel.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mainModule = module {

    single { MainRepository(get()) }
    viewModel { MainViewModel(get()) }

}

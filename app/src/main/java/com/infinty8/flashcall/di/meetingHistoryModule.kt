package com.infinty8.flashcall.di

import com.infinty8.flashcall.repository.MeetingHistoryRepository
import com.infinty8.flashcall.viewmodel.MeetingHistoryViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val meetingHistoryModule = module {

    single { MeetingHistoryRepository(get()) }
    viewModel { MeetingHistoryViewModel(get()) }

}

package com.radutopor.viewmodelfactory

import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {
    @Singleton
    @Provides
    fun providesAppName() = "SampleApp"
}

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {
    fun inject(sampleActivity: SampleActivity)
}

val appComponent = DaggerAppComponent.create()
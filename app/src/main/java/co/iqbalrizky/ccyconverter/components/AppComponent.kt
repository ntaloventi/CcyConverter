package co.iqbalrizky.ccyconverter.components

import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {
 fun getActivityComponent(): ActivityComponent
}
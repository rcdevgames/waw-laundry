package net.rcdevgames.wawlaundry.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.rcdevgames.wawlaundry.domain.printer.EscPosPrinterServiceImpl
import net.rcdevgames.wawlaundry.domain.printer.ThermalPrinterService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PrinterModule {

    @Binds
    @Singleton
    abstract fun bindThermalPrinterService(
        impl: EscPosPrinterServiceImpl
    ): ThermalPrinterService
}

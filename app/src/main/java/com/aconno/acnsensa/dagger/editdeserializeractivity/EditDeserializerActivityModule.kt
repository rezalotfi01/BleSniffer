package com.aconno.acnsensa.dagger.editdeserializeractivity

import com.aconno.acnsensa.domain.deserializing.DeserializerRepository
import com.aconno.acnsensa.domain.interactor.deserializing.*
import com.aconno.acnsensa.ui.EditDeserializerActivity
import dagger.Module
import dagger.Provides

/**
 * @author aconno
 */
@Module
class EditDeserializerActivityModule(private val editDeserializerActivity: EditDeserializerActivity) {
    @Provides
    @EditDeserializerActivityScope
    fun provideEditDeserializerActivity() = editDeserializerActivity


    @Provides
    @EditDeserializerActivityScope
    fun provideGetAllDeserializersUseCase(deserializerRepository: DeserializerRepository): GetAllDeserializersUseCase {
        return GetAllDeserializersUseCase(deserializerRepository)
    }


    @Provides
    @EditDeserializerActivityScope
    fun provideAddDeserializerUseCase(deserializerRepository: DeserializerRepository): AddDeserializerUseCase {
        return AddDeserializerUseCase(deserializerRepository)
    }


    @Provides
    @EditDeserializerActivityScope
    fun provideUpdateDeserializerUseCase(deserializerRepository: DeserializerRepository): UpdateDeserializerUseCase {
        return UpdateDeserializerUseCase(deserializerRepository)
    }


    @Provides
    @EditDeserializerActivityScope
    fun provideGetDeserializerByIdUseCase(deserializerRepository: DeserializerRepository): GetDeserializerByIdUseCase {
        return GetDeserializerByIdUseCase(deserializerRepository)
    }
    @Provides
    @EditDeserializerActivityScope
    fun provideGetDeserializerByFilterUseCase(deserializerRepository: DeserializerRepository): GetDeserializerByFilterUseCase {
        return GetDeserializerByFilterUseCase(deserializerRepository)
    }
}
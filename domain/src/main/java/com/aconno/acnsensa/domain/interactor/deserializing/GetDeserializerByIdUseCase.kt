package com.aconno.acnsensa.domain.interactor.deserializing

import com.aconno.acnsensa.domain.deserializing.Deserializer
import com.aconno.acnsensa.domain.deserializing.DeserializerRepository
import com.aconno.acnsensa.domain.interactor.type.SingleUseCaseWithParameter
import com.aconno.acnsensa.domain.interactor.type.SingleUseCaseWithTwoParameters
import io.reactivex.Single

class GetDeserializerByIdUseCase(
        private val deserializerRepository: DeserializerRepository
) : SingleUseCaseWithParameter<Deserializer, Long> {

    override fun execute(parameter: Long): Single<Deserializer> {
        return deserializerRepository.getDeserializerById(parameter)
    }
}
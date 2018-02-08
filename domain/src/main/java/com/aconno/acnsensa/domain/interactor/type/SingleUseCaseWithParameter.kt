package com.aconno.acnsensa.domain.interactor.type

import io.reactivex.Single

interface SingleUseCaseWithParameter<T, in P> {

    fun execute(parameter: P): Single<T>
}
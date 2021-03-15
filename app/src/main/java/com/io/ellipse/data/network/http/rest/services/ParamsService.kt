package com.io.ellipse.data.network.http.rest.services

import com.io.ellipse.data.network.http.rest.entity.params.ParamsBody
import retrofit2.http.Body
import retrofit2.http.POST

interface ParamsService {

    companion object {

        private const val PARAMS_ENDPOINT = "/api/user/health_params"
    }

    @POST(PARAMS_ENDPOINT)
    suspend fun create(@Body params: List<ParamsBody>)
}
package com.example.warhammer40kscanner

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("/api/login")
    fun login(@Body loginRequest: LoginRequest): Call<ResponseBody>

    @GET("/api/subfacciones")
    fun getSubfacciones(): Call<List<Subfaccion>>

    @GET("api/capitulos")
    fun getCapitulos(@Query("idSubfaccion") idSubfaccion: Long): Call<List<Capitulo>>

    @GET("api/models")
    fun getModels(@Query("idChapter") idChapter: Long): Call<List<SelectionEntry>>

    @GET("api/upgrades")
    fun getUpgrades(@Query("idChapter") idChapter: Long): Call<List<SelectionEntry>>

    @GET("api/units")
    fun getUnits(@Query("idChapter") idChapter: Long): Call<List<SelectionEntry>>

    @GET("api/selection/{idSelection}")
    fun getDescriptionsBySelection(@Path("idSelection") idSelection: Long): Call<List<InfoLink>>

    @GET("api/estadisticas/{nombreUnidad}")
    fun getEstadisticasUnidad(@Path("nombreUnidad") nombre: String): Call<StatisticsDTO>

    @GET("api/cost/{idSelection}")
    fun getCostBySelection(@Path("idSelection") idSelection: Long): Call<Int>

}
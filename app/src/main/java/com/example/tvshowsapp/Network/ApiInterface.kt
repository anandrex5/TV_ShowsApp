package com.example.tvshowsapp.Network

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


interface ApiInterface {
    @GET("most-popular?page=1")
    // fun getData(): Call<JsonArray>
    //fun getData(@Query("page") page: Int): Call<JsonArray>
    fun getData(@Query("page") page: Int): Call<JsonObject>

    //for the search icon
    @GET("search")
    fun searchShows(@Query("q") query: String, @Query("page") page: Int): Call<JsonObject>


}
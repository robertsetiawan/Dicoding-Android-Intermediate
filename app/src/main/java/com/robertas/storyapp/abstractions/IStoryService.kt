package com.robertas.storyapp.abstractions

import com.robertas.storyapp.models.network.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface IStoryService {

    @FormUrlEncoded
    @POST("login")
    suspend fun postLogin(
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<UserResponse>


    @FormUrlEncoded
    @POST("register")
    suspend fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<UserResponse>


    @Multipart
    @POST("stories")
    suspend fun postStory(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
    ): Response<StoryResponse>

    @GET("stories")
    suspend fun getAllStories(
        @Header("Authorization") token: String,
        @Query("location") withLocation: Int = 0,
    ): Response<StoryResponse>
}
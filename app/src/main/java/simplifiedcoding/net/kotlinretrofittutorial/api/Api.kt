package simplifiedcoding.net.kotlinretrofittutorial.api

import retrofit2.Call
import retrofit2.http.*
import simplifiedcoding.net.kotlinretrofittutorial.models.DefaultResponse
import simplifiedcoding.net.kotlinretrofittutorial.models.LoginResponse

interface Api {



    @FormUrlEncoded
    @Headers("Content-Type: application/json")
    @POST("auth/login")
    fun userLogin(
//            @Field("authUser") username: String,
//            @Field("password") password: String,
//            @Field("code") code: String,
//            @Field("uuid") uuid: String
//            @FieldMap loginData: Map<String, String>
            @Field("data") Json_loginData: String
    ):Call<LoginResponse>
}
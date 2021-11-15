package simplifiedcoding.net.kotlinretrofittutorial.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.buttonLogin
import kotlinx.android.synthetic.main.activity_main.editTextEmail
import kotlinx.android.synthetic.main.activity_main.editTextPassword
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import simplifiedcoding.net.kotlinretrofittutorial.R
import simplifiedcoding.net.kotlinretrofittutorial.api.RetrofitClient
import simplifiedcoding.net.kotlinretrofittutorial.models.DefaultResponse
import simplifiedcoding.net.kotlinretrofittutorial.models.LoginResponse
import simplifiedcoding.net.kotlinretrofittutorial.storage.SharedPrefManager
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import android.widget.ImageView
import java.net.URL
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import android.util.Base64
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.ByteString.decodeBase64
import java.io.StringReader
import java.security.spec.PKCS8EncodedKeySpec
import javax.crypto.Cipher
import java.security.spec.X509EncodedKeySpec
import java.security.*
import java.security.spec.EncodedKeySpec
import java.util.*
import org.bouncycastle.util.io.pem.PemReader;
import org.json.JSONObject
import android.net.Uri
import kotlinx.android.synthetic.main.activity_login.textViewRegister as textViewRegister1


data class Topic(
    @SerializedName("uuid") var uuid: String,
    @SerializedName("img") var img: String
)


class MainActivity : AppCompatActivity() {

    internal lateinit var uuid: String
    internal lateinit var imgData: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fetchJson()

        buttonLogin.setOnClickListener {

            var username = editTextEmail.text.toString().trim()
            val raw_password = editTextPassword.text.toString().trim()
            val code = editTextCode.text.toString().trim()

            val publicKeyRaw = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBANL378k3RiZHWx5AfJqdH9xRNBmD9wGD2iRe41HdTNF8RUhNnHit5NpMNtGL0NPTSSpPjjI1kJfVorRvaQerUgkCAwEAAQ=="

            val keyBytes: ByteArray
            keyBytes = Base64.decode(publicKeyRaw, Base64.DEFAULT)
            val keySpec = X509EncodedKeySpec(keyBytes)
            val keyFactory = KeyFactory.getInstance("RSA")
            val key = keyFactory.generatePublic(keySpec)

            val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val encrypt = cipher.doFinal(raw_password.toByteArray())
            val password = Base64.encodeToString(encrypt, Base64.DEFAULT)

//            val reader = PemReader(StringReader(publicKeyRaw))
//            val pemObject = reader.readPemObject()
//            val keyBytes: ByteArray = pemObject.content
//            val keySpec: EncodedKeySpec = X509EncodedKeySpec(keyBytes)
//            val keyFactory = KeyFactory.getInstance("RSA")
//            val key = keyFactory.generatePublic(keySpec)
//            val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
//            cipher.init(Cipher.ENCRYPT_MODE, key)
//            val cipherData: ByteArray = cipher.doFinal(raw_password.toByteArray())
//            var password =  Base64.encodeToString(cipherData, Base64.DEFAULT)
//            password = password.replace("\n","")

            if(username.isEmpty()){
                editTextEmail.error = "username required"
                editTextEmail.requestFocus()
                return@setOnClickListener
            }


            if(password.isEmpty()){
                editTextPassword.error = "Password required"
                editTextPassword.requestFocus()
                return@setOnClickListener
            }

            if(code.isEmpty()){
                editTextPassword.error = "Code required"
                editTextPassword.requestFocus()
                return@setOnClickListener
            }

            val json = JSONObject()

            json.put("username", username)
            json.put("password", password)
            json.put("code", code)
            json.put("uuid", uuid)

            val baseUrl = "http://app.alpharobos.com"
            val url = "$baseUrl/auth/login"
            val client = OkHttpClient()
            val JSON: MediaType? = MediaType.parse("application/json")
            val requestBody: RequestBody = RequestBody.create(JSON, json.toString())
            var builder = Request.Builder()
            builder.url(url)
            builder.addHeader("Content-Type","application/json")
                .post(requestBody)

            client.newCall(builder.build()).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call?, e: IOException?) {
                    println("————失败了$e")
                }

                override fun onResponse(call: okhttp3.Call?, response: okhttp3.Response?) {
                    var stA = response?.body()!!.string()
                    var response_Json = JSONObject(stA.substring(stA.indexOf("{"), stA.lastIndexOf("}") + 1))
                    var message = ""
                    if (response_Json.has("message")) {
                        message = response_Json.getString("message")
                    }

                    if (message.equals("验证码错误") || message.equals("验证码不存在或已过期")) {
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        runOnUiThread(){
                            Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                        }
                        startActivity(intent)
                    } else{
                        SharedPrefManager.getInstance(applicationContext).saveUser(username)
                        val intent = Intent(applicationContext, ProfileActivity::class.java)
                        runOnUiThread(){
                            Toast.makeText(applicationContext, "login successful", Toast.LENGTH_LONG).show()
                        }
                        startActivity(intent)
                        println("success")
                    }
                    println("————成功 $stA")
                }
            })
        }

        textViewRegister.setOnClickListener {
            val url = "http://app.alpharobos.com/signup"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }

    }

    @SuppressLint("StaticFieldLeak")
    @Suppress("DEPRECATION")
    inner class DownloadImageFromInternet(var imageView: ImageView) : AsyncTask<String, Void, Bitmap?>() {
        init {
            Toast.makeText(applicationContext, "Please wait, it may take a few minute...",     Toast.LENGTH_SHORT).show()
        }
        override fun doInBackground(vararg urls: String): Bitmap? {
            val imageURL = urls[0]
            var image: Bitmap? = null
            try {
                val `in` = java.net.URL(imageURL).openStream()
                image = BitmapFactory.decodeStream(`in`)
            }
            catch (e: Exception) {
                Log.e("Error Message", e.message.toString())
                e.printStackTrace()
            }
            return image
        }
        override fun onPostExecute(result: Bitmap?) {
            imageView.setImageBitmap(result)
        }
    }

    override fun onStart() {
        super.onStart()

        if(SharedPrefManager.getInstance(this).isLoggedIn){
            val intent = Intent(applicationContext, ProfileActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
        }
    }



    fun fetchJson(){
        val url = "https://app.alpharobos.com/auth/code"
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        with(client) {
            newCall(request).enqueue(object : okhttp3.Callback {
                override fun onResponse(call: okhttp3.Call?, response: okhttp3.Response?) {
                    val body = response?.body()?.string()
                    val gson = Gson()
                    val data = gson.fromJson(body, Topic::class.java)
                    uuid = data.uuid
                    imgData = data.img


                    this@MainActivity.runOnUiThread(java.lang.Runnable {
                        val parts = imgData.split(",")
                        val codeURL = parts[1]
                        val decodedString = Base64.decode(codeURL, Base64.DEFAULT)
                        val decodedImage = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                        imageView.setImageBitmap(decodedImage)
                    })
                }

                override fun onFailure(call: okhttp3.Call?, e: IOException?) {
                    TODO("not implemented")
                }
            })
        }
    }




}

val URL.toBitmap:Bitmap?
    get() {
        return try {
            BitmapFactory.decodeStream(openStream())
        }catch (e: IOException){null}
    }

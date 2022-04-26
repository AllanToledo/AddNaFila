package com.allantoledo.addnafila.activities

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.MutableLiveData
import com.allantoledo.addnafila.requests.JSONCompleteRequest
import com.allantoledo.addnafila.R
import com.allantoledo.addnafila.ui.theme.AddNaFilaTheme
import com.android.volley.Request
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.lang.NullPointerException

import java.util.Base64

class RegisterToken : ComponentActivity() {

    private val errorCauseText = MutableLiveData<String>("")

    private lateinit var clientId: String
    private lateinit var clientSecret: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        clientId = getString(R.string.client_id)
        clientSecret = getString(R.string.client_secret)

        val action: String? = intent?.action
        val data: Uri? = intent?.data

        setContent {
            AddNaFilaTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = colorResource(R.color.blue_gray_900)) {
                    Body()
                }
            }
        }

        var code: String? = null
        var error: String? = null

        if (data != null) {
            code = data.getQueryParameter("code")
            error = data.getQueryParameter("error")
        }

        if (error == null) {
            getUserAccessToken(code!!)
        } else {
            this.errorCauseText.value = error
        }


    }

    private fun getUserAccessToken(code: String) {
        val sharedPref =
            this.getSharedPreferences(getString(R.string.shared_key), Context.MODE_PRIVATE)
        val grantType = "authorization_code"
        val encoded = Base64.getEncoder().encodeToString(("$clientId:$clientSecret").toByteArray())
        Log.v("intent", "enconded $encoded")
        val authorization = "Basic $encoded"
        val url = "https://accounts.spotify.com/api/token"
        val body = JSONObject()
        val headers = JSONObject()
        body.put("code", code)
        body.put("redirect_uri", "app://addnafila/")
        body.put("grant_type", grantType)
        headers.put("Authorization", authorization)
        val request = JSONCompleteRequest(
            Request.Method.POST,
            url,
            body,
            headers,
            { response ->
                Log.v("intent", "Response is: ${response.toString(3)}")
                val token = response.getString("access_token")
                val refreshToken = response.getString("refresh_token")
                Log.v("intent", "Token: $token")
                Log.v("intent", "refreshToken: $refreshToken")
                with(sharedPref.edit()) {
                    putString(getString(R.string.user_token_key), token)
                    putString(getString(R.string.refresh_token_key), refreshToken)
                    commit()
                }
                finish()
            },
            { error ->
                try {
                    errorCauseText.value = error.networkResponse.statusCode.toString()
                } catch (e: NullPointerException) {
                    Log.v("intent", e.message.toString())
                }
                Log.v("intent", "Network Response ${error.networkResponse.statusCode}")
                Log.v("intent", "Network Response ${String(error.networkResponse.data)}")
                Log.v("intent", "Network Response ${error.networkResponse.headers}")
                Log.v("intent", error.cause.toString())
                Log.v("intent", "Request Failed " + error.message.toString())
            }
        )
        val requestQueue = Volley.newRequestQueue(applicationContext)
        requestQueue.add(request)
    }

    @Composable
    fun Body() {
        val text: String? by errorCauseText.observeAsState()
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.fillMaxWidth()) {
                Text(
                    "Se você consegue ler isso, algo deve ter dado errado.",
                    color = colorResource(R.color.blue_gray_800),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 60.sp,
                    letterSpacing = (-5).sp,
                    lineHeight = 50.sp
                )
                Text(
                    text.toString(),
                    color = colorResource(R.color.green_light),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp
                )
            }
            Button(
                onClick = {
                    finish()
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = colorResource(R.color.green_light),
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            ) {
                Text(
                    "Voltar",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
            }
        }
    }

    @Preview
    @Composable
    fun Preview() {
        Surface(color = colorResource(R.color.blue_gray_900)) {
            Body()
        }
    }

}


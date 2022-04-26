package com.allantoledo.addnafila.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.MutableLiveData
import com.allantoledo.addnafila.*
import com.allantoledo.addnafila.R
import com.allantoledo.addnafila.requests.JSONCompleteRequest
import com.allantoledo.addnafila.requests.StringCompleteRequest
import com.allantoledo.addnafila.models.Track
import com.allantoledo.addnafila.ui.theme.AddNaFilaTheme
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.net.URLEncoder
import java.util.*
import kotlin.collections.ArrayList


@ExperimentalMaterialApi
class MainActivity : ComponentActivity() {

    lateinit var sharedPref: SharedPreferences
    lateinit var requestQueue: RequestQueue
    var userToken: String? = null
    var clientToken: String? = null
    private var tracks = MutableLiveData<List<Track>>()
    private var isQueueUpdated = MutableLiveData<Boolean>(false)
    private var isErrorOccurredTryingUpdateQueue = MutableLiveData<Boolean>(false)
    private var errorMesssageTryingUpdateQueue =
        MutableLiveData<String>("Se tu ta lendo isso varios erros ocorreram...")
    var isSearching = MutableLiveData<Boolean>(false)
    private var searchQuery = ""
    private val searchTrackRunnable = Runnable {
        searchSong(searchQuery)
    }
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var clientId: String
    private lateinit var clientSecret: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        clientId = getString(R.string.client_id)
        clientSecret = getString(R.string.client_secret)
        sharedPref = this.getSharedPreferences(getString(R.string.shared_key), Context.MODE_PRIVATE)
        requestQueue = Volley.newRequestQueue(this)

        setContent {
            AddNaFilaTheme {
                Surface(color = colorResource(R.color.blue_gray_900)) {
                    Body()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        userToken = sharedPref.getString(getString(R.string.user_token_key), null)
        clientToken = sharedPref.getString(getString(R.string.client_token_key), null)

        Log.v("intent", "Token: $userToken")
        Log.v("intent", "TokenClient: $clientToken")

        if (clientToken == null) {
            getClientAccessToken()
        }
    }

    private fun searchSong(query: String, tries: Int = 1) {
        if (query.isEmpty()) {
            isSearching.value = false
            return
        }
        Log.v("intent", "Searching: $query")
        val url =
            "https://api.spotify.com/v1/search?type=track&q=${URLEncoder.encode(query)}&market=BR"
        val authorization = "Bearer $clientToken"
        val body = JSONObject()
        val headers = JSONObject()
        headers.put("Authorization", authorization)
        val request = JSONCompleteRequest(
            Request.Method.GET,
            url,
            body,
            headers,
            { response ->
                Log.v("intent", "Response is: ${response.toString(3)}")
                isSearching.value = false
                try {
                    val items: JSONArray = response.getJSONObject("tracks").getJSONArray("items")
                    val auxList: MutableList<Track> = ArrayList()
                    for (i in 0 until items.length()) {
                        val s = items.getJSONObject(i)
                        val artists = s.getJSONArray("artists")
                        val artistsList: MutableList<String> = ArrayList()
                        for (j in 0 until artists.length()) {
                            artistsList.add(artists.getJSONObject(j).getString("name"))
                        }
                        var imageURL: String? = null
                        val images = s.getJSONObject("album").getJSONArray("images")
                        for (j in 0 until images.length()) {
                            val width = images.getJSONObject(j).getInt("width")
                            if (width in 101..599) {
                                imageURL = images.getJSONObject(j).getString("url")
                            }
                        }
                        auxList.add(
                            Track(
                                s.getString("id"),
                                s.getString("name"),
                                artistsList,
                                imageURL
                            )
                        )
                    }
                    tracks.value = auxList
                } catch (e: JSONException) {
                    Log.v("intent", e.message.toString())
                }
            },
            { error ->
                if (tries > 0)
                    getClientAccessToken { searchSong(query, tries - 1) }
                Log.v("intent", "Network Response ${error.networkResponse.statusCode}")
                Log.v("intent", "Network Response ${String(error.networkResponse.data)}")
                Log.v("intent", "Network Response ${error.networkResponse.headers}")
                Log.v("intent", error.cause.toString())
                Log.v("intent", "Request Failed " + error.message.toString())
            }
        )
        request.setShouldCache(false)
        requestQueue.add(request)
    }

    private fun getClientAccessToken(callback: () -> Unit = {}) {
        val grantType = "client_credentials"
        val encoded = Base64.getEncoder().encodeToString(("$clientId:$clientSecret").toByteArray())
        Log.v("intent", "enconded $encoded")
        val authorization = "Basic $encoded"
        val url = "https://accounts.spotify.com/api/token"
        val body = JSONObject()
        val headers = JSONObject()
        body.put("grant_type", grantType)
        headers.put("Authorization", authorization)
        val request = JSONCompleteRequest(
            Request.Method.POST,
            url,
            body,
            headers,
            { response ->
                Log.v("intent", "Response is: ${response.toString(3)}")
                clientToken = response.getString("access_token")
                Log.v("intent", "token: $clientToken")
                with(sharedPref.edit()) {
                    putString(
                        getString(R.string.client_token_key),
                        response.getString("access_token")
                    )
                    apply()
                }
                callback()
            },
            { error ->
                Log.v("intent", "Network Response ${error.networkResponse.statusCode}")
                Log.v("intent", "Network Response ${String(error.networkResponse.data)}")
                Log.v("intent", "Network Response ${error.networkResponse.headers}")
                Log.v("intent", error.cause.toString())
                Log.v("intent", "Request Failed " + error.message.toString())
            }
        )
        request.setShouldCache(false)
        requestQueue.add(request)
    }

    private fun getAuthorizationCode() {
        val scope = "user-modify-playback-state"
        val redirectUri = "app://addnafila/"
        val responseType = "code"
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(
                "https://accounts.spotify.com/authorize?" +
                        "response_type=" + responseType +
                        "&client_id=" + clientId +
                        "&scope=" + scope +
                        "&redirect_uri=" + URLEncoder.encode(redirectUri)
            )
        )
        startActivity(webIntent)
    }

    private fun refreshUserAccessToken(callback: () -> Unit = {}) {
        val grantType = "refresh_token"
        val refreshToken = sharedPref.getString(getString(R.string.refresh_token_key), null)
        val encoded = Base64.getEncoder().encodeToString(("$clientId:$clientSecret").toByteArray())
        Log.v("intent", "enconded $encoded")
        val authorization = "Basic $encoded"
        val url = "https://accounts.spotify.com/api/token"
        val body = JSONObject()
        val headers = JSONObject()
        body.put("refresh_token", refreshToken)
        body.put("grant_type", grantType)
        headers.put("Authorization", authorization)
        val request = JSONCompleteRequest(
            Request.Method.POST,
            url,
            body,
            headers,
            { response ->
                Log.v("intent", "Response is: ${response.toString(3)}")
                val newToken = response.getString("access_token")
                userToken = newToken
                Log.v("intent", "Token: $newToken")
                with(sharedPref.edit()) {
                    putString(getString(R.string.user_token_key), newToken)
                    apply()
                }
                callback()
            },
            { error ->
                Log.v("intent", "Network Response ${error.networkResponse.statusCode}")
                Log.v("intent", "Network Response ${String(error.networkResponse.data)}")
                Log.v("intent", "Network Response ${error.networkResponse.headers}")
                Log.v("intent", error.cause.toString())
                Log.v("intent", "Request Failed " + error.message.toString())
            }
        )
        requestQueue.add(request)
    }

    private fun setTrackToQueue(id: String, tries: Int = 1) {
        val url = "https://api.spotify.com/v1/me/player/queue?uri=spotify:track:$id"
        val authorization = "Bearer $userToken"
        val headers = JSONObject()
        val body = JSONObject()
        headers.put("Authorization", authorization)
        val request = StringCompleteRequest(
            Request.Method.POST,
            url,
            body,
            headers,
            { response ->
                Log.v("intent", "Response is: $response")
                isQueueUpdated.value = true
            },
            { error ->
                Log.v("intent", "Error: $error")
                if (tries > 0 && error.networkResponse.statusCode != 404)
                    refreshUserAccessToken { setTrackToQueue(id, tries - 1) }
                else {
                    isErrorOccurredTryingUpdateQueue.value = true
                    try {
                        errorMesssageTryingUpdateQueue.value = String(error.networkResponse.data)
                    } catch (e: NullPointerException) {
                        e.printStackTrace()
                    }
                }
                Log.v("intent", "Network Response ${error.networkResponse.statusCode}")
                Log.v("intent", "Network Response ${String(error.networkResponse.data)}")
                Log.v("intent", "Network Response ${error.networkResponse.headers}")
                Log.v("intent", error.cause.toString())
                Log.v("intent", "Request Failed " + error.message.toString())
            }
        )
        request.setShouldCache(false)
        requestQueue.add(request)
    }

    @ExperimentalMaterialApi
    @Composable
    fun Body() {
        var queryValue by remember { mutableStateOf("") }
        val resultTrackList by tracks.observeAsState()
        var showConfirmDialog by remember { mutableStateOf(false) }
        val showSuccessDialog by isQueueUpdated.observeAsState()
        val showErrorDialog by isErrorOccurredTryingUpdateQueue.observeAsState()
        val errorMessageQueue by errorMesssageTryingUpdateQueue.observeAsState()
        val showSearchIndication by isSearching.observeAsState()
        var songName by remember { mutableStateOf("") }
        var songId by remember { mutableStateOf("") }

        if (showConfirmDialog) {
            ConfirmDialog(
                onDismissRequest = { showConfirmDialog = false },
                onConfirm = {
                    showConfirmDialog = false
                    setTrackToQueue(songId)
                },
                message = "Adicionar $songName na fila?"
            )
        }

        if (showSuccessDialog == true) {
            SuccessDialog(
                onDismissRequest = { isQueueUpdated.value = false },
                message = "$songName foi adicinado na fila!"
            )
        }

        if (showErrorDialog == true)
            ErrorDialog(
                onDismissRequest = { isQueueUpdated.value = false },
                errorMessage = "Não foi possível adicionar $songName na lista." +
                        "\n\n${errorMessageQueue.toString()}.",
                actionText = "Re-autenticar",
                onAction = {
                    isErrorOccurredTryingUpdateQueue.value = false
                    getAuthorizationCode()
                }
            )

        Box(
            Modifier
                .padding(PaddingValues(start = 16.dp, end = 16.dp))
                .fillMaxSize(),
        ) {
            LazyColumn() {
                if (resultTrackList != null && resultTrackList!!.isNotEmpty())
                    items(resultTrackList!!.size) { i ->
                        if (i == 0)
                            Spacer(Modifier.height(68.dp))
                        val song = resultTrackList!![i]
                        SongCard(song = song, onClick = {
                            if (userToken == null) {
                                getAuthorizationCode()
                            } else {
                                showConfirmDialog = true
                                songName = song.name
                                songId = song.id
                            }
                        })
                        if (i == (resultTrackList!!.size - 1))
                            Spacer(Modifier.height(48.dp))
                    }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(74.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                colorResource(R.color.blue_gray_900),
                                Color.Transparent
                            )
                        )
                    )
            )
            Column(Modifier.fillMaxWidth()) {
                LoadingTextField(
                    onValueChange = { value ->
                        queryValue = value
                        searchQuery = value
                        isSearching.value = true
                        handler.removeCallbacks(searchTrackRunnable)
                        handler.postDelayed(searchTrackRunnable, 2000)
                    },
                    value = queryValue,
                    loadingColor = colorResource(R.color.white),
                    showSearchIndication = showSearchIndication == true
                )

                Spacer(Modifier.padding(4.dp))
                if (resultTrackList == null || resultTrackList!!.isEmpty()) {
                    Text(
                        "Pesquise \numa \nmúsica \ne \nadicione \nna \nfila".uppercase(),
                        color = colorResource(R.color.green_light),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 68.sp,
                        lineHeight = 50.sp,
                        letterSpacing = (-8).sp
                    )
                }
            }
        }
    }

}


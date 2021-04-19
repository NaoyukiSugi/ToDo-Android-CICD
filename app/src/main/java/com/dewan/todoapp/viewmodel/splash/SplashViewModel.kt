package com.dewan.todoapp.viewmodel.splash

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.*
import com.dewan.todoapp.BuildConfig
import com.dewan.todoapp.model.local.AppPreferences
import com.dewan.todoapp.model.remote.Networking
import com.dewan.todoapp.model.repository.ValidateTokenRepository
import com.dewan.todoapp.util.ResultSet
import com.dewan.todoapp.util.network.NetworkHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import timber.log.Timber

class SplashViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val TAG = "SplashViewModel"
    }

    private val networkService = Networking.create(BuildConfig.BASE_URL)
    private val validateTokenRepository = ValidateTokenRepository(networkService)
    private var sharedPreferences: SharedPreferences =
        application.getSharedPreferences("com.dewan.todoapp.pref", Context.MODE_PRIVATE)
    private var appPreferences: AppPreferences
    var token = MutableLiveData<String>()
    var context: Context = application
    val isNetworkConnected: MutableLiveData<Boolean> = MutableLiveData()
    val progress: MutableLiveData<Boolean> = MutableLiveData()
    val tokenResponse: MutableLiveData<String> = MutableLiveData()
    val errorMsgString: MutableLiveData<String> = MutableLiveData()
    val errorMsgInt: MutableLiveData<Int> = MutableLiveData()

    init {
        appPreferences = AppPreferences(sharedPreferences)
        token.value = appPreferences.getAccessToken()
    }

    fun validateToken() {
        viewModelScope.launch {
            try {
                if (NetworkHelper.isNetworkConnected(context)) {
                    validateTokenRepository.validateToken(token.value.toString())
                        .flowOn(Dispatchers.IO)
                        .collect { result ->
                            when (result) {
                                ResultSet.Loading -> {
                                    progress.value = true
                                }
                                is ResultSet.Success -> {
                                    tokenResponse.value = result.data.toString()
                                    progress.value = false
                                }
                                is ResultSet.Error -> {
                                    if (result.error != null) {
                                        errorMsgString.value = result.error.message
                                    } else {
                                        errorMsgInt.value = result.errorMsg
                                    }
                                }

                            }
                        }
                } else {
                    Timber.d("No internet connection!")
                    isNetworkConnected.value = false
                }
            } catch (e: java.lang.Exception) {
                Timber.e(e)

            }

        }
    }

}

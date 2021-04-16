package com.dewan.todoapp.model.repository

import com.dewan.todoapp.R
import com.dewan.todoapp.model.remote.NetworkService
import com.dewan.todoapp.util.ResultSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.lang.Exception
import java.net.HttpURLConnection

class ValidateTokenRepository(private val networkService: NetworkService) {

    suspend fun validateToken(token: String): Flow<ResultSet<String>> = flow {
        try {
            val response = networkService.validateToken(token)
            if (response.isSuccessful) {
                emit(ResultSet.Success(response.body()?.message))
            } else {
                emit(
                    when (response.code()) {
                        HttpURLConnection.HTTP_UNAUTHORIZED -> {
                            ResultSet.Error(
                                errorMsg = R.string.error_code_401,
                                code = response.code()
                            )
                        }
                        HttpURLConnection.HTTP_BAD_REQUEST -> {
                            ResultSet.Error(
                                errorMsg = R.string.error_code_400,
                                code = response.code()
                            )
                        }
                        HttpURLConnection.HTTP_NOT_FOUND -> {
                            ResultSet.Error(
                                errorMsg = R.string.error_code_404,
                                code = response.code()
                            )
                        }
                        HttpURLConnection.HTTP_SERVER_ERROR -> {
                            ResultSet.Error(
                                errorMsg = R.string.error_code_500,
                                code = response.code()
                            )
                        }
                        else -> {
                            ResultSet.Error(
                                errorMsg = R.string.error_code_unknown,
                                code = response.code()
                            )
                        }
                    }
                )
            }
        } catch (e: Exception) {
            emit(ResultSet.Error(e))
        }
    }
}

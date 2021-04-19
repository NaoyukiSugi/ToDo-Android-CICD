package com.dewan.todoapp.model.repository

import com.dewan.todoapp.R
import com.dewan.todoapp.model.local.db.AppDatabase
import com.dewan.todoapp.model.local.entity.TaskEntity
import com.dewan.todoapp.model.remote.NetworkService
import com.dewan.todoapp.model.remote.request.todo.EditTaskRequest
import com.dewan.todoapp.model.remote.response.todo.EditTaskResponse
import com.dewan.todoapp.util.ResultSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.lang.Exception
import java.net.HttpURLConnection

class EditTaskRepository(
    private val networkService: NetworkService,
    private val appDatabase: AppDatabase
) {

    suspend fun editTask(
        token: String,
        editTaskRequest: EditTaskRequest
    ): Flow<ResultSet<EditTaskResponse>> = flow {
        emit(ResultSet.Loading)
        try {
            val response = networkService.editTask(token, editTaskRequest)
            if (response.isSuccessful) {
                emit(ResultSet.Success(response.body()))
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


    suspend fun updateTask(taskEntity: TaskEntity): Flow<ResultSet<Int>> =
        flow {
            emit(ResultSet.Loading)

            try {
                val result = appDatabase.taskDao().update(taskEntity)
                emit(ResultSet.Success(result))

            } catch (e: Exception) {
                emit(ResultSet.Error(e))
            }
        }
}

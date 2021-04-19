package com.dewan.todoapp.model.repository

import com.dewan.todoapp.R
import com.dewan.todoapp.model.local.db.AppDatabase
import com.dewan.todoapp.model.local.entity.TaskEntity
import com.dewan.todoapp.model.remote.NetworkService
import com.dewan.todoapp.model.remote.request.todo.AddTaskRequest
import com.dewan.todoapp.util.ResultSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.lang.Exception
import java.net.HttpURLConnection

class AddTaskRepository(
    private val networkService: NetworkService,
    private val appDatabase: AppDatabase
) {

    suspend fun addTask(token: String, addTaskRequest: AddTaskRequest): Flow<ResultSet<Boolean>> =
        flow {
            emit(ResultSet.Loading)
            try {
                val response = networkService.addTask(token, addTaskRequest)
                if (response.isSuccessful) {
                    emit(ResultSet.Success(true))
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

//    suspend fun addTaskToDb(taskEntity: TaskEntity): Flow<ResultSet<Long>> =
//        flow {
//            emit(ResultSet.Loading)
//            try {
//                val result = appDatabase.taskDao().insert(taskEntity)
//                emit(ResultSet.Success(result))
//            } catch (e: Exception) {
//                emit(ResultSet.Error(e))
//            }
//        }
//
//    suspend fun getAllTaskFromDbs() = appDatabase.taskDao().getAllTaskFromDdFlow()

}

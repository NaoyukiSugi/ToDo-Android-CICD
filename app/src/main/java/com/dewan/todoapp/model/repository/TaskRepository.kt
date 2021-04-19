package com.dewan.todoapp.model.repository

import com.dewan.todoapp.R
import com.dewan.todoapp.model.local.db.AppDatabase
import com.dewan.todoapp.model.local.entity.TaskEntity
import com.dewan.todoapp.model.remote.NetworkService
import com.dewan.todoapp.util.ResultSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.net.HttpURLConnection

class TaskRepository(
    private val networkService: NetworkService,
    private val appDatabase: AppDatabase
) {

    suspend fun getAllTask(token: String) = networkService.getAllTask(token)

    //suspend fun getTaskById(token: String, maxId: String) = networkService.getTAskById(token, maxId)

    suspend fun getTaskById(token: String, maxId: String): Flow<ResultSet<List<TaskEntity>>> =
        flow {
            emit(ResultSet.Loading)

            try {
                val response = networkService.getTaskById(token, maxId)
                if (response.isSuccessful) {
                    val result = response.body()?.map { taskResponse ->
                        TaskEntity(
                            taskId = taskResponse.id,
                            title = taskResponse.title,
                            body = taskResponse.body,
                            note = taskResponse.note,
                            status = taskResponse.status,
                            userId = taskResponse.userId,
                            createdAt = taskResponse.createdAt,
                            updatedAt = taskResponse.updatedAt
                        )
                    }?.toList()
                    emit(ResultSet.Success(result))
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

    suspend fun insert(taskEntity: TaskEntity) = appDatabase.taskDao().insert(taskEntity)

    suspend fun insertMany(taskEntity: List<TaskEntity>) =
        appDatabase.taskDao().insertMany(taskEntity)

    suspend fun update(taskEntity: TaskEntity) = appDatabase.taskDao().update(taskEntity)

    suspend fun delete(taskEntity: TaskEntity) = appDatabase.taskDao().delete(taskEntity)

    //suspend fun getAllTaskFromDb() = appDatabase.taskDao().getAllTaskFromDd()

    suspend fun getAllTaskFromDb(): ResultSet<List<TaskEntity>> {

        return try {
            ResultSet.Success(appDatabase.taskDao().getAllTaskFromDd())
        } catch (e: Exception) {
            return ResultSet.Error(e)
        }
    }

    suspend fun getAllTaskFromDbFlow() = appDatabase.taskDao().getAllTaskFromDdFlow()

    //suspend fun getMaxId() = appDatabase.taskDao().getMaxTaskId()

    suspend fun getMaxId(): Flow<ResultSet<Int>> = flow {
        emit(ResultSet.Loading)
        try {
            emit(ResultSet.Success(appDatabase.taskDao().getMaxTaskId()))
        } catch (e: Exception) {
            emit(ResultSet.Error(e))
        }
    }

}

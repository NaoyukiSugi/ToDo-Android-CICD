package com.dewan.todoapp.model.repository

import com.dewan.todoapp.model.local.db.AppDatabase
import com.dewan.todoapp.model.local.entity.TaskEntity
import com.dewan.todoapp.model.remote.NetworkService
import com.dewan.todoapp.model.remote.request.todo.AddTaskRequest
import com.dewan.todoapp.util.ResultSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.lang.Exception

class AddTaskRepository(
    private val networkService: NetworkService,
    private val appDatabase: AppDatabase
) {

    suspend fun addTaskToDb(taskEntity: TaskEntity): Flow<ResultSet<Long>> =
        flow {
            emit(ResultSet.Loading)
            try {
                val result = appDatabase.taskDao().insert(taskEntity)
                emit(ResultSet.Success(result))
            } catch (e: Exception) {
                emit(ResultSet.Error(e))
            }
        }

    suspend fun getAllTaskFromDbs() = appDatabase.taskDao().getAllTaskFromDdFlow()

}

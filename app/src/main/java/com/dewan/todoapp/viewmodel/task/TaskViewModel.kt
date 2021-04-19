package com.dewan.todoapp.viewmodel.task

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.*
import com.dewan.todoapp.BuildConfig
import com.dewan.todoapp.model.local.AppPreferences
import com.dewan.todoapp.model.local.db.AppDatabase
import com.dewan.todoapp.model.local.entity.TaskEntity
import com.dewan.todoapp.model.remote.Networking
import com.dewan.todoapp.model.remote.request.todo.AddTaskRequest
import com.dewan.todoapp.model.repository.AddTaskRepository
import com.dewan.todoapp.util.ResultSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber


class TaskViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val TAG = "TaskViewModel"
    }

    private val networkService = Networking.create(BuildConfig.BASE_URL)
    private var addTaskRepository: AddTaskRepository
    private var sharesPreferences =
        application.getSharedPreferences(BuildConfig.PREF_NAME, Context.MODE_PRIVATE)
    private var appPreferences: AppPreferences
    private var token: String = ""

    val userId: MutableLiveData<Int> = MutableLiveData()
    val progress: MutableLiveData<Boolean> = MutableLiveData()
    val isSuccess: MutableLiveData<Boolean> = MutableLiveData()
    val isError: MutableLiveData<String> = MutableLiveData()

    init {
        addTaskRepository = AddTaskRepository(networkService, AppDatabase.getInstance(application))
        appPreferences = AppPreferences(sharesPreferences)
        token = appPreferences.getAccessToken().toString()
        userId.value = appPreferences.getUserId()

//        addAllTaskFromDbs()
    }

//    fun addTaskToDb(taskEntity: TaskEntity) {
//        viewModelScope.launch {
//            addTaskRepository.addTaskToDb(taskEntity)
//                .flowOn(Dispatchers.IO)
//                .collect { result ->
//                    when (result) {
//                        ResultSet.Loading -> {
//                            progress.value = true
//                        }
//                        is ResultSet.Success -> {
//                            isSuccess.value = true
//                            progress.value = false
//                            Timber.d(result.data.toString())
//                        }
//                        is ResultSet.Error -> {
//                            isError.value = result.error.toString()
//                            progress.value = false
//                        }
//                    }
//                }
//        }
//    }

//    private fun addAllTaskFromDbs() {
//        viewModelScope.launch {
//            addTaskRepository.getAllTaskFromDbs()
//                .flowOn(Dispatchers.IO)
//                .collect { result ->
//                    result.forEach { task ->
//                        Timber.d(task.toString())
//                    }
//                }
//        }
//    }

    fun addTask(addTaskRequest: AddTaskRequest) {
        viewModelScope.launch {
            addTaskRepository.addTask(token, addTaskRequest)
                .flowOn(Dispatchers.IO)
                .collect { result ->
                    when (result) {
                        is ResultSet.Loading -> {
                            progress.value = true
                        }
                        is ResultSet.Success -> {
                            isSuccess.value = result.data as Boolean
                            progress.value = false
                        }
                        is ResultSet.Error -> {
                            isError.value = result.error.toString()
                            progress.value = false
                        }
                    }
                }
        }
    }
}

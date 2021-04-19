package com.dewan.todoapp.viewmodel.task

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dewan.todoapp.BuildConfig
import com.dewan.todoapp.model.local.AppPreferences
import com.dewan.todoapp.model.local.db.AppDatabase
import com.dewan.todoapp.model.local.entity.TaskEntity
import com.dewan.todoapp.model.remote.Networking
import com.dewan.todoapp.model.remote.request.todo.EditTaskRequest
import com.dewan.todoapp.model.remote.response.todo.EditTaskResponse
import com.dewan.todoapp.model.repository.EditTaskRepository
import com.dewan.todoapp.util.ResultSet
import com.dewan.todoapp.util.network.NetworkHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber

class EditTaskViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val TAG = "EditTaskViewModel"
    }

    private val networkService = Networking.create(BuildConfig.BASE_URL)
    private var editTaskRepository: EditTaskRepository
    private var sharesPreferences =
        application.getSharedPreferences(BuildConfig.PREF_NAME, Context.MODE_PRIVATE)
    private var appPreferences: AppPreferences
    private var token: String = ""
    private val context: Context
    val userId: MutableLiveData<Int> = MutableLiveData()

    val id: MutableLiveData<String> = MutableLiveData()
    val taskId: MutableLiveData<String> = MutableLiveData()
    val title: MutableLiveData<String> = MutableLiveData()
    val body: MutableLiveData<String> = MutableLiveData()
    val note: MutableLiveData<String> = MutableLiveData()
    val status: MutableLiveData<String> = MutableLiveData()
    val index: MutableLiveData<Int> = MutableLiveData()
    val taskList: ArrayList<String> = ArrayList()
    val loading: MutableLiveData<Boolean> = MutableLiveData()
    val isSuccess: MutableLiveData<Boolean> = MutableLiveData()
    val isError: MutableLiveData<String> = MutableLiveData()

    init {
        context = application
        editTaskRepository =
            EditTaskRepository(networkService, AppDatabase.getInstance(application))

        appPreferences = AppPreferences(sharesPreferences)
        token = appPreferences.getAccessToken().toString()
        userId.value = appPreferences.getUserId()
    }

    fun getIndexFromTaskList() {
        index.value = taskList.indexOf(status.value)
    }

    /*
    this is to update the task in api
     */
    fun editTask() {
        viewModelScope.launch {
            if (NetworkHelper.isNetworkConnected(context)) {
                editTaskRepository.editTask(
                    token, EditTaskRequest(
                        taskId.value!!.toInt(),
                        userId.value.toString(),
                        title.value.toString(),
                        body.value.toString(),
                        note.value!!.toString(),
                        status.value.toString()
                    )
                )
                    .flowOn(Dispatchers.IO)
                    .collect { result ->
                        when (result) {
                            is ResultSet.Loading -> {
                                loading.value = true
                            }
                            is ResultSet.Success -> {
                                isSuccess.value = true
                                loading.value = false
                                updateTask(result.data as EditTaskResponse)
                            }
                            is ResultSet.Error -> {
                                isError.value = result.error.toString()
                                loading.value = false
                                Timber.e(result.error)
                            }
                        }
                    }
            } else {
                Timber.d("No network connection found!")
            }
        }
    }

    /*
    this is to update the task in local db
     */
    private suspend fun updateTask(editTaskResponse: EditTaskResponse) {

        coroutineScope {
            editTaskRepository.updateTask(
                TaskEntity(
                    id = id.value!!.toLong(),
                    taskId = editTaskResponse.id,
                    title = editTaskResponse.title,
                    body = editTaskResponse.body,
                    note = editTaskResponse.note,
                    status = editTaskResponse.status,
                    userId = editTaskResponse.userId.toInt(),
                    createdAt = editTaskResponse.createdAt,
                    updatedAt = editTaskResponse.updatedAt
                )
            )
                .flowOn(Dispatchers.IO)
                .collect { result ->
                    when (result) {
                        ResultSet.Loading -> {
                            loading.value = true
                        }
                        is ResultSet.Success -> {
                            isSuccess.value = true
                            loading.value = false
                            Timber.d(result.data.toString())
                        }
                        is ResultSet.Error -> {
                            isError.value = result.error.toString()
                            loading.value = false
                        }
                    }

                }

        }
    }
}

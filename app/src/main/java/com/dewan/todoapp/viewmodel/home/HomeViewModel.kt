package com.dewan.todoapp.viewmodel.home

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.*
import com.dewan.todoapp.BuildConfig
import com.dewan.todoapp.R
import com.dewan.todoapp.model.local.AppPreferences
import com.dewan.todoapp.model.local.db.AppDatabase
import com.dewan.todoapp.model.local.entity.TaskEntity
import com.dewan.todoapp.model.remote.Networking
import com.dewan.todoapp.model.remote.response.todo.TaskResponse
import com.dewan.todoapp.model.repository.TaskRepository
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
import kotlin.math.max

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val TAG = "HomeViewModel"
    }

    private val networkService = Networking.create(BuildConfig.BASE_URL)
    private var taskRepository: TaskRepository
    private var sharesPreferences =
        application.getSharedPreferences(BuildConfig.PREF_NAME, Context.MODE_PRIVATE)
    private var appPreferences: AppPreferences
    private var token: String
    private val taskList: MutableLiveData<List<TaskResponse>> = MutableLiveData()
    val taskListFromDb: MutableLiveData<List<TaskEntity>> = MutableLiveData()
    val progress: MutableLiveData<Boolean> = MutableLiveData()

    val isError: MutableLiveData<String> = MutableLiveData()
    val errorMsgInt: MutableLiveData<Int> = MutableLiveData()
    val errorMsgString: MutableLiveData<String> = MutableLiveData()

    private val maxRecId: MutableLiveData<String> = MutableLiveData()
    private var context: Context? = null


    init {

        taskRepository = TaskRepository(networkService, AppDatabase.getInstance(application))
        appPreferences = AppPreferences(sharesPreferences)
        token = appPreferences.getAccessToken().toString()
        context = application

        /*

         */
        getMaxIdFromDb()
    }

    fun getAllTask() = liveData {
        try {
            progress.postValue(true)
            val data = taskRepository.getAllTask(token)

            if (data.code() == 200) {
                taskList.value = data.body()

                /*
                insert task list to local database
                 */
                for (task in taskList.value!!) {

                    val id = taskRepository.insert(
                        TaskEntity(
                            taskId = task.id,
                            title = task.title,
                            body = task.body,
                            status = task.status,
                            userId = task.userId,
                            createdAt = task.createdAt,
                            updatedAt = task.updatedAt
                        )
                    )
                    Log.d(TAG, "New record inserted to local db. RowId: $id")
                }

            }
            emit(taskList.value)
            progress.postValue(false)

        } catch (httpException: HttpException) {
            Log.e(TAG, httpException.toString())
            isError.value = httpException.toString()

        } catch (exception: Exception) {
            Log.e(TAG, exception.toString())
            isError.value = exception.toString()
        }
    }

    private fun getMaxIdFromDb() {
        viewModelScope.launch() {
            Timber.d("getMaxIdFromDb: ${Thread.currentThread().name}")
            taskRepository.getMaxId()
                .flowOn(Dispatchers.IO)
                .collect { result ->
                    when (result) {
                        is ResultSet.Loading -> {
                            progress.value = true
                        }
                        is ResultSet.Success -> {
                            // get the task y id from API
                            getTaskById(result.data.toString())

                            // set the max rec id value
                            maxRecId.value = result.data.toString()

                        }
                        is ResultSet.Error -> {
                            progress.value = false
                        }
                    }

                }
        }

    }

    /*
     get the task by id from API
    */
    private suspend fun getTaskById(maxId: String) {

        coroutineScope {

            Timber.d("getTaskById: ${Thread.currentThread().name}")

            try {
                if (NetworkHelper.isNetworkConnected(context!!)) {

                    taskRepository.getTaskById(token, maxId)
                        .flowOn(Dispatchers.IO)
                        .collect { result ->
                            when (result) {
                                is ResultSet.Loading -> {
                                    progress.value = true
                                }
                                is ResultSet.Success -> {
                                    progress.value = false

                                    val data = result.data as List<*>

                                    data.let {
                                        //insert data to local DB
                                        val taskList = it.filterIsInstance<TaskEntity>()
                                        val insertResult = taskRepository.insertMany(taskList)
                                        Timber.d("$insertResult")

                                    }
                                    /*
                                    get the task from db
                                    */
                                    getTaskFromDb()
                                }
                                is ResultSet.Error -> {
                                    progress.value = false
                                }
                            }
                        }
                } else {
                    Timber.d("No network connection found!")
                    // get the task from db
                    getTaskFromDb()
                }
            } catch (error: Exception) {
                Timber.e(error.message.toString())
            }
        }
    }

    /*
    get the task from local db
     */
    private suspend fun getTaskFromDb() {

        coroutineScope {
            Timber.d("getTaskFromDb: ${Thread.currentThread().name}")

            try {
                taskRepository.getAllTaskFromDb()
                    .flowOn(Dispatchers.IO)
                    .collect { result ->
                        when (result) {
                            is ResultSet.Loading -> {
                                progress.value = true
                            }
                            is ResultSet.Success -> {
                                val data = result.data as List<*>
                                data.let {
                                    val taskList = it.filterIsInstance<TaskEntity>()
                                    taskListFromDb.postValue(taskList)
                                }
                                progress.value = false
                            }
                            is ResultSet.Error -> {
                                progress.value = false
                            }
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e)
            }

//            when (val result = taskRepository.getAllTaskFromDb()) {
//
//                is ResultSet.Success -> {
//                    val data = result.data as List<*>
//                    data.let {
//                        val taskList = it.filterIsInstance<TaskEntity>()
//                        taskListFromDb.postValue(taskList)
//                    }
//
//                }
//                is ResultSet.Error -> {
//                    if (result.error != null) {
//                        errorMsgString.postValue(result.error.localizedMessage)
//                    } else {
//                        errorMsgInt.postValue(result.errorMsg)
//                    }
//                }
//            }
        }
    }
}

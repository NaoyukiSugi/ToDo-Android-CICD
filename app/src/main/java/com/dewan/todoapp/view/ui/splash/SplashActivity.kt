package com.dewan.todoapp.view.ui.splash

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.dewan.todoapp.R
import com.dewan.todoapp.util.GeneralHelper
import com.dewan.todoapp.view.ui.auth.LoginActivity
import com.dewan.todoapp.view.ui.main.MainActivity
import com.dewan.todoapp.viewmodel.splash.SplashViewModel
import org.jetbrains.anko.alert
import org.jetbrains.anko.intentFor

class SplashActivity : AppCompatActivity() {

    companion object {
        const val TAG = "SplashActivity"
    }

    private lateinit var viewModel: SplashViewModel
    private val mContext = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        //hide status bar
        GeneralHelper.hideStatusBar(this)

        //view model
        viewModel = ViewModelProvider(this).get(SplashViewModel::class.java)

        observers()

        //Timber.e(RuntimeException("Test Crash"))
    }

    // observer the live data
    private fun observers() {
        viewModel.token.observe(this, Observer {
            if (it.isNullOrEmpty()) {
                startActivity((intentFor<LoginActivity>()))
            }
        })

        viewModel.tokenResponse.observe(this, Observer {
            if (it.isNullOrEmpty()) {
                startActivity((intentFor<LoginActivity>()))
            } else {
                if (it == "true") {
                    finish()
                    startActivity(intentFor<MainActivity>())
                } else {
                    startActivity(intentFor<LoginActivity>())
                }
            }

        })

        viewModel.progress.observe(this, Observer {

        })

        viewModel.errorMsgString.observe(this, Observer {

        })
        viewModel.errorMsgInt.observe(this, Observer {

        })
    }

    fun showAlertDialog() {
        alert {
            isCancelable = false
            title = getString(R.string.error_no_internet)
            message = getString(R.string.error_no_internet_msg)
            positiveButton("OK") {
                it.dismiss()
                finish()
            }
        }.show()
    }

    override fun onResume() {
        super.onResume()
        viewModel.validateToken()
    }
}

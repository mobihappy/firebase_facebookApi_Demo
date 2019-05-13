package com.example.demoapi

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {
    private var isAppRunning: Boolean = true

    private val TAG = "message"
    private var mFirebaseDatabase: DatabaseReference? = null
    private var mFirebaseInstance: FirebaseDatabase? = null

    private var mCallbackManager: CallbackManager? = null

    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mCallbackManager = CallbackManager.Factory.create()

        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setIcon(R.mipmap.ic_launcher)

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "1"
        val channel2 = "2"

        // notification firebase
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val notificationChannel =  NotificationChannel(channelId,
                "Channel 1",NotificationManager.IMPORTANCE_HIGH)

            notificationChannel.description = "This is BNT"
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.setShowBadge(true)
            notificationManager.createNotificationChannel(notificationChannel)

            val notificationChannel2 =  NotificationChannel(channel2,
                "Channel 2",NotificationManager.IMPORTANCE_MIN)

            notificationChannel.description = "This is bTV"
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.setShowBadge(true)
            notificationManager.createNotificationChannel(notificationChannel2)
        }

        mFirebaseInstance = FirebaseDatabase.getInstance()

        mFirebaseDatabase = mFirebaseInstance!!.getReference("users")

        mFirebaseInstance!!.getReference("app_title").setValue("Realtime Database")

        mFirebaseInstance!!.getReference("app_title").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to read app title value.", error.toException())
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.e(TAG, "App title updated")

                val appTitle = dataSnapshot.getValue(String::class.java)

                // update toolbar title
                supportActionBar!!.title = appTitle
            }

        })

        // save user firebase database
        btn_save.setOnClickListener {
            val name = name.text.toString()
            val email = email.text.toString()

            if (TextUtils.isEmpty(userId)) {
                createUser(name, email)
            } else {
                updateUser(name, email)
            }
        }

        toggleButton()


        // Login facebook API
        btn_login_facebook.registerCallback(mCallbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                tv_info.text = "User ID: " + loginResult.accessToken.userId + "\n" +
                        "Auth Token: " + loginResult.accessToken.token
            }

            override fun onCancel() {
                tv_info.text = "Login canceled."
            }

            override fun onError(e: FacebookException) {
                Log.d("errorr","onError: "+ e.message)
                tv_info.text = e.message
            }
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        isAppRunning = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mCallbackManager!!.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    @SuppressLint("SetTextI18n")
    private fun toggleButton() {
        if (TextUtils.isEmpty(userId)) {
            btn_save.text = "Save"
        } else {
            btn_save.text = "Update"
        }
    }

    private fun createUser(name: String, email: String) {
        if (TextUtils.isEmpty(userId)) {
            userId = mFirebaseDatabase!!.push().key
        }

        val user = User(name, email)

        mFirebaseDatabase!!.child(userId.toString()).setValue(user)

        addUserChangeListener()
    }

    private fun addUserChangeListener() {
        // User data change listener
        mFirebaseDatabase!!.child(userId.toString()).addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java)

                // Check for null
                if (user == null) {
                    Log.e(TAG, "User data is null!")
                    return
                }

                Log.e(TAG, "User data is changed!" + user.name + ", " + user.email)

                // Display newly updated name and email
                txt_user.text = user.name + ", " + user.email

                // clear edit text
                email.setText("")
                name.setText("")

                toggleButton()
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.e(TAG, "Failed to read user", error.toException())
            }
        })
    }

    private fun updateUser(name: String, email: String) {
        // updating the user via child nodes
        if (!TextUtils.isEmpty(name))
            mFirebaseDatabase!!.child(userId.toString()).child("name").setValue(name)

        if (!TextUtils.isEmpty(email))
            mFirebaseDatabase!!.child(userId.toString()).child("email").setValue(email)

        
    }

}

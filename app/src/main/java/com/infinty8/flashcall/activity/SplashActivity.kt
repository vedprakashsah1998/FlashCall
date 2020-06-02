package com.infinty8.flashcall.activity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.infinty8.flashcall.R
import com.infinty8.flashcall.databinding.ActivitySplashBinding
import com.infinty8.flashcall.sharedpref.AppPref

class SplashActivity : AppCompatActivity() {


    private lateinit var binding: ActivitySplashBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        val t = Thread(Runnable {
            try {
                Thread.sleep(4000)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {

                if (AppPref.isAppIntroShown) {
                    Log.d("splash", "Test")

                    if (resources.getBoolean(R.bool.enable_mandatory_authentication)) {
                        Log.d("splash", "Test1")

                        // Authentication is mandatory
                        if (FirebaseAuth.getInstance().currentUser != null) {
                            Log.d("splash", "Test2")

                            // Intro shown and user authenticated
                            MainActivity.startActivity(this)
                            finish()
                        } else {
                            Log.d("splash", "Test3")

                            // Intro shown but user unauthenticated
                            AuthenticationActivity.startActivity(this)
                            finish()
                        }
                    } else {
                        Log.d("splash", "Test4")
                        MainActivity.startActivity(this)
                        finish()
                        // Authentication is optional. Intro shown but user unauthenticated

                    }
                } else {
                    Log.d("splash", "Test5")

                    AppIntroActivity.startActivity(this)
                    finish()
                }



            }
        })

        t.start()

    }
}

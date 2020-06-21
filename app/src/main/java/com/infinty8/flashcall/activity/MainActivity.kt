package com.infinty8.flashcall.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.widget.doOnTextChanged
import coil.api.load
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.customview.customView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.ObjectKey
import com.core.extensions.*
import com.facebook.login.LoginManager
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.iid.FirebaseInstanceId
import com.infinty8.flashcall.R
import com.infinty8.flashcall.databinding.ActivityMainBinding
import com.infinty8.flashcall.model.Meeting
import com.infinty8.flashcall.sharedpref.AppPref
import com.infinty8.flashcall.sharedpref.SharedPrefData
import com.infinty8.flashcall.utils.MeetingUtils
import com.infinty8.flashcall.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.dialog_profile.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : AppCompatActivity() {

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }
    }

    var email:String?=null
    var firstName:String?=null
    var lastName:String?=null
    var profileImage:String?=null

     lateinit var auth: FirebaseAuth

    private var sharedPrefData:SharedPrefData?=null

    private val viewModel by viewModel<MainViewModel>() // Lazy inject ViewModel
    private lateinit var binding: ActivityMainBinding

    private val minMeetingCodeLength = 10
    private var currentUser: FirebaseUser? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUser = FirebaseAuth.getInstance().currentUser
        sharedPrefData= SharedPrefData(this)
        auth=FirebaseAuth.getInstance()
        profileImage=intent.extras?.getString("auth_uid")
        firstName=intent.extras?.getString("first_name")
        lastName=intent.extras?.getString("last_name")
        email=intent.extras?.getString("email")



        setProfileIcon()
        onMeetingToggleChange()
        updateToken()
        onCreateMeetingCodeChange()
        onCopyMeetingCodeFromClipboardClick()
        onShareMeetingCodeClick()
        onJoinMeetingClick()
        onCreateMeetingClick()
        onMeetingHistoryClick()
        onProfileClick()


    }



    private fun setProfileIcon() {
        sharedPrefData= SharedPrefData(this)
        Log.d("woqhyroiu",sharedPrefData!!.getAuthId())
        binding.ivProfile.load(sharedPrefData!!.getImage())
        Log.d("wdqdj",sharedPrefData!!.getSkip())
        if (sharedPrefData!!.getSkip().equals("Skip"))
        {
            binding.ivProfile.setImageResource(R.drawable.ic_account)
        }
        else{
            if (currentUser != null) {

                    binding.ivProfile.load(sharedPrefData!!.getImage())
            }
            val requestOptions = RequestOptions()
            requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL)
                .signature(ObjectKey(System.currentTimeMillis()))
                .encodeQuality(70)
            requestOptions.priority(Priority.IMMEDIATE)
            requestOptions.skipMemoryCache(false)
            requestOptions.onlyRetrieveFromCache(true)
            requestOptions.priority(Priority.HIGH)
            requestOptions.placeholder(R.drawable.ic_account)
            requestOptions.isMemoryCacheable
            requestOptions.diskCacheStrategy(DiskCacheStrategy.DATA)
            requestOptions.diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            requestOptions.centerCrop()

            Glide.with(this)
                .load("http://graph.facebook.com/${sharedPrefData!!.getAuthId()}/picture?type=square")
                .thumbnail(
                    Glide.with(this).load("http://graph.facebook.com/${sharedPrefData!!.getAuthId()}/picture?type=square")
                )
                .apply(requestOptions)
                .listener(object : RequestListener<Drawable?> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any,
                        target: Target<Drawable?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any,
                        target: Target<Drawable?>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                })
                .into(binding.ivProfile)
        }


    }







    /**
     * Called when the meeting toggle button check state is changed
     */
    private fun onMeetingToggleChange() {
        binding.tgMeeting.addOnButtonCheckedListener { toggleGroup, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnToggleJoinMeeting -> {
                        binding.groupCreateMeeting.makeGone()
                        binding.groupJoinMeeting.makeVisible()
                    }
                    R.id.btnToggleCreateMeeting -> {
                        binding.groupJoinMeeting.makeGone()
                        binding.groupCreateMeeting.makeVisible()
                        val meetingCode = generateMeetingCode()
                        binding.etCodeCreateMeeting.setText(meetingCode)
                    }
                }
            }
        }
    }

    /**
     * Called when the meeting code in the EditText of the CREATE MEETING toggle changes
     */
    private fun onCreateMeetingCodeChange() {
        binding.tilCodeCreateMeeting.etCodeCreateMeeting.doOnTextChanged { text, start, before, count ->
            if (count >= minMeetingCodeLength) binding.tilCodeCreateMeeting.error = null
        }
    }

    private fun generateMeetingCode(): String {
        val allowedChars = ('A'..'Z') + ('a'..'z')
        return (1..10)
            .map { allowedChars.random() }
            .joinToString("")
    }

    /**
     * Called when the clipboard icon is clicked in the EditText of the JOIN MEETING toggle
     */
    private fun onCopyMeetingCodeFromClipboardClick() {
        binding.tilCodeJoinMeeting.setEndIconOnClickListener {
            val clipboardText = getTextFromClipboard()
            if (clipboardText != null) {
                binding.etCodeJoinMeeting.setText(clipboardText)
                toast(getString(R.string.main_meeting_code_copied))
            } else {
                toast(getString(R.string.main_empty_clipboard))
            }
        }
    }

    /**
     * Called when the share icon is clicked in the EditText of the CREATE MEETING toggle
     */
    private fun onShareMeetingCodeClick() {
        binding.tilCodeCreateMeeting.setEndIconOnClickListener {
            if (binding.etCodeCreateMeeting.text.toString().length >= minMeetingCodeLength) {
                binding.tilCodeCreateMeeting.error = null
                startShareTextIntent(
                    getString(R.string.main_share_meeting_code_title),
                    binding.etCodeCreateMeeting.text.toString()
                )
            } else {
                binding.tilCodeCreateMeeting.error =
                    getString(R.string.main_error_meeting_code_length, minMeetingCodeLength)
            }
        }
    }

    /**
     * Called when the JOIN button is clicked of the JOIN MEETING toggle
     */
    private fun onJoinMeetingClick() {
        binding.btnJoinMeeting.setOnClickListener {
            if (binding.etCodeJoinMeeting.text.toString().length >= minMeetingCodeLength) {
                joinMeeting()
            } else {
                toast(getString(R.string.main_error_meeting_code_length, minMeetingCodeLength))
            }
        }
    }

    private fun joinMeeting() {
        MeetingUtils.startMeeting(
            this,
            binding.etCodeJoinMeeting.text.toString()) // Start Meeting

        viewModel.addMeetingToDb(
            Meeting(
                binding.etCodeJoinMeeting.text.toString(),
                System.currentTimeMillis()
            )
        ) // Add meeting to db
    }

    /**
     * Called when the CREATE button is clicked of the CREATE MEETING toggle
     */
    private fun onCreateMeetingClick() {
        binding.btnCreateMeeting.setOnClickListener {
            if (binding.etCodeCreateMeeting.text.toString().length >= minMeetingCodeLength) {
                createMeeting()
            } else {
                toast(getString(R.string.main_error_meeting_code_length, minMeetingCodeLength))
            }
        }
    }

    private fun createMeeting() {
        MeetingUtils.startMeeting(
            this,
            binding.etCodeCreateMeeting.text.toString()
        ) // Start Meeting

        viewModel.addMeetingToDb(
            Meeting(
                binding.etCodeCreateMeeting.text.toString(),
                System.currentTimeMillis()
            )
        ) // Add meeting to db
    }

    private fun onMeetingHistoryClick() {
        binding.ivMeetingHistory.setOnClickListener {
            MeetingHistoryActivity.startActivity(this)
        }
    }

    private fun onProfileClick() {
        sharedPrefData= SharedPrefData(this)
        Log.d("joqwhfdo", sharedPrefData!!.getEmail())

        binding.ivProfile.setOnClickListener {
            val profileDialog = MaterialDialog(this, BottomSheet(LayoutMode.WRAP_CONTENT)).show {
                customView(R.layout.dialog_profile)
            }

            profileDialog.apply {



                Log.d("bottomSheetLog",sharedPrefData!!.getSkip())
                if(sharedPrefData!!.getSkip().equals("Skip"))
                {
                    btnUserAuthenticationStatus.text = getString(R.string.all_btn_sign_in)
                    tvUserName.text = getString(R.string.profile_user_not_authenticated)
                    tvEmail.text = "Please sign in"
                    ivUserProfileDialog.setImageResource(R.drawable.ic_account)
                }
                else
                {
                    btnUserAuthenticationStatus.text = getString(R.string.all_btn_sign_out)
                    if (currentUser != null) {

                        Log.d("fbTest","firstNamr $firstName")
                        ivUserProfileDialog.load(sharedPrefData!!.getImage())
                        Log.d("joqwhfdo", sharedPrefData!!.getEmail())
                        if (sharedPrefData!!.getName()!=null)
                        tvUserName.text = sharedPrefData!!.getName()
                        tvEmail.text = sharedPrefData!!.getEmail()

                    }

                    tvUserName.text = sharedPrefData!!.getName()
                    tvEmail.text = sharedPrefData!!.getEmail()
                    val requestOptions = RequestOptions()
                    requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL)
                        .signature(ObjectKey(System.currentTimeMillis()))
                        .encodeQuality(70)
                    requestOptions.priority(Priority.IMMEDIATE)
                    requestOptions.skipMemoryCache(false)
                    requestOptions.onlyRetrieveFromCache(true)
                    requestOptions.priority(Priority.HIGH)
                    requestOptions.placeholder(R.drawable.ic_account)
                    requestOptions.isMemoryCacheable
                    requestOptions.diskCacheStrategy(DiskCacheStrategy.DATA)

                    requestOptions.diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    requestOptions.centerCrop()
                    Log.d("weojhfo",sharedPrefData!!.getAuthId())

                    Glide.with(this@MainActivity)
                        .load("http://graph.facebook.com/${sharedPrefData!!.getAuthId()}/picture?type=square")
                        .thumbnail(
                            Glide.with(this@MainActivity).load("http://graph.facebook.com/${sharedPrefData!!.getAuthId()}/picture?type=square")
                        )
                        .apply(requestOptions)
                        .listener(object : RequestListener<Drawable?> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any,
                                target: Target<Drawable?>,
                                isFirstResource: Boolean
                            ): Boolean {
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any,
                                target: Target<Drawable?>,
                                dataSource: DataSource,
                                isFirstResource: Boolean
                            ): Boolean {
                                return false
                            }
                        })
                        .into(ivUserProfileDialog)

                }
                switchDarkMode.isChecked = !AppPref.isLightThemeEnabled

                // UserAuthenticationStatus button onClick
                btnUserAuthenticationStatus.setOnClickListener {
                    dismiss()
                    val mySharedPref: SharedPreferences? = context.getSharedPreferences("filename1", 0)
                        mySharedPref!!.edit().remove("name").commit()
                        mySharedPref!!.edit().remove("email").commit()
                        mySharedPref!!.edit().remove("authId").commit()
                        mySharedPref!!.edit().remove("image").commit()
                        mySharedPref!!.edit().remove("skip").commit()
                    LoginManager.getInstance().logOut()


                    if (currentUser != null) {
                        // User is currently signed in
                        AuthUI.getInstance().signOut(this@MainActivity).addOnCompleteListener {
                            AuthenticationActivity.startActivity(this@MainActivity)
                            finish()
                        }
                    } else {
                        // User is not signed in
                        AuthenticationActivity.startActivity(this@MainActivity)
                        finish()
                    }
                }

                // Dark Mode Switch
                switchDarkMode.setOnCheckedChangeListener { compoundButton, isChecked ->
                    dismiss()

                    // Change theme after dismiss to prevent memory leak
                    onDismiss {
                        if (isChecked) setThemeMode(AppCompatDelegate.MODE_NIGHT_YES) else setThemeMode(
                            AppCompatDelegate.MODE_NIGHT_NO
                        )
                    }
                }

                // Send feedback onClick
                tvSendFeedback.setOnClickListener {
                    startEmailIntent(
                        getString(R.string.app_feedback_contact_email),
                        getString(R.string.profile_feedback_email_subject)
                    )
                }

                // Rate app onClick
                tvRateApp.setOnClickListener {
                    openAppInGooglePlay(applicationContext.packageName, R.color.colorSurface)
                }

                // Share app onClick
                tvShareApp.setOnClickListener {
                    startShareTextIntent(
                        getString(R.string.profile_share_app_title),
                        getString(R.string.profile_share_app_text, applicationContext.packageName)
                    )
                }

                // Open Source Licenses onClick
                tvOpenSourceLicenses.setOnClickListener {
                    startActivity(Intent(this@MainActivity, OssLicensesMenuActivity::class.java))
                }

                // Privacy Policy onClick
                tvPrivacyPolicy.setOnClickListener {
                    openUrl(getString(R.string.app_privacy_policy_url), R.color.colorSurface)
                }

                // Terms of Service onClick
                tvTermsOfService.setOnClickListener {
                    openUrl(getString(R.string.app_terms_of_service_url), R.color.colorSurface)
                }
            }
        }
    }

    private fun setThemeMode(themeMode: Int) {
        AppCompatDelegate.setDefaultNightMode(themeMode)
        AppPref.isLightThemeEnabled = themeMode == AppCompatDelegate.MODE_NIGHT_NO
    }

    private fun updateToken() {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.d("FCMTest8085", "isSuccessful")
                    return@OnCompleteListener
                }
                val token = task.result?.token
                if (token != null && token.isNotEmpty()) {
/*
                     sendTokenToServer(token)
*/
                }
            })


    }





}

package com.infinty8.flashcall.utils

import com.infinty8.flashcall.R
import android.content.Context
import androidx.core.os.bundleOf
import com.google.firebase.auth.FirebaseAuth
import com.infinty8.flashcall.sharedpref.SharedPrefData
import org.jitsi.meet.sdk.JitsiMeet
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.jitsi.meet.sdk.JitsiMeetUserInfo
import java.net.URL

object MeetingUtils {

    fun startMeeting(context: Context, meetingCode: String) {


        val serverUrl = URL(context.getString(R.string.app_server_url))
        val defaultOptions = JitsiMeetConferenceOptions.Builder()
            .setServerURL(serverUrl)
            .setWelcomePageEnabled(false)
            .setFeatureFlag("invite.enabled", false)
            .setFeatureFlag("live-streaming.enabled", false)
            .setFeatureFlag("meeting-name.enabled", false)
            .setFeatureFlag("call-integration.enabled", false)
            .setFeatureFlag("recording.enabled", false)
            .build()
        JitsiMeet.setDefaultConferenceOptions(defaultOptions)

        val options = JitsiMeetConferenceOptions.Builder()
            .setRoom(meetingCode)
            .setUserInfo(null)
        val sharedPrefData= SharedPrefData(context)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (sharedPrefData.getSkip().equals("Skip"))
        {
            val userInfoBundle = bundleOf(
                "displayName" to "User Not Sign in",
                "email" to "Please Sign In",
                "avatarURL" to R.drawable.ic_account
            )

            options.setUserInfo(JitsiMeetUserInfo(userInfoBundle))
        }
        else
        {
            if (currentUser != null) {
                val userInfoBundle = bundleOf(
                    "displayName" to sharedPrefData.getName(),
                    "email" to sharedPrefData.getEmail(),
                    "avatarURL" to sharedPrefData.getImage()
                )

                options.setUserInfo(JitsiMeetUserInfo(userInfoBundle))
            }
            val userInfoBundle = bundleOf(
                "displayName" to sharedPrefData.getName() ,
                "email" to sharedPrefData.getEmail(),
                "avatarURL" to "http://graph.facebook.com/${sharedPrefData.getAuthId()}/picture?type=square"
            )

            options.setUserInfo(JitsiMeetUserInfo(userInfoBundle))
        }


        JitsiMeetActivity.launch(context, options.build())
    }
}
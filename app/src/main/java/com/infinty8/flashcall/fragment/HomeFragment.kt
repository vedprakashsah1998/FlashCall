package com.infinty8.flashcall.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import com.core.extensions.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.infinty8.flashcall.R
import com.infinty8.flashcall.databinding.FragmentHomeBinding
import com.infinty8.flashcall.model.Meeting
import com.infinty8.flashcall.sharedpref.SharedPrefData
import com.infinty8.flashcall.utils.MeetingUtils
import com.infinty8.flashcall.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.fragment_home.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class HomeFragment : Fragment() {

    private var binding: FragmentHomeBinding? = null

    private val minMeetingCodeLength = 10
    private var currentUser: FirebaseUser? = null
    var email:String?=null
    var firstName:String?=null
    var lastName:String?=null
    var profileImage:String?=null
    private val viewModel by viewModel<MainViewModel>()

    lateinit var auth: FirebaseAuth

    private var sharedPrefData: SharedPrefData?=null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding=FragmentHomeBinding.inflate(inflater,container,false)
        val view = binding!!.root
        onCreateMeetingCodeChange()
        onCopyMeetingCodeFromClipboardClick()
        onShareMeetingCodeClick()
        onJoinMeetingClick()
        onCreateMeetingClick()
        onMeetingToggleChange()
        return view
    }

    companion object{
        fun newInstance(text: String?): HomeFragment? {
            val f = HomeFragment()
            val b = Bundle()
            b.putString("msg", text)
            f.arguments = b
            return f
        }
    }

    private fun onMeetingToggleChange() {
        binding?.tgMeeting?.addOnButtonCheckedListener { toggleGroup, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnToggleJoinMeeting -> {
                        binding?.groupCreateMeeting?.makeGone()
                        binding?.groupJoinMeeting?.makeVisible()
                    }
                    R.id.btnToggleCreateMeeting -> {
                        binding?.groupJoinMeeting?.makeGone()
                        binding?.groupCreateMeeting?.makeVisible()
                        val meetingCode = generateMeetingCode()
                        binding?.etCodeCreateMeeting?.setText(meetingCode)
                    }
                }
            }
        }
    }


    private fun onCreateMeetingCodeChange() {
        binding?.tilCodeCreateMeeting?.etCodeCreateMeeting?.doOnTextChanged { text, start, before, count ->
            if (count >= minMeetingCodeLength) binding?.tilCodeCreateMeeting!!.error = null
        }
    }

    private fun generateMeetingCode(): String {
        val allowedChars = ('A'..'Z') + ('a'..'z')
        return (1..10)
            .map { allowedChars.random() }
            .joinToString("")
    }


    private fun onCopyMeetingCodeFromClipboardClick() {
        binding?.tilCodeJoinMeeting?.setEndIconOnClickListener {
            val clipboardText = activity?.getTextFromClipboard()
            if (clipboardText != null) {
                binding?.etCodeJoinMeeting?.setText(clipboardText)
                activity?.toast(getString(R.string.main_meeting_code_copied))
            } else {
                activity?.toast(getString(R.string.main_empty_clipboard))
            }
        }
    }

    private fun onShareMeetingCodeClick() {
        binding?.tilCodeCreateMeeting?.setEndIconOnClickListener {
            if (binding?.etCodeCreateMeeting?.text.toString().length >= minMeetingCodeLength) {
                binding!!.tilCodeCreateMeeting.error = null
                activity?.startShareTextIntent(
                    getString(R.string.main_share_meeting_code_title),
                    "Meeting Code: "+binding!!.etCodeCreateMeeting.text.toString()+"\n "+
                    getString(R.string.profile_share_app_text, activity!!. applicationContext.packageName)
                )

            } else {
                binding!!.tilCodeCreateMeeting.error =
                    getString(R.string.main_error_meeting_code_length, minMeetingCodeLength)
            }
        }
    }

    private fun onJoinMeetingClick() {
        binding?.btnJoinMeeting?.setOnClickListener {
            if (binding!!.etCodeJoinMeeting.text.toString().length >= minMeetingCodeLength) {
                joinMeeting()
            } else {
                activity?.toast(getString(R.string.main_error_meeting_code_length, minMeetingCodeLength))
            }
        }
    }

    private fun joinMeeting() {
        activity?.let {
            MeetingUtils.startMeeting(
                it,
                binding?.etCodeJoinMeeting?.text.toString())
        } // Start Meeting

        viewModel.addMeetingToDb(
            Meeting(
                binding?.etCodeJoinMeeting?.text.toString(),
                System.currentTimeMillis()
            )
        ) // Add meeting to db
    }

    private fun onCreateMeetingClick() {
        binding?.btnCreateMeeting?.setOnClickListener {
            if (binding!!.etCodeCreateMeeting.text.toString().length >= minMeetingCodeLength) {
                createMeeting()
            } else {
                activity?.toast(getString(R.string.main_error_meeting_code_length, minMeetingCodeLength))
            }
        }
    }

    private fun createMeeting() {
        activity?.let {
            MeetingUtils.startMeeting(
                it,
                binding?.etCodeCreateMeeting?.text.toString()
            )
        } // Start Meeting

        viewModel.addMeetingToDb(
            Meeting(
                binding?.etCodeCreateMeeting?.text.toString(),
                System.currentTimeMillis()
            )
        ) // Add meeting to db
    }


}
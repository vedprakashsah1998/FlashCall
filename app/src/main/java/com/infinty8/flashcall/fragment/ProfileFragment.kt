package com.infinty8.flashcall.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.core.extensions.copyTextToClipboard
import com.core.extensions.makeGone
import com.core.extensions.makeVisible
import com.infinty8.flashcall.R
import com.infinty8.flashcall.adapteritem.MeetingHistoryItem
import com.infinty8.flashcall.databinding.FragmentProfileBinding
import com.infinty8.flashcall.model.Meeting
import com.infinty8.flashcall.utils.MeetingUtils
import com.infinty8.flashcall.viewmodel.MeetingHistoryViewModel
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import com.mikepenz.fastadapter.listeners.ClickEventHook
import kotlinx.android.synthetic.main.item_meeting_history.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProfileFragment : Fragment() {

    private var binding: FragmentProfileBinding? = null
    private val viewModel by viewModel<MeetingHistoryViewModel>() // Lazy inject ViewModel

    private lateinit var meetingHistoryAdapter: FastItemAdapter<MeetingHistoryItem>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        binding=FragmentProfileBinding.inflate(inflater,container,false);
        val view = binding!!.root
        setupRecyclerView(savedInstanceState)
        setupObservables()

        return view



    }

    override fun onSaveInstanceState(_outState: Bundle) {
        var outState = _outState
        outState = meetingHistoryAdapter.saveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }


    private fun setupRecyclerView(savedInstanceState: Bundle?) {
        meetingHistoryAdapter = FastItemAdapter()
        meetingHistoryAdapter.setHasStableIds(true)
        meetingHistoryAdapter.withSavedInstanceState(savedInstanceState)

        binding?.recyclerView?.layoutManager = LinearLayoutManager(activity)
        binding?.recyclerView?.adapter = meetingHistoryAdapter

        onMeetingCodeClick()
        onRejoinClick()
    }

    private fun setupObservables() {
        activity?.let {
            viewModel.meetingHistoryLiveData.observe(it, Observer { meetingHistoryList ->
                val meetingHistoryItems = ArrayList<MeetingHistoryItem>()

                for (meeting in meetingHistoryList) {
                    meetingHistoryItems.add(MeetingHistoryItem(meeting))
                }

                FastAdapterDiffUtil[meetingHistoryAdapter.itemAdapter] = meetingHistoryItems
                showEmptyState(meetingHistoryAdapter.itemCount)
            })
        }
    }

    private fun showEmptyState(itemCount: Int) {
        if (itemCount > 0) binding?.groupEmpty?.makeGone() else binding?.groupEmpty?.makeVisible()
    }

    private fun onMeetingCodeClick() {
        meetingHistoryAdapter.addEventHook(object : ClickEventHook<MeetingHistoryItem>() {
            override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
                return viewHolder.itemView.tvMeetingCode
            }

            override fun onClick(
                v: View,
                position: Int,
                fastAdapter: FastAdapter<MeetingHistoryItem>,
                item: MeetingHistoryItem
            ) {
                activity?.copyTextToClipboard(
                    item.meeting.code,
                    getString(R.string.meeting_history_meeting_code_copied)
                )
            }
        })
    }

    /**
     * Called when the Rejoin button is clicked of a RecyclerView Item
     */
    private fun onRejoinClick() {
        meetingHistoryAdapter.addEventHook(object : ClickEventHook<MeetingHistoryItem>() {
            override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
                return viewHolder.itemView.btnRejoinMeeting
            }

            override fun onClick(
                v: View,
                position: Int,
                fastAdapter: FastAdapter<MeetingHistoryItem>,
                item: MeetingHistoryItem
            ) {
                activity?.let {
                    MeetingUtils.startMeeting(
                        it,
                        item.meeting.code)
                } // Start Meeting

                viewModel.addMeetingToDb(
                    Meeting(
                        item.meeting.code,
                        System.currentTimeMillis()
                    )
                ) // Add meeting to db
            }
        })
    }



    companion object{
        fun newInstance(text: String?): ProfileFragment? {
            val f = ProfileFragment()
            val b = Bundle()
            b.putString("msg", text)
            f.arguments = b
            return f
        }
    }


}
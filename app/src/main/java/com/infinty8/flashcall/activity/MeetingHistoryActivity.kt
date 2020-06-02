package com.infinty8.flashcall.activity

import com.core.extensions.copyTextToClipboard
import com.infinty8.flashcall.FlashCall
import com.infinty8.flashcall.R
import com.infinty8.flashcall.adapteritem.MeetingHistoryItem
import com.infinty8.flashcall.databinding.ActivityMeetingHistoryBinding
import com.infinty8.flashcall.model.Meeting
import com.infinty8.flashcall.utils.MeetingUtils
import com.infinty8.flashcall.viewmodel.MeetingHistoryViewModel
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.core.extensions.makeGone
import com.core.extensions.makeVisible
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import com.mikepenz.fastadapter.listeners.ClickEventHook
import kotlinx.android.synthetic.main.item_meeting_history.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class MeetingHistoryActivity : AppCompatActivity() {

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, MeetingHistoryActivity::class.java)
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityMeetingHistoryBinding
    private val viewModel by viewModel<MeetingHistoryViewModel>() // Lazy inject ViewModel

    private lateinit var meetingHistoryAdapter: FastItemAdapter<MeetingHistoryItem>
    private var initialLayoutComplete = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMeetingHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView(savedInstanceState)
        setupObservables()
    }

    override fun onSaveInstanceState(_outState: Bundle) {
        var outState = _outState
        outState = meetingHistoryAdapter.saveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }





    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun setupRecyclerView(savedInstanceState: Bundle?) {
        meetingHistoryAdapter = FastItemAdapter()
        meetingHistoryAdapter.setHasStableIds(true)
        meetingHistoryAdapter.withSavedInstanceState(savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = meetingHistoryAdapter

        onMeetingCodeClick()
        onRejoinClick()
    }

    private fun setupObservables() {
        viewModel.meetingHistoryLiveData.observe(this, Observer { meetingHistoryList ->
            val meetingHistoryItems = ArrayList<MeetingHistoryItem>()

            for (meeting in meetingHistoryList) {
                meetingHistoryItems.add(MeetingHistoryItem(meeting))
            }

            FastAdapterDiffUtil[meetingHistoryAdapter.itemAdapter] = meetingHistoryItems
            showEmptyState(meetingHistoryAdapter.itemCount)
        })
    }

    private fun showEmptyState(itemCount: Int) {
        if (itemCount > 0) binding.groupEmpty.makeGone() else binding.groupEmpty.makeVisible()
    }



    /**
     * Returns the size of the Adaptive Banner Ad based on the screen width
     */

    /**
     * Called when the meeting code is clicked of a RecyclerView Item
     */
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
                copyTextToClipboard(
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
                MeetingUtils.startMeeting(
                    this@MeetingHistoryActivity,
                    item.meeting.code,
                    R.string.all_rejoining_meeting
                ) // Start Meeting

                viewModel.addMeetingToDb(
                    Meeting(
                        item.meeting.code,
                        System.currentTimeMillis()
                    )
                ) // Add meeting to db
            }
        })
    }
}

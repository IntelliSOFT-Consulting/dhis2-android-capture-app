package com.nacare.capture.ui.v2

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.androidskeletonapp.R
import com.google.android.material.button.MaterialButton
import com.nacare.capture.adapters.EventAdapter
import com.nacare.capture.data.Constants.PROGRAM_UUID
import com.nacare.capture.data.FormatterClass
import com.nacare.capture.data.Sdk
import com.nacare.capture.data.service.SyncStatusHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.nacare.capture.data.Constants
import com.nacare.capture.data.Constants.FACILITY_PROGRAM_UUID
import com.nacare.capture.data.Constants.PROGRAM_TRACKED_UUID
import com.nacare.capture.models.CodeValue
import com.nacare.capture.ui.v2.filters.FilterBottomSheetFragment
import com.nacare.capture.ui.v2.filters.FilterBottomSheetListener
import com.nacare.capture.ui.v2.registry.RegistryActivity
import com.nacare.capture.utils.AppUtils
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentCreateProjection
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventCreateProjection

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EventsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EventsFragment : Fragment(), FilterBottomSheetListener {

    private var eventList = mutableListOf<Enrollment>()
    private lateinit var mRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_events, container, false)

        view.findViewById<FloatingActionButton>(R.id.add_fab)
            .apply {
                setOnClickListener {
                    createANewEvent()
                }
            }
        view.findViewById<MaterialButton>(R.id.sync_button)
            .apply {
                setOnClickListener {

                }
            }
        mRecyclerView = view.findViewById(R.id.recyclerView)

        setHasOptionsMenu(true)
        loadActiveProgram()
        loadActiveEnrollments("ALL")

        return view
    }

    private fun loadActiveEnrollments(s: String) {
        var eventFilterList = mutableListOf<Enrollment>()
        eventList = SyncStatusHelper.getAllEnrolments()
        when (s) {
            "ALL" -> {
                eventFilterList = eventList
            }

            "draft" -> {
                eventFilterList.clear()
                eventList.forEach {
                    if (it.syncState()!!.name == "TO_UPLOAD") {
                        eventFilterList.add(it)
                    }
                }
            }

            "not synced" -> {
                eventFilterList.clear()
                eventList.forEach {
                    if (it.syncState()!!.name == "TO_UPLOAD") {
                        eventFilterList.add(it)
                    }
                }
            }

            "completed" -> {
                eventFilterList.clear()
                eventList.forEach {
                    if (it.syncState()!!.name == "SYNCED") {
                        eventFilterList.add(it)
                    }
                }
            }

            "duplicates" -> {
                eventFilterList.clear()
                eventList.forEach {
                    if (it.syncState()!!.name == "DUPLICATE") {
                        eventFilterList.add(it)
                    }
                }
            }
        }

        mRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            val eventAdapter =
                EventAdapter(requireContext(), eventFilterList, this@EventsFragment::handleClick)
            adapter = eventAdapter
            eventAdapter.notifyDataSetChanged()

        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_search, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.action_filter -> {
                // Do something when the menu item is clicked
                showFilterBottomSheet()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showFilterBottomSheet() {
        val bottomSheetFragment = FilterBottomSheetFragment()
        bottomSheetFragment.setFilterBottomSheetListener(this)
        bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)

    }

    private fun handleClick(event: Enrollment) {
        FormatterClass().saveSharedPref(
            "event_date",
            AppUtils().formatEventDate(event.enrollmentDate()),
            requireContext()
        )
        FormatterClass().saveSharedPref(
            "enrollment_id",
            event.uid(),
            requireContext()
        )
        FormatterClass().saveSharedPref(
            "event_organization",
            event.organisationUnit().toString(),
            requireContext()
        )
        val intent = Intent(requireContext(), RegistryActivity::class.java)
        startActivity(intent)

    }

    private fun createANewEvent() {
//       eventEnrollment()
        FormatterClass().deleteSharedPref("enrollment_id", requireContext())
        startActivity(Intent(requireContext(), RegistryActivity::class.java))
    }

    private fun eventEnrollment() {
        val date = FormatterClass().getSharedPref(
            "event_date",
            requireContext()
        )
        val org = FormatterClass().getSharedPref(
            "event_organization",
            requireContext()
        )
        val programUid = FormatterClass().getSharedPref(PROGRAM_UUID, requireContext())
        if (date != null && org != null && programUid != null) {

            val eventBuilder = EnrollmentCreateProjection.builder()
                .trackedEntityInstance("oSrCt5azNKG")
                .program(programUid)
                .organisationUnit(org)
                .build()
            // Create the empty event
            val eventUid = Sdk.d2().enrollmentModule().enrollments()
                .blockingAdd(eventBuilder)
            Sdk.d2().enrollmentModule().enrollments().uid(eventUid).apply {
                setEnrollmentDate(FormatterClass().parseEventDate(date))
                setFollowUp(false)
                setIncidentDate(FormatterClass().parseEventDate(date))
            }

        }
    }

    override fun onResume() {
        loadActiveProgram()
        loadActiveEnrollments("ALL")
        super.onResume()
    }

    private fun loadActiveProgram() {
        val programs = SyncStatusHelper.programList()
        if (programs.isNotEmpty()) {
            val notification =
                programs.find { it.name() == "The National Cancer Registry of Kenya Notification Form" }
            if (notification != null) {

                FormatterClass().saveSharedPref(PROGRAM_UUID, notification.uid(), requireContext())
                if (notification.trackedEntityType() != null) {
                    FormatterClass().saveSharedPref(
                        PROGRAM_TRACKED_UUID,
                        notification.trackedEntityType()!!.uid(),
                        requireContext()
                    )
                }
            }
            val fac =
                programs.find { it.name() == " Facility Details Capture Tool" }
            if (fac != null) {
                FormatterClass().saveSharedPref(FACILITY_PROGRAM_UUID, fac.uid(), requireContext())
            }
        }
    }

    override fun onStatusClicked(status: String) {
        loadActiveEnrollments(status)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDateClick(date: String) {
        Log.e("TAG", "Selected Date $date")
        when (date) {

            "Today" -> {
                val today = FormatterClass().getCurrentDate()
            }

            "Yesterday" -> {}
            "Tomorrow" -> {}
            "This Week" -> {}
            "Last Week" -> {}
            "Next Week" -> {}
            "This Month" -> {}
            "Last Month" -> {}
            "Next Month" -> {}
            "From To" -> {}
            "Other" -> {}
            "Anytime" -> {}
        }

    }

    override fun onDateRangeClicked() {

    }


}
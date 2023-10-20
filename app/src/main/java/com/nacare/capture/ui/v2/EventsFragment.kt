package com.nacare.capture.ui.v2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.nacare.capture.ui.v2.registry.RegistryActivity
import com.nacare.capture.utils.AppUtils
import org.hisp.dhis.android.core.enrollment.Enrollment
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
class EventsFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_events, container, false)
        val eventList = SyncStatusHelper.getAllEnrolments()
        view.findViewById<FloatingActionButton>(R.id.add_fab)
            .apply {
                setOnClickListener {
                    /*  val intent = Intent(requireContext(), RegistryActivity::class.java)
                      startActivity(intent)*/
                    createANewEvent()
                }
            }
        view.findViewById<MaterialButton>(R.id.sync_button)
            .apply {
                setOnClickListener {

                }
            }
        view.findViewById<RecyclerView>(R.id.recyclerView)
            .apply {
                layoutManager = LinearLayoutManager(requireContext())
                val eventAdapter =
                    EventAdapter(requireContext(), eventList, this@EventsFragment::handleClick)
                adapter = eventAdapter
            }
        loadActiveProgram()

        return view
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

        // Assume you have initialized the DHIS2 SDK (Sdk.d2()) before calling this function

        try {

            val programs = SyncStatusHelper.programList()
            if (programs.isNotEmpty()) {
                Log.e("TAG", "Programs -> :::: $programs")
                val notification =
                    programs.find { it.name() == "The National Cancer Registry of Kenya Notification Form" }
                if (notification != null) {
                    FormatterClass().saveSharedPref(
                        PROGRAM_UUID,
                        notification.uid(),
                        requireContext()
                    )
                }

                val fac =
                    programs.find { it.name() == " Facility Details Capture Tool" }
                if (fac != null) {
                    FormatterClass().saveSharedPref(
                        FACILITY_PROGRAM_UUID,
                        fac.uid(),
                        requireContext()
                    )
                }

            } else {
                Log.e("TAG", "Empty Programs")
                Toast.makeText(requireContext(), "Please Sync data first", Toast.LENGTH_SHORT)
                    .show()
            }
            val program = FormatterClass().getSharedPref(PROGRAM_UUID, requireContext())
            if (program != null) {
                val programData = SyncStatusHelper.singleProgram(program)
                val date = FormatterClass().getSharedPref(
                    "event_date",
                    requireContext()
                )
                val org = FormatterClass().getSharedPref(
                    "event_organization",
                    requireContext()
                )
                if (date != null && org != null) {

                    /*  val eventBuilder = EventCreateProjection.builder()
                          .program(programData.uid())
                          .organisationUnit(org)
  //                        .programStage()

                      // Create the empty event
                      val eventUid = Sdk.d2().eventModule().events()
                          .blockingAdd(eventBuilder.build())*/
                    /*     Log.e("TAG", "Event created with UID: $eventUid")
     */

                }
            }

            // Retrieve the program details
            /*   val program = Sdk.d2().programModule().programs()
                   .byUid().eq(programUid)
                   .one()
                   .blockingGet()*/

            // Retrieve the tracked entity instance details
            /*  val trackedEntityInstance = Sdk.d2().trackedEntityModule().trackedEntityInstances()
                  .byUid().eq(trackedEntityInstanceUid)
                  .one()
                  .blockingGet()*/

            // Build the event data
//            val eventBuilder = EventCreateProjection.builder()
////                .trackedEntityInstance(trackedEntityInstance)
////                .program(program)
//                .status(State.TO_POST)
//                .eventDate("2023-10-20") // Set the event date as needed

            // Add data values to the event
//            for ((dataElementUid, value) in dataValues) {
//                eventBuilder.dataValue(dataElementUid, value)
//            }

            // Create the event
            /*   val eventUid = Sdk.d2().eventModule().events()
                   .blockingAdd(eventBuilder.build())

               // The event has been created successfully
               println("Event created with UID: $eventUid")*/
        } catch (exception: Exception) {
            // Handle exceptions
            exception.printStackTrace()
        }

    }

    private fun loadActiveProgram() {
        val programs = SyncStatusHelper.programList()
        if (programs.isNotEmpty()) {
            val notification =
                programs.find { it.name() == "The National Cancer Registry of Kenya Notification Form" }
            if (notification != null) {
                Log.e("TAG", "Retrieved Programs ${notification.uid()}")
                FormatterClass().saveSharedPref(PROGRAM_UUID, notification.uid(), requireContext())
            }
            val fac =
                programs.find { it.name() == " Facility Details Capture Tool" }
            if (fac != null) {
                Log.e("TAG", "Retrieved Programs ${fac.uid()}")
                FormatterClass().saveSharedPref(FACILITY_PROGRAM_UUID, fac.uid(), requireContext())
            }
        }
    }


}
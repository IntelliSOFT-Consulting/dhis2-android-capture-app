package com.nacare.capture.ui.v2

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.SimpleExpandableListAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.android.androidskeletonapp.R
import com.nacare.capture.data.Sdk
import com.nacare.capture.data.Sdk.d2
import com.nacare.capture.utils.AppUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.user.User


/**
 * A simple [Fragment] subclass.
 * Use the [ProgramsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProgramsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_programs, container, false)
        view.findViewById<TextInputEditText>(R.id.edt_date).apply {
            isFocusable = false
            isCursorVisible = false
            keyListener = null
            setOnClickListener {

                AppUtils().showDatePickerDialog(
                    requireContext(),
                    it.findViewById(R.id.edt_date),
                    setMaxNow = true,
                    setMinNow = false
                )
            }
        }
        view.findViewById<TextInputEditText>(R.id.edt_org).apply {
            isFocusable = false
            isCursorVisible = false
            keyListener = null
            setOnClickListener {
                getUserOrganizationAndChildren()
            }
        }
        view.findViewById<TextInputLayout>(R.id.org_holder).apply {
            setOnClickListener {
                getUserOrganizationAndChildren()
            }
        }
        view.findViewById<MaterialButton>(R.id.btn_next).apply {
            setOnClickListener {
                openEventsFragment()
            }
        }

        return view
    }

    private fun openEventsFragment() {
        val fragmentB = EventsFragment()
        val transaction: FragmentTransaction =
            requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, fragmentB)
        transaction.addToBackStack(null)  // Optional: Add to back stack to enable back navigation
        transaction.commit()
    }

    private fun getUserAssignedOrgUnit(): OrganisationUnit? {
        val user: User = Sdk.d2().userModule().user().blockingGet()
        Log.e("TAG", "User -> .... ${user.uid()}")
        return try {
            // Assume the user module provides information about the assigned organisation unit
            val data = Sdk.d2().organisationUnitModule().organisationUnits()
                .byUid().eq(user.uid()) // Replace with the actual UID
                .one()
                .blockingGet()
            Log.e("TAG", "Data Found $data")
            return data
        } catch (exception: Exception) {
            // Handle exceptions
            exception.printStackTrace()
            Log.e("TAG", "User's organization unit error ${exception.message}")
            null
        }
    }

    // Function to get all child organization units for a given organization unit
    private fun getAllChildOrgUnits(parentOrgUnitUid: String): List<OrganisationUnit>? {
        return try {
            Sdk.d2().organisationUnitModule().organisationUnits()
                .byParentUid().eq(parentOrgUnitUid)
                .blockingGet()
        } catch (exception: Exception) {
            // Handle exceptions
            exception.printStackTrace()
            println("User's organization unit error ${exception.message}")
            null
        }
    }

    // Function to get the logged-in user's organization and its children
    private fun getUserOrganizationAndChildren() {
        Log.e("TAG", "Starting to search locations....")
        try {
            // Get the logged-in user's assigned organization unit
            val userOrgUnit = getUserAssignedOrgUnit()

            if (userOrgUnit != null) {
                Log.e("TAG", "Logged-in user's organization unit: ${userOrgUnit.uid()}")

                // Get all child organization units
                val childOrgUnits = getAllChildOrgUnits(userOrgUnit.uid())

                if (childOrgUnits != null) {
                    for (childOrgUnit in childOrgUnits) {
                        Log.e(
                            "TAG",
                            "Child Organization Unit: ${childOrgUnit.uid()} - ${childOrgUnit.displayName()}"
                        )
                        // Process each child organization unit as needed
                    }
                } else {
                    Log.e("TAG", "Failed to retrieve child organization units.")
                }
            } else {
                Log.e("TAG", "User's initial organization unit not found.")
            }
        } catch (exception: Exception) {
            // Handle exceptions
            exception.printStackTrace()
            Log.e("TAG", "User's organization unit error ${exception.message}")
        }
    }

    /*  private fun showOrganizations() {
          try {
              // Get the logged-in user's organization unit
              val userOrgUnit = Sdk.d2().userModule().user().get().blockingGet().or

              if (userOrgUnit != null) {
                  println("Logged-in user's organization unit: ${userOrgUnit.uid()}")

                  // Get all child organization units
                  val childOrgUnits = getAllChildOrgUnits(userOrgUnit.uid())

                  if (childOrgUnits != null) {
                      for (childOrgUnit in childOrgUnits) {
                          println("Child Organization Unit: ${childOrgUnit.uid()} - ${childOrgUnit.displayName()}")
                          // Process each child organization unit as needed
                      }
                  } else {
                      println("Failed to retrieve child organization units.")
                  }
              } else {
                  println("User's organization unit not found.")
              }
          } catch (exception: Exception) {
              // Handle exceptions
              exception.printStackTrace()
          }
       *//*   val data = d2().organisationUnitModule().organisationUnits()
            .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
            .get()
            .subscribeOn(Schedulers.io())  // Perform the network request on a background thread
            .observeOn(AndroidSchedulers.mainThread())  // Observe the result on the main thread for UI updates
            .subscribe(
                // OnSuccess - organizations loaded successfully
                { organisationUnits ->
                    // Create a map to store the parent-child relationships
                    val parentChildMap = mutableMapOf<String, List<OrganisationUnit>>()

                    // Populate the parentChildMap
                    for (organisationUnit: OrganisationUnit in organisationUnits) {
                        val parentId = organisationUnit.parent() ?: ""
                        val children = parentChildMap.getOrDefault(parentId, mutableListOf())
                        parentChildMap[parentId.toString()] =
                            children.toMutableList().apply { add(organisationUnit) }

                    }

                    // Display the hierarchy in an AlertDialog
                    showHierarchyDialog(requireContext(), parentChildMap)
                },
                // OnError - handle errors
                { throwable ->
                    // Handle error (e.g., show an error message)
                }
            )
*//*
    }
*/

    // Function to display the hierarchy in an AlertDialog
    fun showHierarchyDialog(context: Context, parentChildMap: Map<String, List<OrganisationUnit>>) {
        // Prepare data for the ExpandableListView
        val groupData = mutableListOf<Map<String, String>>()
        val childData = mutableListOf<List<Map<String, String?>>>()

        for ((parentUid, children) in parentChildMap) {
            val parentMap = mapOf("parentName" to getOrganisationUnitName(parentUid))
            groupData.add(parentMap)

            val childList = mutableListOf<Map<String, String?>>()
            for (child in children) {
                val childMap = mapOf("childName" to child.displayName())
                childList.add(childMap)
            }
            childData.add(childList)
        }

        // Set up the ExpandableListView
        val groupFrom = arrayOf("parentName")
        val groupTo = intArrayOf(android.R.id.text1)
        val childFrom = arrayOf("childName")
        val childTo = intArrayOf(android.R.id.text1)

        val expandableListAdapter: ExpandableListAdapter = SimpleExpandableListAdapter(
            context,
            groupData,
            android.R.layout.simple_expandable_list_item_1,
            groupFrom,
            groupTo,
            childData,
            android.R.layout.simple_expandable_list_item_1,
            childFrom,
            childTo
        )

        // Create and show the AlertDialog with ExpandableListView
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Organisation Unit Hierarchy")
        val expandableListView = ExpandableListView(context)
        expandableListView.setAdapter(expandableListAdapter)
        builder.setView(expandableListView)

        // Set up the positive button (if needed)
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }

        // Show the AlertDialog
        builder.create().show()
    }

    // Function to get the name of an OrganisationUnit given its UID
    fun getOrganisationUnitName(organisationUnitUid: String): String {
        // You may need to fetch the name from your local data or make an additional API call
        // For simplicity, this function returns the UID if the name is not available
        return d2().organisationUnitModule().organisationUnits().uid(organisationUnitUid)
            .blockingGet()?.displayName()
            ?: organisationUnitUid
    }
}
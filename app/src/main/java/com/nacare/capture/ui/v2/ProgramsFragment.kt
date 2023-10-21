package com.nacare.capture.ui.v2

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.androidskeletonapp.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.nacare.capture.adapters.TreeAdapter
import com.nacare.capture.data.Sdk.d2
import com.nacare.capture.data.service.SyncStatusHelper
import com.nacare.capture.models.OrgTreeNode
import com.nacare.capture.ui.v2.room.Converters
import com.nacare.capture.ui.v2.room.MainViewModel
import com.nacare.capture.ui.v2.room.OrganizationData
import com.nacare.capture.utils.AppUtils
import org.hisp.dhis.android.core.user.User


/**
 * A simple [Fragment] subclass.
 * Use the [ProgramsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProgramsFragment : Fragment() {
    private val hashMap = mutableMapOf<String, String>()
    private lateinit var viewModel: MainViewModel
    private lateinit var dialog: AlertDialog
    private lateinit var edtOrg: TextInputEditText
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_programs, container, false)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

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
        edtOrg = view.findViewById(R.id.edt_org)
        edtOrg.apply {
            isFocusable = false
            isCursorVisible = false
            keyListener = null
            setOnClickListener {
                val org = viewModel.loadOrganizations(requireActivity())
                if (!org.isNullOrEmpty()) {
                    showOrgUnitDialog(org)
                } else {
                    AppUtils().showNoOrgUnits(requireContext())
                }
            }
        }
        view.findViewById<TextInputLayout>(R.id.org_holder).apply {
            setOnClickListener {

            }
        }
        view.findViewById<MaterialButton>(R.id.btn_next).apply {
            setOnClickListener {
                val name = view.findViewById<TextInputEditText>(R.id.edt_org).text.toString()
                val date = view.findViewById<TextInputEditText>(R.id.edt_date).text.toString()

                if (date.isEmpty()) {
                    view.findViewById<TextInputLayout>(R.id.date_holder).error =
                        "Please select date"
                    view.findViewById<TextInputEditText>(R.id.edt_date).requestFocus()
                    return@setOnClickListener
                }
                openEventsFragment()
            }
        }

        return view
    }

    private fun showOrgUnitDialog(org: List<OrganizationData>) {
        val or = org.firstOrNull()
        if (or != null) {
            val treeNodes = mutableListOf<OrgTreeNode>()
            try {
                val converters = Converters().fromJsonOrgUnit(or.children)
                val orgNode = OrgTreeNode(
                    label = converters.name,
                    code = converters.id,
                    children = AppUtils().generateChild(converters.children)
                )
                treeNodes.add(orgNode)

            } catch (e: Exception) {
                e.printStackTrace()
            }

            val dialogBuilder = AlertDialog.Builder(requireActivity())
            val inflater = layoutInflater
            val dialogView = inflater.inflate(R.layout.dialog_tree, null)
            dialogBuilder.setView(dialogView)

            val recyclerView: RecyclerView = dialogView.findViewById(R.id.recyclerView)
            val adapter = TreeAdapter(requireContext(), treeNodes, this::selectOrganization)
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(requireContext())

            dialogBuilder.setPositiveButton("OK") { dialog, which ->
                // Handle positive button click if needed
            }

            dialog = dialogBuilder.create()
            dialog.show()
        }
    }

    private fun selectOrganization(data: OrgTreeNode) {
        edtOrg.apply {
            setText(data.label)
            hashMap[data.label] = data.code
            if (::dialog.isInitialized && dialog.isShowing) {
                dialog.dismiss()
            }
        }
    }


    private fun generateCode(value: String): String {
        return hashMap[value].toString()
    }

    private fun initOrganizations() {
        val organizationList = mutableListOf<String>()
        val org = viewModel.loadOrganizations(requireActivity())
        if (!org.isNullOrEmpty()) {
            org.forEach {
                hashMap[it.name] = it.code
                organizationList.add(it.name)
            }
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                organizationList
            )
        }
    }


    private fun openEventsFragment() {
        val fragmentB = EventsFragment()
        val transaction: FragmentTransaction =
            requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, fragmentB)
        transaction.addToBackStack(null)  // Optional: Add to back stack to enable back navigation
        transaction.commit()
    }


}
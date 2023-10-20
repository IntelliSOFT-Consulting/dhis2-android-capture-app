package com.nacare.capture.ui.v2

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.android.androidskeletonapp.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.nacare.capture.data.Sdk.d2
import com.nacare.capture.utils.AppUtils
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

    private fun getUserOrganizationAndChildren() {
        val user = getUser()
        Log.e("TAG", "User Data $user")
    }

    private fun getUser(): User {
        return d2().userModule().user().blockingGet()
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
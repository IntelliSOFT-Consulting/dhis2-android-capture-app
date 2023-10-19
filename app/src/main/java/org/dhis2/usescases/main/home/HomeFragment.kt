package org.dhis2.usescases.main.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.dhis2.R
import org.dhis2.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.apply {
          /*  edtDate.setText(formatterClass.getFormattedDateMonth())
            disableTextInputEditText(edtDate)
            disableTextInputEditText(edtOrg)*/
        }

        binding.edtDate.apply {
            setOnClickListener {
               /* showDatePickerDialog(
                    requireContext(), binding.edtDate, setMaxNow = true, setMinNow = false
                )*/
            }
        }
        binding.edtOrg.apply {
            setOnClickListener {

            }
        }
        binding.orgHolder.apply {

        }
        binding.btnNext.apply {
            setOnClickListener {
//                val code = generateCode(binding.edtOrg.text.toString())
                val name = binding.edtOrg.text.toString()
                val date = binding.edtDate.text.toString()

                if (name.isEmpty()) {
                    binding.orgHolder.error = "Please select unit"
                    binding.edtOrg.requestFocus()
                    return@setOnClickListener
                }

            }
        }

        return binding.root
    }

}
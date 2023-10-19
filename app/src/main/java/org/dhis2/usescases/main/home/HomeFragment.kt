package org.dhis2.usescases.main.home

import android.app.DatePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import org.dhis2.Bindings.clipWithRoundedCorners
import org.dhis2.Bindings.dp
import org.dhis2.android.rtsm.data.AppConfig
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.orgunitselector.OUTreeFragment
import org.dhis2.databinding.FragmentHomeBinding
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.main.program.ProgramPresenter
import org.dhis2.usescases.main.program.ProgramView
import org.dhis2.usescases.main.program.ProgramViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject


class HomeFragment : FragmentGlobalAbstract(), ProgramView {

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
            isFocusable = false
            isCursorVisible = false
            keyListener = null
            setOnClickListener {
                showDatePicker()
            }
        }
        binding.edtOrg.apply {
            isFocusable = false
            isCursorVisible = false
            keyListener = null
            setOnClickListener {
                openOrgUnitTreeSelector()
            }
        }
        binding.orgHolder.apply {
            setOnClickListener {
                openOrgUnitTreeSelector()
            }
        }
        binding.btnNext.apply {
            setOnClickListener {
//                val code = generateCode(binding.edtOrg.text.toString())
                val name = binding.edtOrg.text.toString()
                val date = binding.edtDate.text.toString()
                if (date.isEmpty()) {
                    binding.orgHolder.error = "Please select date "
                    binding.edtOrg.requestFocus()
                    return@setOnClickListener
                }
                if (name.isEmpty()) {
                    binding.orgHolder.error = "Please select unit"
                    binding.edtOrg.requestFocus()
                    return@setOnClickListener
                }

                saveDataToSharedPreferences(date, name)
            }
        }
        return binding.root


    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog =
            DatePickerDialog(
                requireContext(),
                { _: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val selectedDate = dateFormat.format(Date(year - 1900, monthOfYear, dayOfMonth))

                    binding.edtDate.setText(selectedDate)
                },
                year,
                month,
                day
            )

        // Set date picker dialog limits (optional)
        /* if (setMaxNow) {*/
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis() + (1000 * 60 * 60 * 24)
        /*   }
           if (setMinNow) {
               datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
           }*/

        datePickerDialog.show()
    }

    private fun saveDataToSharedPreferences(date: String, name: String) {
        // Get the SharedPreferences instance
        val sharedPreferences: SharedPreferences =
            requireContext().getSharedPreferences("sharedPrefFile", Context.MODE_PRIVATE)

        // Use an editor to modify the SharedPreferences
        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        // Put data into SharedPreferences
        editor.putString("org", name)
        editor.putString("date", date)

        // Apply changes
        editor.apply()
    }

    private fun retrieveDataFromSharedPreferences() {
        // Get the SharedPreferences instance
        val sharedPreferences: SharedPreferences =
            requireContext().getSharedPreferences("sharedPrefFile", Context.MODE_PRIVATE)

        // Retrieve data from SharedPreferences
        val date: String? = sharedPreferences.getString("date", null)
        val org: Int = sharedPreferences.getInt("org", 0)

    }

    override fun swapProgramModelData(programs: List<ProgramViewModel>) {

    }

    override fun showFilterProgress() {

    }

    override fun openOrgUnitTreeSelector() {
        OUTreeFragment.Builder()
            .showAsDialog()
            .withPreselectedOrgUnits(
                FilterManager.getInstance().orgUnitFilters.map { it.uid() }.toMutableList()
            )
            .onSelection { selectedOrgUnits ->
                Log.e("TAG", "Selected Organization $selectedOrgUnits")
                if (selectedOrgUnits.isNotEmpty()) {
                    // Get the first entry
                    val firstUnit = selectedOrgUnits.first()

                    // Extract uid and displayName from the first entry
                    val uid = firstUnit.uid()
                    val displayName = firstUnit.displayName()

                    binding.edtOrg.setText(displayName)

                    // Print or use the extracted values
                    println("UID: $uid, DisplayName: $displayName")
                } else {
                    println("The list is empty.")
                }
            }
            .build()
            .show(childFragmentManager, "OUTreeFragment")
    }

    override fun showHideFilter() {

    }

    override fun clearFilters() {

    }

    override fun navigateTo(program: ProgramViewModel) {

    }

    override fun navigateToStockManagement(config: AppConfig) {

    }

    override fun showSyncDialog(program: ProgramViewModel) {

    }
}
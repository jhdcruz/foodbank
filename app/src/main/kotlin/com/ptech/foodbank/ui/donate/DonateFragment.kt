package com.ptech.foodbank.ui.donate

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK
import com.ptech.foodbank.databinding.FragmentDonateBinding
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.TimeZone

class DonateFragment : Fragment() {

    private var _binding: FragmentDonateBinding? = null
    private val binding get() = _binding!!

    private lateinit var bankReference: MaterialTextView
    private lateinit var foodCategory: TextInputLayout
    private lateinit var foodServing: TextInputLayout
    private lateinit var datePicker: TextInputLayout
    private lateinit var timePicker: TextInputLayout
    private lateinit var pickupAddress: TextInputLayout
    private lateinit var additionalInfo: TextInputLayout
    private lateinit var cancelDonate: MaterialButton

    private var currentSelectedDate: Long? = null
    private var selectedHour: Int? = null
    private var selectedMinute: Int? = null

    // we'll use this to get the arguments passed from the previous fragment
    // which is the bank name, for referencing in pushing data
    private val args: DonateFragmentArgs by navArgs()
    private val editable = Editable.Factory.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDonateBinding.inflate(inflater, container, false)
        val view = binding.root

        bankReference = binding.bankReference
        foodCategory = binding.foodCategory
        foodServing = binding.foodServing
        datePicker = binding.datePicker
        timePicker = binding.timePicker
        pickupAddress = binding.pickupAddress
        additionalInfo = binding.additionalInfo
        cancelDonate = binding.cancelDonate

        cancelDonate.setOnClickListener {
            // navigate back up
            view.findNavController().navigateUp()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // use the bank reference as a mock toolbar title
        bankReference.text = args.bank

        datePicker.setStartIconOnClickListener { showDatePicker() }
        timePicker.setStartIconOnClickListener { showTimePicker() }
    }

    private fun showDatePicker() {
        val today = MaterialDatePicker.todayInUtcMilliseconds()
        val calendar = Calendar.getInstance(TimeZone.getDefault())

        calendar.timeInMillis = today
        calendar[Calendar.MONTH] = Calendar.JANUARY
        val janThisYear = calendar.timeInMillis

        calendar.timeInMillis = today
        calendar[Calendar.MONTH] = Calendar.DECEMBER
        val decThisYear = calendar.timeInMillis

        // date constraints to current year
        val constraintsBuilder =
            CalendarConstraints.Builder()
                .setStart(janThisYear)
                .setEnd(decThisYear)

        MaterialDatePicker.Builder.datePicker()
            .setTitleText("Date of Pickup")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .setCalendarConstraints(constraintsBuilder.build())
            .build()
            .apply {
                addOnPositiveButtonClickListener { dateInMillis -> onDateSelected(dateInMillis) }
            }
            .show(parentFragmentManager, MaterialDatePicker::class.java.canonicalName)
    }

    private fun onDateSelected(dateTimeStampInMillis: Long) {
        currentSelectedDate = dateTimeStampInMillis
        val dateTime: LocalDateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(currentSelectedDate!!),
            ZoneId.systemDefault()
        )
        val dateAsFormattedText: String = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        datePicker.editText?.text = editable.newEditable(dateAsFormattedText)
    }

    private fun showTimePicker() {
        val hour = selectedHour ?: LocalDateTime.now().hour
        val minute = selectedMinute ?: LocalDateTime.now().minute

        MaterialTimePicker.Builder()
            .setTitleText("Time of Pickup")
            .setInputMode(INPUT_MODE_CLOCK)
            .setHour(hour)
            .setMinute(minute)
            .build()
            .apply {
                addOnPositiveButtonClickListener { onTimeSelected(this.hour, this.minute) }
            }
            .show(parentFragmentManager, MaterialTimePicker::class.java.canonicalName)
    }

    @Suppress("MagicNumber")
    private fun onTimeSelected(hour: Int, minute: Int) {
        selectedHour = hour
        selectedMinute = minute

        val hourAsText = if (hour < 10) "0$hour" else hour
        val minuteAsText = if (minute < 10) "0$minute" else minute

        "$hourAsText:$minuteAsText".also { timePicker.editText?.text = editable.newEditable(it) }
    }
}

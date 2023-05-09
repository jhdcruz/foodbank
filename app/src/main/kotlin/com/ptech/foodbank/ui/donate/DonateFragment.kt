package com.ptech.foodbank.ui.donate

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
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
import com.google.firebase.Timestamp
import com.ptech.foodbank.data.Donation
import com.ptech.foodbank.databinding.FragmentDonateBinding
import com.ptech.foodbank.db.FirebaseFactoryImpl
import com.ptech.foodbank.utils.Auth.getAuth
import com.ptech.foodbank.utils.Feedback.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

private const val MILLISECOND = 1000
private const val MINUTE = 60

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
    private lateinit var submitDonation: MaterialButton
    private lateinit var cancelDonate: MaterialButton

    private var currentSelectedDate: Long? = null
    private var selectedHour: Int? = null
    private var selectedMinute: Int? = null

    // we'll use this to get the arguments passed from the previous fragment
    // which is the bank name, for referencing in pushing data
    private val args: DonateFragmentArgs by navArgs()
    private val editable = Editable.Factory.getInstance()
    private val currentUser = getAuth.currentUser

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
        submitDonation = binding.submitDonation
        cancelDonate = binding.cancelDonate

        cancelDonate.setOnClickListener {
            // navigate back up
            view.findNavController().navigateUp()
        }

        return view
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // use the bank reference as a mock toolbar title
        bankReference.text = args.bank

        // text fields listeners
        fieldsListener()

        submitDonation.setOnClickListener {
            if (!validateFields()) {
                requireContext().showToast("Please fill in all required fields")
            } else {
                submitDonation.text = "Submitting..."
                submitDonation.isEnabled = false

                val data = Donation(
                    bank = args.bank.toString(),
                    category = foodCategory.editText?.text.toString(),
                    serving = foodServing.editText?.text.toString().toInt(),
                    pickupDate = processPickupDate(),
                    dateCreated = Timestamp.now(),
                    notes = additionalInfo.editText?.text.toString(),
                    pickupAddress = pickupAddress.editText?.text.toString(),
                    donor = mapOf<String, Any>(
                        "id" to currentUser?.uid!!.toString(),
                        "name" to currentUser.displayName!!.toString(),
                        "email" to currentUser.email!!.toString(),
                        "phone" to currentUser.phoneNumber!!.toString()
                    )
                )

                CoroutineScope(Dispatchers.Default).launch {
                    sendDonation(data)
                }
            }
        }
    }

    private fun fieldsListener() {
        // date and time pickers
        datePicker.setStartIconOnClickListener { showDatePicker() }
        timePicker.setStartIconOnClickListener { showTimePicker() }

        // errors
        foodCategory.editText?.addTextChangedListener {
            foodCategory.isErrorEnabled = false
        }

        foodServing.editText?.addTextChangedListener {
            foodServing.isErrorEnabled = false
        }

        datePicker.editText?.addTextChangedListener {
            datePicker.isErrorEnabled = false
        }

        timePicker.editText?.addTextChangedListener {
            timePicker.isErrorEnabled = false
        }

        pickupAddress.editText?.addTextChangedListener {
            pickupAddress.isErrorEnabled = false
        }
    }

    /** Process date and time from the fields to be saved as firestore's timestamp */
    private fun processPickupDate(): Timestamp {
        // combine date and time from the fields to return date
        val dateTime = Date(
            currentSelectedDate!! +
                (selectedHour!! * MINUTE * MINUTE * MILLISECOND) +
                (selectedMinute!! * MINUTE * MILLISECOND)
        )

        // convert to timestamp,
        // timestamp requires Date over LocalDateTime
        return Timestamp(dateTime)
    }

    private fun showDatePicker() {
        val today = MaterialDatePicker.todayInUtcMilliseconds()

        // date constraints to "today"
        val constraintsBuilder =
            CalendarConstraints.Builder()
                .setStart(today)

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

    /** Validate required fields */
    private fun validateFields(): Boolean {
        val errorMessage = "Field is required"
        var timeErrors = false

        val category = foodCategory.editText?.text.toString()
        val serving = foodServing.editText?.text.toString()
        val date = datePicker.editText?.text.toString()
        val time = timePicker.editText?.text.toString()
        val address = pickupAddress.editText?.text.toString()

        if (category.isEmpty()) {
            foodCategory.isErrorEnabled = true
            foodCategory.error = errorMessage
        }

        if (serving.isEmpty()) {
            foodServing.isErrorEnabled = true
            foodServing.error = errorMessage
        }

        if (date.isEmpty()) {
            datePicker.isErrorEnabled = true
            datePicker.error = errorMessage
        }

        // check if date is from previous and not today
        if (currentSelectedDate!! < MaterialDatePicker.todayInUtcMilliseconds()) {
            datePicker.isErrorEnabled = true
            datePicker.error = "Date cannot be from previous days"
            timeErrors = true
        }

        // check if time is has passed
        if (selectedHour!! < LocalDateTime.now().hour) {
            timePicker.isErrorEnabled = true
            timePicker.error = "Time has passed, set a future time"
            timeErrors = true
        }

        if (time.isEmpty()) {
            timePicker.isErrorEnabled = true
            timePicker.error = errorMessage
        }

        if (address.isEmpty()) {
            pickupAddress.isErrorEnabled = true
            pickupAddress.error = errorMessage
        }

        return category.isNotEmpty() &&
            serving.isNotEmpty() &&
            date.isNotEmpty() &&
            time.isNotEmpty() &&
            address.isNotEmpty() &&
            timeErrors.not()
    }

    private suspend fun sendDonation(data: Donation) {
        val db = FirebaseFactoryImpl()

        db.addDonation(data)
            .addOnSuccessListener {
                // navigate back up
                requireContext().showToast("Donation offer sent successfully")
                view?.findNavController()?.navigateUp()
            }
            .addOnFailureListener {
                requireContext().showToast("Failed to send donation offer")
            }
            .addOnCanceledListener {
                requireContext().showToast("Donation offer cancelled")
            }
            .await()
    }
}

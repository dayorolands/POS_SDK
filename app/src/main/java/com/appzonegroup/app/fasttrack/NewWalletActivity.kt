package com.appzonegroup.app.fasttrack

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.appzonegroup.app.fasttrack.databinding.ActivityOpenAccountBinding
import com.appzonegroup.app.fasttrack.utility.Misc
import com.appzonegroup.app.fasttrack.utility.online.ImageUtils
import com.crashlytics.android.Crashlytics
import com.creditclub.core.data.request.CustomerRequest
import com.creditclub.core.ui.CreditClubFragment
import com.creditclub.core.ui.widget.DateInputParams
import com.creditclub.core.util.*
import com.creditclub.core.util.delegates.contentView
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.features.ReturnMode
import com.esafirm.imagepicker.model.Image
import kotlinx.android.synthetic.main.fragment_agent_pin_submit_btn.*
import kotlinx.android.synthetic.main.fragment_customer_request_general_info.*
import kotlinx.android.synthetic.main.fragment_customer_request_general_info.view.*
import kotlinx.android.synthetic.main.fragment_next_of_kin.*
import kotlinx.android.synthetic.main.fragment_next_of_kin.view.*
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import java.io.File
import java.io.FileOutputStream

class NewWalletActivity : BaseActivity() {
    private val binding by contentView<NewWalletActivity, ActivityOpenAccountBinding>(
        R.layout.activity_open_account
    )

    private var dob: String = ""
    private var agentPIN = ""

    private var surname: String = ""
    private var firstName: String = ""
    private var phoneNumber: String = ""
    private var gender: String = ""
    private var address: String = ""
    private var placeOfBirth: String = ""
    private var starterPackNo: String = ""

    private var imageType: ImageType = ImageType.Passport

    private var passportString: String? = null

    private val customerRequest by lazy {
        CustomerRequest().apply {
            uniqueReferenceID = Misc.getGUID()
        }
    }

    private var im: ImageView? = null

    private var bitmap: Bitmap? = null

    internal enum class ImageType {
        //        IDCard,
        Passport,
//        Signature
    }

    internal enum class Form {
        GENERAL_INFO,
        CONTACT_DETAILS,
        //        PHOTO_CAPTURE,
        OTHER_DETAILS
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val adapter = SectionsPagerAdapter(supportFragmentManager)

        binding.container.adapter = adapter
        binding.tabs.setupWithViewPager(binding.container)
        binding.tabs.clearOnTabSelectedListeners()
        binding.container.offscreenPageLimit = adapter.count
    }

    internal fun indicateError(message: String, position: Int, view: EditText?) {
        view?.error = message
        view?.requestFocus()
    }

    private fun compressImage(imageFile: File): Bitmap? {
        return try {
            val file = File(imageFile.absolutePath)

            val fOut = FileOutputStream(file)
            val newBitmap = ImageUtils.getResizedBitmap(bitmap!!, 400)
            newBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut)
            fOut.flush()
            fOut.close()

            newBitmap
        } catch (e: Exception) {
            Crashlytics.logException(e)
            e.printStackTrace()
            if (BuildConfig.DEBUG) Log.e("Image", "Save file error!$e")

            null
        }
    }

    fun createAccount_click(view: View) {
//        if (starter_pack_number_et.value.isEmpty()) {
//            indicateError(
//                "Please enter the card serial number",
//                Form.PHOTO_CAPTURE.ordinal,
//                starter_pack_number_et
//            )
//            return
//        }

        starterPackNo = starter_pack_number_et.value

        agentPIN = agent_pin_et.value
        if (agentPIN.isEmpty()) {
            indicateError("Please enter your PIN", Form.OTHER_DETAILS.ordinal, agent_pin_et)
            return
        }

        if (agentPIN.length != 4) {
            indicateError("Please enter the correct PIN", Form.OTHER_DETAILS.ordinal, agent_pin_et)
            return
        }

        val location = String.format("%s;%s", gps.longitude.toString(), gps.latitude.toString())

        customerRequest.customerLastName = surname_et.value
        customerRequest.customerFirstName = first_name_et.value
        customerRequest.dateOfBirth = dob
        customerRequest.placeOfBirth = place_of_birth_et.value
        customerRequest.customerPhoneNumber = phone_et.value
        customerRequest.gender = gender
        customerRequest.geoLocation = location
        customerRequest.starterPackNumber = starter_pack_number_et.value
        customerRequest.nokName = nok_name_et.value
        customerRequest.nokPhone = nok_phone_et.value
        customerRequest.address = address_et.value
        customerRequest.agentPhoneNumber = localStorage.agentPhone
        customerRequest.agentPin = agentPIN
        customerRequest.institutionCode = localStorage.institutionCode

        val additionalInformation = CustomerRequest.Additional()
        additionalInformation.passport = passportString
        additionalInformation.email = email_et.value

        //additionalInformation.setIDCard(idCardString);
        /*additionalInformation.setMiddleName(middleName);
        additionalInformation.setOccupation(occupation);*/

        //additionalInformation.setSignature(signatureString);
        //additionalInformation.setProvince(province);

        customerRequest.additionalInformation = additionalInformation.toJson()

        mainScope.launch {
            showProgressBar("Creating customer wallet")

            val service = creditClubMiddleWareAPI.staticService

            val (response, error) = safeRunIO {
                service.register(customerRequest)
            }
            hideProgressBar()

            if (error.hasOccurred) return@launch showError(error!!)
            response ?: return@launch showNetworkError()

            if (response.isSuccessful) {
                showSuccess<Unit>(getString(R.string.customer_was_created_successfully)) {
                    onClose {
                        finish()
                    }
                }
            } else {
                showError(
                    response.responseMessage
                        ?: getString(R.string.an_error_occurred_please_try_again_later)
                )
            }
        }
    }

    override fun onBackPressed() {
        if (binding.container.currentItem > 0) {
            binding.container.setCurrentItem(binding.container.currentItem - 1, true)
        } else {
            super.onBackPressed()
        }
    }

    fun next_button_click1(view: View) {

        surname = surname_et.value

        if (surname.isEmpty()) {
            indicateError("Please enter customer's surname", Form.GENERAL_INFO.ordinal, surname_et)

            Crashlytics.logException(Exception("incorrect user name"))
            Crashlytics.log("this is a crash")
            return
        }

        if (!validate("Last name", surname)) return

        firstName = first_name_et.value
        if (firstName.isEmpty()) {
            indicateError(
                "Please enter customer's first name",
                Form.GENERAL_INFO.ordinal,
                first_name_et
            )
            return
        }

        if (!validate("First name", firstName)) return

        if (gender_spinner.selectedItemPosition == 0) {
            showError("Please select a gender")
            // indicateError("Please select a gender", Form.GENERAL_INFO.ordinal(), genderSpinner);
            return
        }

        gender = gender_spinner.selectedItem.toString()

        phoneNumber = phone_et.value
        if (phoneNumber.isEmpty()) {
            indicateError(
                "Please enter customer's phone number",
                Form.GENERAL_INFO.ordinal,
                phone_et
            )
            return
        }

        if (phoneNumber.length != 11) {
            indicateError(
                "Customer's phone number must be 11 digits",
                Form.GENERAL_INFO.ordinal,
                phone_et
            )
            return
        }

        address = address_et.value

        if (address.isEmpty()) {
            indicateError(
                "Please enter customer's address",
                Form.CONTACT_DETAILS.ordinal,
                address_et
            )
            return
        }

        if (TextPatterns.invalidAddress.matcher(address).find()) {
            indicateError(
                getString(R.string.please_enter_a_valid_customer_address),
                Form.CONTACT_DETAILS.ordinal,
                address_et
            )
            return
        }

        placeOfBirth = place_of_birth_et.value
        if (placeOfBirth.isEmpty()) {
            indicateError(
                "Please enter customer's place of birth",
                Form.CONTACT_DETAILS.ordinal,
                place_of_birth_et
            )
            return
        }

        val email = email_et.value

        if (email.isNotEmpty() && !email.isValidEmail()) {
            return showError(getString(R.string.email_is_invalid))
        }

        if (!validate("Place of birth", placeOfBirth)) return

        dob = dob_tv.value
        if (dob.contains("Click")) {
            showError("Please enter customer's date of birth")
            return
        }

        binding.container.setCurrentItem(binding.container.currentItem + 1, true)
    }

    fun next_button_click2(view: View) {

        if (nok_name_et.value.isEmpty()) {
            indicateError(
                "Please enter the name of your next of kin",
                Form.CONTACT_DETAILS.ordinal,
                nok_name_et
            )
            return
        }

        if (!validate("Next of kin name", "${nok_name_et.text}")) return

        if (nok_phone_et.value.isEmpty()) {
            indicateError(
                "Please enter the phone number of the next of kin",
                Form.CONTACT_DETAILS.ordinal,
                nok_phone_et
            )
            return
        }

        if (nok_phone_et.value.length != 9 && nok_phone_et.value.length != 11) {
            indicateError(
                "Please enter the correct phone number of the next of kin",
                Form.CONTACT_DETAILS.ordinal,
                nok_phone_et
            )
            return
        }


        binding.container.setCurrentItem(binding.container.currentItem + 1, true)
    }

    private fun validate(name: String, value: String, required: Boolean = true): Boolean {
        if (required && value.isEmpty()) {
            showError(resources.getString(R.string.field_is_required, name))
            return false
        }

        if (value.includesSpecialCharacters() || value.includesNumbers()) {
            showError(resources.getString(R.string.special_characters_not_permitted, name))
            return false
        }

        return true
    }

    fun image_upload_next_clicked(view: View) {
        if (passportString == null) {
            showError(getString(R.string.please_upload_customer_passport_photo))
            return
        }

        binding.container.setCurrentItem(binding.container.currentItem + 1, true)
    }

    fun show_calendar(view: View) {}

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_open_account, menu);
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            android.R.id.home -> if (binding.container.currentItem > 0) {
                binding.container.setCurrentItem(binding.container.currentItem - 1, true)
                true
            } else {
                super.onOptionsItemSelected(item)
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    class GeneralInfoFragment : CreditClubFragment() {

        private val dateInputParams:DateInputParams by lazy {
            DateInputParams("Date of birth", maxDate = LocalDate.now().minusYears(18))
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {

            val root = inflater.inflate(
                R.layout.fragment_customer_request_general_info,
                container,
                false
            )

            root.surname_et.isEnabled = true
            root.first_name_et.isEnabled = true
            root.phone_et.isEnabled = true
            root.address_et.isEnabled = true
            root.dob_tv.isEnabled = true
            root.email_et.isEnabled = true

            root.dob_tv.setOnClickListener {

                activity.showDateInput(dateInputParams) {
                    onSubmit { date ->
                        root.dob_tv.value = "${date.year}-${date.month + 1}-${date.dayOfMonth}"
                        root.dob_tv.gravity = Gravity.START
                    }
                }
            }

            return root
        }
    }

    class NextOfKINFragment : Fragment() {

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val rootView = inflater.inflate(R.layout.fragment_next_of_kin, container, false)

            (activity as NewWalletActivity).addValidPhoneNumberListener(rootView.nok_phone_et)

            return rootView
        }
    }

    class DocumentUploadFragment : Fragment() {
        val activity get() = getActivity() as NewWalletActivity

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {

            val rootView = inflater.inflate(R.layout.fragment_document_upload, container, false)

            rootView.findViewById<View>(R.id.passport_gallery_btn).setOnClickListener {
                activity.imageType = ImageType.Passport
                ImagePicker.create(this).returnMode(ReturnMode.ALL)
                    .folderMode(true)
                    .single().single().showCamera(false).start()
            }

            rootView.findViewById<View>(R.id.passport_takePhoto_btn).setOnClickListener {
                activity.imageType = ImageType.Passport
                ImagePicker.cameraOnly().start(this)
            }

            return rootView
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
                activity.run {
                    try {
                        val image: Image? = ImagePicker.getFirstImageOrNull(data)
                        im = view?.findViewById(R.id.passport_image_view)

                        image ?: return@run showInternalError()

                        mainScope.launch {
                            showProgressBar("Processing image")
                            safeRunIO {
                                val imageFile = File(image.path)
                                bitmap = BitmapFactory.decodeFile(image.path)

                                bitmap = compressImage(imageFile)
                                passportString = Misc.bitmapToString(bitmap!!)
                            }
                            im?.setImageBitmap(bitmap)
                            hideProgressBar()
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        showInternalError()
                    }
                }
            } else
                super.onActivityResult(requestCode, resultCode, data)
        }
    }

    class AgentPINFragment : Fragment() {
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(R.layout.fragment_agent_pin_submit_btn, container, false)
        }
    }

    inner class SectionsPagerAdapter(fm: FragmentManager) :
        FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> GeneralInfoFragment()
                1 -> NextOfKINFragment()
                2 -> DocumentUploadFragment()
                3 -> AgentPINFragment()
                else -> GeneralInfoFragment()
            }
        }

        override fun getCount(): Int {
            return 5
        }

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return "General Details"
                1 -> return "Contact Details"
                2 -> return "Document Upload"
                3 -> return "Agent PIN"
            }
            return null
        }
    }
}


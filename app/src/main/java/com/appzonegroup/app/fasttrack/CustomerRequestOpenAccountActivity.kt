package com.appzonegroup.app.fasttrack

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.appzonegroup.app.fasttrack.dataaccess.ProductDAO
import com.appzonegroup.app.fasttrack.databinding.ActivityOpenAccountBinding
import com.appzonegroup.app.fasttrack.receipt.NewAccountReceipt
import com.appzonegroup.app.fasttrack.ui.MySpinnerAdapter
import com.appzonegroup.app.fasttrack.utility.CalendarDialog
import com.appzonegroup.app.fasttrack.utility.FunctionIds
import com.appzonegroup.app.fasttrack.utility.Misc
import com.appzonegroup.app.fasttrack.utility.online.ImageUtils
import com.appzonegroup.creditclub.pos.Platform
import com.creditclub.pos.printer.PrinterStatus
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.creditclub.core.contract.FormDataHolder
import com.creditclub.core.data.model.AccountInfo
import com.creditclub.core.data.model.Product
import com.creditclub.core.data.request.CustomerRequest
import com.creditclub.core.model.CreditClubImage
import com.creditclub.core.type.TokenType
import com.creditclub.core.util.*
import com.creditclub.core.util.delegates.contentView
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.features.ReturnMode
import com.esafirm.imagepicker.model.Image
import kotlinx.android.synthetic.main.fragment_agent_pin_submit_btn.*
import kotlinx.android.synthetic.main.fragment_customer_request_account_info.*
import kotlinx.android.synthetic.main.fragment_customer_request_account_info.view.*
import kotlinx.android.synthetic.main.fragment_customer_request_general_info.*
import kotlinx.android.synthetic.main.fragment_customer_request_general_info.view.*
import kotlinx.android.synthetic.main.fragment_next_of_kin.*
import kotlinx.android.synthetic.main.fragment_next_of_kin.view.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import java.io.File
import java.io.FileOutputStream
import java.util.*

class CustomerRequestOpenAccountActivity : BaseActivity(), FormDataHolder<CustomerRequest> {
    private val binding by contentView<CustomerRequestOpenAccountActivity, ActivityOpenAccountBinding>(
        R.layout.activity_open_account
    )

    override val functionId = FunctionIds.ACCOUNT_OPENING

    internal var lastName: String = ""
    internal var bvn: String = ""
    private var dob: String = ""
    internal var agentPIN = ""

    private var surname: String = ""
    internal var firstName: String = ""
    internal var phoneNumber: String = ""
    internal var gender: String = ""
    internal var address: String = ""
    private var placeOfBirth: String = ""
    private var starterPackNo: String = ""
    private var productName: String = ""
    private var productCode: String = ""

    internal var imageType: ImageType = ImageType.Passport
    internal var productNames: ArrayList<String> = arrayListOf()
    internal var products: List<Product> = emptyList()

    private var passportString: String? = null

    override val formData by lazy {
        CustomerRequest().apply {
            uniqueReferenceID = Misc.getGUID()
        }
    }

    private var im: ImageView? = null

    private var bitmap: Bitmap? = null

    internal enum class ImageType {
        IDCard,
        Passport,
        Signature
    }

    internal enum class Form {
        GENERAL_INFO,
        CONTACT_DETAILS,
        PHOTO_CAPTURE,
        OTHER_DETAILS
    }

    private val receipt by lazy { NewAccountReceipt(this) }

    private val config get() = institutionConfig.flows.accountOpening

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val adapter = SectionsPagerAdapter(supportFragmentManager)

        binding.container.adapter = adapter
        binding.tabs.setupWithViewPager(binding.container)
        binding.tabs.clearOnTabSelectedListeners()
        binding.container.offscreenPageLimit = adapter.count

        val productDAO = ProductDAO(baseContext)
        products = productDAO.GetAll()
        productDAO.close()

        productNames = ArrayList()
        productNames.add("Select product...")

        for (product in products) {
            productNames.add(product.name)
        }
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
            FirebaseCrashlytics.getInstance().recordException(e)
            e.printStackTrace()
            if (BuildConfig.DEBUG) Log.e("Image", "Save file error!$e")

            null
        }
    }

    fun createAccount_click(view: View) {
//        if (starter_pack_number_et.text.toString().trim { it <= ' ' }.isEmpty()) {
//            indicateError(
//                "Please enter the card serial number",
//                Form.PHOTO_CAPTURE.ordinal,
//                starter_pack_number_et
//            )
//            return
//        }

        starterPackNo = starter_pack_number_et.text.toString().trim { it <= ' ' }

        agentPIN = agent_pin_et.text.toString().trim { it <= ' ' }
        if (agentPIN.isEmpty()) {
            indicateError("Please enter your PIN", Form.OTHER_DETAILS.ordinal, agent_pin_et)
            return
        }

        if (agentPIN.length != 4) {
            indicateError("Please enter the correct PIN", Form.OTHER_DETAILS.ordinal, agent_pin_et)
            return
        }

        val location = gps.geolocationString

        formData.customerLastName = surname_et.text.toString().trim { it <= ' ' }
        formData.customerFirstName = first_name_et.text.toString().trim { it <= ' ' }
        formData.dateOfBirth = dob
        formData.placeOfBirth = place_of_birth_et.text.toString().trim { it <= ' ' }
        formData.customerPhoneNumber = phone_et.text.toString().trim { it <= ' ' }
        formData.gender = gender.substring(0, 1).toLowerCase()
        formData.geoLocation = location
        formData.starterPackNumber =
            starter_pack_number_et.text.toString().trim { it <= ' ' }
        formData.address = address_et.text.toString().trim { it <= ' ' }
        formData.productCode = productCode
        formData.productName = productName
        formData.bvn = bvn
        formData.agentPhoneNumber = localStorage.agentPhone
        formData.agentPin = agentPIN
        formData.institutionCode = localStorage.institutionCode

        val additionalInformation = CustomerRequest.Additional()
        additionalInformation.passport = passportString
        additionalInformation.email = email_et.text.toString()
        additionalInformation.middleName = middle_name_et.value
        additionalInformation.title = if (gender == "female") "Ms" else "Mr"
        additionalInformation.country = "NGN"
        additionalInformation.state = states_et.value

        //additionalInformation.setIDCard(idCardString);
        //additionalInformation.setOccupation(occupation);

        //additionalInformation.setSignature(signatureString);
        //additionalInformation.setProvince(province);

        formData.additionalInformation =
            Json(JsonConfiguration.Stable).stringify(CustomerRequest.Additional.serializer(), additionalInformation)

        mainScope.launch {
            showProgressBar("Creating customer account")

            val service = creditClubMiddleWareAPI.staticService

            val (response, error) = safeRunIO {
                service.register(formData)
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

            if (Platform.hasPrinter) {
                receipt.apply {
                    isSuccessful = response.isSuccessful
                    reason = response.responseMessage

                    bvn = formData.bvn
                    institutionCode = localStorage.institutionCode!!
                    agentPhoneNumber = localStorage.agentPhone!!
                    uniqueReferenceID = formData.uniqueReferenceID!!

                    if (response.isSuccessful) {
                        accountName =
                            "${formData.customerFirstName} ${additionalInformation.middleName} ${formData.customerLastName}"

                        response.responseMessage?.run {
                            accountNumber = this
                        }
                    }
                }

                printer.printAsync(receipt) { printerStatus ->
                    if (printerStatus != PrinterStatus.READY) showError(printerStatus.message)
                }
            }
        }
    }

    private fun getCustomerBVN(bvn: String) {

        mainScope.launch {
            showProgressBar("Getting the BVN information...")

            val (result, error) = safeRunIO {
                creditClubMiddleWareAPI.staticService.getCustomerDetailsByBVN(
                    localStorage.institutionCode,
                    bvn
                )
            }

            hideProgressBar()

            if (error != null && error.isKotlinNPE()) return@launch showError("BVN is invalid")
            if (error != null) return@launch showError(error)

            if (result == null) {
                showError("BVN is invalid")
                return@launch
            }

            try {
                phoneNumber = result.phoneNumber
                firstName = result.firstName
                lastName = result.lastName
                dob = result.dob

                //here update the edit texts with the BVN gotten data
                surname_et.setText(lastName)
                surname_et.isFocusable = false

                first_name_et.setText(firstName)
                first_name_et.isFocusable = false

                middle_name_et.value = result.otherNames
                middle_name_et.isFocusable = false

                phone_et.setText(phoneNumber)
                phone_et.isFocusable = false

                dob_tv.text = dob
                dob_tv.isFocusable = false

                val accountInfo = AccountInfo()
                accountInfo.phoneNumber = phoneNumber

                requireAndValidateToken(accountInfo, operationType = TokenType.AccountOpening) {
                    onSubmit {
                        binding.container.setCurrentItem(
                            binding.container.currentItem + 1,
                            true
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                FirebaseCrashlytics.getInstance().recordException(Exception(e.message))

                showError("An error occurred. Please try again")
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

        surname = surname_et.text.toString().trim { it <= ' ' }

        if (surname.isEmpty()) {
            indicateError(
                "Please enter customer's surname",
                Form.GENERAL_INFO.ordinal,
                surname_et
            )

            firebaseCrashlytics.recordException(Exception("incorrect user name"))
            firebaseCrashlytics.log("this is a crash")
            return
        }

        if (!validate("Last name", surname)) return

        val middleName = middle_name_et.value

        if (BuildConfig.FLAVOR == "access") {

            if (middleName.isEmpty()) {
                indicateError(
                    "Please enter customer's middle name",
                    Form.GENERAL_INFO.ordinal,
                    middle_name_et
                )
                return
            }
        }

        if (!validate("Middle name", middleName, required = false)) return

        firstName = first_name_et.text.toString().trim { it <= ' ' }
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

        phoneNumber = phone_et.text.toString().trim { it <= ' ' }
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

        address = address_et.text.toString().trim { it <= ' ' }

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

        placeOfBirth = place_of_birth_et.text.toString().trim { it <= ' ' }
//        if (placeOfBirth.isEmpty()) {
//            indicateError(
//                "Please enter customer's place of birth",
//                Form.CONTACT_DETAILS.ordinal,
//                place_of_birth_et
//            )
//            return
//        }

        val email = email_et.text.toString().trim { it <= ' ' }

        if (email.isEmpty()) {
            return showError(resources.getString(R.string.field_is_required, "Email"))
        }

        if (email.isNotEmpty() && !email.isValidEmail()) {
            return showError(getString(R.string.email_is_invalid))
        }

//        if (!validate("Place of birth", placeOfBirth)) return

        dob = dob_tv.text.toString().trim { it <= ' ' }
        if (dob.contains("Click")) {
            showError("Please enter customer's date of birth")
            return
        }

        binding.container.setCurrentItem(binding.container.currentItem + 1, true)
    }

    fun next_button_click2(view: View) {

        if (nok_name_et.text.toString().trim { it <= ' ' }.isEmpty()) {
            indicateError(
                "Please enter the name of your next of kin",
                Form.CONTACT_DETAILS.ordinal,
                nok_name_et
            )
            return
        }

        if (!validate("Next of kin name", "${nok_name_et.text}")) return

        if (nok_phone_et.text.toString().trim { it <= ' ' }.isEmpty()) {
            indicateError(
                "Please enter the phone number of the next of kin",
                Form.CONTACT_DETAILS.ordinal,
                nok_phone_et
            )
            return
        }

        if (nok_phone_et.text.toString().trim { it <= ' ' }.length != 9 && nok_phone_et.text.toString().trim { it <= ' ' }.length != 11) {
            indicateError(
                "Please enter the correct phone number of the next of kin",
                Form.CONTACT_DETAILS.ordinal,
                nok_phone_et
            )
            return
        }

        formData.nokName = nok_name_et.text.toString().trim { it <= ' ' }
        formData.nokPhone = nok_phone_et.text.toString().trim { it <= ' ' }

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


    fun next_button_click(view: View) {
        when (view.id) {
            R.id.account_info_next_btn -> {
                bvn = bvn_et.text.toString().trim { it <= ' ' }

                if (config.products && product_spinner.selectedItemPosition == 0) {
                    //    indicateError("Please select a product", Form.PHOTO_CAPTURE.ordinal(), productSpinner);
                    showError("Please select a product")
                    return
                }

                if (bvn.length != 11) {
                    showError("Please enter the BVN")
                    return
                }

                if (config.products) {
                    val product = products[product_spinner.selectedItemPosition - 1]
                    productName = product.name
                    productCode = product.code
                }

                getCustomerBVN(bvn)
            }
        }
    }

    fun show_calendar(view: View) {
        val dialog = CalendarDialog.showCalendarDialog(this@CustomerRequestOpenAccountActivity)
        val datePicker = dialog.findViewById<View>(R.id.datePicker) as DatePicker

        dialog.findViewById<View>(R.id.calendarViewButton).setOnClickListener {
            val dayOfMonth = datePicker.dayOfMonth
            val month = datePicker.month + 1

            val DD = if (dayOfMonth > 9) dayOfMonth.toString() + "" else "0$dayOfMonth"
            val MM = if (month > 9) month.toString() + "" else "0$month"

            dob_tv.text = "${datePicker.year}-$MM-$DD"
            dob_tv.gravity = Gravity.START
            dialog.dismiss()
        }
    }

    fun image_upload_next_clicked(view: View) {
        if (passportString == null) {
            showError(getString(R.string.please_upload_customer_passport_photo))
            return
        }

        /*if (idCardString == null)
        {
            showError(getString(R.string.please_upload_customers_id_card));
            return;
        }

        if (signatureString == null)
        {
            showError(getString(R.string.please_upload_customers_signature));
            return;
        }*/

        binding.container.setCurrentItem(binding.container.currentItem + 1, true)
    }

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

    class GeneralInfoFragment : Fragment() {
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val rootView = inflater.inflate(
                R.layout.fragment_customer_request_general_info,
                container,
                false
            )

            rootView.email_et.hint = "Email"
            rootView.place_of_birth_et.visibility = View.GONE

            if (BuildConfig.FLAVOR == "access") {
                rootView.middle_name_et.hint = "Enter middle name"
            }

            return rootView
        }
    }

    class NextOfKINFragment : Fragment() {

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val rootView = inflater.inflate(R.layout.fragment_next_of_kin, container, false)

            (activity as CustomerRequestOpenAccountActivity).addValidPhoneNumberListener(
                rootView.nok_phone_et
            )

            return rootView
        }
    }


    class AccountInfoFragment : Fragment() {
        val activity get() = getActivity() as CustomerRequestOpenAccountActivity

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {

            val rootView =
                inflater.inflate(
                    R.layout.fragment_customer_request_account_info,
                    container,
                    false
                )

            if (activity.config.products) {
                rootView.product_spinner.visibility = View.VISIBLE
                val productAdapter = MySpinnerAdapter(
                    context,
                    android.R.layout.simple_spinner_item,
                    activity.productNames
                )
                productAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                rootView.product_spinner.adapter = productAdapter

                activity.run {

                    mainScope.launch {
                        showProgressBar("Getting Products...")

                        val (products) = safeRunIO {
                            creditClubMiddleWareAPI.staticService.getAllProducts(
                                localStorage.institutionCode,
                                localStorage.agentPhone
                            )
                        }

                        hideProgressBar()

                        products ?: return@launch showNetworkError()

                        this@run.products = products

                        val productDAO = ProductDAO(activity.baseContext)
                        productDAO.Insert(products)

                        productNames = ArrayList()
                        productNames.add("Select product...")
                        for (product in products) {
                            productNames.add(product.name)
                        }
                        productDAO.close()

                        Misc.populateSpinnerWithString(
                            activity,
                            productNames,
                            view?.product_spinner
                        )
                    }
                }
            } else {
                rootView.product_spinner.visibility = View.GONE
            }

            return rootView
        }
    }

    class DocumentUploadFragment : Fragment() {
        val activity get() = getActivity() as CustomerRequestOpenAccountActivity

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {

            val rootView = inflater.inflate(R.layout.fragment_document_upload, container, false)

            /*signatureIV = rootView.findViewById(R.id.signature_image_view);
            idCardIV = rootView.findViewById(R.id.id_card_image_view);*/
//            passportIV = rootView.findViewById(R.id.passport_image_view)

            /*rootView.findViewById(R.id.signature_gallery_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imageType = ImageType.Signature;
                    openGallery(getActivity());
                }
            });

            rootView.findViewById(R.id.id_card_gallery_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imageType = ImageType.IDCard;
                    openGallery(getActivity());
                }
            });*/

            rootView.findViewById<View>(R.id.passport_gallery_btn).setOnClickListener {
                activity.imageType = ImageType.Passport
//                openGallery(activity)
                ImagePicker.create(this).returnMode(ReturnMode.ALL)
                    .folderMode(true)
                    .single().single().showCamera(false).start()
            }

            /*rootView.findViewById(R.id.signature_takePhoto_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imageType = ImageType.Signature;
                    takePhoto(getActivity());
                }
            });

            rootView.findViewById(R.id.id_card_takePhoto_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imageType = ImageType.IDCard;
                    takePhoto(getActivity());
                }
            });*/

            rootView.findViewById<View>(R.id.passport_takePhoto_btn).setOnClickListener {
                activity.imageType = ImageType.Passport
//                takePhoto(activity)
                ImagePicker.cameraOnly()
                    .start(this)
            }
//
//            rootView.findViewById<Button>(R.id.select_passport_btn).setOnClickListener {
//                ImagePicker.create(activity).single().start()
//            }

            return rootView
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
                activity.run {
                    try {
                        val tmpImage: Image? = ImagePicker.getFirstImageOrNull(data)
                        im = view?.findViewById(R.id.passport_image_view)
                        tmpImage ?: return@run showInternalError()
                        val image = CreditClubImage(tmpImage)

                        mainScope.launch {
                            showProgressBar("Processing image")

                            val (bitmap) = safeRunIO { image.bitmap }
                            im?.setImageBitmap(bitmap)

                            passportString = safeRunIO { image.bitmapString }.data
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

    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> AccountInfoFragment()
                1 -> GeneralInfoFragment()
                2 -> DocumentUploadFragment()
                3 -> AgentPINFragment()
                else -> AccountInfoFragment()
            }
        }

        override fun getCount(): Int {
            return 5
        }

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return "Account Info"
                1 -> return "General Details"
                2 -> return "Document Upload"
                3 -> return "Agent PIN"
            }
            return null
        }
    }
}


package com.cluster.fragment.online

import android.content.Context
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import com.cluster.ClusterApplication
import com.cluster.OnlineActivity
import com.cluster.R
import com.cluster.adapter.online.OptionsAdapter
import com.cluster.databinding.FragmentListviewBinding
import com.cluster.fragment.online.CustomerImageFragment.Companion.instantiate
import com.cluster.model.TransactionCountType
import com.cluster.model.online.AuthResponse
import com.cluster.model.online.Option
import com.cluster.model.online.Option.Companion.parseMenu
import com.cluster.model.online.Response.fixResponse
import com.cluster.network.online.APIHelper
import com.cluster.ui.dataBinding
import com.cluster.utility.Misc
import com.cluster.utility.online.ErrorMessages
import com.cluster.utility.online.convertXmlToJson
import com.cluster.core.data.Encryption.decrypt
import com.cluster.core.ui.CreditClubFragment
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import org.koin.android.ext.android.get
import java.util.concurrent.TimeoutException

class ListOptionsFragment : CreditClubFragment(R.layout.fragment_listview), OnItemClickListener,
    View.OnClickListener {
    private val authResponse: AuthResponse by lazy {
        (requireActivity().application as ClusterApplication).authResponse
    }
    private val binding by dataBinding<FragmentListviewBinding>()
    private val ah by lazy {
        APIHelper(
            ctx = requireContext(),
            scope = mainScope,
            localStorage = localStorage,
            client = get()
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.optionsName.text = title
        OnlineActivity.isHome = arguments != null
        binding.listView.adapter = OptionsAdapter(
            requireContext(),
            R.layout.item_option,
            menuOptions ?: emptyList(),
        )
        binding.listView.onItemClickListener = this
        requireActivity().currentFocus?.run {
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(windowToken, 0)
        }
    }

    private var selectedOption: Option? = null
    override fun onItemClick(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
        selectedOption = adapterView.adapter.getItem(i) as Option
        if (selectedOption!!.name.equals("CANCEL", ignoreCase = true)) {
            (activity as OnlineActivity?)!!.goHome()
            return
        }
        mainScope.launch { nextOperation() }
    }

    private fun handleException(goHomeOnClose: Boolean) {
        dialogProvider.confirm(
            "Something went wrong",
            "Please try again"
        ) {
            onSubmit {
                mainScope.launch { nextOperation() }
            }

            onClose {
                if (goHomeOnClose) {
                    (activity as OnlineActivity?)!!.goHome()
                }
            }
        }
    }

    private fun processData(result: String?) {
        try {
            val answer = fixResponse(result!!)
            val decryptedAnswer = decrypt(answer)
            val response = convertXmlToJson(decryptedAnswer)
            if (response == null) {
                Misc.increaseTransactionMonitorCounter(
                    activity,
                    TransactionCountType.NO_INTERNET_COUNT,
                    authResponse.sessionId,
                )
                dialogProvider.showError("Connection lost")
            } else {
                Log.e("ResponseJsonList", response.toString())
                val resp = response.toString()
                val responseBase = response.getJSONObject("Response")
                val shouldClose = responseBase.optInt("ShouldClose", 1)
                if (shouldClose == 0) {
                    if (resp.contains("MenuItem")) {
                        Misc.increaseTransactionMonitorCounter(
                            activity,
                            TransactionCountType.SUCCESS_COUNT,
                            authResponse.sessionId
                        )
                        val menuWrapper =
                            responseBase
                                .getJSONObject("Menu")
                                .getJSONObject("Response")
                                .getJSONObject("Display")
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.container, instantiate(menuWrapper, false)).commit()
                    } else {
                        val menuWrapper = responseBase.getJSONObject("Menu")
                            .getJSONObject("Response")["Display"]
                        if (menuWrapper is String && resp.contains("ShouldMask") && !resp.contains(
                                "Invalid Response"
                            )
                        ) {
                            Misc.increaseTransactionMonitorCounter(
                                activity,
                                TransactionCountType.SUCCESS_COUNT,
                                authResponse.sessionId
                            )
                            val data = responseBase
                                .getJSONObject("Menu")
                                .getJSONObject("Response")
                            if (resp.contains("IsImage=\"true\"")) {
                                requireActivity()
                                    .supportFragmentManager
                                    .beginTransaction()
                                    .replace(R.id.container, instantiate(data))
                                    .commit()
                            } else {
                                requireActivity()
                                    .supportFragmentManager
                                    .beginTransaction()
                                    .replace(R.id.container, EnterDetailFragment.instantiate(data))
                                    .commit()
                            }
                        } else {
                            Misc.increaseTransactionMonitorCounter(
                                activity,
                                TransactionCountType.ERROR_RESPONSE_COUNT,
                                authResponse.sessionId
                            )
                            val message = responseBase
                                .getJSONObject("Menu")
                                .getJSONObject("Response")
                                .getString("Display")
                            dialogProvider.showError(Html.fromHtml(message).toString())
                        }
                    }
                } else {
                    if (responseBase.toString().contains("Display")) {
                        Misc.increaseTransactionMonitorCounter(
                            activity,
                            TransactionCountType.ERROR_RESPONSE_COUNT,
                            authResponse.sessionId
                        )
                        val span = Html.fromHtml(
                            responseBase
                                .getJSONObject("Menu")
                                .getJSONObject("Response")
                                .optString("Display", ErrorMessages.OPERATION_NOT_COMPLETED)
                        )
                        dialogProvider.showError(span.toString()) {
                            onClose {
                                (activity as OnlineActivity?)!!.goHome()
                            }
                        }
                    } else {
                        Misc.increaseTransactionMonitorCounter(
                            activity,
                            TransactionCountType.ERROR_RESPONSE_COUNT,
                            authResponse.sessionId
                        )
                        val span = Html.fromHtml(
                            responseBase.optString(
                                "Menu",
                                ErrorMessages.PHONE_NOT_REGISTERED
                            ),
                        )
                        dialogProvider.showError(span.toString())
                    }
                }
            }
        } catch (c: Exception) {
            Misc.increaseTransactionMonitorCounter(
                activity,
                TransactionCountType.NO_INTERNET_COUNT,
                authResponse.sessionId
            )
            handleException(true)
        }
    }

    private suspend fun nextOperation() {
        dialogProvider.showProgressBar("Loading")
        val (result, e) = ah.getNextOperation(
            authResponse.phoneNumber,
            authResponse.sessionId,
            selectedOption!!.index!!,
            localStorage.lastKnownLocation,
        )
        dialogProvider.hideProgressBar()

        if (e != null) {
            if (e is TimeoutException) {
                handleException(false)
            } else {
                Misc.increaseTransactionMonitorCounter(
                    activity,
                    TransactionCountType.NO_INTERNET_COUNT,
                    authResponse.sessionId
                )
                dialogProvider.showError("Connection lost")
            }
            return
        }

        processData(result)
    }

    override fun onClick(view: View) {
        if (view === binding.optionsName) {
            dialogProvider.showError(binding.optionsName.text.toString())
        }
    }

    companion object {
        private var menuOptions: List<Option>? = null
        private var title: String? = null

        @JvmStatic
        @Throws(Exception::class)
        fun instantiate(menuWrapped: JSONObject, isHome: Boolean): ListOptionsFragment {
            menuOptions = if (menuWrapped.getJSONObject("Menu")["MenuItem"] is JSONArray) {
                parseMenu(
                    menuWrapped
                        .getJSONObject("Menu")
                        .getJSONArray("MenuItem")
                )
            } else {
                val jsonArray = JSONArray()
                val jsonObject = menuWrapped
                    .getJSONObject("Menu")
                    .getJSONObject("MenuItem")
                jsonArray.put(jsonObject)
                parseMenu(jsonArray)
            }
            title = menuWrapped.optString("BeforeMenu")
            if (isHome) {
                val fragment = ListOptionsFragment()
                val bundle = Bundle()
                bundle.putString("IsHome", isHome.toString())
                fragment.arguments = bundle
                return fragment
            }
            return ListOptionsFragment()
        }
    }
}

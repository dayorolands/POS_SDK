package com.cluster.fragment.online;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.cluster.OnlineActivity;
import com.cluster.R;
import com.cluster.model.TransactionCountType;
import com.cluster.model.online.Response;
import com.cluster.network.online.APIHelper;
import com.cluster.utility.Misc;
import com.cluster.utility.online.ErrorMessages;
import com.cluster.utility.online.XmlToJson;
import com.cluster.core.data.Encryption;
import com.cluster.core.ui.CreditClubFragment;

import org.json.JSONObject;

import java.util.concurrent.TimeoutException;

/**
 * A placeholder fragment containing a simple view.
 */
public class FirstActivityAgentFragment extends CreditClubFragment implements View.OnClickListener {

    @Nullable
    EditText ePhone;
    @Nullable
    EditText eVerificationCode;
    @Nullable
    Button btnActivate;

    AlertDialog.Builder dialog;

    public static FirstActivityAgentFragment instantiate() {
        return new FirstActivityAgentFragment();
    }

    public FirstActivityAgentFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_first, container, false);
        OnlineActivity.isHome = false;
        ePhone = view.findViewById(R.id.ePhone);
        eVerificationCode = view.findViewById(R.id.eVerificationCode);
        btnActivate = view.findViewById(R.id.btnActivate);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dialog = new AlertDialog.Builder(requireContext()).setPositiveButton("OK", null);
        btnActivate.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == btnActivate) {
            final String phoneNumber = ePhone.getText().toString().trim();
            final String verificationCode = eVerificationCode.getText().toString().trim();
            final ProgressDialog loading = new ProgressDialog(getActivity());
            loading.setMessage("Validating..");
            loading.setCanceledOnTouchOutside(false);
            //<IsGeotag>

            //{"latitude":8.6, "longitude":7.5}

            if (TextUtils.isEmpty(phoneNumber)) {
                new AlertDialog.Builder(requireContext())
                        .setMessage("Phone number or verification code cannot be empty!")
                        .setPositiveButton("OK", null)
                        .show();
            } else {
                loading.show();
                final APIHelper ah = new APIHelper(requireContext());
                final String sessionId = Encryption.generateSessionId(phoneNumber);

                Misc.resetTransactionMonitorCounter(getActivity());

                ah.attemptActivation(phoneNumber, sessionId, verificationCode, getLocalStorage().getLastKnownLocation(), true, (e, result, status) -> {
                    loading.dismiss();
                    if (status) {
                        try {
                            String answer = Response.fixResponse(result);
                            String decryptedAnswer = Encryption.decrypt(answer);
                            JSONObject response = XmlToJson.convertXmlToJson(decryptedAnswer);
                            if (response == null) {
                                dialog.setMessage("Connection lost")
                                        .setPositiveButton("OK", null)
                                        .show();
                                Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.NO_RESPONSE_COUNT, sessionId);

                            } else {
                                Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.SUCCESS_COUNT, sessionId);
                                String resp = response.toString();
                                JSONObject response_base = response.getJSONObject("Response");
                                int shouldClose = response_base.optInt("ShouldClose", 1);
                                if (shouldClose == 0) {
                                    JSONObject auth = new JSONObject();
                                    auth.put("phone_number", phoneNumber);
                                    auth.put("session_id", sessionId);
                                    getLocalStorage().setCacheAuth(auth.toString());
                                    if (resp.contains("MenuItem")) {
                                        JSONObject menuWrapper = response_base.getJSONObject("Menu").getJSONObject("Response").getJSONObject("Display");
                                        requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, ListOptionsFragment.instantiate(menuWrapper, false)).commit();
                                    } else {
                                        Object menuWrapper = response_base.getJSONObject("Menu").getJSONObject("Response").get("Display");
                                        if (menuWrapper instanceof String && resp.contains("ShouldMask") && !resp.contains("Invalid Response")) {
                                            JSONObject data = response_base.getJSONObject("Menu").getJSONObject("Response");
                                            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, EnterDetailFragment.instantiate(data, true)).commit();
                                        } else {
                                            String message = response_base.getJSONObject("Menu").getJSONObject("Response").getString("Display");
                                            dialog.setMessage(Html.fromHtml(message)).show();
                                        }
                                    }
                                } else {
                                    if (response_base.toString().contains("Display")) {
                                        Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.ERROR_RESPONSE_COUNT, sessionId);
                                        dialog.setMessage(Html.fromHtml(response_base.getJSONObject("Menu").getJSONObject("Response").optString("Display", ErrorMessages.OPERATION_NOT_COMPLETED)))
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        ((OnlineActivity) getActivity()).goHome();
                                                    }
                                                })
                                                .show();
                                    } else {
                                        Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.ERROR_RESPONSE_COUNT, sessionId);
                                        String responseString = response_base.optString("Menu", ErrorMessages.PHONE_NOT_REGISTERED);
                                        if (responseString.equalsIgnoreCase("1")) {
                                            loading.show();
                                            ah.attemptValidation(phoneNumber, sessionId, verificationCode, getLocalStorage().getLastKnownLocation(), true, (e1, result1, status1) -> {
                                                loading.dismiss();
                                                if (status1) {
                                                    try {
                                                        String answer1 = Response.fixResponse(result1);
                                                        Log.e("FixedResponse", answer1 + "EMPTY");
                                                        String decryptedAnswer1 = Encryption.decrypt(answer1);
                                                        Log.e("DecryptedAnswer", decryptedAnswer1);
                                                        JSONObject response1 = XmlToJson.convertXmlToJson(decryptedAnswer1);
                                                        if (response1 == null) {
                                                            dialog.setMessage("Connection lost")
                                                                    .setPositiveButton("OK", null)
                                                                    .show();
                                                        } else {
                                                            String resp1 = response1.toString();
                                                            JSONObject response_base1 = response1.getJSONObject("Response");
                                                            int shouldClose1 = response_base1.optInt("ShouldClose", 1);
                                                            if (shouldClose1 == 0) {
                                                                JSONObject auth = new JSONObject();
                                                                auth.put("phone_number", phoneNumber);
                                                                auth.put("session_id", sessionId);
                                                                getLocalStorage().setCacheAuth(auth.toString());
                                                                if (resp1.contains("MenuItem")) {
                                                                    JSONObject menuWrapper = response_base1.getJSONObject("Menu").getJSONObject("Response").getJSONObject("Display");
                                                                    requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, ListOptionsFragment.instantiate(menuWrapper, false)).commit();
                                                                } else {
                                                                    Object menuWrapper = response_base1.getJSONObject("Menu").getJSONObject("Response").get("Display");
                                                                    if (menuWrapper instanceof String && resp1.contains("ShouldMask") && !resp1.contains("Invalid Response")) {
                                                                        JSONObject data = response_base1.getJSONObject("Menu").getJSONObject("Response");
                                                                        requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, EnterDetailFragment.instantiate(data, true)).commit();
                                                                    } else {
                                                                        String message = response_base1.getJSONObject("Menu").getJSONObject("Response").getString("Display");
                                                                        dialog.setMessage(Html.fromHtml(message)).show();
                                                                    }
                                                                }
                                                            } else {
                                                                if (response_base1.toString().contains("Display")) {
                                                                    dialog.setMessage(Html.fromHtml(response_base1.getJSONObject("Menu").getJSONObject("Response")
                                                                            .optString("Display", ErrorMessages.OPERATION_NOT_COMPLETED)))
                                                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                                                @Override
                                                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                                                    ((OnlineActivity) getActivity()).goHome();
                                                                                }
                                                                            })
                                                                            .show();
                                                                } else {
                                                                    Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.ERROR_RESPONSE_COUNT, sessionId);
                                                                    dialog.setMessage(Html.fromHtml(response_base1.
                                                                            optString("Menu", ErrorMessages.PHONE_NOT_REGISTERED)))
                                                                            .show();
                                                                }
                                                            }
                                                        }
                                                    } catch (Exception c) {
                                                        c.printStackTrace();
                                                        dialog.setMessage("Connection lost : " + c.getMessage()).setPositiveButton("OK", null).show();
                                                    }
                                                } else {
//                                                                if (e != null) {
                                                    e1.printStackTrace();
//                                                                    if (e instanceof TimeoutException) {
//                                                                        dialog.setMessage("Something went wrong! Please try again.")
//                                                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                                                                                    @Override
//                                                                                    public void onClick(DialogInterface dialogInterface, int i) {
//                                                                                        ((FirstActivity) getActivity()).goHome();
//                                                                                    }
//                                                                                }).setCancelable(false)
//                                                                                .show();
//                                                                    } else {
                                                    dialog.setMessage(String.valueOf(e1.getMessage())).setPositiveButton("OK", null).show();
//                                                                    }
//                                                                } else {
//                                                                    dialog.setMessage("Connection lost")
//                                                                            .setPositiveButton("OK", null)
//                                                                            .show();
//                                                                }
                                                }
                                            }/*, new FutureCallback<String>() {
                                            @Override
                                            public void onCompleted(Exception e, String result) {
                                                Log.e("FutureCall", result + "");
                                            }
                                        }*/);
                                        } else if (responseString.startsWith("0")) {
                                            if (responseString.contains(":")) {
                                                Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.ERROR_RESPONSE_COUNT, sessionId);

                                                String[] serverResponse = responseString.split(":");
                                                dialog.setMessage(serverResponse[1]).show();
                                            } else {
                                                Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.ERROR_RESPONSE_COUNT, sessionId);
                                                dialog.setMessage("Phone number could not be registered!").show();
                                            }

                                        } else {
                                            Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.ERROR_RESPONSE_COUNT, sessionId);
                                            dialog.setMessage(Html.fromHtml(response_base.optString("Menu", ErrorMessages.PHONE_NOT_REGISTERED))).show();
                                        }
                                    }
                                }

                            }
                        } catch (Exception c) {
                            c.printStackTrace();
                            Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.NO_INTERNET_COUNT, sessionId);
                            dialog.setMessage("Connection lost : " + c.getMessage())
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    } else {
                        if (e != null) {
                            e.printStackTrace();
                            if (e instanceof TimeoutException) {
                                Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.NO_INTERNET_COUNT, sessionId);
                                dialog.setMessage("Something went wrong! Please try again.")
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                ((OnlineActivity) getActivity()).goHome();
                                            }
                                        }).setCancelable(false)
                                        .show();
                            } else {
                                Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.NO_INTERNET_COUNT, sessionId);
                                dialog.setMessage("Connection lost")
                                        .setPositiveButton("OK", null)
                                        .show();
                            }
                        } else {
                            Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.NO_INTERNET_COUNT, sessionId);
                            dialog.setMessage("Connection lost")
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    }
                });

            }
        }
    }
}
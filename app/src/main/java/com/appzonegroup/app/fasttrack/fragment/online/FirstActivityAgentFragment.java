package com.appzonegroup.app.fasttrack.fragment.online;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.appzonegroup.app.fasttrack.OnlineActivity;
import com.appzonegroup.app.fasttrack.LocationChangedService;
import com.appzonegroup.app.fasttrack.R;
import com.appzonegroup.app.fasttrack.model.TransactionCountType;
import com.appzonegroup.app.fasttrack.model.online.Response;
import com.appzonegroup.app.fasttrack.network.online.APIHelper;
import com.appzonegroup.app.fasttrack.utility.GPSTracker;
import com.appzonegroup.app.fasttrack.utility.LocalStorage;
import com.appzonegroup.app.fasttrack.utility.Misc;
import com.creditclub.core.data.Encryption;
import com.appzonegroup.app.fasttrack.utility.online.ErrorMessages;
import com.appzonegroup.app.fasttrack.utility.online.XmlToJson;

import org.json.JSONObject;

import java.util.concurrent.TimeoutException;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A placeholder fragment containing a simple view.
 */
public class FirstActivityAgentFragment extends Fragment implements View.OnClickListener {

    @Nullable @Bind(R.id.ePhone)
    EditText ePhone;
    @Nullable @Bind(R.id.eVerificationCode)
    EditText eVerificationCode;
    @Nullable @Bind(R.id.btnActivate)
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
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dialog = new AlertDialog.Builder(getActivity()).setPositiveButton("OK", null);
        btnActivate.setOnClickListener(this);
    }

    String finalLocation;
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
                new AlertDialog.Builder(getActivity())
                        .setMessage("Phone number or verification code cannot be empty!")
                        .setPositiveButton("OK", null)
                        .show();
            } else {
                loading.show();
                final APIHelper ah = new APIHelper(getActivity());
                final String sessionId = Encryption.generateSessionId(phoneNumber);
                finalLocation = "0.00;0.00";
                final GPSTracker gpsTracker = new GPSTracker(getActivity());
                if(gpsTracker.getLocation() != null){
                    String longitude = String.valueOf(gpsTracker.getLocation().getLongitude());
                    String latitude = String.valueOf(gpsTracker.getLocation().getLatitude());
                    finalLocation = latitude+";"+longitude;
                }

                Misc.resetTransactionMonitorCounter(getActivity());

                ah.attemptActivation(phoneNumber, sessionId, verificationCode, finalLocation, true, new APIHelper.VolleyCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result,boolean status) {
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
                                if (response_base != null) {
                                    int shouldClose = response_base.optInt("ShouldClose", 1);
                                    if (shouldClose == 0) {
                                        JSONObject auth = new JSONObject();
                                        auth.put("phone_number", phoneNumber);
                                        auth.put("session_id", sessionId);
                                        LocalStorage.saveCacheAuth(auth.toString(), getActivity());
                                        getActivity().startService(new Intent(getActivity(), LocationChangedService.class));
                                        if (resp.contains("MenuItem")) {
                                            JSONObject menuWrapper = response_base.getJSONObject("Menu").getJSONObject("Response").getJSONObject("Display");
                                            getFragmentManager().beginTransaction().replace(R.id.container, ListOptionsFragment.instantiate(menuWrapper, false)).commit();
                                        } else {
                                            Object menuWrapper = response_base.getJSONObject("Menu").getJSONObject("Response").get("Display");
                                            if (menuWrapper instanceof String && resp.contains("ShouldMask") && !resp.contains("Invalid Response")) {
                                                JSONObject data = response_base.getJSONObject("Menu").getJSONObject("Response");
                                                getFragmentManager().beginTransaction().replace(R.id.container, EnterDetailFragment.instantiate(data, true)).commit();
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
                                                ah.attemptValidation(phoneNumber, sessionId, verificationCode, finalLocation, true, new APIHelper.VolleyCallback<String>() {
                                                    @Override
                                                    public void onCompleted(Exception e, String result, boolean status) {
                                                        loading.dismiss();
                                                        if (status) {
                                                            try {
                                                                String answer = Response.fixResponse(result);
                                                                Log.e("FixedResponse", answer + "EMPTY");
                                                                String decryptedAnswer = Encryption.decrypt(answer);
                                                                Log.e("DecryptedAnswer", decryptedAnswer);
                                                                JSONObject response = XmlToJson.convertXmlToJson(decryptedAnswer);
                                                                if (response == null) {
                                                                    dialog.setMessage("Connection lost")
                                                                            .setPositiveButton("OK", null)
                                                                            .show();
                                                                } else {
                                                                    String resp = response.toString();
                                                                    JSONObject response_base = response.getJSONObject("Response");
                                                                    if (response_base != null) {
                                                                        int shouldClose = response_base.optInt("ShouldClose", 1);
                                                                        if (shouldClose == 0) {
                                                                            JSONObject auth = new JSONObject();
                                                                            auth.put("phone_number", phoneNumber);
                                                                            auth.put("session_id", sessionId);
                                                                            LocalStorage.saveCacheAuth(auth.toString(), getActivity());
                                                                            getActivity().startService(new Intent(getActivity(), LocationChangedService.class));
                                                                            if (resp.contains("MenuItem")) {
                                                                                JSONObject menuWrapper = response_base.getJSONObject("Menu").getJSONObject("Response").getJSONObject("Display");
                                                                                getFragmentManager().beginTransaction().replace(R.id.container, ListOptionsFragment.instantiate(menuWrapper, false)).commit();
                                                                            } else {
                                                                                Object menuWrapper = response_base.getJSONObject("Menu").getJSONObject("Response").get("Display");
                                                                                if (menuWrapper instanceof String && resp.contains("ShouldMask") && !resp.contains("Invalid Response")) {
                                                                                    JSONObject data = response_base.getJSONObject("Menu").getJSONObject("Response");
                                                                                    getFragmentManager().beginTransaction().replace(R.id.container, EnterDetailFragment.instantiate(data, true)).commit();
                                                                                } else {
                                                                                    String message = response_base.getJSONObject("Menu").getJSONObject("Response").getString("Display");
                                                                                    dialog.setMessage(Html.fromHtml(message)).show();
                                                                                }
                                                                            }
                                                                        } else {
                                                                            if (response_base.toString().contains("Display")) {
                                                                                dialog.setMessage(Html.fromHtml(response_base.getJSONObject("Menu")                                                                                        .getJSONObject("Response")
                                                                                        .optString("Display", ErrorMessages.OPERATION_NOT_COMPLETED)))
                                                                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                                                            @Override
                                                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                                             ((OnlineActivity) getActivity()).goHome();
                                                                                            }
                                                                                        })
                                                                                        .show();
                                                                            } else {
                                                                                Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.ERROR_RESPONSE_COUNT,sessionId);
                                                                                dialog.setMessage(Html.fromHtml(response_base.
                                                                                        optString("Menu", ErrorMessages.PHONE_NOT_REGISTERED)))
                                                                                        .show();
                                                                            }
                                                                        }
                                                                    }

                                                                }
                                                            } catch (Exception c) {
                                                                c.printStackTrace();
                                                                dialog.setMessage("Connection lost : " + c.getMessage()).setPositiveButton("OK", null).show();
                                                            }
                                                        } else {
//                                                                if (e != null) {
                                                            e.printStackTrace();
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
                                                            dialog.setMessage(String.valueOf(e.getMessage())).setPositiveButton("OK", null).show();
//                                                                    }
//                                                                } else {
//                                                                    dialog.setMessage("Connection lost")
//                                                                            .setPositiveButton("OK", null)
//                                                                            .show();
//                                                                }
                                                        }
                                                    }
                                                }/*, new FutureCallback<String>() {
                                                    @Override
                                                    public void onCompleted(Exception e, String result) {
                                                        Log.e("FutureCall", result + "");
                                                    }
                                                }*/);
                                            }else if(responseString.startsWith("0")){
                                                if(responseString.contains(":")){
                                                    Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.ERROR_RESPONSE_COUNT, sessionId);

                                                    String[] serverResponse =  responseString.split(":");
                                                    dialog.setMessage(serverResponse[1]).show();
                                                }else{
                                                    Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.ERROR_RESPONSE_COUNT, sessionId);
                                                    dialog.setMessage("Phone number could not be registered!").show();
                                                }

                                            }else {
                                                Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.ERROR_RESPONSE_COUNT, sessionId);
                                                dialog.setMessage(Html.fromHtml(response_base.optString("Menu", ErrorMessages.PHONE_NOT_REGISTERED))).show();
                                            }
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
                    }
                });

            }
        }
    }
}
package com.appzonegroup.app.fasttrack.fragment.online;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.appzonegroup.app.fasttrack.BankOneApplication;
import com.appzonegroup.app.fasttrack.OnlineActivity;
import com.appzonegroup.app.fasttrack.R;
import com.appzonegroup.app.fasttrack.model.TransactionCountType;
import com.appzonegroup.app.fasttrack.model.online.AuthResponse;
import com.appzonegroup.app.fasttrack.model.online.Response;
import com.appzonegroup.app.fasttrack.network.online.APIHelper;
import com.appzonegroup.app.fasttrack.utility.GPSTracker;
import com.appzonegroup.app.fasttrack.utility.Misc;
import com.appzonegroup.app.fasttrack.utility.online.ErrorMessages;
import com.appzonegroup.app.fasttrack.utility.online.XmlToJson;
import com.creditclub.core.data.Encryption;
import com.creditclub.core.ui.CreditClubFragment;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.json.JSONObject;

import java.util.concurrent.TimeoutException;

/**
 * Created by fdamilola on 8/14/15.
 * Contact fdamilola@gmail.com or fdamilola@hextremelabs.com or fdamilola@echurch.ng or fdamilola@cottacush.com
 * +2348166200715
 */
public class EnterDetailFragment extends CreditClubFragment implements View.OnClickListener {

    TextView upperHint;
    EditText textDetail;
    Button next;
    TextInputLayout tInput;

    AlertDialog.Builder dialog;

    static boolean state = false;

    private static OptionsText optionsText;

    public static EnterDetailFragment instantiate(JSONObject data) {
        setOptionsText(new OptionsText(data));
        return new EnterDetailFragment();
    }

    public static EnterDetailFragment instantiate(JSONObject data, boolean value) {
        setOptionsText(new OptionsText(data));
        state = value;
        return new EnterDetailFragment();
    }

    public EnterDetailFragment() {
        // Required empty public constructor
    }

    public static OptionsText getOptionsText() {
        return optionsText;
    }

    public static void setOptionsText(OptionsText optionsText) {
        EnterDetailFragment.optionsText = optionsText;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_enter_detail, container, false);
        OnlineActivity.isHome = false;
        textDetail=view.findViewById(R.id.eText);
        upperHint=view.findViewById(R.id.upperHint);
        tInput=view.findViewById(R.id.tInput);
        next=view.findViewById(R.id.btnActivate);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dialog = new AlertDialog.Builder(getActivity());
        dialog.setPositiveButton("OK", null);
        dialog.setCancelable(false);
        OptionsText.applyOptions(getOptionsText(), textDetail);
        upperHint.setText(getOptionsText().getHintText());
        //tInput.setHint(getOptionsText().getHintText());
        next.setOnClickListener(this);
    }

    String finalLocation;

    @Override
    public void onClick(View view) {
        if (view == next) {
            final String txt = textDetail.getText().toString().trim();
            if (TextUtils.isEmpty(txt)) {
                Toast.makeText(getActivity(), "Input cannot be empty!", Toast.LENGTH_SHORT).show();
            } else {
                final ProgressDialog pdialog = new ProgressDialog(getActivity());
                pdialog.setCancelable(false);
                pdialog.setMessage("Loading...");
                pdialog.show();
                final APIHelper ah = new APIHelper(getActivity());
                final AuthResponse authResponse = ((BankOneApplication) getActivity().getApplication()).getAuthResponse();
                finalLocation = "0.00;0.00";
                final GPSTracker gpsTracker = new GPSTracker(getActivity());
                if (gpsTracker.getLocation() != null) {
                    String longitude = String.valueOf(gpsTracker.getLocation().getLongitude());
                    String latitude = String.valueOf(gpsTracker.getLocation().getLatitude());
                    finalLocation = latitude + ";" + longitude;
                }

                ah.getNextOperation(authResponse.getPhoneNumber(), authResponse.getSessionId(), txt, finalLocation, new APIHelper.VolleyCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result, boolean status) {
                        pdialog.dismiss();
                        if (status) {
                            processData(result, txt);
                        } else {
                            if (e != null) {
                                e.printStackTrace();
                                if (e instanceof TimeoutException) {
                                    dialog.setMessage("Something went wrong! Please try again.")
                                            .setPositiveButton("CONTINUE", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    pdialog.show();
                                                    ah.continueNextOperation(authResponse.getPhoneNumber(), authResponse.getSessionId(), txt, finalLocation, new APIHelper.VolleyCallback<String>() {

                                                        @Override
                                                        public void onCompleted(Exception e, String result, boolean status) {
                                                            pdialog.dismiss();
                                                            if (status) {
                                                                processData(result, txt);
                                                            } else {
                                                                if (e != null) {
                                                                    FirebaseCrashlytics.getInstance().recordException(e);
                                                                }
                                                                Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.NO_INTERNET_COUNT, authResponse.getSessionId());
                                                                getDialogProvider().showError("Connection lost");
                                                            }
                                                        }
                                                    });
                                                }
                                            }).setCancelable(false).create().setCanceledOnTouchOutside(false);
                                    dialog.show();
                                } else {
                                    Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.NO_INTERNET_COUNT, authResponse.getSessionId());
                                    getDialogProvider().showError("Connection lost");
                                }
                            } else {
                                Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.NO_INTERNET_COUNT, authResponse.getSessionId());
                                getDialogProvider().showError("Connection lost");
                            }
                        }

                    }
                });
            }
        }
    }

    private void processData(String result, String txt) {
        final AuthResponse authResponse = ((BankOneApplication) getActivity().getApplication()).getAuthResponse();
        try {
            String answer = Response.fixResponse(result);
            String decryptedAnswer = Encryption.decrypt(answer);
            JSONObject response = XmlToJson.convertXmlToJson(decryptedAnswer);
            if (response == null) {
                Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.NO_INTERNET_COUNT, authResponse.getSessionId());
                getDialogProvider().showError("Connection lost");
            } else {
                String resp = response.toString();

                JSONObject response_base = response.getJSONObject("Response");
                if (response_base != null) {
                    int shouldClose = response_base.optInt("ShouldClose", 1);
                    if (shouldClose == 0) {
                        if (resp.contains("IN-CORRECT ACTIVATION CODE") && state) {
                            Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.ERROR_RESPONSE_COUNT, authResponse.getSessionId());
                            //Log.e("Case", "Incorrect activation code||Deleted cache auth");
                            getLocalStorage().setCacheAuth(null);
                        } else if (state) {
                            //Log.e("Case", "correct activation code||"+txt);
                            Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.ERROR_RESPONSE_COUNT, authResponse.getSessionId());
                            AuthResponse ar = ((BankOneApplication) getActivity().getApplication()).getAuthResponse();
                            JSONObject auth = new JSONObject();
                            auth.put("phone_number", ar.getPhoneNumber());
                            auth.put("session_id", ar.getSessionId());
                            auth.put("activation_code", txt);
                            getLocalStorage().setCacheAuth(auth.toString());
                        }
                        if (resp.contains("MenuItem")) {
                            Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.SUCCESS_COUNT, authResponse.getSessionId());
                            JSONObject menuWrapper = response_base.getJSONObject("Menu").getJSONObject("Response").getJSONObject("Display");
                            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, ListOptionsFragment.instantiate(menuWrapper, false)).commit();
                        } else {
                            Object menuWrapper = response_base.getJSONObject("Menu").getJSONObject("Response").get("Display");
                            if (menuWrapper instanceof String && resp.contains("ShouldMask") && !resp.contains("Invalid Response")) {
                                Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.SUCCESS_COUNT, authResponse.getSessionId());
                                JSONObject data = response_base.getJSONObject("Menu").getJSONObject("Response");
                                if (resp.contains("\"IsImage\":true")) {
                                    requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, CustomerImageFragment.instantiate(data)).commit();
                                } else {
                                    requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, EnterDetailFragment.instantiate(data)).commit();
                                }
                            } else {
                                Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.ERROR_RESPONSE_COUNT, authResponse.getSessionId());
                                String message = response_base.getJSONObject("Menu").getJSONObject("Response").getString("Display");
                                dialog.setMessage(Html.fromHtml(message)).create().setCanceledOnTouchOutside(false);
                                dialog.show();
                            }
                        }
                    } else {
                        Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.ERROR_RESPONSE_COUNT, authResponse.getSessionId());
                        if (response_base.toString().contains("Display")) {
                            loadAlertWebView(response_base.getJSONObject("Menu").getJSONObject("Response").optString("Display", ErrorMessages.PHONE_NOT_REGISTERED));
                        } else {
                            dialog.setMessage(Html.fromHtml(response_base.optString("Menu", ErrorMessages.PHONE_NOT_REGISTERED))).create().setCanceledOnTouchOutside(false);
                            dialog.show();
                        }
                    }
                }

            }
        } catch (Exception c) {
            Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.NO_INTERNET_COUNT, authResponse.getSessionId());
            dialog.setMessage("Something went wrong! Please try again.")
                    .setPositiveButton("CONTINUE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            final ProgressDialog pd = new ProgressDialog(getActivity());
                            pd.setMessage("Loading");
                            pd.setCancelable(false);
                            pd.show();
                            final String txt = textDetail.getText().toString().trim();
                            finalLocation = "0.00;0.00";
                            final GPSTracker gpsTracker = new GPSTracker(getActivity());
                            if (gpsTracker.getLocation() != null) {
                                String longitude = String.valueOf(gpsTracker.getLocation().getLongitude());
                                String latitude = String.valueOf(gpsTracker.getLocation().getLatitude());
                                finalLocation = latitude + ";" + longitude;
                            }

                            AuthResponse authResponse = ((BankOneApplication) requireActivity().getApplication()).getAuthResponse();

                            new APIHelper(getActivity()).continueNextOperation(authResponse.getPhoneNumber(), authResponse.getSessionId(), txt, finalLocation, new APIHelper.VolleyCallback<String>() {
                                @Override
                                public void onCompleted(Exception e, String result, boolean status) {
                                    pd.dismiss();
                                    if (status) {
                                        processData(result, txt);
                                    } else {
                                        Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.NO_INTERNET_COUNT, ((BankOneApplication) getActivity().getApplication()).getAuthResponse().getSessionId());
                                        getDialogProvider().showError("Connection lost");
                                    }
                                }
                            });
                        }
                    }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    ((OnlineActivity) getActivity()).goHome();
                }
            }).setCancelable(false).create().setCanceledOnTouchOutside(false);
            dialog.show();
        }
    }

    private void loadAlertWebView(String message) {

        final Dialog dialog = new Dialog(getActivity());

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_message);

        ((TextView) dialog.findViewById(R.id.message_tv)).setText(message);

        dialog.findViewById(R.id.ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((OnlineActivity) getActivity()).goHome();
                dialog.dismiss();
            }
        });
        dialog.show();

        /*android.support.v7.app.AlertDialog.Builder ab = new android.support.v7.app.AlertDialog.Builder(getActivity());
        final FrameLayout fm = new FrameLayout(getActivity());
        final LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        fm.setLayoutParams(lparams);
        ab.setView(fm);
        ab.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((FirstActivity)getActivity()).goHome();
                alertDialog.dismiss();
            }
        });

        final android.support.v7.app.AlertDialog alertDialog = ab.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);
        LayoutInflater inflaterDialog = alertDialog.getLayoutInflater();
        View dialogView = inflaterDialog.inflate(R.layout.view_webview, fm);

        final WebView webview = dialogView.findViewById(R.id.webView);
        WebSettings settings = webview.getSettings();
        settings.setSupportZoom(false);
        settings.setJavaScriptEnabled(false);
        settings.setDefaultTextEncodingName("utf-8");
        webview.loadData(message, "text/html", "utf-8");
        alertDialog.show();*/

    }

    public static class OptionsText {
        private boolean isNumeric, isImage, shouldMask;
        private boolean ShouldCompress;
        private String hintText;

        public OptionsText(JSONObject jo) {
            try {
                //setIsImage(jo.getJSONObject("IsImage").getBoolean("IsImage"));
                setIsNumeric(jo.getJSONObject("IsNumeric").getBoolean("IsNumeric"));
                setShouldMask(jo.getJSONObject("ShouldMask").getBoolean("Mask"));
                setHintText(jo.optString("Display").toUpperCase());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public boolean isNumeric() {
            return isNumeric;
        }

        public void setIsNumeric(boolean isNumeric) {
            this.isNumeric = isNumeric;
        }

        public boolean isImage() {
            return isImage;
        }

        public void setIsImage(boolean isImage) {
            this.isImage = isImage;
        }

        public boolean isShouldMask() {
            return shouldMask;
        }

        public void setShouldMask(boolean shouldMask) {
            this.shouldMask = shouldMask;
        }

        public String getHintText() {
            return hintText;
        }

        public void setHintText(String hintText) {
            this.hintText = hintText;
        }

        public static void applyOptions(OptionsText otxt, EditText eText) {
            //eText.setHint(otxt.getHintText());

            if (!otxt.isNumeric() && otxt.isShouldMask()) {
                eText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                eText.setInputType(InputType.TYPE_CLASS_TEXT);
            } else {
                if (otxt.isNumeric() && otxt.isShouldMask()) {
                    eText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    eText.setInputType(otxt.isNumeric() ? InputType.TYPE_CLASS_NUMBER : InputType.TYPE_CLASS_TEXT);
                } else if (!otxt.isShouldMask() && otxt.isNumeric()) {
                    eText.setInputType(otxt.isNumeric() ? InputType.TYPE_CLASS_NUMBER : InputType.TYPE_CLASS_TEXT);
                } else if (!otxt.isNumeric()) {
                    eText.setInputType(InputType.TYPE_CLASS_TEXT);
                }
            }
        }

        public boolean isShouldCompress() {
            return ShouldCompress;
        }

        public void setShouldCompress(boolean shouldCompress) {
            ShouldCompress = shouldCompress;
        }
    }
}

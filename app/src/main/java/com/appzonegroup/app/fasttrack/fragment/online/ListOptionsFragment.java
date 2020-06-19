package com.appzonegroup.app.fasttrack.fragment.online;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.appzonegroup.app.fasttrack.BankOneApplication;
import com.appzonegroup.app.fasttrack.OnlineActivity;
import com.appzonegroup.app.fasttrack.R;
import com.appzonegroup.app.fasttrack.adapter.online.OptionsAdapter;
import com.appzonegroup.app.fasttrack.model.TransactionCountType;
import com.appzonegroup.app.fasttrack.model.online.AuthResponse;
import com.appzonegroup.app.fasttrack.model.online.Option;
import com.appzonegroup.app.fasttrack.model.online.Response;
import com.appzonegroup.app.fasttrack.network.online.APIHelper;
import com.appzonegroup.app.fasttrack.utility.GPSTracker;
import com.appzonegroup.app.fasttrack.utility.LocalStorage;
import com.appzonegroup.app.fasttrack.utility.Misc;
import com.appzonegroup.app.fasttrack.utility.online.ErrorMessages;
import com.appzonegroup.app.fasttrack.utility.online.XmlToJson;
import com.creditclub.core.data.Encryption;
import com.creditclub.core.ui.CreditClubActivity;
import com.creditclub.core.ui.CreditClubFragment;
import com.creditclub.core.ui.widget.DialogListener;
import com.creditclub.core.ui.widget.DialogProvider;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

/**
 * A simple {@link Fragment} subclass.
 */
public class ListOptionsFragment extends Fragment implements AdapterView.OnItemClickListener,
        View.OnClickListener {

    ListView listView;

    TextView optionName;

    private static ArrayList<Option> menuOptions;
    private static String title;
    private AuthResponse authResponse;

    public static ListOptionsFragment instantiate(JSONObject menuWrapped, boolean isHome) throws Exception {
        if (menuWrapped.getJSONObject("Menu").get("MenuItem") instanceof JSONArray) {
            setMenuOptions(Option.parseMenu(menuWrapped
                    .getJSONObject("Menu").getJSONArray("MenuItem")));
        } else {
            JSONArray ja = new JSONArray();
            JSONObject jo = menuWrapped
                    .getJSONObject("Menu").getJSONObject("MenuItem");
            ja.put(jo);
            setMenuOptions(Option.parseMenu(ja));
        }
        setTitle(menuWrapped.optString("BeforeMenu"));
        if (isHome) {
            ListOptionsFragment fragment = new ListOptionsFragment();
            Bundle bundle = new Bundle();
            bundle.putString("IsHome", String.valueOf(isHome));
            fragment.setArguments(bundle);
            return fragment;
        }

        return new ListOptionsFragment();
    }

    public ListOptionsFragment() {
        // Required empty public constructor
    }

    public static ArrayList<Option> getMenuOptions() {
        return menuOptions;
    }

    public static void setMenuOptions(ArrayList<Option> menuOptions) {
        ListOptionsFragment.menuOptions = menuOptions;
    }

    public static String getTitle() {
        return title;
    }

    public static void setTitle(String title) {
        ListOptionsFragment.title = title;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_listview, container, false);
        listView = view.findViewById(R.id.listView);
        optionName = view.findViewById(R.id.optionsName);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        optionName.setText(getTitle());
        //optionName.setOnClickListener(this);
        OnlineActivity.isHome = getArguments() != null;
        /*if (getArguments() != null)
        {
            OnlineActivity.isHome = savedInstanceState.getString("IsHome") != null;
        }*/

        listView.setAdapter(new OptionsAdapter(getActivity(), R.layout.item_option, getMenuOptions()));
        listView.setOnItemClickListener(this);
        view = ((CreditClubActivity) getActivity()).getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) ((CreditClubActivity) getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        authResponse = ((BankOneApplication) ((CreditClubActivity) getActivity()).getApplication()).getAuthResponse();// LocalStorage.getCachedAuthResponse(getActivity());

    }

    String finalLocation;
    Option selectedOption;

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        selectedOption = (Option) adapterView.getAdapter().getItem(i);
        DialogProvider dialogProvider = ((CreditClubActivity) getActivity()).getDialogProvider();

        if (selectedOption.getName().equalsIgnoreCase("CANCEL")) {
            ((OnlineActivity) getActivity()).goHome();
        } else {
            dialogProvider.showProgressBar("Loading");
            final APIHelper ah = new APIHelper(getActivity());
            finalLocation = "0.00;0.00";
            final GPSTracker gpsTracker = new GPSTracker(getActivity());
            if (gpsTracker.getLocation() != null) {
                Log.e("CangetLocation", "NULL");
                String longitude = String.valueOf(gpsTracker.getLocation().getLongitude());
                String latitude = String.valueOf(gpsTracker.getLocation().getLatitude());
                finalLocation = latitude + ";" + longitude;
                Log.e("Location", finalLocation);
            }

            ah.getNextOperation(authResponse.getPhoneNumber(), authResponse.getSessionId(), selectedOption.getIndex(), finalLocation, new APIHelper.VolleyCallback<String>() {
                @Override
                public void onCompleted(Exception e, String result, boolean status) {
                    dialogProvider.hideProgressBar();
                    if (status) {
                        processData(result);
                    } else {
                        if (e != null) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                            e.printStackTrace();
                            if (e instanceof TimeoutException) {
                                handleException(false);
                            } else {
                                Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.NO_INTERNET_COUNT, authResponse.getSessionId());
                                dialogProvider.showError("Connection lost");
                            }
                        } else {
                            Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.NO_INTERNET_COUNT, authResponse.getSessionId());
                            dialogProvider.showError("Connection lost");
                        }
                    }
                }
            });
        }
    }

    private void handleException(boolean goHomeOnClose) {
        DialogProvider dialogProvider = ((CreditClubActivity) getActivity()).getDialogProvider();
        dialogProvider.confirm("Something went wrong", "Please try again", new Function1<DialogListener<Boolean>, Unit>() {
            @Override
            public Unit invoke(DialogListener<Boolean> booleanDialogListener) {
                booleanDialogListener.onSubmit(new Function2<Dialog, Boolean, Unit>() {
                    @Override
                    public Unit invoke(Dialog dialog, Boolean aBoolean) {
                        dialogProvider.showProgressBar("Loading");
                        final APIHelper ah = new APIHelper(getActivity());
                        finalLocation = "0.00;0.00";
                        final GPSTracker gpsTracker = new GPSTracker(getActivity());
                        if (gpsTracker.getLocation() != null) {
                            String longitude = String.valueOf(gpsTracker.getLocation().getLongitude());
                            String latitude = String.valueOf(gpsTracker.getLocation().getLatitude());
                            finalLocation = latitude + ";" + longitude;
                        }

                        AuthResponse authResponse = LocalStorage.getCachedAuthResponse(getActivity());
                        ah.continueNextOperation(authResponse.getPhoneNumber(), authResponse.getSessionId(), selectedOption.getIndex(), finalLocation, new APIHelper.VolleyCallback<String>() {
                            @Override
                            public void onCompleted(Exception e, String result, boolean status) {
                                if (status) {
                                    processData(result);
                                } else {
                                    Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.NO_INTERNET_COUNT, authResponse.getSessionId());
                                    dialogProvider.showError("Connection lost");
                                }
                            }
                        });
                        return null;
                    }
                });

                booleanDialogListener.onClose(new Function0<Unit>() {
                    @Override
                    public Unit invoke() {
                        if (goHomeOnClose) {
                            ((OnlineActivity) getActivity()).goHome();
                        }
                        return null;
                    }
                });

                return null;
            }
        });
    }

    private void processData(String result) {
        DialogProvider dialogProvider = ((CreditClubActivity) getActivity()).getDialogProvider();
        try {
            String answer = Response.fixResponse(result);
            String decryptedAnswer = Encryption.decrypt(answer);
            JSONObject response = XmlToJson.convertXmlToJson(decryptedAnswer);
            if (response == null) {
                Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.NO_INTERNET_COUNT, authResponse.getSessionId());
                dialogProvider.showError("Connection lost");
            } else {

                Log.e("ResponseJsonList", response.toString());
                String resp = response.toString();
                JSONObject response_base = response.getJSONObject("Response");
                if (response_base != null) {
                    int shouldClose = response_base.optInt("ShouldClose", 1);
                    if (shouldClose == 0) {
                        if (resp.contains("MenuItem")) {
                            Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.SUCCESS_COUNT, authResponse.getSessionId());

                            JSONObject menuWrapper = response_base.getJSONObject("Menu").getJSONObject("Response").getJSONObject("Display");
                            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, ListOptionsFragment.instantiate(menuWrapper, false)).commit();
                        } else {
                            Object menuWrapper = response_base.getJSONObject("Menu").getJSONObject("Response").get("Display");
                            if (menuWrapper instanceof String && resp.contains("ShouldMask") && !resp.contains("Invalid Response")) {
                                Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.SUCCESS_COUNT, authResponse.getSessionId());
                                JSONObject data = response_base.getJSONObject("Menu").getJSONObject("Response");
                                if (resp.contains("IsImage=\"true\"")) {
                                    requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, CustomerImageFragment.instantiate(data)).commit();
                                } else {
                                    requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, EnterDetailFragment.instantiate(data)).commit();
                                }
                            } else {
                                Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.ERROR_RESPONSE_COUNT, authResponse.getSessionId());
                                String message = response_base.getJSONObject("Menu").getJSONObject("Response").getString("Display");
                                dialogProvider.showError(Html.fromHtml(message).toString());
                            }
                        }
                    } else {
                        if (response_base.toString().contains("Display")) {
                            Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.ERROR_RESPONSE_COUNT, authResponse.getSessionId());
                            Spanned span = Html.fromHtml(response_base.getJSONObject("Menu")
                                    .getJSONObject("Response")
                                    .optString("Display", ErrorMessages.OPERATION_NOT_COMPLETED));
                            dialogProvider.showError(span.toString(), new Function1<DialogListener<? extends Object>, Unit>() {
                                @Override
                                public Unit invoke(DialogListener<?> dialogListener) {
                                    ((OnlineActivity) getActivity()).goHome();
                                    return null;
                                }
                            });
                        } else {
                            Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.ERROR_RESPONSE_COUNT, authResponse.getSessionId());
                            Spanned span = Html.fromHtml(response_base.
                                    optString("Menu", ErrorMessages.PHONE_NOT_REGISTERED));
                            dialogProvider.showError(span.toString());
                        }
                    }
                }

            }
        } catch (Exception c) {
            Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.NO_INTERNET_COUNT, authResponse.getSessionId());
            handleException(true);
        }
    }

    @Override
    public void onClick(View view) {
        if (view == optionName) {
            ((CreditClubActivity) getActivity()).getDialogProvider().showError(optionName.getText().toString());
        }
    }
}


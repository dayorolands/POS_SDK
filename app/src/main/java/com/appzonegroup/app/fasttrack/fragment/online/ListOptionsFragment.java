package com.appzonegroup.app.fasttrack.fragment.online;


import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.appzonegroup.app.fasttrack.OnlineActivity;
import com.appzonegroup.app.fasttrack.BankOneApplication;
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
import com.creditclub.core.data.Encryption;
import com.appzonegroup.app.fasttrack.utility.online.ErrorMessages;
import com.appzonegroup.app.fasttrack.utility.online.XmlToJson;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class ListOptionsFragment extends Fragment implements AdapterView.OnItemClickListener,
        View.OnClickListener {

    @Bind(R.id.listView)
    ListView listView;

    @Bind(R.id.optionsName)
    TextView optionName;

    private static ArrayList<Option> menuOptions;
    private static String title;
    private AuthResponse authResponse;


    AlertDialog.Builder dialog;

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
        ButterKnife.bind(this, view);
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

        dialog = new AlertDialog.Builder(getActivity());
        dialog.setPositiveButton("OK", null);
        dialog.setCancelable(false);

        listView.setAdapter(new OptionsAdapter(getActivity(), R.layout.item_option, getMenuOptions()));
        listView.setOnItemClickListener(this);
        view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

      authResponse = ((BankOneApplication)getActivity().getApplication()).getAuthResponse();// LocalStorage.getCachedAuthResponse(getActivity());

    }

    String finalLocation;
    Option selectedOption;

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        selectedOption = (Option) adapterView.getAdapter().getItem(i);
        if (selectedOption.getName().equalsIgnoreCase("CANCEL")) {
            ((OnlineActivity) getActivity()).goHome();
        } else {
            final ProgressDialog pdialog = new ProgressDialog(getActivity());
            pdialog.setCancelable(false);
            pdialog.setMessage("Loading...");
            pdialog.show();
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


            ah.getNextOperation(authResponse.getPhoneNumber(), authResponse.getSessionId(), selectedOption.getIndex(), finalLocation,new APIHelper.VolleyCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result,boolean status) {
                pdialog.dismiss();
                if (status) {
                    processData(result);
                } else {
                    if (e != null) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                        e.printStackTrace();
                        if (e instanceof TimeoutException) {
                            dialog.setMessage("Something went wrong! Please try again.")
                                .setPositiveButton("CONTINUE", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i)
                                    {
                                        dialogInterface.dismiss();
                                        pdialog.show();
                                        ah.continueNextOperation(authResponse.getPhoneNumber(), authResponse.getSessionId(), selectedOption.getIndex(), finalLocation, new APIHelper.VolleyCallback<String>(){


                                                    @Override
                                                    public void onCompleted(Exception e, String result,boolean status) {
                                            pdialog.dismiss();
                                            if (status) {
                                                processData(result);
                                            } else {
                                                Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.NO_INTERNET_COUNT, authResponse.getSessionId());
                                                dialog.setMessage("Connection lost").setPositiveButton("OK", null).create().setCanceledOnTouchOutside(false);
                                                dialog.show();
                                            }
                                            }
                                        });
                                    }
                                }).setCancelable(false).create()
                            .setCanceledOnTouchOutside(false);
                            dialog.show();
                        } else {
                            Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.NO_INTERNET_COUNT, authResponse.getSessionId());
                            dialog.setMessage("Connection lost").setPositiveButton("OK", null).create().setCanceledOnTouchOutside(false);
                            dialog.show();
                        }
                    } else {
                        Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.NO_INTERNET_COUNT, authResponse.getSessionId());
                        dialog.setMessage("Connection lost").setPositiveButton("OK", null).create().setCanceledOnTouchOutside(false);
                        dialog.show();
                    }
                }
                }
            });
        }
    }

    private void processData(String result) {
        try {
            String answer = Response.fixResponse(result);
            String decryptedAnswer = Encryption.decrypt(answer);
            JSONObject response = XmlToJson.convertXmlToJson(decryptedAnswer);
            if (response == null) {
                Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.NO_INTERNET_COUNT, authResponse.getSessionId());
                dialog.setMessage("Connection lost")
                        .setPositiveButton("OK", null)
                        .create().setCanceledOnTouchOutside(false);
                dialog.show();
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
                            getFragmentManager().beginTransaction().replace(R.id.container, ListOptionsFragment.instantiate(menuWrapper, false)).commit();
                        } else {
                            Object menuWrapper = response_base.getJSONObject("Menu").getJSONObject("Response").get("Display");
                            if (menuWrapper instanceof String && resp.contains("ShouldMask") && !resp.contains("Invalid Response")) {
                                Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.SUCCESS_COUNT, authResponse.getSessionId());
                                JSONObject data = response_base.getJSONObject("Menu").getJSONObject("Response");
                                if (resp.contains("IsImage=\"true\"")) {
                                    getFragmentManager().beginTransaction().replace(R.id.container, CustomerImageFragment.instantiate(data)).commit();
                                } else {
                                    getFragmentManager().beginTransaction().replace(R.id.container, EnterDetailFragment.instantiate(data)).commit();
                                }
                            } else {
                                Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.ERROR_RESPONSE_COUNT, authResponse.getSessionId());
                                String message = response_base.getJSONObject("Menu").getJSONObject("Response").getString("Display");
                                dialog.setMessage(Html.fromHtml(message)).create().setCanceledOnTouchOutside(false);
                                dialog.show();
                            }
                        }
                    } else {
                        if (response_base.toString().contains("Display")) {
                            Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.ERROR_RESPONSE_COUNT, authResponse.getSessionId());
                            dialog.setMessage(Html.fromHtml(response_base.getJSONObject("Menu")
                                .getJSONObject("Response")
                                .optString("Display", ErrorMessages.OPERATION_NOT_COMPLETED)))
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        ((OnlineActivity) getActivity()).goHome();
                                    }
                                }).create().setCanceledOnTouchOutside(false);
                            dialog.show();
                        } else {
                            Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.ERROR_RESPONSE_COUNT, authResponse.getSessionId());
                            dialog.setMessage(Html.fromHtml(response_base.
                                    optString("Menu", ErrorMessages.PHONE_NOT_REGISTERED)))
                                    .create().setCanceledOnTouchOutside(false);
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
                            final ProgressDialog pdialog = new ProgressDialog(getActivity());
                            pdialog.setCancelable(false);
                            pdialog.setMessage("Loading...");
                            pdialog.show();
                            final APIHelper ah = new APIHelper(getActivity());
                            finalLocation = "0.00;0.00";
                            final GPSTracker gpsTracker = new GPSTracker(getActivity());
                            if (gpsTracker.getLocation() != null) {
                                String longitude = String.valueOf(gpsTracker.getLocation().getLongitude());
                                String latitude = String.valueOf(gpsTracker.getLocation().getLatitude());
                                finalLocation = latitude + ";" + longitude;
                            }

                            AuthResponse authResponse = LocalStorage.getCachedAuthResponse(getActivity());
                            new APIHelper(getActivity()).continueNextOperation(authResponse.getPhoneNumber(), authResponse.getSessionId(), selectedOption.getIndex(), finalLocation,new APIHelper.VolleyCallback<String>() {
                                    @Override
                                    public void onCompleted(Exception e, String result,boolean status) {
                                        if (status) {
                                            processData(result);
                                        } else {
                                            dialog.setMessage("Connection lost")
                                                .setPositiveButton("OK", null)
                                                .create()
                                                .setCanceledOnTouchOutside(false);
                                            dialog.show();
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

    @Override
    public void onClick(View view) {
        if (view == optionName) {
            dialog.setMessage(optionName.getText().toString()).show();
        }
    }
}


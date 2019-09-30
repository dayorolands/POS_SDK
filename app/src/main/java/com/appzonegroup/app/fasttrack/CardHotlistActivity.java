package com.appzonegroup.app.fasttrack;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.appzonegroup.app.fasttrack.model.AppConstants;
import com.appzonegroup.app.fasttrack.model.BankOneAPICallBaseResponse;
import com.appzonegroup.app.fasttrack.network.APICaller;
import com.appzonegroup.app.fasttrack.scheduler.AndroidSchedulers;
import com.appzonegroup.app.fasttrack.scheduler.HandlerScheduler;
import com.appzonegroup.app.fasttrack.utility.Dialogs;
import com.appzonegroup.app.fasttrack.utility.Misc;
import com.google.gson.Gson;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func0;

public class CardHotlistActivity extends AppCompatActivity {

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    Dialog loadingDialog;
    Handler handler;
    private String cardPAN, accountNumber, hotlistReason;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_hotlist);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    public void hotlist_card_click(View view) {

        cardPAN = ((EditText)findViewById(R.id.card_pan_et)).getText().toString().trim();
        accountNumber = ((EditText)findViewById(R.id.account_number_et)).getText().toString();
        if (cardPAN.length() == 0 && accountNumber.length() == 0)
        {
            Dialogs.showErrorMessage(this, "Please enter either the card PAN or customer number");
            return;
        }

        hotlistReason = ((EditText)findViewById(R.id.hotlist_reason_et)).getText().toString();
        if (hotlistReason.length() == 0)
        {
            Dialogs.showErrorMessage(this, "Please enter the reason you want to hot list the card");
            return;
        }

        /*token = ((EditText)findViewById(R.id.hotlist_reason_et)).getText().toString();
        if (token.length() == 0)
        {
            Dialogs.showErrorMessage(this, "Please enter your token");
            return;
        }*/



        handler = Misc.setupScheduler();
        loadingDialog = Dialogs.getProgress(this, "Hotlisting the card...");
        loadingDialog.show();

    }

    Observable<String> myObservable() {
        return Observable.defer(new Func0<Observable<String>>() {
            @Override
            public Observable<String> call() {
                String result = null;
                try
                {
                    result = APICaller.makeGetRequest2(AppConstants.getCardHotlistUrl(cardPAN, accountNumber, hotlistReason, AppConstants.getSterlingHotlistToken()));
                    Log.e("Server r", result);

                } catch (Exception e) {

                }

                return Observable.just(result);
            }
        });
    }


    void runScheduler() {
        myObservable()
                // Run on a background thread
                .subscribeOn(HandlerScheduler.from(handler))
                // Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("API Call ERROR", "onError()", e);
                    }

                    @Override
                    public void onNext(String result) {
                        if (loadingDialog.isShowing())
                            loadingDialog.dismiss();

                        Gson gson = new Gson();


                        if (!result.startsWith("Error")) {

                            BankOneAPICallBaseResponse response = gson.fromJson(result, BankOneAPICallBaseResponse.class);
                            if (response.isStatus())
                            {
                                final Dialog dialog = Dialogs.getInformationDialog(CardHotlistActivity.this, response.getStatusDetails(), true);
                                /*dialog.findViewById(R.id.ok_btn).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                });*/
                                dialog.show();
                            }else
                            {
                                Dialogs.showErrorMessage(CardHotlistActivity.this, response.getStatusDetails());
                            }

                        } else {
                            Dialogs.showErrorMessage(CardHotlistActivity.this, "A network-related error just occurred.");
                        }



                    }
                });
    }


}

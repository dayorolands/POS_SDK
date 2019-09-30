package com.appzonegroup.app.fasttrack;

import android.app.Dialog;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.appzonegroup.app.fasttrack.adapter.DepositReportAdapter;
import com.appzonegroup.app.fasttrack.model.AppConstants;
import com.appzonegroup.app.fasttrack.model.Report;
import com.appzonegroup.app.fasttrack.model.ReportItem;
import com.appzonegroup.app.fasttrack.model.TransactionStatus;
import com.appzonegroup.app.fasttrack.model.TransactionTypeName;
import com.appzonegroup.app.fasttrack.network.APICaller;
import com.appzonegroup.app.fasttrack.scheduler.AndroidSchedulers;
import com.appzonegroup.app.fasttrack.scheduler.HandlerScheduler;
import com.appzonegroup.app.fasttrack.utility.CalendarDialog;
import com.appzonegroup.app.fasttrack.utility.Dialogs;
import com.appzonegroup.app.fasttrack.utility.LocalStorage;
import com.appzonegroup.app.fasttrack.utility.Misc;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;

import java.util.Date;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func0;

public class DepositReportActivity extends AppCompatActivity implements View.OnClickListener {

    Date fromDate, toDate;
    TextView from_date_tv, to_date_tv;
    Handler backgroundHandler;
    Dialog loadingDialog;
    ListView listView;
    boolean isFromDate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposit_report);

        loadingDialog = Dialogs.getProgressDialog(this);
        backgroundHandler = Misc.setupScheduler();

        fromDate = Misc.getCurrentDateTime();
        toDate = Misc.getCurrentDateTime();

        listView = (ListView)findViewById(R.id.deposits_listview);
        from_date_tv = (TextView)findViewById(R.id.from_date_tv);
        to_date_tv = (TextView)findViewById(R.id.to_date_tv);

        from_date_tv.setOnClickListener(this);
        to_date_tv.setOnClickListener(this);

        String currentDateString = Misc.dateToShortString(fromDate);
        from_date_tv.setText(currentDateString);
        to_date_tv.setText(currentDateString);

        loadingDialog.show();
        runScheduler();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId())
        {
            case R.id.from_date_tv:{
                isFromDate = true;
                show_calendar();
                break;
            }
            case R.id.to_date_tv:{
                isFromDate = false;
                show_calendar();
                break;
            }
            default:{
                loadingDialog.show();
                runScheduler();
                break;
            }
        }

    }

    public void show_calendar() {

        final Dialog dialog = CalendarDialog.showCalendarDialog(DepositReportActivity.this);
        final DatePicker datePicker = (DatePicker)dialog.findViewById(R.id.datePicker);

        dialog.findViewById(R.id.calendarViewButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int dayOfMonth = datePicker.getDayOfMonth();
                int month = datePicker.getMonth() + 1;

                String DD = dayOfMonth > 9 ? (dayOfMonth + "") : ("0" + dayOfMonth);
                String MM = month > 9 ?(month + "") : ("0" + month);
                String dateString = String.format("%d-%s-%s", datePicker.getYear(), MM, DD);
                if (isFromDate)
                {
                    fromDate = Misc.stringToDate(dateString);
                    from_date_tv.setText(dateString);
                }else{
                    toDate = Misc.stringToDate(dateString);
                    to_date_tv.setText(dateString);
                }
                dialog.dismiss();

            }
        });
        dialog.show();
    }

    void runScheduler() {
        myObservable()
                // Run on a background thread
                .subscribeOn(HandlerScheduler.from(backgroundHandler))
                // Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (loadingDialog.isShowing())
                        {
                            loadingDialog.dismiss();
                        }
                        Toast.makeText(getBaseContext(), "An error just occurred. Please try again later", Toast.LENGTH_LONG).show();
                        Crashlytics.logException(new Exception(e.getMessage()));
                    }

                    @Override
                    public void onNext(String result) {

                        if (loadingDialog.isShowing())
                            loadingDialog.dismiss();

                        if (result.length() == 0)
                        {
                            Toast.makeText(getBaseContext(), "You don't seem to have internet... Please ensure that you have internet connection", Toast.LENGTH_LONG).show();
                            //return;
                        }else{

                            result = result.replace("\\", "").replace("\n", "").trim();
                            if (result.startsWith("\"") && result.endsWith("\""))
                            {
                                result = result.substring(1, result.length() - 1);
                            }
                            final Report report = new Gson().fromJson(result, Report.class);
                            DepositReportAdapter adapter2 = new DepositReportAdapter(DepositReportActivity.this, report.getReports());
                            listView.setAdapter(adapter2);

                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    ReportItem reportItem = report.getReports().get(position);

                                    final Dialog dialog = Dialogs.getDialog(R.layout.deposit_report_detail, DepositReportActivity.this);
                                    ((TextView)dialog.findViewById(R.id.ID_tv)).setText(String.valueOf(reportItem.getID()));
                                    ((TextView)dialog.findViewById(R.id.customer_name_tv)).setText(reportItem.getCustomerName());
                                    ((TextView)dialog.findViewById(R.id.customer_phone_tv)).setText(reportItem.getCustomerPhone());
                                    ((TextView)dialog.findViewById(R.id.amount_tv)).setText(Misc.toMoneyFormat(reportItem.getAmount() / 100.0));
                                    ((TextView)dialog.findViewById(R.id.product_tv)).setText(reportItem.getProductName());
                                    ((TextView)dialog.findViewById(R.id.date_tv)).setText(reportItem.getDate());
                                    ((TextView)dialog.findViewById(R.id.from_number_tv)).setText(reportItem.getFromPhoneNumber());
                                    ((TextView)dialog.findViewById(R.id.from_account_tv)).setText(reportItem.getFrom());
                                    ((TextView)dialog.findViewById(R.id.to_account_tv)).setText(reportItem.getTo());

                                    dialog.findViewById(R.id.close_btn).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            dialog.dismiss();
                                        }
                                    });

                                    dialog.show();


                                }
                            });

                            listView.invalidate();
                        }

                    }
                });
    }

    Observable<String> myObservable() {
        return Observable.defer(new Func0<Observable<String>>() {
            @Override
            public Observable<String> call() {
                String result = "";
                try
                {
                    String Token = LocalStorage.GetValueFor(AppConstants.API_TOKEN, getBaseContext());
                    String agentPhone = LocalStorage.GetValueFor(AppConstants.AGENT_PHONE, getBaseContext());
                    String institutionCode = LocalStorage.GetValueFor(AppConstants.INSTITUTION_CODE, getBaseContext());

                    String url = Misc.getReportURL(agentPhone,
                            institutionCode,
                            TransactionTypeName.CashIn.ordinal() + 1,
                            Misc.dateToShortString(fromDate),
                            Misc.dateToShortString(toDate),
                            TransactionStatus.Successful.ordinal() + 1,
                            0, 200);
                    result = APICaller.makeGetRequest(url,Token);

                } catch (Exception e) {
                    Log.e("Register", e.getMessage());
                    Crashlytics.logException(new Exception(e.getMessage()));
                }

                return Observable.just(result);
            }
        });
    }

}

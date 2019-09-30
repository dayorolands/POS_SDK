package com.appzonegroup.app.fasttrack;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Spinner;

import com.appzonegroup.app.fasttrack.ui.TextView;
import com.appzonegroup.app.fasttrack.utility.CalendarDialog;
import com.appzonegroup.app.fasttrack.utility.Misc;

import java.util.Calendar;
import java.util.Locale;

public class SelectReportActivity extends BaseActivity {
    private TextView startDateET;
    private TextView endDateET;
    private Spinner transactionTypeSpinner;
    private String startDate;
    private String endDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_report);
        endDateET = findViewById(R.id.end_date_et);
        startDateET = findViewById(R.id.start_date_et);
        transactionTypeSpinner = findViewById(R.id.report_type_spinner);
        setDefaultTime();
    }

    public void show_calendar(View view) {

        final Dialog dialog = CalendarDialog.showCalendarDialog(SelectReportActivity.this);
        final DatePicker datePicker = (DatePicker) dialog.findViewById(R.id.datePicker);

        dialog.findViewById(R.id.calendarViewButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int dayOfMonth = datePicker.getDayOfMonth();
                int month = datePicker.getMonth() + 1;

                String DD = dayOfMonth > 9 ? (dayOfMonth + "") : ("0" + dayOfMonth);
                String MM = month > 9 ?(month + "") : ("0" + month);

                startDate = datePicker.getYear()+"-"+ MM +"-"+ DD;

                startDateET.setText(String.format(Locale.getDefault(), "%s-%s-%s", DD, MM, datePicker.getYear()));
                //startDateET.setGravity(Gravity.LEFT);
                dialog.dismiss();

            }
        });
    }

    public void show_calendar2(View view) {

        final Dialog dialog = CalendarDialog.showCalendarDialog(SelectReportActivity.this);
        final DatePicker datePicker = (DatePicker) dialog.findViewById(R.id.datePicker);

        dialog.findViewById(R.id.calendarViewButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int dayOfMonth = datePicker.getDayOfMonth();
                int month = datePicker.getMonth() + 1;

                String DD = dayOfMonth > 9 ? (dayOfMonth + "") : ("0" + dayOfMonth);
                String MM = month > 9 ?(month + "") : ("0" + month);
                endDate = datePicker.getYear()+"-"+ MM +"-"+ DD;

                endDateET.setText(String.format(Locale.getDefault(), "%s-%s-%s", DD, MM, datePicker.getYear()));
                //endDateET.setGravity(Gravity.LEFT);
                dialog.dismiss();

            }
        });
    }

    public void showReport(View view){
        if(transactionTypeSpinner.getSelectedItemId() == 0){
            showError("please select a report type");
            return;
        }

        Intent toReport = new Intent(getBaseContext(), AccountReportActivity.class);
        toReport.putExtra("TRANSACTION_TYPE", transactionTypeSpinner.getSelectedItem().toString());
        toReport.putExtra("TRANSACTION_TYPE_ID",String.valueOf(getReportId(transactionTypeSpinner.getSelectedItemId())));
        toReport.putExtra("START_DATE", startDate);
        toReport.putExtra("END_DATE", endDate);
        startActivity(toReport);
    }

    public void setDefaultTime(){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        startDate = Misc.dateToShortString(cal.getTime());
        endDate = Misc.dateToShortString(Misc.getCurrentDateTime());
        startDateET.setText(Misc.dateToShortStringDDMMYYYY(cal.getTime()));
        endDateET.setText(Misc.dateToShortStringDDMMYYYY(Misc.getCurrentDateTime()));
    }

    long getReportId(long selectedReportPosition)
    {
        if (selectedReportPosition >= 1 && selectedReportPosition <= 8) {

            if (selectedReportPosition > 2)
                return selectedReportPosition + 2;

            return selectedReportPosition;
        }

        if (selectedReportPosition >= 9 && selectedReportPosition <= 13)
            return selectedReportPosition + 9 + 2;

        return -1;
    }

}

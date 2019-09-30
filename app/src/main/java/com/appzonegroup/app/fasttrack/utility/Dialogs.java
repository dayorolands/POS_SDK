package com.appzonegroup.app.fasttrack.utility;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.appzonegroup.app.fasttrack.R;
import com.appzonegroup.app.fasttrack.adapter.PinpadGridViewAdapter;

/**
 * Created by Joseph on 6/5/2016.
 */
public class Dialogs {

    public static ProgressDialog getProgressDialog(Activity activity){
        ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.show();

        return progressDialog;
    }

    public static Dialog getPINDialog(final Activity activity)
    {
        try {
            final Dialog dialog = getDialog(R.layout.dialog_scambled_keyboard, activity);
            ((GridView) dialog.findViewById(R.id.pin_pad_grid_view)).setAdapter(new PinpadGridViewAdapter(activity, Misc.getScrambledPINPadText()));

            dialog.findViewById(R.id.instruction_info_image_view).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Dialog instructionDialog = getDialog(R.layout.card_pan_instruction, activity);
                    instructionDialog.findViewById(R.id.close_btn).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            instructionDialog.dismiss();
                        }
                    });
                    instructionDialog.show();
                }
            });

            dialog.findViewById(R.id.cancel_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });

            return dialog;
        }catch (Exception ex)
        {
            Log.e("PIN Dialog", ex.toString());
            return null;
        }
    }

    public static Dialog getDialog(Activity activity, String message, String okButtonText, String cancelButtonText, View.OnClickListener okButtonListener, View.OnClickListener cancelButtonListener)
    {
        Dialog dialog = getDialog(R.layout.dialog_question_with_two_buttons, activity);
        ((TextView)dialog.findViewById(R.id.message_tv)).setText(message);
        if (okButtonText != null)
        {
            ((Button)dialog.findViewById(R.id.ok_btn)).setText(okButtonText);
        }
        dialog.findViewById(R.id.ok_btn).setOnClickListener(okButtonListener);

        if (cancelButtonText != null)
        {
            ((Button)dialog.findViewById(R.id.cancel_btn)).setText(cancelButtonText);
        }

        dialog.findViewById(R.id.cancel_btn).setOnClickListener(cancelButtonListener);

        return dialog;
    }

    public static Dialog getInformationDialog(final Activity activity, String message, final boolean shouldClose)
    {
        final Dialog dialog = getDialog(R.layout.dialog_info_success, activity);
        if (message != null)
            ((TextView)dialog.findViewById(R.id.message_tv)).setText(message);

        dialog.findViewById(R.id.ok_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if (shouldClose)
                    activity.finish();
            }
        });

        return dialog;
    }

    public static Dialog getErrorDialog(Activity activity, String message)
    {
        final Dialog dialog = getDialog(R.layout.pos_dialog_error, activity);
        if (message != null)
            ((TextView)dialog.findViewById(R.id.message_tv)).setText(message);

        dialog.findViewById(R.id.close_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        return dialog;
    }

    public static Dialog getSuccessDialog(Activity activity, String message)
    {
        final Dialog dialog = getDialog(R.layout.dialog_success, activity);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        if (message != null)
            ((TextView)dialog.findViewById(R.id.message_tv)).setText(message);

        dialog.findViewById(R.id.close_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        return dialog;
    }

    public static Dialog getBankSelectionDialog(Activity activity)
    {
        final Dialog dialog = getDialog(R.layout.dialog_card_transaction_bank_selection, activity);

        dialog.findViewById(R.id.cancel_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        return dialog;
    }

    public static Dialog getDialog(int layoutID, Activity context){

        Dialog dialog = new Dialog(context);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.setContentView(layoutID);
        //dialog.show();
        return dialog;
    }

    public static Dialog getProgress(Activity activity, String header)
    {
        Dialog dialog = getDialog(R.layout.dialog_progress_layout, activity);
        if (header != null)
            ((TextView)dialog.findViewById(R.id.header_tv)).setText(header);

        return dialog;
    }

    public static Dialog getQuestionDialog(Activity activity, String message)
    {
        Dialog dialog = Dialogs.getDialog(R.layout.dialog_question_with_two_buttons, activity);
        ((TextView)dialog.findViewById(R.id.message_tv)).setText(message);
        return dialog;
    }

    public static ProgressDialog getProgressDialog(Activity activity, String msg){
        ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setMessage(msg);
        progressDialog.show();

        return progressDialog;
    }

    public static AlertDialog getAlertDialog(Context context, String msg) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
        builder1.setMessage(msg);
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });


        AlertDialog alert11 = builder1.create();

        return alert11;
    }

    public static Dialog getWithdrawalTransactionModeDialog(Activity activity)
    {
        final Dialog dialog = getDialog(R.layout.dialog_inter_bank_transaction_type_selection, activity);
        dialog.findViewById(R.id.cancel_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        return dialog;
    }

    public static Dialog getPhoneNumberInputDialog(Activity activity)
    {
        final Dialog dialog = getDialog(R.layout.dialog_card_transaction_phone_input, activity);

        dialog.findViewById(R.id.cancel_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        return dialog;
    }

    public static void showErrorMessage(Activity activity, String message)
    {
        getErrorDialog(activity, message).show();
    }

}

package com.appzonegroup.app.fasttrack;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;

import com.acs.audiojack.AudioJackReader;
import com.acs.audiojack.CardTimeoutException;
import com.acs.audiojack.CommunicationErrorException;
import com.acs.audiojack.CommunicationTimeoutException;
import com.acs.audiojack.InvalidDeviceStateException;
import com.acs.audiojack.ReaderNotStartedException;
import com.acs.audiojack.RemovedCardException;
import com.acs.audiojack.RequestQueueFullException;
import com.acs.audiojack.UnresponsiveCardException;
import com.acs.audiojack.UnsupportedCardException;
import com.appzonegroup.app.fasttrack.model.AID;
import com.appzonegroup.app.fasttrack.model.ATMCard;
import com.appzonegroup.app.fasttrack.model.AppConstants;
import com.appzonegroup.app.fasttrack.model.ZoneBank;
import com.appzonegroup.app.fasttrack.model.CardTransactionResponse;
import com.appzonegroup.app.fasttrack.model.CardValidationResponseModel;
import com.appzonegroup.app.fasttrack.model.MPosCashOutTransactionType;
import com.appzonegroup.app.fasttrack.network.APICaller;
import com.appzonegroup.app.fasttrack.scheduler.AndroidSchedulers;
import com.appzonegroup.app.fasttrack.scheduler.HandlerScheduler;
import com.appzonegroup.app.fasttrack.utility.AppStatus;
import com.appzonegroup.app.fasttrack.utility.CardMisc;
import com.appzonegroup.app.fasttrack.utility.Dialogs;
import com.appzonegroup.app.fasttrack.utility.LocalStorage;
import com.appzonegroup.app.fasttrack.utility.Misc;
import com.appzonegroup.app.fasttrack.utility.TrackGPS;
import com.appzonegroup.app.fasttrack.utility.TripleDES;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func0;

/**
 * Created by DELL on 2/27/2017.
 */

public class CardBasedWithdrawalActivity extends AppCompatActivity {

    enum InternetAction {
        Verify,
        Transfer
    }
    InternetAction internetAction;// = InternetAction.Verify;
    Handler backgroundHandler;

    String abduResult = null;
    byte[] command;
    private ArrayList<ZoneBank> zoneBanks;

    private AudioJackReader mReader;
    private AudioManager mAudioManager;
    private final int mIccWaitTimeout = 10000;
    //private final int mIccControlCode = AudioJackReader.IOCTL_CCID_ESCAPE;
    boolean isUserData = false;
    //private String mIccPowerAction = "Warm reset";
    int currentVolume = 0;
    String cardPAN = "", cardHolder = "", expiryDate = "";

    private final BroadcastReceiver mHeadsetPlugReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {

                boolean plugged = (intent.getIntExtra("state", 0) == 1);

                /* Mute the audio output if the reader is unplugged. */
                mReader.setMute(!plugged);
            }
        }
    };

    TextView message;
    Dialog progressDialog;
    AppStatus appStatus = new AppStatus();
    String agentPhone = "";
    String institutionCode = "";
    public TrackGPS gps;
    String clearPIN = "", cardCipher;
    double amount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_based_withdrawal);

        gps = new TrackGPS(CardBasedWithdrawalActivity.this);

        if(gps.canGetLocation()) {

        } else
        {
            gps.showSettingsAlert();
        }

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mReader = new AudioJackReader(mAudioManager, true);

        currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        /* Register the headset plug receiver. */
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(mHeadsetPlugReceiver, filter);

        progressDialog = Dialogs.getProgress(this, null);

    }

    void showPINDialog(){
        hideProgressBar();
        final Dialog dialog = Dialogs.getPINDialog(this);

        dialog.findViewById(R.id.submit_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String CVV = "039";// ((EditText)dialog.findViewById(R.id.cvv_et)).getText().toString().trim();
                String enteredPIN = "2580"; ((TextView)dialog.findViewById(R.id.pin_et)).getText().toString();


                if (CVV.length() < 3 || CVV.length() > 4)
                {
                    showNotification("Incorrect CVV length. Please verify and try again.");
                    return;
                }

                if (enteredPIN.length() != 4)
                {
                    showNotification("Please enter your 4-digit PIN");
                    return;
                }

                ATMCard atmCard = new ATMCard();
                atmCard.setCVV(CVV);
                atmCard.setExpiryDate(expiryDate);
                atmCard.setPIN(enteredPIN);
                atmCard.setPAN(cardPAN);
                String json = new Gson().toJson(atmCard);
                cardCipher = TripleDES.encrypt(json);// Misc.encryptText(new Gson().toJson(atmCard));

                /*Intent intent = new Intent(CardBasedWithdrawalActivity.this, InternetCallActivity.class);
                intent.putExtra("DATA", cardCipher);
                startActivity(intent);*/

                backgroundHandler = Misc.setupScheduler();
                showProgressBar("Sending the data...");
                internetAction = InternetAction.Verify;
                runScheduler(cardCipher);


            }
        });

        ((GridView)dialog.findViewById(R.id.pin_pad_grid_view)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String enteredPIN = ((TextView)dialog.findViewById(R.id.pin_et)).getText().toString();

                String buttonText = ((TextView)view.findViewById(R.id.pin_tv)).getText().toString();
                buttonText = enteredPIN.concat(buttonText);
                //If its a digit
                if (buttonText.length() == 1 && !buttonText.contains("<"))
                {
                    if (enteredPIN.length() >= 4)
                    {
                        ////showError("Your PIN cannot be more than four digits");
                        return;
                    }
                    //buttonText = enteredPIN.concat(buttonText);
                    ((TextView)dialog.findViewById(R.id.pin_et)).setText(buttonText);
                }
                else if (buttonText.contains("<"))
                {
                    buttonText = buttonText.substring(0, buttonText.length() - 2);
                    ((TextView)dialog.findViewById(R.id.pin_et)).setText(buttonText);
                }
                else
                {
                    clearPIN = buttonText;
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }

    void runScheduler(String postData) {
        myObservable(postData)
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
                        hideProgressBar();
                        Dialogs.getErrorDialog(CardBasedWithdrawalActivity.this, "An network-related error occurred.").show();
                    }

                    @Override
                    public void onNext(String result) {

                        hideProgressBar();

                        if (result == null)
                        {
                            showError("An error just occurred. Please ensure that you have internet and try again.");
                            return;
                        }
                        else
                        {
                            Log.e("Network call: ", result);
                        }

                        //Standard .NET additions to serialized objects
                        result = result.replace("\\", "").replace("\n", "").trim();

                        switch (internetAction)
                        {
                            case Verify:{

                                CardValidationResponseModel cardValidationResponseModel = new Gson().fromJson(result, CardValidationResponseModel.class);

                                internetAction = InternetAction.Transfer;
                                MPosCashOutTransactionType mPosCashOutTransactionType = new MPosCashOutTransactionType();
                                mPosCashOutTransactionType.setAgentsPhoneNumber(LocalStorage.GetValueFor(AppConstants.AGENT_PHONE, getBaseContext()));
                                mPosCashOutTransactionType.setAgentsPIN(LocalStorage.GetValueFor(AppConstants.AGENT_PIN, getBaseContext()));
                                mPosCashOutTransactionType.setTransactionAmount(String.valueOf((int)(amount*100)));
                                mPosCashOutTransactionType.setBeneficiaryAccountNumber(null);
                                mPosCashOutTransactionType.setBeneficiaryBank(null);
                                mPosCashOutTransactionType.setCardCipher(cardCipher);
                                mPosCashOutTransactionType.setInstitutionCode(LocalStorage.GetValueFor(AppConstants.INSTITUTION_CODE, getBaseContext()));
                                mPosCashOutTransactionType.setSenderBank(null);
                                mPosCashOutTransactionType.setSenderPhoneNumber(null);
                                mPosCashOutTransactionType.setTerminalID(null);
                                //mPosCashOutTransactionType.setToken(cardValidationResponseModel.getToken());

                                //Card already exists on Zone, make Transaction
                                if (cardValidationResponseModel.isStatus())
                                {
                                    showProgressBar("Completing transaction...");
                                //    Misc.increaseTransactionMonitorCounter(getBaseContext(), AppConstants.getSuccessCount());
                                    //runScheduler(new Gson().toJson(mPosCashOutTransactionType));

                                    if (!cardValidationResponseModel.isPhoneNumberExist() || !cardValidationResponseModel.isCardExist())//Card doesn't exist on zone; get the customer's phone number
                                    {
                                        //Misc.increaseTransactionMonitorCounter(getBaseContext(), AppConstants.getErrorResponseCount());
                                        getCustomerPhoneNumber(mPosCashOutTransactionType);
                                        return;
                                    }else if (!cardValidationResponseModel.isBinExist())
                                    {
                                        getATMCardBank(mPosCashOutTransactionType);
                                        return;
                                    }else
                                    {

                                    }

                                }else
                                {
                                    showError(cardValidationResponseModel.getMessage());
                                    return;
                                }


                            }
                            case Transfer:{

                                CardTransactionResponse cardTransactionResponse = new Gson().fromJson(result, CardTransactionResponse.class);
                                if (cardTransactionResponse.isStatus())
                                {
                                    //Misc.increaseTransactionMonitorCounter(getBaseContext(), AppConstants.getSuccessCount());
                                    final Dialog dialog = Dialogs.getInformationDialog(CardBasedWithdrawalActivity.this, "Transaction successful!\n You can now detach the POS", true);

                                    /*dialog.findViewById(R.id.ok_btn).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            dialog.dismiss();
                                            finish();
                                        }
                                    });*/

                                    dialog.show();
                                }
                                else
                                {
                                    //Misc.increaseTransactionMonitorCounter(getBaseContext(), AppConstants.getErrorResponseCount());
                                    showError(String.format(Locale.getDefault(), "%s: %s", cardTransactionResponse.getMessage(), cardTransactionResponse.getResponseDetails()));
                                }

                                break;
                            }
                        }


                    }
                });
    }

    Observable<String> myObservable(final String postData) {
        return Observable.defer(new Func0<Observable<String>>() {
            @Override
            public Observable<String> call() {
                String result = "";
                try
                {

                    String url = AppConstants.getBaseUrl();// + String.format(Constants.getRegisterDeviceURL(), merchantCode, terminalId);

                    switch (internetAction)
                    {
                        case Verify:{
                            LocalStorage.SaveValue(AppConstants.getSessionID(), Misc.getRandomString(), getBaseContext());

                            url += "/CreditClubMiddleWareAPI/MPOSCashOut/ValidateCard?phoneNumber=%s&institutionCode=%s";
                            url = String.format(Locale.getDefault(),
                                    url,
                                    LocalStorage.GetValueFor(AppConstants.AGENT_PHONE, getBaseContext()),
                                    LocalStorage.GetValueFor(AppConstants.INSTITUTION_CODE, getBaseContext())
                                    );
                            break;
                        }
                        case Transfer:{
                            url += "/CreditClubMiddleWareAPI/MPOSCashOut/Transfer";
                        }
                    }

                    result = APICaller.postRequest(getBaseContext(), url, internetAction == InternetAction.Transfer ?
                            postData : "\"" + postData + "\"");


                } catch (Exception e) {
                    Log.e("Register", e.getMessage());
                }

                return Observable.just(result);
            }
        });
    }

    private void getCustomerPhoneNumber(final MPosCashOutTransactionType mPosCashOutTransactionType)
    {
        final Dialog phoneInputDialog = Dialogs.getPhoneNumberInputDialog(this);
        phoneInputDialog.findViewById(R.id.continue_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String customerPhoneNumber = ((EditText)phoneInputDialog.findViewById(R.id.phone_no_et)).getText().toString().trim();

                if (customerPhoneNumber.length() != 11)
                {
                    showError("Phone number is incorrect.");
                    return;
                }

                mPosCashOutTransactionType.setSenderPhoneNumber(customerPhoneNumber);
                showProgressBar("Completing the transaction...");
                runScheduler(new Gson().toJson(mPosCashOutTransactionType));

            }
        });
        phoneInputDialog.show();
    }

    private void getATMCardBank(final MPosCashOutTransactionType mPosCashOutTransactionType)
    {
        zoneBanks = ZoneBank.getZoneBankList();
        ArrayList<String> bankNames = new ArrayList<>();
        bankNames.add("Select bank...");

        for (ZoneBank zoneBank : zoneBanks) {
            bankNames.add(zoneBank.getName());
        }

        final Dialog bankSelectionDialog = Dialogs.getBankSelectionDialog(this);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, bankNames);
        spinnerArrayAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
        ((Spinner)bankSelectionDialog.findViewById(R.id.bank_spinner)).setAdapter(spinnerArrayAdapter);
        bankSelectionDialog.findViewById(R.id.continue_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int selectedPosition = ((Spinner)bankSelectionDialog.findViewById(R.id.bank_spinner)).getSelectedItemPosition();
                if (selectedPosition == 0)
                {
                    Dialogs.showErrorMessage(CardBasedWithdrawalActivity.this, "Please select a bank");
                    return;
                }

                mPosCashOutTransactionType.setSenderBank(zoneBanks.get(selectedPosition - 1).getBank_Code());
                bankSelectionDialog.dismiss();
                showProgressBar("Saving your bank...");
                runScheduler(new Gson().toJson(mPosCashOutTransactionType));
            }
        });

        bankSelectionDialog.show();

    }

    void showNotification(String message){
        //Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
        if (progressDialog.isShowing())
            progressDialog.dismiss();
        Dialogs.getErrorDialog(this, message).show();
    }

    public void withdraw_button_click(View view){

        if(appStatus.getInstance(this).isOnline()) {

            String amountString = ((EditText)findViewById(R.id.withdrawal_amount_et)).getText().toString();

            if (amountString.length() == 0) {
                showNotification("Please enter the amount");
                return;
            }

            try
            {
                amount = Double.parseDouble(amountString);
            }catch (Exception ex)
            {
                showNotification("Please enter a valid amount");
                return;
            }

            if (amount <= 0)
            {
                showNotification("You cannot cash out less than =N= 1.00. Please enter a valid amount");
                return;
            }
            readCardAndTransmitData(0);

            /*final Dialog infoDialog = Dialogs.getInformationDialog(this, "1. Insert the card into POS\n\n2. Attach the POS to your ic_phone.\n\n3. Then click OK.");
            infoDialog.findViewById(R.id.ok_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    infoDialog.dismiss();
                    readCardAndTransmitData(0);
                }
            });
            infoDialog.show();*/
            //progressDialog = Dialogs.getProgress(this, null);

            /*AccountNoSendOTP accountnosendotp = new AccountNoSendOTP(CardBasedWithdrawalActivity.this, customerAccountNumber, amount, phoneNo,narration, transactionRef, sendOtp_dialog);
            accountnosendotp.execute();*/
        }else
        {
            showError("You do not have a working internet connection. Please connect to the internet and try again.");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        raiseVolumeToMaximum();
        mReader.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        returnVolumeToFirstState();
        mReader.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        returnVolumeToFirstState();
        mReader.stop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        returnVolumeToFirstState();
        mReader.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        raiseVolumeToMaximum();
        mReader.start();
    }

    void readCardAndTransmitData(final int errorCount)
    {
        //showProgressBar("Reading data from card...");
        //resetReader();
        cardPAN = "";
        expiryDate = "";
        new Thread(new Runnable() {
            @Override
            public void run()
            {

                showProgressBar("Resetting the device...");
                try {
                    mReader.reset();
                    mReader.power(0, 2, mIccWaitTimeout);
                    mReader.updateCardState(0, mIccWaitTimeout);
                    //mReader.setOnRawDataAvailableListener(new OnRawDataAvailableListener());
                } catch (ReaderNotStartedException e) {
                    e.printStackTrace();
                    hideProgressBar();
                    return;
                } catch (RequestQueueFullException e) {
                    e.printStackTrace();
                    hideProgressBar();
                    return;
                } catch (CommunicationTimeoutException e) {
                    e.printStackTrace();
                    hideProgressBar();
                    if (errorCount > 3) {
                        showError("The device is not responding. Please ensure it is well connected and try again.");
                        return;
                    }
                    readCardAndTransmitData(errorCount + 1);
                    return;
                } catch (CommunicationErrorException e) {
                    e.printStackTrace();
                    hideProgressBar();
                    return;
                } catch (RemovedCardException e) {
                    e.printStackTrace();
                    hideProgressBar();
                    return;
                } catch (UnresponsiveCardException e) {
                    e.printStackTrace();
                    hideProgressBar();
                    return;
                } catch (UnsupportedCardException e) {
                    e.printStackTrace();
                    hideProgressBar();
                    return;
                }

                hideProgressBar();
                showProgressBar("Reading card data...");

                for (AID aid : AID.values())
                {
                    Log.e("AID", aid.name());
                    if (cardPAN.length() > 0 && expiryDate.length() > 0 && cardHolder.length() > 0)
                    {
                        break;
                    }

                    try {
                        String commandString = Misc.getAID(aid);
                        Log.e("Command", commandString);
                        command = Misc.toByteArray(commandString);

                        byte[] mResponseApdu = transmit(command);// mReader.transmit(0, command, mIccWaitTimeout);

                        if (mResponseApdu == null)
                        {
                            return;
                        }

                        String resultString = Misc.toHexString(mResponseApdu);
                        Log.e("Result: ", resultString);
                        if (resultString.startsWith("61") || resultString.startsWith("90") || resultString.startsWith("77"))
                        {
                            //commandString = "80 a8 00 00 02 83 00 00";
                            Log.e("Command", commandString);
                            //Send GET PROCESSING OPTIONS command
                            command = Misc.toByteArray("80 a8 00 00 02 83 00 00");
                            mResponseApdu = transmit(command);// mReader.transmit(0, command, mIccWaitTimeout);

                            resultString = Misc.toHexString(mResponseApdu);
                            Log.e("G Proc command res: ", resultString);

                            if (resultString.startsWith("61") || resultString.startsWith("90") || resultString.startsWith("77"))
                            //if (resultString.startsWith("61"))
                            {
                                isUserData = true;

                                commandString = "80 a8 00 00 02 83 00 " + resultString.substring(3);
                                command = Misc.toByteArray(commandString.trim());
                                Log.e("Command for data", commandString + ".");

                                //mReader.setOnRawDataAvailableListener(new OnRawDataAvailableListener());

                                mResponseApdu = transmit(command);
                                resultString = Misc.toHexString(mResponseApdu);
                                abduResult = resultString;
                                Log.e("Result for data: ", resultString);
                                String nextDataLength = resultString.substring(3);
                                //================================
                                //VISA card application
                                commandString = "00 c0 00 00 " + nextDataLength;
                                command = Misc.toByteArray(commandString);
                                //Log.e("Command: ", commandString);
                                mResponseApdu = transmit(command);//sendReadRecord(commandString);
                                resultString = Misc.toHexString(mResponseApdu);
                                /*Log.e("Result", resultString);*/

                                if (resultString.startsWith("69") || resultString.startsWith("6D"))
                                {
                                    //TRY MasterCard Read Record Command
                                    /*commandString = "00 b2 01 14 00";// + nextDataLength;
                                    command = Misc.toByteArray(commandString);
                                    Log.e("Command: ", commandString);*/
                                    mResponseApdu = sendReadRecord("00 b2 01 14 00");//transmit(command);
                                    resultString = Misc.toHexString(mResponseApdu);
                                    //Log.e("Master Read: ", resultString);

                                    if (resultString.endsWith("69") || resultString.startsWith("6D"))
                                    {
                                        //TRY VERVE Read record command
                                        commandString = "00 b2 04 0c 00";// + nextDataLength;
                                        /*command = Misc.toByteArray(commandString);
                                        Log.e("Command: ", commandString);*/
                                        mResponseApdu = sendReadRecord(commandString);//transmit(command);
                                        resultString = Misc.toHexString(mResponseApdu);
                                        //Log.e("Verve Read: ", resultString);
                                    }
                                }


                                if (mResponseApdu != null)
                                    processCardDataLocation(mResponseApdu);

                                hideProgressBar();

                            }
                            return;
                        }

                    }
                    catch (Exception ex){
                        ex.printStackTrace();
                        return;
                    }
                }
            }

        }).start();
        hideProgressBar();

    }

    byte[] sendReadRecord(String commandString)
    {
        command = Misc.toByteArray(commandString);
        Log.e("Command: ", commandString);
        byte[] mResponseApdu = transmit(command);
        String resultString = Misc.toHexString(mResponseApdu);
        Log.e("Result", resultString);

        if (resultString.startsWith("6C") || resultString.startsWith("91"))
        {
            commandString = commandString.substring(0, commandString.lastIndexOf(' ')) + " " + resultString.substring(3);
            return sendReadRecord(commandString);
        }
        return mResponseApdu;
    }

    byte[] transmit(byte[] instruction)
    {
        try {
            return mReader.transmit(0, instruction, mIccWaitTimeout);
        } catch (ReaderNotStartedException e) {
            e.printStackTrace();
            showError("Reader has not started");
        } catch (RequestQueueFullException e) {
            e.printStackTrace();
            showError("Too many requests to process. Please detach and re-attach the POS");
        } catch (CommunicationTimeoutException e) {
            e.printStackTrace();
            showError("The operation timed out. Please try again");
        } catch (CommunicationErrorException e) {
            e.printStackTrace();
            showError("There was an error in communication. Please detach abd re-attach the POS and try again");
        } catch (RemovedCardException e) {
            e.printStackTrace();
            showError("You have not attached the card. Please attach a card and try again");
        } /*catch (UnresponsiveCardException e) {
                        e.printStackTrace();
                        showError("The card is not responding. Please ensure it is not a damaged card.");
                        return;
                    } catch (UnsupportedCardException e) {
                        e.printStackTrace();
                        showError("The inserted card is not supported. Please try again with another card");
                        return;
                    }*/ catch (InvalidDeviceStateException e){
            e.printStackTrace();
            showError("The device is not ready. Please re-attach the device and try again");
        } catch (CardTimeoutException e){
            e.printStackTrace();
            showError("The inserted card is not responding on time. Please ensure that it is not damaged.");
        }
        return null;
    }

    void processCardDataLocation(byte[] bytes)
    {

        String inString = Misc.toHexString(bytes).trim();
        Log.e("OnRawData", inString + " " + bytes.length);

        if (inString.endsWith("90 00"))
        {
            byte[] temp = Arrays.copyOfRange(bytes, 0, bytes.length - 2);
            bytes = temp;
        }

        if (inString.startsWith("70")){
            /*byte[] temp = Arrays.copyOfRange(bytes, 1, bytes.length - 1);
            bytes = temp;
            inString = Misc.toHexString(bytes).trim();*/

            getAllCardInformation(inString);
            return;
        }
        inString = Misc.toHexString(bytes).trim();
        Log.e("OnRawData", inString + " " + bytes.length);
        //Sample response
        //80 0e 5c 00 08 01 01 00 10 01 03 00 18 01 02 01
        //800e 5c00 08010100 10010300 18010201 - Data break down
        if (bytes.length > 4)
        //if (resultString.startsWith("61") || resultString.startsWith("90"))
        {
            int octet = 0;
            //Loop through records
            while (octet < bytes.length)
            {
                if (octet == 0)
                {
                    octet += 4;
                    continue;
                }
                byte[] temp =
                        {
                                bytes[octet],
                                bytes[octet + 1],
                                bytes[octet + 2],
                                bytes[octet + 3]
                        };

                //SFI File
                //records start index
                //records end index
                //number of records
                int[] aflData = Misc.getAFLOctetData(Misc.toHexString(temp));
                inString = "";
                for (int i : aflData)
                    inString += " " + i;

                Log.e("AFL Data", inString);
                int i = aflData[1];
                String SFI = Integer.toBinaryString(aflData[0]) + "000";
                int newSFI = Misc.binaryToDecimal(SFI) + 4;
                SFI = Integer.toHexString(newSFI).trim();

                if (SFI.length() == 1)
                {
                    SFI = "0" + SFI;
                }
                Log.e("SFI", SFI);
                //This will skip if no record exists
                //for (int i = 0; i < aflData[3]; i++)
                while (i <= aflData[2])// && aflData[3] > 0)
                {
                    //For the command
                    //00 B2 - Standard beginning for the read
                    // add P1 which is the record number - aflData[3]
                    // SFI File number + 4
                    // Send 00 first, Get the correct data length, then query for right data
                    String P1 = Integer.toHexString(i);
                    if (P1.length() == 1)
                        P1 = "0" + P1;

                    inString = String.format(Locale.getDefault(), "00 B2 %s %s %s",
                            P1,
                            SFI, //P2,//P2
                            "00"
                    );

                    command = Misc.toByteArray(inString);

                    byte[] data = transmit(command);//mReader.transmit(0, command, mIccWaitTimeout);
                    String resultString = Misc.toHexString(data);
                    Log.e("Result in raw: ", resultString);

                    if (resultString.startsWith("6C"))
                    {
                        inString = String.format(Locale.getDefault(), "00 B2 %s %s %s",
                                P1,
                                SFI, //P2,
                                resultString.substring(resultString.indexOf(" ")).trim()
                        );
                        Log.e("Command", inString);

                        command = Misc.toByteArray(inString);

                        data = transmit(command);//mReader.transmit(0, command, mIccWaitTimeout);

                        if (data == null)
                        {
                            hideProgressBar();
                            showError("Reader did not respond on time");
                            return;
                        }

                        inString = Misc.toHexString(data).toUpperCase();

                        Log.e("ResponseData", inString);

                        getAllCardInformation(inString);

                    }
                    i++;
                }

                octet += 4;
            }
        }
        isUserData = false;
    }

    void getAllCardInformation(String inString)
    {
        Log.e("Getting data", "getting data");
        //Cardholder name
        if (inString.contains("5F 20") && (inString.startsWith("70") || inString.startsWith("77")))
        {
            if (cardHolder.length() == 0) {
                cardHolder = Misc.getCardHolderName(inString);
                Log.e("CardHolder", cardHolder);
            }
        }

        //Expiry date
        if (inString.contains("5F 24"))
        {
            if (expiryDate.length() == 0) {
                expiryDate = CardMisc.getCardExpiryDate(inString);
                                /*expiryDate = Misc.getUserDataFromCard(data,
                                        Integer.valueOf(95).byteValue(),
                                        Integer.valueOf(36).byteValue(),
                                        true);*/
                Log.e("ExpiryDate", expiryDate);
            }
        }
        Log.e("PAN Check", inString);
        //PAN
        if (inString.contains("5A") && (inString.startsWith("70") || inString.startsWith("77")))
        {
            if (cardPAN.length() == 0) {
                cardPAN = Misc.getCardPAN(inString);

                Log.e("CardPAN", cardPAN);
            }
        }

        if (cardPAN.length() > 0 && expiryDate.length() > 0)
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showPINDialog();
                }
            });

        }
    }

    void hideProgressBar()
    {
        /* Hide the progress. */
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            };
        });
    }

    void showError(final String message)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideProgressBar();
                Dialogs.getErrorDialog(CardBasedWithdrawalActivity.this, message).show();
            }
        });

    }

    void showProgressBar(final String message)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                /* Show the progress. */
                if (!progressDialog.isShowing()) {
                    if (message != null)
                        ((TextView)progressDialog.findViewById(R.id.header_tv)).setText(message);
                    else
                        ((TextView)progressDialog.findViewById(R.id.header_tv)).setText("LOADING...");

                    progressDialog.show();
                }
            }
        });

    }

    private void raiseVolumeToMaximum() {

        int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        if (currentVolume < maxVolume) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, AudioManager.FLAG_SHOW_UI);
        }
    }

    private void returnVolumeToFirstState()
    {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, AudioManager.FLAG_SHOW_UI);
    }


    /*private class AccountNoSendOTP extends AsyncTask<String, String, String> {

        Context activity;
        String accountNo;
        String amount;
        String debitphoneNumber;
        String narration;
        String token;
        String reference;
        Gson gson = new Gson();
        AlertDialog sendOtp_dialog;





        AccountNoSendOTP(Context activity, String accountNo, String amount, String phoneNo, String narration, String reference ,AlertDialog sendOtp_dialog){
            this.activity = activity;
            this.accountNo = accountNo;
            this.amount = amount;
            this.debitphoneNumber = phoneNo;
            this.narration = narration;
            this.reference = reference;
            this.sendOtp_dialog = sendOtp_dialog;


        }

        @Override
        protected void onPreExecute() {
            Toast.makeText(getBaseContext(),"Sending Token", Toast.LENGTH_LONG).show();
        }

        @Override
        protected String doInBackground(String... params) {


            String Token = LocalStorage.GetValueFor(AppConstants.API_TOKEN, getBaseContext());
            agentPhone = LocalStorage.GetValueFor(AppConstants.AGENT_PHONE, getBaseContext());
            institutionCode = LocalStorage.GetValueFor(AppConstants.INSTITUTION_CODE, getBaseContext());

            String url = AppConstants.getAccessUrl() + "InitiateWithdrawal?customerAccountNumber="+accountNo+"&agentPhoneNumber="+agentPhone+"&institutionCode="+institutionCode+"&amount="+amount;

            TokenResponse serverResponse;
            String response = APICaller.makePostRequestNoJson(url,Token);



            try {

                serverResponse = gson.fromJson(response, TokenResponse.class);

                if (serverResponse.getStatus().equalsIgnoreCase("true")) {

                    return serverResponse.getStatusMessage();
                }else {

                    Crashlytics.logException(new Exception(serverResponse.getStatusMessage()));

                    return serverResponse.getStatusMessage();
                }

            }catch (Exception ex){

                Crashlytics.logException(new Exception(ex.getMessage()));

                return null;
            }


        }

        @Override
        protected void onPostExecute(String Successful) {

            if(Successful == null){

                progressDialog.setVisibility(View.GONE);
                buttonHolder.setVisibility(View.VISIBLE);
                message.setText(Successful);
                Toast.makeText(getBaseContext(), "An error has occurred", Toast.LENGTH_LONG).show();


            }else if(Successful.equalsIgnoreCase("Token sent successfully")){



                Intent i = new Intent(activity, WithdrawOTPActivity.class);
                i.putExtra("accountNo", accountNo);
                i.putExtra("amount", amount);
                i.putExtra("narration", narration);
                i.putExtra("transactionReference", reference);
                startActivity(i);

            }else {

                progressDialog.setVisibility(View.GONE);
                buttonHolder.setVisibility(View.VISIBLE);
                message.setText(Successful);
                Toast.makeText(getBaseContext(), "An error has occurred", Toast.LENGTH_LONG).show();
            }



        }
    }
*/

}




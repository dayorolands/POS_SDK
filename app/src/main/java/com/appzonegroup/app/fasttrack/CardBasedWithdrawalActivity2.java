package com.appzonegroup.app.fasttrack;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

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
import com.appzonegroup.app.fasttrack.adapter.PinpadGridViewAdapter;
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
import com.appzonegroup.app.fasttrack.utility.CardMisc;
import com.appzonegroup.app.fasttrack.utility.Dialogs;
import com.appzonegroup.app.fasttrack.utility.LocalStorage;
import com.appzonegroup.app.fasttrack.utility.Misc;
import com.appzonegroup.app.fasttrack.utility.TripleDES;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func0;

public class CardBasedWithdrawalActivity2 extends BaseActivity {

    enum InternetAction {
        Verify,
        Transfer
    }
    InternetAction internetAction;// = InternetAction.Verify;
    List<Integer> pageIndices = new ArrayList<>();
    String abduResult = null;
    byte[] command;

    Handler backgroundHandler;
    double amount;
    String cardCipher, CVV, phoneNumber;

    static CardBasedWithdrawalActivity2 thisPage;
    MPosCashOutTransactionType mPosCashOutTransactionType;
    private AudioJackReader mReader;
    private AudioManager mAudioManager;
    private final int mIccWaitTimeout = 10000;
    public static final int RESULT_CODE = 100;
    //private final int mIccControlCode = AudioJackReader.IOCTL_CCID_ESCAPE;
    boolean isUserData = false;//, isPhoneNumberExists;
    //private String mIccPowerAction = "Warm reset";
    int currentVolume = 0;
    String cardPAN = "", cardHolder = "", expiryDate = "";
    CardValidationResponseModel cardValidationResponseModel;
    //boolean shouldResend;

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

    //Dialog progressDialog;
    //static AppStatus appStatus = new AppStatus();

    /**
     * The {@link PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_based_withdrawal2);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        registerReceiver();
        pageIndices.add(0);
        //progressDialog = Dialogs.getProgress(this, null);
        thisPage = this;
    }

    void registerReceiver()
    {
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setMicrophoneMute(false);
        mReader = new AudioJackReader(mAudioManager, true);

        currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        //Register the headset plug receiver.
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(mHeadsetPlugReceiver, filter);
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

    private void swipeViewPagerForward(final int index)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mViewPager.setCurrentItem(index, true);
                pageIndices.add(index);
            }
        });
    }

    private void swipeViewPagerBackward()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (pageIndices.size() > 0) {
                    mViewPager.setCurrentItem(pageIndices.get(pageIndices.size() - 1));
                    pageIndices.remove(pageIndices.size() - 1);
                }else
                {
                    mViewPager.setCurrentItem(0, true);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_card_based_withdrawal_activity2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private byte[] tryGetData(int count, String nextDataLength)
    {
        if (count > 2)
            return null;

        nextDataLength = nextDataLength == null ? "00" : nextDataLength;
        String commandString = Misc.getReadCommand(count) + nextDataLength;
        Log.e("commandString", commandString);

        command = Misc.toByteArray(commandString);
        //Log.e("Command: ", commandString);
        byte[] mResponseApdu = transmit(command);//sendReadRecord(commandString);
        String resultString = Misc.toHexString(mResponseApdu).trim();
        Log.e("ResultString", resultString);

        if (resultString.startsWith("61") || resultString.startsWith("90") || resultString.startsWith("77"))
        {
            nextDataLength = resultString.substring(resultString.length() - 3);
            return tryGetData(count, nextDataLength);
        }
        else if (resultString.startsWith("69") || resultString.startsWith("6D") || resultString.startsWith("6A"))
        {
            return tryGetData(count + 1, null);
        }
        else if (resultString.startsWith("6C"))
        {
            commandString = (commandString.substring(0, commandString.length() - 3) + " " + resultString.substring(resultString.length() - 3)).trim();
            Log.e("CommandString", commandString);
            command = Misc.toByteArray(commandString);
            mResponseApdu = transmit(command);
            Log.e("ResultString", resultString);
        }

        return mResponseApdu;
    }

    /**
     * Method iteratively tries different sends different 'GET PROCESSING OPTIONS' and returns an array with the following information:
     * [0] - commandString
     * [1] - resultString
     * @param index
     * @return
     */
    private String[] sendProcessingCommand(int index)
    {
        if (index > 2)
            return null;

        String commandString = null;
        switch (index)
        {
            case 0: commandString = "80 a8 00 00 02 83 00 00"; break;
            case 1: commandString = "80 a8 00 00 0b 83 09 14 00 00 00 00 00 00 60 c0 00"; break;
            case 2: commandString = "80 a8 00 00 0a 83 08 14 00 00 00 00 00 08 40 00"; break;
        }

        command = Misc.toByteArray(commandString);
        byte[] mResponseApdu = transmit(command);
        String resultString = Misc.toHexString(mResponseApdu);

        if (resultString.startsWith("61") || resultString.startsWith("90") || resultString.startsWith("77"))
        {
            return new String[]{commandString, resultString};
        }

        return sendProcessingCommand(index + 1);
    }

    void readCardAndTransmitData(final int errorCount)
    {
        cardPAN = "";
        expiryDate = "";
        new Thread(new Runnable() {
            @Override
            public void run()
            {

                showProgressBar("Preparing...");
                try {
                    mReader.start();
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

                        if (aid == AID.InterSwitch_Verve_Card)
                        {
                            Log.e("Verve", "I'm here");
                        }

                        if (resultString.startsWith("61") || resultString.startsWith("90") || resultString.startsWith("77"))
                        {
                            /*commandString = "80 a8 00 00 02 83 00 00";
                            Log.e("Command", commandString);
                            //Send GET PROCESSING OPTIONS command
                            if (aid == AID.InterSwitch_Verve_Card)
                            {
                                commandString = "80 a8 00 00 0b 83 09 14 00 00 00 00 00 00 60 c0 00";
                            }

                            command = Misc.toByteArray(commandString);
                            mResponseApdu = transmit(command);
                            resultString = Misc.toHexString(mResponseApdu);

                            Log.e("G Proc command res: ", resultString);*/

                            String[] sendProcessingCommandData = sendProcessingCommand(0);

                            if (sendProcessingCommandData == null)
                            {
                                showError("We are sorry we can't recognize the card. Please try another card.");
                                return;
                            }

                            //if (resultString.startsWith("61") || resultString.startsWith("90") || resultString.startsWith("77"))
                            {
                                isUserData = true;
                                commandString = sendProcessingCommandData[0];
                                resultString = sendProcessingCommandData[1];
                                //commandString = "80 a8 00 00 02 83 00 " + resultString.substring(3);
                                commandString = (commandString.substring(0, commandString.length() - 3).trim() + " " + resultString.substring(3)).trim();

                                /*if (aid == AID.InterSwitch_Verve_Card)
                                    commandString = "80 a8 00 00 0b 83 09 14 00 00 00 00 00 00 60 c0 00";*/

                                command = Misc.toByteArray(commandString.trim());
                                Log.e("Command for data", commandString + ".");

                                //mReader.setOnRawDataAvailableListener(new OnRawDataAvailableListener());

                                mResponseApdu = transmit(command);
                                resultString = Misc.toHexString(mResponseApdu);
                                abduResult = resultString;
                                Log.e("Result for data: ", resultString);
                                String nextDataLength = resultString.substring(3);

                                mResponseApdu = tryGetData(0, null);// nextDataLength);

                                /*//================================
                                //VISA card application
                                commandString = "00 c0 00 00 " + nextDataLength;

                                if (aid == AID.InterSwitch_Verve_Card)
                                {
                                    commandString = "00 b2 04 0c " + nextDataLength;
                                }

                                command = Misc.toByteArray(commandString);
                                //Log.e("Command: ", commandString);
                                mResponseApdu = transmit(command);//sendReadRecord(commandString);
                                resultString = Misc.toHexString(mResponseApdu);
                                *//*Log.e("Result", resultString);*//*

                                if (resultString.startsWith("69") || resultString.startsWith("6D"))
                                {
                                    //TRY MasterCard Read Record Command
                                    *//*commandString = "00 b2 01 14 00";// + nextDataLength;
                                    command = Misc.toByteArray(commandString);
                                    Log.e("Command: ", commandString);*//*
                                    commandString = "00 b2 01 14 00";
                                    mResponseApdu = sendReadRecord(commandString);//transmit(command);
                                    resultString = Misc.toHexString(mResponseApdu);
                                    //Log.e("Master Read: ", resultString);

                                    if (resultString.endsWith("69") || resultString.startsWith("6D") || resultString.startsWith("6A"))
                                    {
                                        //TRY VERVE Read record command
                                        commandString = "00 b2 04 0c 00";// + nextDataLength;
                                        *//*command = Misc.toByteArray(commandString);
                                        Log.e("Command: ", commandString);*//*
                                        mResponseApdu = sendReadRecord(commandString);//transmit(command);
                                        resultString = Misc.toHexString(mResponseApdu);
                                        //Log.e("Verve Read: ", resultString);
                                    }
                                }*/

                                if (mResponseApdu != null)
                                    processCardDataLocation(mResponseApdu);
                                else
                                    showError("We are sorry we could not process this card. Please try another one");
                            }
                            return;
                        }

                    }
                    catch (Exception ex){
                        ex.printStackTrace();
                        return;
                    }

                    if (aid == AID.Etranzact_Genesis_Card_2)
                    {
                        showError("We could not read your card. Please try another card.");
                        return;
                    }
                }
            }

        }).start();

    }

    byte[] sendReadRecord(String commandString)
    {
        command = Misc.toByteArray(commandString);
        //Log.e("Command: ", commandString);
        byte[] mResponseApdu = transmit(command);
        String resultString = Misc.toHexString(mResponseApdu);
        //Log.e("Result", resultString);

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

                    //showPINDialog();
                    ATMCard atmCard = getATMCard(null);
                    String json = new Gson().toJson(atmCard);
                    cardCipher = TripleDES.encrypt(json);
                    backgroundHandler = Misc.setupScheduler();
                    showProgressBar("Sending the data...");
                    internetAction = InternetAction.Verify;
                    runScheduler(cardCipher);
                }
            });
        }
        else
        {
            showError("We are sorry we could not get the card details out");
        }
    }

    ATMCard getATMCard(String enteredPIN)
    {
        ATMCard atmCard = new ATMCard();
        atmCard.setCVV(CVV);
        atmCard.setExpiryDate(expiryDate);
        atmCard.setPIN(enteredPIN == null ? "" : enteredPIN);
        atmCard.setPAN(cardPAN);
        return atmCard;
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
                        Dialogs.getErrorDialog(CardBasedWithdrawalActivity2.this, "An network-related error occurred.").show();
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

                                cardValidationResponseModel = new Gson().fromJson(result, CardValidationResponseModel.class);

                                internetAction = InternetAction.Transfer;
                                mPosCashOutTransactionType = new MPosCashOutTransactionType();
                                mPosCashOutTransactionType.setAgentsPhoneNumber(LocalStorage.GetValueFor(AppConstants.AGENT_PHONE, getBaseContext()));
                                mPosCashOutTransactionType.setAgentsPIN(LocalStorage.GetValueFor(AppConstants.AGENT_PIN, getBaseContext()));
                                mPosCashOutTransactionType.setTransactionAmount(String.valueOf((int)(amount*100)));
                                mPosCashOutTransactionType.setBeneficiaryAccountNumber(null);
                                mPosCashOutTransactionType.setBeneficiaryBank(null);
                                mPosCashOutTransactionType.setCardCipher(cardCipher);
                                mPosCashOutTransactionType.setInstitutionCode(LocalStorage.GetValueFor(AppConstants.INSTITUTION_CODE, getBaseContext()));
                                mPosCashOutTransactionType.setSenderBank(null);
                                mPosCashOutTransactionType.setSenderPhoneNumber(phoneNumber);
                                mPosCashOutTransactionType.setTerminalID(null);
                                //mPosCashOutTransactionType.setIgnoreExistingTransaction(shouldResend);
                                //mPosCashOutTransactionType.setToken(cardValidationResponseModel.getToken());

                                //Card already exists on Zone, make Transaction
                                if (cardValidationResponseModel.isStatus())
                                {
                                    //showProgressBar("Completing transaction...");

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            if (!cardValidationResponseModel.isPhoneNumberExist())
                                            {
                                                swipeViewPagerForward(1);
                                                //mViewPager.setCurrentItem(1, true);

                                            }
                                            else if (!cardValidationResponseModel.isBinExist())
                                            {
                                                swipeViewPagerForward(2);
                                                //mViewPager.setCurrentItem(2, true);
                                            }
                                            else
                                            {
                                                swipeViewPagerForward(4);
                                                //mViewPager.setCurrentItem(3, true);
                                            }


                                        }
                                    });

                                }else
                                {
                                    showError(cardValidationResponseModel.getMessage());
                                }
                                return;

                            }
                            case Transfer:{

                                CardTransactionResponse cardTransactionResponse = new Gson().fromJson(result, CardTransactionResponse.class);
                                if (cardTransactionResponse.isStatus())
                                {
                                    LocalStorage.SaveValue(AppConstants.AGENT_PIN, mPosCashOutTransactionType.getAgentsPIN(), getBaseContext());
                                    //Misc.increaseTransactionMonitorCounter(getBaseContext(), AppConstants.getSuccessCount());
                                    final Dialog dialog = Dialogs.getInformationDialog(CardBasedWithdrawalActivity2.this,
                                            "Transaction successful!\n You can now detach the POS", true);

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
                                    if (cardTransactionResponse.isTransactionExists())
                                    {
                                        showExistingTransactionRetryDialog();
                                    }
                                    else
                                    {
                                        showError(String.format(Locale.getDefault(), "%s%s", cardTransactionResponse.getMessage(),
                                                cardTransactionResponse.getResponseDetails() == null ?
                                                        "" : ": " + cardTransactionResponse.getResponseDetails()));
                                        mPosCashOutTransactionType.setCardCipher(TripleDES.encrypt(new Gson().toJson(getATMCard(null))));
                                        swipeViewPagerBackward();
                                    }

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

    void showExistingTransactionRetryDialog()
    {
        Dialog dialog = Dialogs.getDialog(
                this,
                "The transaction you made a moment ago was successful.\nDo you want to make another one?",
                "Yes",
                "No",
                new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        //shouldResend = true;
                        showProgressBar("Sending the data again");
                        thisPage.mPosCashOutTransactionType.setIgnoreExistingTransaction(true);
                        thisPage.runScheduler(new Gson().toJson(thisPage.mPosCashOutTransactionType));
                    }
                },
                new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {

                    }
                }
        );
        dialog.show();
    }


    /*
    *=================================================================
    This section is for the fragments
    *=================================================================
     */

    public static class AmountInputFragment extends Fragment
    {

        public AmountInputFragment()
        {

        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

            final View rootView = inflater.inflate(R.layout.fragment_card_trx_amount_input, container, false);

            rootView.findViewById(R.id.btn_next).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //if (appStatus.isOnline())
                    {

                        String amountString = ((EditText)rootView.findViewById(R.id.withdrawal_amount_et)).getText().toString();
                        //double amount = 0;
                        if (amountString.length() == 0) {
                            ((BaseActivity)getActivity()).showNotification("Please enter the amount");
                            return;
                        }

                        try
                        {
                            thisPage.amount = Double.parseDouble(amountString);
                        }catch (Exception ex)
                        {
                            thisPage.showNotification("Please enter a valid amount");
                            return;
                        }

                        if (thisPage.amount <= 0)
                        {
                            thisPage.showNotification("You cannot cash out less than =N= 1.00. Please enter a valid amount");
                            return;
                        }

                        /*thisPage.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                            }
                        });*/

                        //startActivityForResult(new Intent(thisPage.getBaseContext(), CardReaderActivity.class), RESULT_CODE);
                        thisPage.readCardAndTransmitData(0);



                    }/*else
                    {
                        showError(getActivity(), "You do not have a working internet connection. Please connect to the internet and try again.");
                    }*/
                }
            });

            return rootView;
        }
    }

    public static class TransactionSummaryFragment extends Fragment
    {

        public TransactionSummaryFragment()
        {

        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.fragment_card_transaction_summary, container, false);

            ((TextView)rootView.findViewById(R.id.transaction_summary_tv))
                    .setText(String.format(Locale.getDefault(),
                            getString(R.string.transaction_summary),
                            Misc.toMoneyFormat(thisPage.amount)));

            rootView.findViewById(R.id.back_clear_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    thisPage.swipeViewPagerBackward();
                }
            });

            ((GridView) rootView.findViewById(R.id.pin_pad_grid_view)).setAdapter(new PinpadGridViewAdapter(thisPage, Misc.getScrambledPINPadText()));

            rootView.findViewById(R.id.submit_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String PIN = ((TextView)rootView.findViewById(R.id.pin_et)).getText().toString().trim();
                    ((TextView)rootView.findViewById(R.id.pin_et)).setText("");

                    if (PIN.length() != 4)
                    {
                        thisPage.showError("Please enter a 4-digit card PIN");
                    }else
                    {
                        thisPage.mPosCashOutTransactionType.setCardCipher(TripleDES.encrypt(new Gson().toJson(thisPage.getATMCard(PIN))));
                        thisPage.mPosCashOutTransactionType.setIgnoreExistingTransaction(false);
                        //thisPage.mPosCashOutTransactionType.setAgentsPIN(PIN);
                        thisPage.mViewPager.setCurrentItem(5, true);
                    }
                }
            });

            ((GridView)rootView.findViewById(R.id.pin_pad_grid_view)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    String enteredPIN = ((TextView)rootView.findViewById(R.id.pin_et)).getText().toString();

                    String numberOnButton = ((TextView)view.findViewById(R.id.pin_tv)).getText().toString();


                    if (numberOnButton.contains("x"))
                    {
                        if (enteredPIN.length() > 0)
                        {
                            enteredPIN = enteredPIN.substring(0, enteredPIN.length() - 2);
                            ((TextView)rootView.findViewById(R.id.pin_et)).setText(enteredPIN);
                        }
                        return;
                    }

                    numberOnButton = enteredPIN.concat(numberOnButton);
                    //If its a digit
                    if (!numberOnButton.contains("-"))
                    {
                        if (enteredPIN.length() >= 4)
                        {
                            thisPage.showError("Your PIN cannot be more than four digits");
                            return;
                        }
                        //buttonText = enteredPIN.concat(buttonText);
                        ((TextView)rootView.findViewById(R.id.pin_et)).setText(numberOnButton);
                    }
                    else if (numberOnButton.contains("x"))
                    {
                        ((TextView)rootView.findViewById(R.id.pin_et)).setText("");
                    }
                }
            });

            return rootView;
        }
    }

    public static class PhoneNumberFragment extends Fragment
    {

        public PhoneNumberFragment()
        {

        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.fragment_phone_number, container, false);

            rootView.findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    thisPage.swipeViewPagerBackward();
                }
            });

            rootView.findViewById(R.id.continue_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    thisPage.phoneNumber = ((TextView)rootView.findViewById(R.id.phone_no_et)).getText().toString().trim();


                    if (thisPage.phoneNumber.length() < 11) {
                        thisPage.showError("The inputted phone number is incorrect.");
                        return;
                    }

                    thisPage.mPosCashOutTransactionType.setSenderPhoneNumber(thisPage.phoneNumber);

                    if (!thisPage.cardValidationResponseModel.isBinExist())
                    {
                        thisPage.mViewPager.setCurrentItem(2, true);
                    }else
                    {
                        thisPage.mViewPager.setCurrentItem(3, true);
                    }

                }
            });



            return rootView;
        }
    }

    public static class BankSelectionFragment extends Fragment
    {

        public BankSelectionFragment()
        {

        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

            final View rootView = inflater.inflate(R.layout.fragment_bank_selection, container, false);

            rootView.findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    thisPage.swipeViewPagerBackward();
                }
            });

            final ArrayList<ZoneBank> zoneBanks = ZoneBank.getZoneBankList();
            ArrayList<String> bankNames = new ArrayList<>();
            bankNames.add("Select bank...");

            for (ZoneBank zoneBank : zoneBanks) {
                bankNames.add(zoneBank.getName());
            }
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(thisPage, android.R.layout.simple_spinner_item, bankNames);
            spinnerArrayAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
            ((Spinner)rootView.findViewById(R.id.bank_spinner)).setAdapter(spinnerArrayAdapter);
            /*rootView.findViewById(R.id.continue_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    int selectedPosition = ((Spinner)rootView.findViewById(R.id.bank_spinner)).getSelectedItemPosition();
                    if (selectedPosition == 0)
                    {
                        thisPage.showError("Please select a bank");
                        return;
                    }

                    thisPage.mPosCashOutTransactionType.setSenderBank(banks.get(selectedPosition - 1).getBank_Code());

                    thisPage.showProgressBar("Saving your bank...");
                    thisPage.runScheduler(new Gson().toJson(thisPage.mPosCashOutTransactionType));
                }
            });*/

            rootView.findViewById(R.id.continue_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    int selectedIndex = ((Spinner)rootView.findViewById(R.id.bank_spinner)).getSelectedItemPosition();
                    if (selectedIndex == 0)
                    {
                        thisPage.showError("Please select a bank.");
                        return;
                    }

                    thisPage.mPosCashOutTransactionType.setSenderBank(zoneBanks.get(selectedIndex - 1).getBank_Code());

                    //thisPage.runScheduler(TripleDES.encrypt(new Gson().toJson(thisPage.getATMCard(null))));
                    thisPage.mViewPager.setCurrentItem(3, true);

                }
            });

            return rootView;
        }
    }

    public static class CVVInputFragment extends Fragment
    {
        public CVVInputFragment()
        {

        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.fragment_cvv_input, container, false);

            rootView.findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    thisPage.swipeViewPagerBackward();
                }
            });

            rootView.findViewById(R.id.continue_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    thisPage.CVV = ((TextView)rootView.findViewById(R.id.cvv_et)).getText().toString().trim();

                    if (thisPage.CVV.length() < 3)
                    {
                        thisPage.showError("The inputted CVV is incorrect. Please enter the correct CVV");
                        return;
                    }

                    thisPage.mViewPager.setCurrentItem(4, true);
                }
            });

            return rootView;
        }
    }

    public static class AgentPINFragment extends Fragment
    {
        public AgentPINFragment()
        {

        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

            final View rootView = inflater.inflate(R.layout.fragment_agent_pin_input, container, false);

            rootView.findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    thisPage.swipeViewPagerBackward();
                }
            });

            rootView.findViewById(R.id.submit_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String pin = ((EditText)rootView.findViewById(R.id.agent_pin_et)).getText().toString();

                    if (pin.length() < 4)
                    {
                        thisPage.showError("Please enter your PIN");
                        return;
                    }
                    thisPage.showProgressBar("Completing the transaction...");
                    thisPage.mPosCashOutTransactionType.setAgentsPIN(pin);
                    thisPage.runScheduler(new Gson().toJson(thisPage.mPosCashOutTransactionType));
                }
            });

            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            switch (position)
            {
                case 0:default:
                {
                    return new AmountInputFragment();
                }
                case 1:
                {
                    return new PhoneNumberFragment();
                }
                case 2:
                {
                    return new BankSelectionFragment();
                }
                case 3:
                {
                    return new CVVInputFragment();
                }
                case 4:
                {
                    return new TransactionSummaryFragment();
                }
                case 5:
                {
                    return new AgentPINFragment();
                }

            }

            //return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return 6;
        }
    }
}

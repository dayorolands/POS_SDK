package com.appzonegroup.app.fasttrack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;

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
import com.appzonegroup.app.fasttrack.utility.CardMisc;
import com.appzonegroup.app.fasttrack.utility.Misc;

import java.util.Arrays;
import java.util.Locale;

public class CardReaderActivity extends BaseActivity {

    String abduResult = null;
    byte[] command;


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_reader);

        readCardAndTransmitData(0);
    }

    void registerReceiver()
    {
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mReader = new AudioJackReader(mAudioManager, true);

        currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        /* Register the headset plug receiver. */
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(mHeadsetPlugReceiver, filter);
    }

    void readCardAndTransmitData(final int errorCount)
    {
        //registerReceiver();
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
            Intent intent = new Intent();
            intent.putExtra("PAN", cardPAN);
            intent.putExtra("EXP", expiryDate);

            setResult(CardBasedWithdrawalActivity2.RESULT_CODE, intent);
            finish();
            /*runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    //showPINDialog();
                    ATMCard atmCard = getATMCard(null);
                    String json = new Gson().toJson(atmCard);
                    cardCipher = TripleDES.encrypt(json);
                    backgroundHandler = Misc.setupScheduler();
                    showProgressBar("Sending the data...");
                    internetAction = CardBasedWithdrawalActivity2.InternetAction.Verify;
                    runScheduler(cardCipher);
                }
            });*/

        }
    }


}

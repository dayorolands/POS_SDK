package com.appzonegroup.app.fasttrack.utility;

import androidx.annotation.NonNull;

import com.appzonegroup.app.fasttrack.model.AID;

import java.math.BigInteger;
import java.util.Locale;

/**
 * Created by Joseph on 12/6/2017.
 */

public class CardMisc {


    /**
     * Gets the user data out of the card
     * @param cardData Data extracted out of the card
     * @param firstByte The first of the 2 consecutive bytes to be used for check
     * @param secondByte The second of the 2 consecutive bytes to be used for check
     * @param isNumericData This determines whether the data is the Expiry Date, Primary Account Number (PAN) when TRUE
     *                      or the name on the card when FALSE
     * @return
     */
    @NonNull
    public static String getUserDataFromCard(byte[] cardData, byte firstByte, byte secondByte, boolean isNumericData)
    {
        StringBuilder returnData = new StringBuilder();
        for (int i = 0; i < cardData.length - 1; i++)
        {
            if (cardData[i] == firstByte && cardData[i + 1] == secondByte && secondByte != Integer.valueOf(0).byteValue())
            {
                int dataLength = cardData[i + 2];

                byte[] data = new byte[dataLength];

                for (int j = 0; j < dataLength; j++)
                {
                    if (!isNumericData)
                        data[j] = cardData[i + 3 + j];
                    else
                        returnData.append(toHexString(new byte[]{cardData[i + 3 + j]}));
                }

                if (!isNumericData)
                    returnData.append(toHexString(data));
            }
            else if (cardData[i] == firstByte && secondByte == Integer.valueOf(0).byteValue())
            {
                int dataLength = cardData[i + 1];
                for (int j = 0; j < dataLength; j++)
                {
                    returnData.append(toHexString(new byte[]{cardData[i + 2 + j]}));
                }
            }
        }
        return returnData.toString();
    }

    /**
     * The data is in the format:
     *
     * 70 4E [5F 24] 03 18 12 31 5A 08 49 60 09
     * where
     *      [03] is the data length
     *      [18 12 31] is the card expiry date in reverse order
     *      The date is usually in reverse order
     * @param data
     * @return
     */
    public static String getCardExpiryDate(String data)
    {
        int dataIndex = data.indexOf("5F 24");
        String[] splittedDate = data.substring(dataIndex + 9, dataIndex + 17).split(" ");
        return String.format(Locale.getDefault(), "%s/%s", splittedDate[1].trim(), splittedDate[0].trim());
    }

    /*public static String getUserDataFromCard(String[] cardData, String firstByte, String secondByte, boolean isNumericData)
    {
        StringBuilder returnData = new StringBuilder();
        for (int i = 0; i < cardData.length - 1; i++)
        {
            if (cardData[i] == firstByte && cardData[i + 1] == secondByte && secondByte != "00")
            {
                int dataLength = Integer.parseInt(cardData[i + 2], 16);

                byte[] data = new byte[dataLength];

                for (int j = 0; j < dataLength; j++)
                {
                    if (!isNumericData)
                        data[j] = cardData[i + 3 + j];
                    else
                        returnData.append(toHexString(new byte[]{cardData[i + 3 + j]}));
                }

                if (!isNumericData)
                    returnData.append(toHexString(data));
            }
            else if (cardData[i] == firstByte && secondByte == "00")
            {
                int dataLength = Integer.parseInt(cardData[i + 1], 16);
                for (int j = 0; j < dataLength; j++)
                {
                    returnData.append(toHexString(new byte[]{cardData[i + 2 + j]}));
                }
            }
        }
        return returnData.toString();
    }*/

    public static String getCardHolderName(String cardData)
    {
        cardData = cardData.toUpperCase().substring(cardData.indexOf("5F 20") + 5).trim();
        int length = Integer.parseInt(cardData.substring(0, cardData.indexOf(" ")), 16);
        cardData = convertHexToString(cardData.replace(" ", ""));
        return length < cardData.length() ?
                cardData.substring(0, length) //make space for the first character for length
                :
                cardData;
    }

    public static String getCardPAN(String data)
    {

        String[] arrayedHex = data.split(" ");

        boolean indexFound = false;

        for (int i = 0; i < arrayedHex.length; i++)// (String hex : arrayedHex)
        {
            String hex = arrayedHex[i];

            if (hex.contentEquals("5A"))
            {
                indexFound = true;
                continue;
            }

            if (indexFound)
            {
                int length = Integer.parseInt(hex.toString(), 16);
                int startIndex = i + 1; //Current index + 1
                int count = 0;
                StringBuilder returnData = new StringBuilder();
                while (count < length)
                {
                    returnData.append(arrayedHex[startIndex]);//Integer.parseInt(arrayedHex[startIndex], 16));
                    count++;
                    startIndex++;
                }
                return returnData.toString().trim();
            }
        }

        return null;
    }

    @NonNull
    private static String convertHexToString(String hex){

        StringBuilder sb = new StringBuilder();

        //49204c6f7665204a617661 split into two characters 49, 20, 4c...
        for( int i = 0; i < hex.length()-1; i+=2 ){

            //grab the hex in pairs
            String output = hex.substring(i, (i + 2));
            //convert hex to decimal
            int decimal = Integer.parseInt(output, 16);
            //convert the decimal to character
            sb.append((char)decimal);
        }

        return sb.toString().trim();
    }

    public static String getAID(AID aid)
    {
        switch (aid)
        {
            case VISA:
                return "00 a4 04 00 07 a0 00 00 00 03 20 20 00";
            case VISA_Debit_Credit_Classic:
                return "00 a4 04 00 07 a0 00 00 00 03 10 10 00";
            case VISA_Debit:
                return "00 a4 04 00 08 a0 00 00 00 03 10 10 02 00";
            case VISA_Credit:
                return "00 a4 04 00 08 a0 00 00 00 03 10 10 01 00";
            case VISA_Electron:
                return "00 a4 04 00 07 a0 00 00 00 03 20 10 00";
            case Etranzact_Genesis_Card:
                return "00 a4 04 00 07 a0 00 00 04 54 00 10 00";
            case Etranzact_Genesis_Card_2:
                return "00 a4 04 00 07 a0 00 00 04 54 00 11 00";
            case MasterCard_US:
                return "00 a4 04 00 07 a0 00 00 00 04 22 03 00";
            case MasterCard_Credit:
                return "00 a4 04 00 07 a0 00 00 00 04 10 10 00";
            case MasterCard_Specific_1:
                return "00 a4 04 00 07 a0 00 00 00 04 20 10 00";
            case MasterCard_Specific_2:
                return "00 a4 04 00 07 a0 00 00 00 04 30 10 00";
            case MasterCard_Specific_3:
                return "00 a4 04 00 07 a0 00 00 00 04 50 10 00";
            case InterSwitch_Verve_Card:
                return "00 a4 04 00 07 a0 00 00 03 71 00 01 00";


        }

        return null;
    }

    public static String getReadCommand(int index)
    {
        switch (index)
        {
            case 1: return "00 c0 00 00 ";//VISA
            case 0: return "00 b2 04 0c ";//VERVE
            case 2: default: return "00 b2 01 14 ";//MasterCard
        }
    }

    /**
     * Converts the HEX string to byte array.
     *
     * @param hexString
     *            the HEX string.
     * @return the byte array.
     */
    public static byte[] toByteArray(String hexString) {

        byte[] byteArray = null;
        int count = 0;
        char c = 0;
        int i = 0;

        boolean first = true;
        int length = 0;
        int value = 0;

        // Count number of hex characters
        for (i = 0; i < hexString.length(); i++) {

            c = hexString.charAt(i);
            if (c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a'
                    && c <= 'f') {
                count++;
            }
        }

        byteArray = new byte[(count + 1) / 2];
        for (i = 0; i < hexString.length(); i++) {

            c = hexString.charAt(i);
            if (c >= '0' && c <= '9') {
                value = c - '0';
            } else if (c >= 'A' && c <= 'F') {
                value = c - 'A' + 10;
            } else if (c >= 'a' && c <= 'f') {
                value = c - 'a' + 10;
            } else {
                value = -1;
            }

            if (value >= 0) {

                if (first) {

                    byteArray[length] = (byte) (value << 4);

                } else {

                    byteArray[length] |= value;
                    length++;
                }

                first = !first;
            }
        }

        return byteArray;
    }

    /**
     * Converts the byte array to HEX string.
     *
     * @param buffer
     *            the buffer.
     * @return the HEX string.
     */
    public static String toHexString(byte[] buffer) {

        String bufferString = "";

        if (buffer != null) {

            for (int i = 0; i < buffer.length; i++) {

                String hexChar = Integer.toHexString(buffer[i] & 0xFF);
                if (hexChar.length() == 1) {
                    hexChar = "0" + hexChar;
                }

                bufferString += hexChar.toUpperCase(Locale.US) + " ";
            }
        }

        return bufferString;
    }

    /**
     * Application File Locator (AFL) Info
     *
     * @param byteHex the byteHex should be 8-hexadecimal characters long
     *                Position 1 - SFI file
     *                Position 2 - First record number
     *                Position 3 - Last record number
     *                Position 4 - Data length
     * @return
     */
    public static int[] getAFLOctetData(String byteHex)
    {
        int[] data = new int[4];
        byteHex = byteHex.replace(" ", "");
        //String mostSignificant5Bits = hexToBin(byteHex).substring(0, 5);
        String temp = byteHex.substring(0, 2);
        data[0] = binaryToDecimal(hexToBin(temp));// + 4;

        temp = byteHex.substring(2, 4);
        data[1] = hexToDecimal(temp);

        temp = byteHex.substring(4, 6);
        data[2] = hexToDecimal(temp);

        temp = byteHex.substring(6, 8);
        data[3] = hexToDecimal(temp);

        return data;
    }

    static int hexToDecimal(String hexaString){
        return Integer.parseInt(hexaString.trim(), 16 );
    }

    static String hexToBin(String s) {
        s = new BigInteger(s, 16).toString(2);

        //Remove all trailing zeros
        while (s.toCharArray()[s.length() - 1] == '0')
        {
            s = s.substring(0, s.length() - 1);
        }

        //Add leading zeros till length is 5
        while (s.length() < 5)
        {
            s = "0" + s;
        }

        return s;
    }

    public static int binaryToDecimal(String binaryString){
        return Integer.parseInt(String.valueOf(new BigInteger(binaryString, 2)));
    }


}

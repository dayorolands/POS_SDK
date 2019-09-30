package com.appzonegroup.app.fasttrack.dataaccess;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.appzonegroup.app.fasttrack.model.DeviceTransactionInformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Joseph on 12/18/2017.
 */

public class DeviceTransactionInformationDAO extends AbstractDAO<DeviceTransactionInformation> implements ICore {

    String[] columns = {"ID", "DateReceived", "SessionID", "InstitutionCode", "AgentPhoneNumber",
            "RequestCount" , "SuccessCount", "NoInternet", "NoResponse", "ErrorResponse",
            "RamSize", "PercentageLeftOver", "MemorySpace", "MemorySpaceLeft"
    };

    Context context;

    public DeviceTransactionInformationDAO(Context context)
    {
        super.dbHelper = new DatabaseHelper(context, this);
        this.Open();
        db.execSQL(this.GetSqlCreate());
        this.context = context;
    }

    @Override
    public String GetTableName() {
        return "DeviceTransactionInformationTable";
    }

    @Override
    public String GetSqlCreate() {
        return "CREATE TABLE  IF NOT EXISTS "
                + GetTableName()
                + "("
                + "ID integer primary key autoincrement, "
                + "DateReceived text , "
                + "SessionID text , "
                + "InstitutionCode text , "
                + "AgentPhoneNumber text , "

                + "RequestCount integer , "
                + "SuccessCount integer , "
                + "NoInternet integer , "
                + "NoResponse integer , "
                + "ErrorResponse integer , "

                + "RamSize text , "
                + "PercentageLeftOver real , "
                + "MemorySpace text , "
                + "MemorySpaceLeft text "

                + ");";
    }

    @Override
    public DeviceTransactionInformation Get(long ID) {
        return null;
    }


    @Override
    public List<DeviceTransactionInformation> GetAll() {
        List<DeviceTransactionInformation> transactionInformations = new ArrayList<>();
        Cursor cursor = db.query(GetTableName(), columns, null, null, null, null, null);
        cursor.moveToFirst();

        if(cursor.getCount() > 0)
        {
            while(!cursor.isAfterLast())
            {
                transactionInformations.add(CursorToItem(cursor));
                cursor.moveToNext();
            }
        }
        cursor.close();
        return transactionInformations;
    }

    public List<DeviceTransactionInformation> GetMostRecentSessionTransactionInfo()
    {
        List<DeviceTransactionInformation> transactionInformations = new ArrayList<>();
        String query = String.format(Locale.getDefault(),
                "Select * from %s where SessionID in (Select SessionID from %s Order by ID ASC Limit 1)", GetTableName(), GetTableName());
        Cursor cursor = db.rawQuery(query, null);
                //LATEST----(String.format(Locale.getDefault(),"Select * from %s Order by ID ASC Limit 10", GetTableName()), null);
                //db.query(GetTableName(), columns, null, null, null, null, null);
        cursor.moveToFirst();

        if(cursor.getCount() > 0)
        {
            while(!cursor.isAfterLast())
            {
                transactionInformations.add(CursorToItem(cursor));
                cursor.moveToNext();
            }
        }
        cursor.close();
        return transactionInformations;
    }

    public boolean DeleteSentRecords(int startID, int endID)
    {
        try
        {
            db.execSQL(String.format(Locale.getDefault(), "Delete from %s where ID >= %d AND ID <= %d", GetTableName(), startID, endID));
            return true;
        }catch (Exception ex)
        {
            return false;
        }
    }

    public DeviceTransactionInformation GetBySessionId(String sessionId){
        DeviceTransactionInformation transactionInformations = null;
        Cursor cursor = db.query(GetTableName(), columns,  "SessionID='" + sessionId + "'", null, null, null, null);
        cursor.moveToFirst();

        if(cursor.getCount() > 0)
        {
            while(!cursor.isAfterLast())
            {
                transactionInformations = CursorToItem(cursor);
                cursor.moveToNext();
            }
        }
        cursor.close();
        return transactionInformations;
    }

    @Override
    public long Insert(DeviceTransactionInformation information) {
        long id = -1;

        if(information == null)
            return id;
        if(information.getSessionID() == null){
            return id;
        }

        ContentValues values = new ContentValues();
        values.put("DateReceived", information.getDateReceived());
        values.put("SessionID", information.getSessionID());
        values.put("InstitutionCode", information.getInstitutionCode());
        values.put("AgentPhoneNumber", information.getAgentPhoneNumber());

        values.put("RequestCount", information.getRequestCount());
        values.put("SuccessCount", information.getSuccessCount());
        values.put("NoInternet", information.getNoInternet());
        values.put("NoResponse", information.getNoResponse());
        values.put("ErrorResponse", information.getErrorResponse());

        values.put("RamSize", information.getRamSize());
        values.put("PercentageLeftOver", information.getPercentageLeftOver());
        values.put("MemorySpace", information.getMemorySpace());
        values.put("MemorySpaceLeft", information.getMemorySpaceLeft());

        id = super.db.insert(GetTableName(), null, values);
        return id;
    }

    public long Update(DeviceTransactionInformation information, String sessionId){
        long id = -1;
        ContentValues values = new ContentValues();
        values.put("DateReceived", information.getDateReceived());
        values.put("SessionID", information.getSessionID());
        values.put("InstitutionCode", information.getInstitutionCode());
        values.put("AgentPhoneNumber", information.getAgentPhoneNumber());

        values.put("RequestCount", information.getRequestCount());
        // here I added error responses to success responses
        values.put("SuccessCount", information.getSuccessCount() + information.getErrorResponse());
        values.put("NoInternet", information.getNoInternet());
        values.put("NoResponse", information.getNoResponse());

        //here I changed error responses to success responses.
        values.put("ErrorResponse", 0);

        values.put("RamSize", information.getRamSize());
        values.put("PercentageLeftOver", information.getPercentageLeftOver());
        values.put("MemorySpace", information.getMemorySpace());
        values.put("MemorySpaceLeft", information.getMemorySpaceLeft());

        id = super.db.update(GetTableName(), values, "SessionID='"+sessionId+"'", null);
        return id;
    }

    public long InsertOrUpdate(DeviceTransactionInformation information){
        if(GetBySessionId(information.getSessionID())!= null){
          return Update(information,information.getSessionID());
        }
        else {
            return Insert(information);
        }
    }

    public static long Insert(Context context, String sessionID)
    {
        return new DeviceTransactionInformationDAO(context).InsertOrUpdate(DeviceTransactionInformation.getInstance(context, sessionID));
    }

    @Override
    protected DeviceTransactionInformation CursorToItem(Cursor cursor) {
        DeviceTransactionInformation transactionInformation = new DeviceTransactionInformation();
        transactionInformation.setID(cursor.getInt(cursor.getColumnIndex("ID")));
        transactionInformation.setDateReceived(cursor.getString(cursor.getColumnIndex("DateReceived")));
        transactionInformation.setSessionID(cursor.getString(cursor.getColumnIndex("SessionID")));
        transactionInformation.setInstitutionCode(cursor.getString(cursor.getColumnIndex("InstitutionCode")));
        transactionInformation.setAgentPhoneNumber(cursor.getString(cursor.getColumnIndex("AgentPhoneNumber")));

        transactionInformation.setRequestCount(cursor.getInt(cursor.getColumnIndex("RequestCount")));
        transactionInformation.setSuccessCount(cursor.getInt(cursor.getColumnIndex("SuccessCount")));
        transactionInformation.setNoInternet(cursor.getInt(cursor.getColumnIndex("NoInternet")));
        transactionInformation.setNoResponse(cursor.getInt(cursor.getColumnIndex("NoResponse")));
        transactionInformation.setErrorResponse(cursor.getInt(cursor.getColumnIndex("ErrorResponse")));

        transactionInformation.setRamSize(cursor.getString(cursor.getColumnIndex("RamSize")));
        transactionInformation.setPercentageLeftOver(cursor.getFloat(cursor.getColumnIndex("PercentageLeftOver")));
        transactionInformation.setMemorySpace(cursor.getString(cursor.getColumnIndex("MemorySpace")));
        transactionInformation.setMemorySpaceLeft(cursor.getString(cursor.getColumnIndex("MemorySpaceLeft")));

        transactionInformation.setAppName("CreditClub");

        return transactionInformation;
    }
}

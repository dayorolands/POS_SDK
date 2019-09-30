package com.appzonegroup.app.fasttrack.dataaccess;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.appzonegroup.app.fasttrack.model.Account;
import com.appzonegroup.app.fasttrack.model.Beneficiary;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Oto-obong on 20/08/2017.
 */

public class BeneficiaryDAO extends AbstractDAO<Beneficiary> implements ICore {

    String[] columns = {"ID", "AccountNumber", "Address", "AgentPhoneNumber", "DateOfBirth", "EligibleAmount", "FirstName",
            "Gender", "LastName", "MiddleName", "Paid",
            "PhoneNumber", "TrackingReference", "Photo", "Sync" };

    public BeneficiaryDAO (Context context)
    {
        super.dbHelper = new DatabaseHelper(context, this);
        this.Open();
        db.execSQL(this.GetSqlCreate());
    }

    @Override
    public Beneficiary Get(long ID) {

        Beneficiary beneficiary = null;

        Cursor cursor = db.query(GetTableName(), columns , "ID=" + ID, null, null, null, null);
        cursor.moveToFirst();

        if (cursor.getCount() > 0)
        {
            while (!cursor.isAfterLast())
            {
                beneficiary = CursorToItem(cursor);
                cursor.moveToNext();
            }
        }

        return beneficiary;
    }

    public Beneficiary GetByPaid(Boolean isPaid)
    {

        String value = String.valueOf(isPaid);

        Beneficiary beneficiary = null;

        Cursor cursor = db.query(GetTableName(), columns , "Paid='" + value + "'", null, null, null, null);
        cursor.moveToFirst();

        if (cursor.getCount() > 0)
        {
            while (!cursor.isAfterLast())
            {
                beneficiary = CursorToItem(cursor);
                cursor.moveToNext();
            }
        }

        return beneficiary;
    }


    public Beneficiary getTrackingReference(String trackingReference)
    {


        Beneficiary beneficiary = null;
        try {
            Cursor cursor = db.query(GetTableName(), columns, "TrackingReference='" + trackingReference + "'", null, null, null, null);
            cursor.moveToFirst();

            if (cursor.getCount() > 0) {
                while (!cursor.isAfterLast()) {
                    beneficiary = CursorToItem(cursor);
                    cursor.moveToNext();
                }
            }
        }catch (Exception ex)
        {
            Log.e("Beneficiary Error", ex.getMessage());
        }

        return beneficiary;
    }



    public long UpdateBeneficiary(Beneficiary beneficiary){


        long suc =- 1;
        if (beneficiary == null)
            return suc;

        ContentValues values = ContentValues(beneficiary, true);

        suc = super.db.update(this.GetTableName(), values, "TrackingReference = " + beneficiary.getTrackingReference(), null);
        return suc;
    }

    private ContentValues ContentValues(Beneficiary beneficiary, boolean hasID){

        ContentValues values = new ContentValues();


        values.put("ID", beneficiary.getID());
        values.put("AccountNumber", beneficiary.getAccountNumber());
        values.put("Address", beneficiary.getAddress());
        values.put("AgentPhoneNumber", beneficiary.getAgentPhoneNumber());
        values.put("DateOfBirth", beneficiary.getDateOfBirth());
        values.put("EligibleAmount", beneficiary.getEligibleAmount());
        values.put("FirstName", beneficiary.getFirstName());
        values.put("Gender", beneficiary.getGender());
        values.put("LastName", beneficiary.getLastName());
        values.put("MiddleName", beneficiary.getMiddleName());
        values.put("Paid",String.valueOf(beneficiary.getPaid()));
        values.put("PhoneNumber", beneficiary.getPhoneNumber());
        values.put("TrackingReference", beneficiary.getTrackingReference());
        values.put("Photo", beneficiary.getPhoto());
        values.put("Sync", beneficiary.getSync() + "");
        //values.put("isRegistered", account.isRegistered() + "");
        //values.put("Reference", account.getReference());
        //values.put("ProductCode", account.getProductCode());
        //values.put("ImageUploaded", account.getImageLoaded());
        //values.put("GeoLocation", account.getGeoLocation());

        return values;
    }

    public List<Beneficiary> GetPaid(String value)
    {
        List<Beneficiary> beneficiaries = new ArrayList<>();
        try {

            Cursor cursor = db.query(GetTableName(), columns,"Paid='" + value + "'", null, null, null, null);
            cursor.moveToFirst();

               if (cursor.getCount() > 0) {
                   while (!cursor.isAfterLast()) {
                       Beneficiary beneficiary = CursorToItem(cursor);
                       beneficiaries.add(beneficiary);
                       cursor.moveToNext();
                   }
               }

        }catch (Exception ex){

            Log.e("Beneficiary Get all ", ex.getMessage());

        }

        return beneficiaries;
    }



    public List<Beneficiary> GetSync(String value)
    {
        List<Beneficiary> beneficiaries = new ArrayList<>();
        try {

            Cursor cursor = db.query(GetTableName(), columns,"Sync='" + value + "'", null, null, null, null);
            cursor.moveToFirst();

            if (cursor.getCount() > 0) {
                while (!cursor.isAfterLast()) {
                    Beneficiary beneficiary = CursorToItem(cursor);
                    beneficiaries.add(beneficiary);
                    cursor.moveToNext();
                }
            }

        }catch (Exception ex){

            Log.e("Beneficiary Get all ", ex.getMessage());

        }

        return beneficiaries;
    }



    public List<Beneficiary> GetByField(String... fieldVales)
    {
        List<Beneficiary> beneficiaries = new ArrayList<>();

        String query = "";
        for (int i = 0; i < fieldVales.length; i++){

            if ((i%2) == 0){
                query += fieldVales[i];
            }

            if ((i%2) == 1){
                if (i != fieldVales.length - 1){
                    query += " = '" + fieldVales[i] + "' AND ";
                }
                else{
                    query += " = '" + fieldVales[i] + "'";
                }
            }

        }

        Cursor cursor = db.query(GetTableName(), columns , query, null, null, null, null);
        cursor.moveToFirst();

        if (cursor.getCount() > 0)
        {
            while (!cursor.isAfterLast())
            {
                Beneficiary beneficiary = CursorToItem(cursor);
                beneficiaries.add(beneficiary);
                cursor.moveToNext();
            }
        }

        return beneficiaries;
    }

    public List<Beneficiary> Get(long ID, int limit, String... queryParams){

        String query = "";

        for (String param : queryParams){
            query += " AND " + param;
        }

        List<Beneficiary> beneficiaries = new ArrayList<>();

        Cursor cursor = db.query(GetTableName(), columns, "ID > " + ID + query, null, null, null, "ID DESC", limit + "");
        cursor.moveToFirst();

        if (cursor.getCount() > 0)
        {
            while (!cursor.isAfterLast())
            {
                Beneficiary beneficiary = CursorToItem(cursor);
                beneficiaries.add(beneficiary);
                cursor.moveToNext();
            }
        }

        return beneficiaries;

    }



    public boolean isEmpty(){

        boolean hasTables = true;
        Cursor cursor = db.rawQuery("SELECT * FROM " +GetTableName(), null);

        if(cursor != null && cursor.getCount() > 0){
            hasTables=false;
            cursor.close();
        }

        return hasTables;
    }




    @Override
    public List<Beneficiary> GetAll() {

        List<Beneficiary> beneficiaries = new ArrayList<>();
        Cursor cursor = db.query(GetTableName(), columns, null, null, null, null, null);
        cursor.moveToFirst();

        if(cursor.getCount() > 0)
        {
            while(!cursor.isAfterLast())
            {
                beneficiaries.add(CursorToItem(cursor));
                cursor.moveToNext();
            }
        }
        cursor.close();
        return beneficiaries;
    }

    @Override
    public long Insert(Beneficiary beneficiary) {

        long id = -1;

        if(beneficiary == null)
            return id;

        ContentValues values = ContentValues(beneficiary, false);
        /*values.put("Surname", customerRequest.getLastName());
        values.put("FirstName", customerRequest.getFirstName());
        values.put("Gender", customerRequest.getGender());
        values.put("PhoneNumber", customerRequest.getPhoneNo());
        values.put("DOB", customerRequest.getDateOfBirth());
        values.put("Photo", customerRequest.getPhoto());
        values.put("NextOfKinName", customerRequest.getNextofkinName());
        values.put("NextOfKinPhone", customerRequest.getNextofkinPhoneNo());
        values.put("Address", customerRequest.getAddress());
        values.put("PlaceOfBirth", customerRequest.getPlaceOfBirth());
        values.put("CardSerialNumber", customerRequest.getCardSerialNumber());
        values.put("ValidationRemark", customerRequest.getValidationRemark());
        values.put("DateSync", customerRequest.getDateSync());
        values.put("isProcessed", customerRequest.isProcessed() + "");
        values.put("isRegistered", customerRequest.isRegistered() + "");
        values.put("Reference", customerRequest.getReference());
        values.put("ProductCode", customerRequest.getProductCode());
        values.put("ImageUploaded", customerRequest.getImageLoaded());*/


        id = super.db.insert(GetTableName(), null, values);


        return id;
    }

    @Override
    protected Beneficiary CursorToItem(Cursor cursor) {

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setID(cursor.getInt(cursor.getColumnIndex("ID")));
        beneficiary.setAccountNumber(cursor.getString(cursor.getColumnIndex("AccountNumber")));
        beneficiary.setAddress(cursor.getString(cursor.getColumnIndex("Address")));
        beneficiary.setAgentPhoneNumber(cursor.getString(cursor.getColumnIndex("AgentPhoneNumber")));
        beneficiary.setDateOfBirth(cursor.getString(cursor.getColumnIndex("DateOfBirth")));
        beneficiary.setEligibleAmount(cursor.getDouble(cursor.getColumnIndex("EligibleAmount")));
        beneficiary.setFirstName(cursor.getString(cursor.getColumnIndex("FirstName")));
        beneficiary.setGender(cursor.getInt(cursor.getColumnIndex("Gender")));
        beneficiary.setLastName(cursor.getString(cursor.getColumnIndex("LastName")));
        beneficiary.setMiddleName(cursor.getString(cursor.getColumnIndex("MiddleName")));
        beneficiary.setPaid(Boolean.valueOf(cursor.getString(cursor.getColumnIndex("Paid"))));
        beneficiary.setPhoneNumber(cursor.getString(cursor.getColumnIndex("PhoneNumber")));
        beneficiary.setTrackingReference(cursor.getString(cursor.getColumnIndex("TrackingReference")));
        beneficiary.setPhoto(cursor.getString(cursor.getColumnIndex("Photo")));
        beneficiary.setSync(cursor.getString(cursor.getColumnIndex("Sync")));
        // account.setRegistered(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex("isRegistered"))));
        //account.setReference(cursor.getString(cursor.getColumnIndex("Reference")));

        //account.setImageLoaded(cursor.getString(cursor.getColumnIndex("ImageUploaded")));
        //account.setGeoLocation(cursor.getString(cursor.getColumnIndex("GeoLocation")));


        return beneficiary;
    }

    @Override
    public String GetTableName() {
        return "Beneficiaries_Table";
    }

    @Override
    public String GetSqlCreate() {
        return "CREATE TABLE  IF NOT EXISTS "
                + GetTableName()
                + "("
                + "ID integer , "
                + "AccountNumber text , "
                + "Address text , "
                + "AgentPhoneNumber text , "
                + "DateOfBirth text , "
                + "EligibleAmount text , "
                + "FirstName text , "
                + "Gender integer , "
                + "LastName text , "
                + "MiddleName text , "
                + "Paid text , "
                + "PhoneNumber text , "
                + "TrackingReference text primary key, "
                + "Photo text, "
                + "Sync text"
                + ");";
    }
}


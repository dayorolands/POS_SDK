package com.appzonegroup.app.fasttrack.dataaccess;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.appzonegroup.app.fasttrack.model.Account;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joseph on 5/26/2016.
 */
public class AccountDAO extends AbstractDAO<Account> implements ICore {

    String[] columns = {"ID", "Surname", "FirstName", "Gender", "PhoneNumber", "DOB", "Photo",
            "Address", "PlaceOfBirth", "CardSerialNumber", "BVN",
            "ValidationRemark", "DateSync", "isProcessed", "isRegistered", "Reference", "ProductCode", "ImageUploaded", "GeoLocation" };

    public AccountDAO(Context context)
    {
        super.dbHelper = new DatabaseHelper(context, this);
        this.Open();
        db.execSQL(this.GetSqlCreate());
    }

    @Override
    public Account Get(long ID) {

        Account account = null;

        Cursor cursor = db.query(GetTableName(), columns , "ID=" + ID, null, null, null, null);
        cursor.moveToFirst();

        if (cursor.getCount() > 0)
        {
            while (!cursor.isAfterLast())
            {
                account = CursorToItem(cursor);
                cursor.moveToNext();
            }
        }

        return account;
    }

    public Account GetByPhoneNumber(String PhoneNumber)
    {
        Account account = null;

        Cursor cursor = db.query(GetTableName(), columns , "PhoneNumber='" + PhoneNumber + "'", null, null, null, null);
        cursor.moveToFirst();

        if (cursor.getCount() > 0)
        {
            while (!cursor.isAfterLast())
            {
                account = CursorToItem(cursor);
                cursor.moveToNext();
            }
        }

        return account;
    }

    public long UpdateAccount(Account account){
        long suc =- 1;
        if (account == null)
            return suc;

        ContentValues values = ContentValues(account, true);

        suc = super.db.update(this.GetTableName(), values, "ID = " + account.getID(), null);
        return suc;
    }

    private ContentValues ContentValues(Account account, boolean hasID){

        ContentValues values = new ContentValues();

        if (hasID)
            values.put("ID", account.getID());

        values.put("Surname", account.getLastName());
        values.put("FirstName", account.getFirstName());
        values.put("Gender", account.getGender());
        values.put("PhoneNumber", account.getPhoneNo());
        values.put("DOB", account.getDateOfBirth());
        //values.put("Photo", customerRequest.getPhoto());
        values.put("Address", account.getAddress());
        values.put("PlaceOfBirth", account.getPlaceOfBirth());
       // values.put("CardSerialNumber", customerRequest.getCardSerialNumber());
        values.put("BVN",account.getBvn());
        //values.put("ValidationRemark", customerRequest.getValidationRemark());
        //values.put("DateSync", customerRequest.getDateSync());
        //values.put("isProcessed", customerRequest.isProcessed() + "");
        //values.put("isRegistered", customerRequest.isRegistered() + "");
        //values.put("Reference", customerRequest.getReference());
        //values.put("ProductCode", customerRequest.getProductCode());
        //values.put("ImageUploaded", customerRequest.getImageLoaded());
        //values.put("GeoLocation", customerRequest.getGeoLocation());

        return values;
    }

    public List<Account> GetByField(String field, String value)
    {
        List<Account> accounts = new ArrayList<>();

        Cursor cursor = db.query(GetTableName(), columns , "" + field + "='" + value + "'", null, null, null, null);
        cursor.moveToFirst();

        if (cursor.getCount() > 0)
        {
            while (!cursor.isAfterLast())
            {
                Account account = CursorToItem(cursor);
                accounts.add(account);
                cursor.moveToNext();
            }
        }

        return accounts;
    }

    public List<Account> GetByField(String... fieldVales)
    {
        List<Account> accounts = new ArrayList<>();

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
                Account account = CursorToItem(cursor);
                accounts.add(account);
                cursor.moveToNext();
            }
        }

        return accounts;
    }

    public List<Account> Get(long ID, int limit, String... queryParams){

        String query = "";

        for (String param : queryParams){
            query += " AND " + param;
        }

        List<Account> accounts = new ArrayList<>();

        Cursor cursor = db.query(GetTableName(), columns, "ID > " + ID + query, null, null, null, "ID DESC", limit + "");
        cursor.moveToFirst();

        if (cursor.getCount() > 0)
        {
            while (!cursor.isAfterLast())
            {
                Account account = CursorToItem(cursor);
                accounts.add(account);
                cursor.moveToNext();
            }
        }

        return accounts;

    }

    @Override
    public List<Account> GetAll() {

        List<Account> accounts = new ArrayList<>();
        Cursor cursor = db.query(GetTableName(), columns, null, null, null, null, null);
        cursor.moveToFirst();

        if(cursor.getCount() > 0)
        {
            while(!cursor.isAfterLast())
            {
                accounts.add(CursorToItem(cursor));
                cursor.moveToNext();
            }
        }
        cursor.close();
        return accounts;
    }

    @Override
    public long Insert(Account account) {

        long id = -1;

        if(account == null)
            return id;

        ContentValues values = ContentValues(account, false);
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
    protected Account CursorToItem(Cursor cursor) {

        Account account = new Account();
        account.setID(cursor.getLong(cursor.getColumnIndex("ID")));
        account.setLastName(cursor.getString(cursor.getColumnIndex("Surname")));
        account.setFirstName(cursor.getString(cursor.getColumnIndex("FirstName")));
        account.setGender(cursor.getString(cursor.getColumnIndex("Gender")));
        account.setPhoneNo(cursor.getString(cursor.getColumnIndex("PhoneNumber")));
        account.setDateOfBirth(cursor.getString(cursor.getColumnIndex("DOB")));
        //customerRequest.setPhoto(cursor.getString(cursor.getColumnIndex("Photo")));
        account.setAddress(cursor.getString(cursor.getColumnIndex("Address")));
        account.setPlaceOfBirth(cursor.getString(cursor.getColumnIndex("PlaceOfBirth")));
        //customerRequest.setCardSerialNumber(cursor.getString(cursor.getColumnIndex("CardSerialNumber")));
        account.setBvn(cursor.getString(cursor.getColumnIndex("BVN")));
       // customerRequest.setValidationRemark(cursor.getString(cursor.getColumnIndex("ValidationRemark")));
        //customerRequest.setDateSync(cursor.getString(cursor.getColumnIndex("DateSync")));
        //customerRequest.setProcessed(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex("isProcessed"))));
       // customerRequest.setRegistered(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex("isRegistered"))));
        //customerRequest.setReference(cursor.getString(cursor.getColumnIndex("Reference")));
        account.setProductCode(cursor.getString(cursor.getColumnIndex("ProductCode")));
        //customerRequest.setImageLoaded(cursor.getString(cursor.getColumnIndex("ImageUploaded")));
        //customerRequest.setGeoLocation(cursor.getString(cursor.getColumnIndex("GeoLocation")));


        return account;
    }

    @Override
    public String GetTableName() {
        return "Accounts_Table";
    }

    @Override
    public String GetSqlCreate() {
        return "CREATE TABLE  IF NOT EXISTS "
                + GetTableName()
                + "("
                + "ID integer primary key autoincrement, "
                + "Surname text , "
                + "FirstName text , "
                + "Gender text , "
                + "PhoneNumber text , "
                + "DOB text , "
                + "Photo text , "
                + "Address text , "
                + "PlaceOfBirth text , "
                + "CardSerialNumber text , "
                + "BVN text , "
                + "ValidationRemark text , "
                + "DateSync text, "
                + "isProcessed text, "
                + "isRegistered text, "
                + "Reference text,"
                + "ProductCode text,"
                + "GeoLocation text,"
                + "ImageUploaded text"

                + ");";
    }
}

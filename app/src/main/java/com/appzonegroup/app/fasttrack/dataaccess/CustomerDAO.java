package com.appzonegroup.app.fasttrack.dataaccess;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.appzonegroup.app.fasttrack.model.Customer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joseph on 6/5/2016.
 */
public class CustomerDAO extends AbstractDAO<Customer> implements ICore {

    String[] columns = {"ProductCode", "ProductName", "AccountNumber", "PIN", "CustomerLastName", "CustomerFirstName",
            "CustomerPhoneNumber", "Gender", "StarterPackNumber", "BVN", "Address", "NOKPhone",
            "NOKName", "PlaceOfBirth", "DateOfBirth","GeoLocation"};

    public CustomerDAO(Context context)
    {
        super.dbHelper = new DatabaseHelper(context, this);
        this.Open();
        db.execSQL(this.GetSqlCreate());
    }

    @Override
    public Customer Get(long ID) {
        return null;
    }

    public Customer GetByPhoneNumber(String PhoneNumber)
    {
        Customer customer = null;

        Cursor cursor = db.query(GetTableName(), columns , "CustomerPhoneNumber='" + PhoneNumber + "'", null, null, null, null);
        cursor.moveToFirst();

        if (cursor.getCount() > 0)
        {
            while (!cursor.isAfterLast())
            {
                customer = CursorToItem(cursor);
                cursor.moveToNext();
            }
        }

        return customer;
    }

    @Override
    public List<Customer> GetAll() {
        List<Customer> customers = new ArrayList<>();
        Cursor cursor = db.query(GetTableName(), columns, null, null, null, null, null);
        cursor.moveToFirst();

        if(cursor.getCount() > 0)
        {
            while(!cursor.isAfterLast())
            {
                customers.add(CursorToItem(cursor));
                cursor.moveToNext();
            }
        }
        cursor.close();
        return customers;
    }

    @Override
    public long Insert(Customer customer) {
        long id = -1;

        if(customer == null)
            return id;

        ContentValues values = new ContentValues();
        values.put("ProductCode", customer.getProductCode());
        values.put("ProductName", customer.getProductName());
        values.put("AccountNumber", customer.getAccountNumber());
        values.put("PIN", customer.getPIN());
        values.put("CustomerLastName", customer.getCustomerLastName());
        values.put("CustomerFirstName", customer.getCustomerFirstName());
        values.put("CustomerPhoneNumber", customer.getCustomerPhoneNumber());
        values.put("Gender", customer.getGender());
        values.put("StarterPackNumber", customer.getStarterPackNumber());
        values.put("BVN", customer.getBVN());
        values.put("Address", customer.getAddress());
        values.put("NOKPhone", customer.getNOKPhone());
        values.put("NOKName", customer.getNOKName());
        values.put("PlaceOfBirth", customer.getPlaceOfBirth());
        values.put("DateOfBirth", customer.getDateOfBirth());
        values.put("GeoLocation", customer.getGeoLocation());

        id = super.db.insert(GetTableName(), null, values);


        return id;
    }

    public void Insert(List<Customer> customers){
        RecreateTable();

        String sql = "INSERT INTO " + GetTableName() + " ( ProductCode, ProductName, AccountNumber, PIN, " +
                "CustomerLastName, CustomerFirstName, CustomerPhoneNumber, Gender, StarterPackNumber, BVN, Address, " +
                "NOKPhone, NOKName, PlaceOfBirth, DateOfBirth, GeoLocation) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? );";

        db.beginTransactionNonExclusive();
        // db.beginTransaction();

        SQLiteStatement stmt = db.compileStatement(sql);

        for(Customer customer : customers){

            stmt.bindString(1, customer.getProductCode() + "");
            stmt.bindString(2, customer.getProductName() + "");
            stmt.bindString(3, customer.getAccountNumber() + "");
            stmt.bindString(4, customer.getPIN() + "");
            stmt.bindString(5, customer.getCustomerLastName() + "");
            stmt.bindString(6, customer.getCustomerFirstName() + "");

            stmt.bindString(7, customer.getCustomerPhoneNumber() + "");
            stmt.bindString(8, customer.getGender() + "");
            stmt.bindString(9, customer.getStarterPackNumber() + "");
            stmt.bindString(10, customer.getBVN() + "");
            stmt.bindString(11, customer.getAddress() + "");
            stmt.bindString(12, customer.getNOKPhone() + "");
            stmt.bindString(13, customer.getNOKName() + "");

            stmt.bindString(14, customer.getPlaceOfBirth() + "");
            stmt.bindString(15, customer.getDateOfBirth() + "");
            stmt.bindString(16, customer.getGeoLocation() + "");

            stmt.execute();
            stmt.clearBindings();

        }

        db.setTransactionSuccessful();
        db.endTransaction();

        //db.close();
    }

    public void RecreateTable()
    {
        db.execSQL("DROP TABLE IF EXISTS " + this.GetTableName());
        db.execSQL(this.GetSqlCreate());
    }

    @Override
    protected Customer CursorToItem(Cursor cursor) {
        Customer customer = new Customer();
        customer.setProductCode(cursor.getString(cursor.getColumnIndex("ProductCode")));
        customer.setProductName(cursor.getString(cursor.getColumnIndex("ProductName")));
        customer.setAccountNumber(cursor.getString(cursor.getColumnIndex("AccountNumber")));
        customer.setPIN(cursor.getString(cursor.getColumnIndex("PIN")));
        customer.setCustomerLastName(cursor.getString(cursor.getColumnIndex("CustomerLastName")));
        customer.setCustomerFirstName(cursor.getString(cursor.getColumnIndex("CustomerFirstName")));
        customer.setCustomerPhoneNumber(cursor.getString(cursor.getColumnIndex("CustomerPhoneNumber")));
        customer.setGender(cursor.getString(cursor.getColumnIndex("Gender")));
        customer.setStarterPackNumber(cursor.getString(cursor.getColumnIndex("StarterPackNumber")));
        customer.setBVN(cursor.getString(cursor.getColumnIndex("BVN")));
        customer.setAddress(cursor.getString(cursor.getColumnIndex("Address")));
        customer.setNOKPhone(cursor.getString(cursor.getColumnIndex("NOKPhone")));
        customer.setNOKName(cursor.getString(cursor.getColumnIndex("NOKName")));
        customer.setPlaceOfBirth(cursor.getString(cursor.getColumnIndex("PlaceOfBirth")));
        customer.setDateOfBirth(cursor.getString(cursor.getColumnIndex("DateOfBirth")));
        customer.setGeoLocation(cursor.getString(cursor.getColumnIndex("GeoLocation")));

        return customer;
    }

    @Override
    public String GetTableName() {
        return "Customers_Table";
    }

    @Override
    public String GetSqlCreate() {
        return "CREATE TABLE  IF NOT EXISTS "
                + GetTableName()
                + "("
                + "ID integer primary key autoincrement, "
                + "ProductCode text , "
                + "ProductName text , "
                + "AccountNumber text , "
                + "PIN text , "
                + "CustomerLastName text , "
                + "CustomerFirstName text , "
                + "CustomerPhoneNumber text, "
                + "Gender text, "
                + "StarterPackNumber text , "
                + "BVN text , "
                + "Address text , "
                + "NOKPhone text , "
                + "NOKName text , "
                + "PlaceOfBirth text, "
                + "DateOfBirth text "
                + "GeoLocation text"
                + ");";
    }
}

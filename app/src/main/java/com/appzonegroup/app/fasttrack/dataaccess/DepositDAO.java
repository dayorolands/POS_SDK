package com.appzonegroup.app.fasttrack.dataaccess;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.appzonegroup.app.fasttrack.model.Deposit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Oto-obong on 17/07/2017.
 */

public class DepositDAO extends AbstractDAO<Deposit> implements ICore {

    String[] columns = {"Name", "AccountNumber", "Amount", "Date"};

    public DepositDAO(Context context)
    {
        super.dbHelper = new DatabaseHelper(context, this);
        this.Open();
        db.execSQL(this.GetSqlCreate());
    }

    @Override
    public Deposit Get(long ID) {
        return null;
    }

    public Deposit GetByDate(Date date)
    {
        Deposit deposit = null;

        Cursor cursor = db.query(GetTableName(), columns , "Date='" + date + "'", null, null, null, null);
        cursor.moveToFirst();

        if (cursor.getCount() > 0)
        {
            while (!cursor.isAfterLast())
            {
                deposit = CursorToItem(cursor);
                cursor.moveToNext();
            }
        }

        return deposit;
    }

    @Override
    public List<Deposit> GetAll() {
        List<Deposit> deposits = new ArrayList<>();
        Cursor cursor = db.query(GetTableName(), columns, null, null, null, null, null);
        cursor.moveToFirst();

        if(cursor.getCount() > 0)
        {
            while(!cursor.isAfterLast())
            {
                deposits.add(CursorToItem(cursor));
                cursor.moveToNext();
            }
        }
        cursor.close();
        return deposits;
    }

    @Override
    public long Insert(Deposit deposit) {
        long id = -1;

        if(deposit == null)
            return id;

        ContentValues values = new ContentValues();
        values.put("Name", deposit.getName());
        values.put("AccountNumber", deposit.getAccountNumber());
        values.put("Amount", deposit.getAmount());
        values.put("Date", String.valueOf(deposit.getDate()));
        id = super.db.insert(GetTableName(), null, values);
        return id;
    }

    public void Insert(List<Deposit> deposits){
        RecreateTable();

        String sql = "INSERT INTO " + GetTableName() + " ( Name, AccountNumber, Amount, " +
                "Date) VALUES ( ?, ?, ?, ?);";

        db.beginTransactionNonExclusive();
        // db.beginTransaction();

        SQLiteStatement stmt = db.compileStatement(sql);

        for(Deposit deposit : deposits){

            stmt.bindString(1, deposit.getAmount() + "");
            stmt.bindString(2, deposit.getAccountNumber() + "");
            stmt.bindString(3, deposit.getAmount() + "");
            stmt.bindString(4, deposit.getDate() + "");
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
    protected Deposit CursorToItem(Cursor cursor) {
        Deposit deposit = new Deposit();
        deposit.setName(cursor.getString(cursor.getColumnIndex("ProductCode")));
        deposit.setAccountNumber(cursor.getString(cursor.getColumnIndex("ProductName")));
        deposit.setAmount(cursor.getString(cursor.getColumnIndex("AccountNumber")));
        deposit.setDate(cursor.getString(cursor.getColumnIndex("Date")));

        return deposit;
    }

    @Override
    public String GetTableName() {
        return "Deposit_Table";
    }

    @Override
    public String GetSqlCreate() {
        return "CREATE TABLE  IF NOT EXISTS "
                + GetTableName()
                + "("
                + "ID integer primary key autoincrement, "
                + "Name text , "
                + "AccountNumber text , "
                + "Amount text , "
                + "Date text"
                + ");";
    }
}

package com.appzonegroup.app.fasttrack.dataaccess;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.appzonegroup.app.fasttrack.model.LoanProduct;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joseph on 6/5/2016.
 */
public class LoanProductDAO extends AbstractDAO<LoanProduct> implements ICore {

    String[] columns = {"ID", "Name", "MinimumAmount", "MaximumAmount"};

    public LoanProductDAO(Context context){

        super.dbHelper = new DatabaseHelper(context, this);
        this.Open();
        db.execSQL(this.GetSqlCreate());
    }

    @Override
    public LoanProduct Get(long ID) {
        LoanProduct loanProduct = null;// = new LoanProduct();
        Cursor cursor = db.query(GetTableName(), columns, "ID="+ID, null, null, null, null);
        cursor.moveToFirst();

        if(cursor.getCount()>0)
        {
            loanProduct = CursorToItem(cursor);
        }
        cursor.close();
        return loanProduct;
    }

    @Override
    public List<LoanProduct> GetAll() {
        List<LoanProduct> loanProducts = new ArrayList<>();
        Cursor cursor = db.query(GetTableName(), columns, null, null, null, null, null);
        cursor.moveToFirst();

        if(cursor.getCount() > 0)
        {
            while(!cursor.isAfterLast())
            {
                loanProducts.add(CursorToItem(cursor));
                cursor.moveToNext();
            }
        }
        cursor.close();
        return loanProducts;
    }

    @Override
    public long Insert(LoanProduct loanProduct) {
        long id = -1;

        if(loanProduct == null)
            return id;

        ContentValues values = new ContentValues();
        values.put("ID", loanProduct.getID());
        values.put("Name", loanProduct.getName());
        values.put("MinimumAmount", loanProduct.getMinimumAmount());
        values.put("MaximumAmount", loanProduct.getMaximumAmount());

        id = super.db.insert(GetTableName(), null, values);

        return id;
    }

    public void Insert(List<LoanProduct> loanProducts){
        RecreateTable();

        String sql = "INSERT INTO " + GetTableName() + " ( ID, Name, MinimumAmount, MaximumAmount ) VALUES ( ?, ?, ?, ? );";

        db.beginTransactionNonExclusive();

        SQLiteStatement stmt = db.compileStatement(sql);

        for(LoanProduct loanProduct : loanProducts){

            stmt.bindString(1, loanProduct.getID() + "");
            stmt.bindString(2, loanProduct.getName() + "");
            stmt.bindString(3, loanProduct.getMinimumAmount() + "");
            stmt.bindString(4, loanProduct.getMaximumAmount() + "");

            stmt.execute();
            stmt.clearBindings();

        }

        db.setTransactionSuccessful();
        db.endTransaction();

        //db.close();
    }

    @Override
    protected LoanProduct CursorToItem(Cursor cursor) {
        LoanProduct loanProduct = new LoanProduct();
        loanProduct.setID(cursor.getLong(cursor.getColumnIndex("ID")));
        loanProduct.setName(cursor.getString(cursor.getColumnIndex("Name")));
        loanProduct.setMinimumAmount(cursor.getDouble(cursor.getColumnIndex("MinimumAmount")));
        loanProduct.setMaximumAmount(cursor.getDouble(cursor.getColumnIndex("MaximumAmount")));
        return loanProduct;
    }

    @Override
    public String GetTableName() {
        return "Loan_Product_Table";
    }

    public void RecreateTable()
    {
        db.execSQL("DROP TABLE IF EXISTS " + this.GetTableName());
        db.execSQL(this.GetSqlCreate());
    }

    @Override
    public String GetSqlCreate() {
        return "CREATE TABLE  IF NOT EXISTS "
                + GetTableName()
                + "("
                + "auto_id integer primary key autoincrement, "
                + "ID integer not null, "
                + "Name text not null, "
                + "MinimumAmount real, "
                + "MaximumAmount real"
                + ");";
    }

}

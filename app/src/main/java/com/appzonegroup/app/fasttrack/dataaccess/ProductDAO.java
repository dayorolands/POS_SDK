package com.appzonegroup.app.fasttrack.dataaccess;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.creditclub.core.data.model.Product;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joseph on 6/6/2016.
 */
public class ProductDAO extends AbstractDAO<Product> implements ICore {
    String[] columns = {"ID", "Name", "Code"};

    public ProductDAO(Context context){

        super.dbHelper = new DatabaseHelper(context, this);
        this.Open();
        db.execSQL(this.GetSqlCreate());
    }

    @Override
    public Product Get(long ID) {
        Product product = null;// = new Product();
        Cursor cursor = db.query(GetTableName(), columns, "ID="+ID, null, null, null, null);
        cursor.moveToFirst();

        if(cursor.getCount()>0)
        {
            product = CursorToItem(cursor);
        }
        cursor.close();
        return product;
    }

    @Override
    public List<Product> GetAll() {
        List<Product> products = new ArrayList<>();
        Cursor cursor = db.query(GetTableName(), columns, null, null, null, null, null);
        cursor.moveToFirst();

        if(cursor.getCount() > 0)
        {
            while(!cursor.isAfterLast())
            {
                products.add(CursorToItem(cursor));
                cursor.moveToNext();
            }
        }
        cursor.close();
        return products;
    }

    @Override
    public long Insert(Product product) {
        long id = -1;

        if(product == null)
            return id;

        ContentValues values = new ContentValues();
        values.put("ID", product.getId());
        values.put("Name", product.getName());
        values.put("Code", product.getCode());

        id = super.db.insert(GetTableName(), null, values);

        return id;
    }

    public void Insert(List<Product> products){
        RecreateTable();

        String sql = "INSERT INTO " + GetTableName() + " ( ID, Name, Code ) VALUES ( ?, ?, ? );";

        db.beginTransactionNonExclusive();

        SQLiteStatement stmt = db.compileStatement(sql);

        for(Product product : products){

            stmt.bindString(1, product.getId() + "");
            stmt.bindString(2, product.getName() + "");
            stmt.bindString(3, product.getCode() + "");

            stmt.execute();
            stmt.clearBindings();

        }

        db.setTransactionSuccessful();
        db.endTransaction();

        //db.close();
    }

    @Override
    protected Product CursorToItem(Cursor cursor) {
        Product product = new Product();
        product.setId(cursor.getLong(cursor.getColumnIndex("ID")));
        product.setName(cursor.getString(cursor.getColumnIndex("Name")));
        product.setCode(cursor.getString(cursor.getColumnIndex("Code")));
        return product;
    }

    @Override
    public String GetTableName() {
        return "Product_Table";
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
                + "ID integer, "
                + "Name text, "
                + "Code text"
                + ");";
    }

}

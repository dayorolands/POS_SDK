package com.appzonegroup.app.fasttrack.dataaccess;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.appzonegroup.app.fasttrack.model.Institution;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joseph on 6/4/2016.
 */
public class InstitutionDAO extends AbstractDAO<Institution> implements ICore {

    String[] columns = {"ID", "Name", "InstitutionCode"};

    public InstitutionDAO(Context context){

        super.dbHelper = new DatabaseHelper(context, this);
        this.Open();
        db.execSQL(this.GetSqlCreate());
    }

    @Override
    public Institution Get(long ID) {
        return null;
    }

    public Institution GetByInstitutionCode(String InstitutionCode)
    {
        Institution institution = null;

        Cursor cursor = db.query(GetTableName(), columns , "InstitutionCode='" + InstitutionCode + "'", null, null, null, null);
        cursor.moveToFirst();

        if (cursor.getCount() > 0)
        {
            while (!cursor.isAfterLast())
            {
                institution = CursorToItem(cursor);
                cursor.moveToNext();
            }
        }

        return institution;
    }

    @Override
    public List<Institution> GetAll() {
        List<Institution> institutions = new ArrayList<>();
        Cursor cursor = db.query(GetTableName(), columns, null, null, null, null, null);
        cursor.moveToFirst();

        if(cursor.getCount() > 0)
        {
            while(!cursor.isAfterLast())
            {
                institutions.add(CursorToItem(cursor));
                cursor.moveToNext();
            }
        }
        cursor.close();
        return institutions;
    }

    @Override
    public long Insert(Institution institution) {

        long id = -1;

        if(institution == null)
            return id;

        ContentValues values = new ContentValues();
        values.put("Name", institution.getName());
        values.put("InstitutionCode", institution.getInstitutionCode());

        id = super.db.insert(GetTableName(), null, values);


        return id;
    }

    @Override
    protected Institution CursorToItem(Cursor cursor) {
        Institution institution = new Institution();
        institution.setInstitutionCode(cursor.getString(cursor.getColumnIndex("InstitutionCode")));
        institution.setName(cursor.getString(cursor.getColumnIndex("Name")));
        return institution;
    }

    @Override
    public String GetTableName() {
        return "Institutions_Table";
    }

    @Override
    public String GetSqlCreate() {
        return "CREATE TABLE  IF NOT EXISTS "
                + GetTableName()
                + "("
                + "ID integer primary key autoincrement, "
                + "Name text not null, "
                + "InstitutionCode text not null);";
    }

    public void Insert(List<Institution> institutions){
        RecreateTable();

        String sql = "INSERT INTO " + GetTableName() + " ( Name, InstitutionCode ) VALUES ( ?, ? );";

        db.beginTransactionNonExclusive();

        SQLiteStatement stmt = db.compileStatement(sql);

        for(Institution institution : institutions){

            stmt.bindString(1, institution.getName() + "");
            stmt.bindString(2, institution.getInstitutionCode() + "");

            stmt.execute();
            stmt.clearBindings();

        }

        db.setTransactionSuccessful();
        db.endTransaction();

        db.close();
    }

    public void RecreateTable()
    {
        db.execSQL("DROP TABLE IF EXISTS " + this.GetTableName());
        db.execSQL(this.GetSqlCreate());
    }
}

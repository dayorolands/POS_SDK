package com.appzonegroup.app.fasttrack.dataaccess;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.appzonegroup.app.fasttrack.model.Association;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joseph on 6/4/2016.
 */
public class AssociationDAO extends AbstractDAO<Association> implements ICore {

    String[] columns = {"auto_id", "id", "group_id", "name", "group_email", "group_address", "physical_address",
            "type", "accreditation_status", "beneficiary_loan_eligibility", "loan_eligibility"};

    public AssociationDAO(Context context){

        super.dbHelper = new DatabaseHelper(context, this);
        this.Open();
        db.execSQL(this.GetSqlCreate());
    }

    @Override
    public Association Get(long ID) {
        Association association = null;
        String id = "auto_id";
        Cursor cursor = db.rawQuery("SELECT * FROM " + GetTableName() + " WHERE " + id + " = '" + ID + "'", null);
        cursor.moveToFirst();
        association = CursorToItem(cursor);
        return association;
    }


    @Override
    public List<Association> GetAll() {
        List<Association> associations = new ArrayList<>();
        Cursor cursor = db.query(GetTableName(), columns, null, null, null, null, null);
        cursor.moveToFirst();

        if(cursor.getCount() > 0)
        {
            while(!cursor.isAfterLast())
            {
                associations.add(CursorToItem(cursor));
                cursor.moveToNext();
            }
        }
        cursor.close();
        return associations;
    }

    public String[] getList(){
        List<Association> associations = this.GetAll();
        String[] associationNames = new String[associations.size()];
        int index = 0;

        for (Association association : associations) {
            associationNames[index] = association.getName();
            index++;
        }
        return associationNames;
    }

    @Override
    public long Insert(Association association) {
        long id = -1;

        if(association == null)
            return id;

        ContentValues values = new ContentValues();
        values.put("id", association.getId());
        values.put("group_id", association.getId());
        values.put("name", association.getName());
        values.put("group_email", association.getId());
        values.put("group_address", association.getId());
        values.put("physical_address", association.getName());
        values.put("type", association.getName());
        values.put("accreditation_status", association.getId());
        values.put("beneficiary_loan_eligibility", association.getId());
        values.put("loan_eligibility", association.getName());

        id = super.db.insert(GetTableName(), null, values);


        return id;
    }

    public void Insert(List<Association> associations){
//        RecreateTable();

        String sql = "INSERT INTO " + GetTableName() + " (id, group_id, name, group_email, group_address, "
                + "physical_address, type, accreditation_status, beneficiary_loan_eligibility, "
                + "loan_eligibility) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? );";

        db.beginTransactionNonExclusive();

        SQLiteStatement stmt = db.compileStatement(sql);

        for(Association association : associations){

            stmt.bindString(1, association.getId() + "");
            stmt.bindString(2, association.getGroup_id() + "");
            stmt.bindString(3, association.getName() + "");
            stmt.bindString(4, association.getGroup_email() + "");

            stmt.bindString(5, association.getGroup_address() + "");
            stmt.bindString(6, association.getPhysical_address() + "");
            stmt.bindString(7, association.getType() + "");
            stmt.bindString(8, association.getAccreditation_status() + "");

            stmt.bindString(9, association.getBeneficiary_loan_eligibility() + "");
            stmt.bindString(10, association.getLoan_eligibility() + "");

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
    protected Association CursorToItem(Cursor cursor) {

        Association association = new Association();
        association.setId(cursor.getString(cursor.getColumnIndex("id")));
        association.setGroup_id(cursor.getString(cursor.getColumnIndex("group_id")));
        association.setName(cursor.getString(cursor.getColumnIndex("name")));
        association.setGroup_email(cursor.getString(cursor.getColumnIndex("group_email")));
        association.setGroup_address(cursor.getString(cursor.getColumnIndex("group_address")));
        association.setPhysical_address(cursor.getString(cursor.getColumnIndex("physical_address")));
        association.setType(cursor.getString(cursor.getColumnIndex("type")));
        association.setAccreditation_status(cursor.getString(cursor.getColumnIndex("accreditation_status")));
        association.setBeneficiary_loan_eligibility(cursor.getString(cursor.getColumnIndex("beneficiary_loan_eligibility")));
        association.setLoan_eligibility(cursor.getString(cursor.getColumnIndex("loan_eligibility")));

        return association;
    }

    @Override
    public String GetTableName() {
        return "Associations_Table";
    }

    @Override
    public String GetSqlCreate() {
        return "CREATE TABLE  IF NOT EXISTS "
                + GetTableName()
                + "("
                + "auto_id integer primary key autoincrement, "
                + "id text, "
                + "group_id text, "
                + "name text, "
                + "group_email text, "
                + "group_address text, "
                + "physical_address text, "
                + "type text, "
                + "accreditation_status text, "
                + "beneficiary_loan_eligibility text, "
                + "loan_eligibility text "
                + " );";
    }
}

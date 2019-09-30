package com.appzonegroup.app.fasttrack.dataaccess;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.appzonegroup.app.fasttrack.model.BVNRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joseph on 6/13/2016.
 */
public class BVNRequestDAO extends AbstractDAO<BVNRequest> implements ICore {

    String[] columns = {"ID", "BVN", "CustomerPhoneNumber", "CustomerAccountNumber",
            "CustomerPIN", "InstitutionCode", "IsSync", "Remark", "IsConfirmed", "GeoLocation"};

    public BVNRequestDAO(Context context){
        super.dbHelper = new DatabaseHelper(context, this);
        this.Open();
        db.execSQL(this.GetSqlCreate());
    }

    @Override
    public BVNRequest Get(long ID) {
        BVNRequest bvnRequest = null;

        Cursor cursor = db.query(GetTableName(), columns , "ID=" + ID , null, null, null, null);
        cursor.moveToFirst();

        if (cursor.getCount() > 0)
        {
            while (!cursor.isAfterLast())
            {
                bvnRequest = CursorToItem(cursor);
                cursor.moveToNext();
            }
        }

        return bvnRequest;
    }

    @Override
    public List<BVNRequest> GetAll() {
        List<BVNRequest> bvnRequests = new ArrayList<>();
        Cursor cursor = db.query(GetTableName(), columns, null, null, null, null, null);
        cursor.moveToFirst();

        if(cursor.getCount() > 0)
        {
            while(!cursor.isAfterLast())
            {
                bvnRequests.add(CursorToItem(cursor));
                cursor.moveToNext();
            }
        }
        cursor.close();
        return bvnRequests;
    }

    /**
     * Set ID == 0 for the first request
     * @param ID
     * @param limit
     * @return
     */
    public List<BVNRequest> Get(long ID, int limit, String... queryParams){

        String query = "";

        for (String param : queryParams){
            query += " AND " + param;
        }

        List<BVNRequest> bvnRequests = new ArrayList<>();

        Cursor cursor = //db.query(GetTableName(), columns , "ID > " + ID, null, null, "ID DESC", limit + "");
                db.query(GetTableName(), columns, "ID > " + ID + query, null, null, null, "ID DESC", limit + "");
        cursor.moveToFirst();

        if (cursor.getCount() > 0)
        {
            while (!cursor.isAfterLast())
            {
                BVNRequest bvnRequest = CursorToItem(cursor);
                bvnRequests.add(bvnRequest);
                cursor.moveToNext();
            }
        }

        return bvnRequests;

    }

    @Override
    public long Insert(BVNRequest bvnRequest) {
        long id = -1;

        if(bvnRequest == null)
            return id;

        ContentValues values = ContentValues(bvnRequest);

        id = super.db.insert(GetTableName(), null, values);

        return id;
    }

    public List<BVNRequest> GetByField(String... fieldVales)
    {
        List<BVNRequest> bvnRequests = new ArrayList<>();

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
                BVNRequest bvnRequest = CursorToItem(cursor);
                bvnRequests.add(bvnRequest);
                cursor.moveToNext();
            }
        }

        return bvnRequests;
    }

    public long UpdateBVNRequest(BVNRequest bvnRequest){

        long suc =- 1;
        if (bvnRequest == null)
            return suc;

        ContentValues values = ContentValues(bvnRequest);

        suc = super.db.update(this.GetTableName(), values, "ID = " + bvnRequest.getID(), null);
        return suc;
    }

    private ContentValues ContentValues(BVNRequest bvnRequest){

        ContentValues values = new ContentValues();
        //values.put("ID", 0);
        values.put("BVN", bvnRequest.getBVN());
        values.put("CustomerPhoneNumber", bvnRequest.getCustomerPhoneNumber());
        values.put("CustomerAccountNumber", bvnRequest.getCustomerAccountNumber());
        values.put("CustomerPIN", bvnRequest.getCustomerPIN());
        values.put("InstitutionCode", bvnRequest.getInstitutionCode());

        values.put("IsSync", bvnRequest.getIsSync());
        values.put("Remark", bvnRequest.getRemark());
        values.put("IsConfirmed", bvnRequest.getIsConfirmed());
        values.put("GeoLocation", bvnRequest.getGeoLocation());

        return values;

    }

    @Override
    protected BVNRequest CursorToItem(Cursor cursor) {
        BVNRequest bvnRequest = new BVNRequest();
        bvnRequest.setID(cursor.getLong(cursor.getColumnIndex("ID")));
        bvnRequest.setInstitutionCode(cursor.getString(cursor.getColumnIndex("InstitutionCode")));
        bvnRequest.setBVN(cursor.getString(cursor.getColumnIndex("BVN")));
        bvnRequest.setCustomerPhoneNumber(cursor.getString(cursor.getColumnIndex("CustomerPhoneNumber")));
        bvnRequest.setCustomerAccountNumber(cursor.getString(cursor.getColumnIndex("CustomerAccountNumber")));
        bvnRequest.setCustomerPIN(cursor.getString(cursor.getColumnIndex("CustomerPIN")));

        bvnRequest.setIsSync(cursor.getString(cursor.getColumnIndex("IsSync")));
        bvnRequest.setRemark(cursor.getString(cursor.getColumnIndex("Remark")));
        bvnRequest.setIsConfirmed(cursor.getString(cursor.getColumnIndex("IsConfirmed")));
        bvnRequest.setGeoLocation(cursor.getString(cursor.getColumnIndex("GeoLocation")));

        return bvnRequest;
    }

    @Override
    public String GetTableName() {
        return "BVN_Request_Table";
    }

    @Override
    public String GetSqlCreate() {
        return "CREATE TABLE  IF NOT EXISTS "
                + GetTableName()
                + "( "
                + "ID integer primary key autoincrement, "
                + "BVN text not null, "
                + "CustomerPhoneNumber text, "
                + "CustomerAccountNumber text, "
                + "CustomerPIN text, "
                + "IsSync text, "
                + "Remark text, "
                + "IsConfirmed text, "
                + "GeoLocation text, "
                + "InstitutionCode);";
    }
}

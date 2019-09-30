package com.appzonegroup.app.fasttrack.dataaccess;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class DatabaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "credit_club.db";
	private static  int DATABASE_VERSION=2;
	private String sql_create_table;
	private String table_name;

	public DatabaseHelper(Context context, ICore object)
	{
		super(context,DATABASE_NAME,null, DATABASE_VERSION);
		sql_create_table = object.GetSqlCreate();
		table_name=object.GetTableName();
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
	db.execSQL(sql_create_table);	
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		 Log.w(DatabaseHelper.class.getName(),
			        "Upgrading database from version " + oldVersion + " to "
			            + newVersion + ", which will destroy all old data");
			    db.execSQL("DROP TABLE IF EXISTS " + table_name);
			    onCreate(db);
		
	}
	

	

}

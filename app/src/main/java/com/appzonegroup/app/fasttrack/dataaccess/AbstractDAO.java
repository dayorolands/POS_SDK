package com.appzonegroup.app.fasttrack.dataaccess;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

public abstract class AbstractDAO<Result> {
	protected DatabaseHelper dbHelper;
	protected SQLiteDatabase db;

	public void Open()
	{
		if(db!=null)
		{
			db.close();
			db=dbHelper.getWritableDatabase();
		}else
		{
			db=dbHelper.getWritableDatabase();
		}

	}
	
	public void close()
	{
		if(db.isOpen())
		dbHelper.close();
	}

	public String GetTableName(){
		return "noTableNameDescribed";
	}

	public long count()
	{
		return DatabaseUtils.queryNumEntries(db, GetTableName());
	}
	
	public abstract Result Get(long ID);
	public abstract List<Result> GetAll();
	public abstract long Insert(Result T);
	protected abstract Result CursorToItem(Cursor c);


}

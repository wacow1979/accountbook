package net.nctucs.lazchi.marco79423.ExpenseBook;

import android.content.Context;

import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import android.util.Log;

class DatabaseHelper extends SQLiteOpenHelper
{
	public DatabaseHelper(Context context)
	{
		/*super(
			new ContextWrapper(context)
			{
				@Override
				public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler errorHandler)
				{
					return super.openOrCreateDatabase("/sdcard/mydata/accountbook.db", mode, factory, errorHandler);
				}
			},
			Globals.DATABASE_NAME, null, Globals.DATABASE_VERSION);*/
		super(context, Globals.DATABASE_NAME, null, Globals.DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database)
	{
		database.execSQL(Globals.ExpenseTable.CREATE_TABLE);
		database.execSQL(Globals.CategoryTable.CREATE_TABLE);
		database.execSQL(Globals.InfoTable.CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion)
	{
		Log.w(Globals.LOG_TAG, "更新資料庫檔案，從版本" + oldVersion + "更新至" + newVersion +
				"注意，這會清除資料庫檔案");
		database.execSQL(Globals.ExpenseTable.DELETE_TABLE);
		database.execSQL(Globals.CategoryTable.DELETE_TABLE);
		database.execSQL(Globals.InfoTable.DELETE_TABLE);
		onCreate(database);
	}
}

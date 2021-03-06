package net.nctucs.lazchi.marco79423.ExpenseBook;

//Java
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;

//Android
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.res.Resources;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;

//介面
import android.view.Menu;
import android.view.MenuItem;


//Dropbox
import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;

//工具
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;


//功能

public class MainActivity extends Activity
{
	private static final int _REQUEST_LINK_TO_DROPBOX = 0;

	private DbxAccountManager _dbxAccountManager;
	AnimatorSet _dialogAnimatorSet;

	/*
	 * 生命週期
	 */
	
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

	    //dialog
	    FrameLayout dialogFrameLayout = (FrameLayout) findViewById(R.id.main_layout_dialog);
		_dialogAnimatorSet = (AnimatorSet) AnimatorInflater.loadAnimator(MainActivity.this, R.animator.main_dialog);
	    _dialogAnimatorSet.setTarget(dialogFrameLayout);

	    //Dropbox
	    _dbxAccountManager = DbxAccountManager.getInstance(
			    getApplicationContext(),
			    Globals.DROPBOX_KEY ,
			    Globals.DROPBOX_SECRET
	    );
	}

	@Override
	public void onResume()
	{
		super.onResume();

		//設定累死雞的對話內容
		_setTalkTextView();
	}

	/*
     * Menu
     */

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		menu.clear();
		if(_dbxAccountManager.hasLinkedAccount())
			getMenuInflater().inflate(R.menu.menu_logged, menu);
		else
			getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case R.id.menu_create_new_expense: _onCreateNewExpenseItemClicked(); break;
			case R.id.menu_browse: _onBrowseItemClicked(); break;
			case R.id.menu_upload_database: _onUploadDatabaseItemClicked(); break;
			case R.id.menu_download_database_from_dropbox: _onDownloadDatabaseFromDropboxItemClicked(); break;
			case R.id.menu_link_to_dropbox: _onLinkToDropboxItemClicked(); break;
			case R.id.menu_logout_from_dropbox: _onLogoutFromDropboxItemClicked(); break;
			case R.id.menu_exit: finish(); break;
			default: return false;
		}
		return true;
	}

	/*
	 * 事件
	 */

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch(requestCode)
		{
			case _REQUEST_LINK_TO_DROPBOX:
				if(resultCode == Activity.RESULT_OK)
					Toast.makeText(MainActivity.this, R.string.message_link_to_dropbox_successful, Toast.LENGTH_LONG).show();
				else
					Toast.makeText(MainActivity.this, R.string.message_link_to_dropbox_failed, Toast.LENGTH_LONG).show();
				break;
		}
	}


	/*
	 * 功能
	 */
	private void _onLinkToDropboxItemClicked()
	{
		_dbxAccountManager.startLink(MainActivity.this, _REQUEST_LINK_TO_DROPBOX);
	}

	private void _onLogoutFromDropboxItemClicked()
	{
		_dbxAccountManager.unlink();
	}

	private void _onDownloadDatabaseFromDropboxItemClicked()
	{
		Resources resource = getResources();
		if(!_dbxAccountManager.hasLinkedAccount())
		{
			Toast.makeText(MainActivity.this, resource.getString(R.string.message_must_link_to_dropbox), Toast.LENGTH_LONG).show();
			return;
		}

		try
		{
			DbxFileSystem dbxFileSystem = DbxFileSystem.forAccount(_dbxAccountManager.getLinkedAccount());
			DbxPath dbxDatabasePath = new DbxPath(DbxPath.ROOT, Globals.DATABASE_NAME);

			if(!dbxFileSystem.exists(dbxDatabasePath))
			{
				Toast.makeText(MainActivity.this, resource.getString(R.string.message_must_link_to_dropbox), Toast.LENGTH_LONG).show();
				return;
			}

			DbxFile dbxDatabaseFile = dbxFileSystem.open(dbxDatabasePath);

			//File databaseFile = new File("/sdcard/mydata/accountbook.db");//getDatabasePath(Globals.DATABASE_NAME);
			File databaseFile = getDatabasePath(Globals.DATABASE_NAME);

			FileInputStream fromStream = dbxDatabaseFile.getReadStream();
			FileOutputStream toStream = new FileOutputStream(databaseFile);

			//for database
			byte[] buffer = new byte[1024];
			int bufferLen;
			while((bufferLen = fromStream.read(buffer)) != -1)
				toStream.write(buffer, 0, bufferLen);

			fromStream.close();
			toStream.close();
			dbxDatabaseFile.close();

			Toast.makeText(MainActivity.this, resource.getString(R.string.message_download_successful), Toast.LENGTH_LONG).show();

			_setTalkTextView();
		}
		catch(Exception e)
		{
			Toast.makeText(MainActivity.this, resource.getString(R.string.message_download_failed), Toast.LENGTH_LONG).show();
		}
	}

	private  void _onUploadDatabaseItemClicked()
	{
		Resources resource = getResources();
		if(!_dbxAccountManager.hasLinkedAccount())
		{
			Toast.makeText(MainActivity.this, resource.getString(R.string.message_must_link_to_dropbox), Toast.LENGTH_LONG).show();
			return;
		}

		try
		{
			DbxFileSystem dbxFileSystem = DbxFileSystem.forAccount(_dbxAccountManager.getLinkedAccount());

			//Database file

			File databaseFile = getDatabasePath(Globals.DATABASE_NAME);
			//File databaseFile = new File("/sdcard/mydata/accountbook.db");
			if(databaseFile.exists())
			{
				DbxPath dbxDatabasePath = new DbxPath(DbxPath.ROOT, Globals.DATABASE_NAME);
				DbxFile dbxDatabaseFile;
				if(!dbxFileSystem.exists(dbxDatabasePath))
					dbxDatabaseFile = dbxFileSystem.create(dbxDatabasePath);
				else
					dbxDatabaseFile = dbxFileSystem.open(dbxDatabasePath);

				dbxDatabaseFile.writeFromExistingFile(databaseFile, false);
				dbxDatabaseFile.close();
			}

			Toast.makeText(MainActivity.this, resource.getString(R.string.message_upload_successful), Toast.LENGTH_LONG).show();
		}
		catch(Exception e)
		{
			Toast.makeText(MainActivity.this, resource.getString(R.string.message_upload_failed), Toast.LENGTH_LONG).show();
		}
	}

	private void _onCreateNewExpenseItemClicked()
	{
		Intent intent = new Intent();
		intent.setClass(MainActivity.this, ExpenseActivity.class);
		startActivity(intent);
	}

	private void _onBrowseItemClicked()
	{
		Intent intent = new Intent();
		intent.setClass(MainActivity.this, BrowseActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
	}

	private void _setTalkTextView()
	{
		Resources resource = getResources();

		ExpenseSqlModel expenseSqlModel = new ExpenseSqlModel(MainActivity.this);
		expenseSqlModel.open();
		String sumString = expenseSqlModel.getSumStringOfMonthlyExpenses();
		expenseSqlModel.close();

		TextView talkTextView = (TextView) findViewById(R.id.main_view_talk);
		talkTextView.setText(String.format(resource.getString(R.string.main_view_talk), sumString));

		_dialogAnimatorSet.start();
	}
}

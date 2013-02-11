package com.warren.wkrcontacts;

import java.util.ArrayList;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;


public class SelectGroups extends Activity {

	private final static String TAG = "WorkContactsActivitySelectGroups"; 

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_groups);
        ListView Groups = (ListView) findViewById(R.id.groupsLv);
        Cursor GroupCur = getAllGroups();
        Toast t = Toast.makeText(this,"GroupCount " + GroupCur.getCount(),Toast.LENGTH_SHORT);
        t.show();
//        ArrayList<String> GroupArray = getUnique(GroupCur, null);
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.groupselectlayout , groupCur, fieldsT, to);
        String[] fieldsT = new String[] {
                ContactsContract.Groups.TITLE
        };
        int[] to = new int[] {R.id.groupNameChk};
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.groupselectlayout, GroupCur, fieldsT, to);
        Groups.setAdapter(adapter);

    }

    private Cursor getAllGroups() {
    	Log.v(TAG,"Activity getAllGroups Started");
    	Uri uri = ContactsContract.Groups.CONTENT_URI;

        String[] projection = new String[] {
        		ContactsContract.Groups._ID,
        		ContactsContract.Groups.TITLE,
        };
        String selection = "";
        String[] selectionArgs = null;
        String sortOrder = ContactsContract.Groups.TITLE + " COLLATE LOCALIZED ASC";

        return managedQuery(uri, projection, selection, selectionArgs, sortOrder);
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_select_groups, menu);
        return true;
    }

    private ArrayList<String> getUnique(Cursor c, ArrayList<String> InitialArray){
    	//Appends InitialArray with each UniqueValue in col 1 of Cursor
    	ArrayList<String> myArraylist = InitialArray;
    	Integer i=0;
    	if (c.getCount()>0){
	     	while(!c.isLast()){
	     		//Check to see if the cursor field is blank
	     		if (c.getString(1)!=null) {
	     			myArraylist.add(c.getString(1));
		    		i ++;
		    		while(c.getString(1).equals(myArraylist.get(i))) {
		    			if (c.isLast()){
		    				break;
		    			}
		    			c.moveToNext();   			
		    		};
	     		}
	     		else{
	     			//while cursor field is null move to next until it isn't null
		    		while(c.getString(1)==null) {
		    			if (c.isLast()){
		    				break;
		    			}
		    			c.moveToNext();   			
		    		};	     			
	     		}
	     			
	     		
	    	};
    	};
    	return myArraylist;
    }

    
}

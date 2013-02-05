package com.warren.wkrcontacts;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

public class WorkerContactsActivityActivity extends Activity {
    /** Called when the activity is first created. */
	//Initialize the main variables
    private ListView Contacts;
    private Spinner Field;
    private Spinner Meeting;
    
    //Used for Database interface
    private Cursor contactsCursor;
    private Cursor cursorField, cursorMeeting;
    private SpinnerAdapter fieldAdapter;
    private SpinnerAdapter meetingAdapter;
    
    //This var keeps the onclicklisteners from updating until initial onCreate complete
    private boolean InitComplete;
    //On click listener doesn't do anything if it's state hasn't changed
    private String lastMeeting, lastField;
    
    private Context context = this;
    
    //Static identifiers for the SharedPrefs File - saving the view when paused
    private final static String CONTACTSINT = "ContactPosition";
    private final static String FIELDINT = "FieldSpPosition";
    private final static String MEETINGINT = "MeetingSpPosition";
    private final static String PREFS_NAME = "WorkerContactsPrefs";
    
    //Use this TAG with every Logcat entry
    public static final String TAG = "WorkerContacts";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
	    //remove title bar from the view before it shows
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
    	Log.v(TAG,"Activity onCreate Started");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //Initialize Objects
        InitComplete = false;
	    Contacts = (ListView) findViewById(R.id.lvContacts);
	    Field = (Spinner) findViewById(R.id.spField);
	    Meeting = (Spinner) findViewById(R.id.spMeeting);
	    
	    //Initialize Field Listener
	    Field.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				//Don't do anything if this is the initial setup of the activity
				if (InitComplete){
					//Make sure the value in the spinner has changed before doing anything
					if (lastField!=Field.getSelectedItem().toString()){
						Log.v(TAG, "FieldSelected Begin");
						//GetMeetingsInField
						populateMeetings();
						populateContactList();
						lastField = Field.getSelectedItem().toString();
						Log.v(TAG, "FieldSelected End");
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				//This should never happen
				Log.v(TAG, "Nothing Selected Begin");
			}
		});
	    Meeting.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				if (InitComplete){
					if (lastMeeting!=Meeting.getSelectedItem().toString()){
						Log.v(TAG, "MeetingSelected Begin");
						//GetMeetingsInField
						populateContactList();
						lastMeeting = Meeting.getSelectedItem().toString();
						Log.v(TAG, "MeetingSelected End");
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				Log.v(TAG, "Nothing Selected Begin");
			}
		});
	    
	    Contacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (InitComplete){
					Log.v(TAG,"ContactsLV On Click Begin");
					//Launch People Card for this contact
					Intent contactIn = new Intent(Intent.ACTION_VIEW);
					Long contactID = (long) 0;
					contactID = (long) Contacts.getPositionForView(arg1);
					contactsCursor.moveToPosition((int) Contacts.getPositionForView(arg1));
					contactID=contactsCursor.getLong(1);
					Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(contactID));
					contactIn.setData(uri);
					context.startActivity(contactIn);
					
					
					Log.v(TAG,"ContactsLV On Click End");					
				}
				
			}
		});
	    SharedPreferences mPrefs = getSharedPreferences(PREFS_NAME, 0);
        //last variables keep the spinner listener from updating when there is no change.  Makes quicker load
        populateFieldSpinner();
        Log.v(TAG,"FIELDINT is " + mPrefs.getInt(FIELDINT, 0));
    	Field.setSelection(mPrefs.getInt(FIELDINT,0));
        populateMeetings();
		Meeting.setSelection(mPrefs.getInt(MEETINGINT,0));
        populateContactList();
		Contacts.setSelectionFromTop((mPrefs.getInt(CONTACTSINT,0)),0);
	    lastField=Field.getSelectedItem().toString();
	    lastMeeting=Meeting.getSelectedItem().toString();
        InitComplete=true;
        Log.v(TAG, "Activity onCreate Finished");
    }
    	
    @Override
	protected void onPause() {
		super.onPause();
    	Log.v(TAG,"Activity onPause Started");
    	// Add view state to SharedPreferences PREFS_NAME File
    	SharedPreferences mPrefs = getSharedPreferences(PREFS_NAME,0);
        SharedPreferences.Editor ed = mPrefs.edit();
        // Record the position of the Contacts Listview, The Field Spinner, and Meeting Spinner
		ed.putInt(CONTACTSINT, Contacts.getFirstVisiblePosition());
		ed.putInt(FIELDINT, Field.getSelectedItemPosition());
		ed.putInt(MEETINGINT, Meeting.getSelectedItemPosition());
        ed.commit();
    	Log.v(TAG,"Activity onPause Ended");
	}
    
    private void populateMeetings() {
    	Log.v(TAG,"Activity populateMeetings Started");
    	cursorMeeting = getMeetings(fieldAdapter.getItem(Field.getSelectedItemPosition()).toString());
    	//Deal with 0 Meetings
		if (cursorMeeting.getCount()==0){
			String errString = "0 Meetings found for " + Field.getSelectedItem().toString() + " Field"; 
			Log.e(TAG, errString);
			Toast errToast = Toast.makeText(context, errString, 2);
			errToast.show();
			//Select all by default
			Field.setSelection(0);
			return;
		}
		else{
			Log.v(TAG,"A total of " + cursorMeeting.getCount() + " Meetings found");
		}
			
//    	if cursorField.getCount()==0; TODO  Need to add error checking if there are 0 fields
    	cursorMeeting.moveToFirst();
    	ArrayList<String> MeetingArray = new ArrayList<String>();
    	MeetingArray.add("All");
     	MeetingArray = getUnique(cursorMeeting,MeetingArray);

    	meetingAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,MeetingArray);
    	Meeting.setAdapter(meetingAdapter);

    	Log.v(TAG,"Activity populateMeetings Ended");
    	}

	private Cursor getMeetings(String string) {
    	Log.v(TAG,"Activity getMeetings Started");
    	//Get All
    	if (string == "All"){
            // Run query
        	Log.v(TAG,"getMeetings ALL Started");

            Uri uri = ContactsContract.Data.CONTENT_URI;

            String[] projection = new String[] {
            		ContactsContract.Data._ID,
            		ContactsContract.CommonDataKinds.Organization.TITLE
            };
            String selection = ContactsContract.Data.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE + "'";
            String[] selectionArgs = null;
            String sortOrder = ContactsContract.CommonDataKinds.Organization.TITLE + " COLLATE LOCALIZED ASC";
            Log.v(TAG,"getMeetings ALL Ended");
            return managedQuery(uri, projection, selection, selectionArgs, sortOrder);
    	}
    	//Get Some
            // Run query
    	Log.v(TAG,"getMeetings " + string + " Started");
        Uri uri = ContactsContract.Data.CONTENT_URI;
// TODO This could throw an Error or return null
        String[] projection = new String[] {
        		ContactsContract.Data._ID,
        		ContactsContract.CommonDataKinds.Organization.TITLE
        };
        //fix string that contains apostrophes ' to have double apostrophes '' for SQL  O'Fallon Meeting Causes Crash
        String fixedstring = string.replace("'", "''");
        Log.v(TAG,fixedstring + " " + string.replace("'", "''"));
        String selection = ContactsContract.Data.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE + "'" + " AND " + ContactsContract.CommonDataKinds.Organization.COMPANY  + " = '" + fixedstring +"'";
        String[] selectionArgs = null;
        String sortOrder = ContactsContract.CommonDataKinds.Organization.TITLE + " COLLATE LOCALIZED ASC";
    	Log.v(TAG,"getMeetings " + string + " Ended");
        return managedQuery(uri, projection, selection, selectionArgs, sortOrder);
  	}

	private void populateFieldSpinner() {
		// PopulateFieldSpinner
//    	totalFields = 0;
    	Log.v(TAG, "Activity populateFieldSpinner Started");
    	cursorField = getFields();
//    	if cursorField.getCount()==0; TODO  Need to add error checking if there are 0 fields
    	cursorField.moveToFirst();
    	ArrayList<String> FieldArray = new ArrayList<String>();
    	FieldArray.add("All");
     	FieldArray = getUnique(cursorField, FieldArray);

    	fieldAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,FieldArray);
    	Field.setAdapter(fieldAdapter);
  	   	
    	Log.v(TAG, "Activity populateFieldSpinner Finished");
	}

	/**
     * Populate the contact list based on account currently selected in the account spinner.
     */
    private void populateContactList() {
        // Build adapter with contact entries
        contactsCursor = getContacts();
    	Log.v(TAG,"populateContactList Started");
        String[] fieldsT = new String[] {
                ContactsContract.Data.DISPLAY_NAME
        };
        int[] to = new int[] {R.id.txtContactRow};
       SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,R.layout.contactrow , contactsCursor, fieldsT, to);
       Contacts.setAdapter(adapter);
    }

    /**
     * Obtains the contact list for the currently selected account.
     *
     * @return A cursor for for accessing the contact list.
     */
    private Cursor getContacts()
    {
    	String selection;
        // Run query
    	Log.v(TAG,"getContacts Started");
        Uri uri = ContactsContract.Data.CONTENT_URI;

        String[] projection = new String[] {
        		ContactsContract.Data._ID,
        		ContactsContract.Data.CONTACT_ID,
                ContactsContract.Data.DISPLAY_NAME
        };
        //Build Query for by Field
        //fix string that contains apostrophes ' to have double apostrophes '' for SQL  O'Fallon Meeting Causes Crash
        String mtgstring = Meeting.getSelectedItem().toString().replace("'", "''");
        String fldstring = Field.getSelectedItem().toString().replace("'", "''");
        Log.v(TAG, mtgstring + " "+ fldstring);
        selection = "";
        if (Field.getSelectedItem().toString()!="All"){
        	selection = ContactsContract.CommonDataKinds.Organization.COMPANY + " = '" + fldstring + "'";
        	if (Meeting.getSelectedItem().toString()!="All"){
            	selection = selection + " AND " + ContactsContract.CommonDataKinds.Organization.TITLE + " = '" + mtgstring + "'";
            }
        }
        else if (Meeting.getSelectedItem().toString()!="All"){
        	selection = ContactsContract.CommonDataKinds.Organization.TITLE + " = '" + mtgstring + "'";
        }
        if (selection==""){
        	selection = ContactsContract.Data.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE + "'";
        }
        Log.v(TAG, "Selection is " + selection);
        String[] selectionArgs = null;
        String sortOrder = ContactsContract.Data.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
    	Log.v(TAG,"getContacts Finished");
        return managedQuery(uri, projection, selection, selectionArgs, sortOrder);
    }
    private Cursor getFields()
    {
        // Run query
    	Log.v(TAG,"getFields Started");

        Uri uri = ContactsContract.Data.CONTENT_URI;

        String[] projection = new String[] {
        		ContactsContract.Data._ID,
        		ContactsContract.CommonDataKinds.Organization.COMPANY 
        };
        String selection = ContactsContract.Data.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE + "'";
        String[] selectionArgs = null;
        String sortOrder = ContactsContract.CommonDataKinds.Organization.COMPANY + " COLLATE LOCALIZED ASC";
        Log.v(TAG,"getFields Finished");
        return managedQuery(uri, projection, selection, selectionArgs, sortOrder);
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
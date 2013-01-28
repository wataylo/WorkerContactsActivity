package com.warren.wkrcontacts;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

public class WorkerContactsActivityActivity extends Activity {
    /** Called when the activity is first created. */
    private ListView Contacts;
    private Spinner Field;
    private Spinner Meeting;
    private Cursor contactsCursor;
    private Cursor cursorField, cursorMeeting;
    private SpinnerAdapter fieldAdapter;
    private SpinnerAdapter meetingAdapter;
    private boolean InitComplete;
    private String lastMeeting, lastField;
    private Context context = this;
    private final static String CONTACTSINT = "ContactPosition";
    private final static String FIELDINT = "FieldSpPosition";
    private final static String MEETINGINT = "MeetingSpPosition";
   //Initialize Objects
    
 
    public static final String TAG = "WorkerContacts";
    
    
    //public Exp
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
	    //remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        
    	Log.v(TAG,"Activity onCreate Started");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //Initialize Objects
        InitComplete = false;
	    Contacts = (ListView) findViewById(R.id.lvContacts);
	    Field = (Spinner) findViewById(R.id.spField);
	    Meeting = (Spinner) findViewById(R.id.spMeeting);
	    
	    //InitializeListeners
	    Field.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				if (InitComplete){
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
	    if(savedInstanceState!=null){
	    	Log.v(TAG,"Activity RestoreInstanceState Started");
			// Restore state members from saved instance
	    	InitComplete=true;
			Field.setSelection(savedInstanceState.getInt(FIELDINT));
			Meeting.setSelection(savedInstanceState.getInt(MEETINGINT));
			Contacts.setSelection(savedInstanceState.getInt(CONTACTSINT));
			Log.v(TAG,"Activity onRestoreInstanceState Ended");

	    } else { 
	        //last variables keep the spinner listener from updating when there is no change.  Makes quicker load
		    lastField="All";
		    lastMeeting="All";
	        populateFieldSpinner();
	        Field.setId(1);
	        populateMeetings();
	        Meeting.setId(1);
	        populateContactList();
	        InitComplete=true;
	    }
        Log.v(TAG, "Activity onCreate Finished");
    }
//    @Override	
//	public void onRestoreInstanceState(Bundle savedInstanceState) {     
//    	Log.v(TAG,"Activity onRestoreInstanceState Started");
//		// Always call the superclass so it can restore the view hierarchy     
//		super.onRestoreInstanceState(savedInstanceState);     
//		// Restore state members from saved instance     
//		Field.setSelection(savedInstanceState.getInt(FIELDINT));
//		Meeting.setSelection(savedInstanceState.getInt(MEETINGINT));
//		Contacts.setSelection(savedInstanceState.getInt(CONTACTSINT));
//		Log.v(TAG,"Activity onRestoreInstanceState Ended");
//	}
    	
    @Override	
	public void onSaveInstanceState(Bundle savedInstanceState) {     
    	Log.v(TAG,"Activity onSaveInstanceState Started");
		// Always call the superclass so it can restore the view hierarchy     
		super.onSaveInstanceState(savedInstanceState);     
		// Restore state members from saved instance     
		savedInstanceState.putInt(CONTACTSINT, Contacts.getSelectedItemPosition());
		savedInstanceState.putInt(FIELDINT, Field.getSelectedItemPosition());
		savedInstanceState.putInt(MEETINGINT, Meeting.getSelectedItemPosition());
    	Log.v(TAG,"Activity onSaveInstanceState Ended");
	}
    
    
    private void populateMeetings() {
    	Log.v(TAG,"Activity populateMeetings Started");
    	//Is this an update?
//    	if (totalMeetings>0){  TODO
//    		//UpdateTheCursorAndAdapterView
//    	}
		cursorMeeting = getMeetings(fieldAdapter.getItem(Field.getSelectedItemPosition()).toString());
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
	     		myArraylist.add(c.getString(1));
	    		i ++;
	    		while(c.getString(1).equals(myArraylist.get(i))) {
	    			if (c.isLast()){
	    				break;
	    			}
	    			c.moveToNext();   			
	    		};
	    	};
    	};
    	return myArraylist;
    }
    
  
    

}
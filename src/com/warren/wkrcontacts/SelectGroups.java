package com.warren.wkrcontacts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class SelectGroups extends Activity {

    //Static identifiers for the SharedPrefs File - saving the view when paused
    private final static String GROUPSCOUNT = "GroupsCount";
    private final static String GROUPSID = "GroupsID";
    private final static String PREFS_NAME = "WorkerContactsPrefs";
    private SharedPreferences mPrefs;
	
	LayoutInflater inflater;
	private final static String TAG = "WorkContactsActivitySelectGroups"; 
	private CustomGroupAdapter adapter;
	private Cursor GroupCur;
	private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	int i,j,SavedGroupCount;
    	context = this;
    	inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_groups);
        ListView Groups = (ListView) findViewById(R.id.groupsLv);
        Button Save = (Button) findViewById(R.id.saveBtn);
        GroupCur = getAllGroups();
        String[] fieldsT = new String[] {
                ContactsContract.Groups.TITLE
        };
        int[] to = new int[] {R.id.groupNameChk};
        adapter = new CustomGroupAdapter(this, R.layout.groupselectlayout, GroupCur, fieldsT, to);
        Groups.setAdapter(adapter);
    	Save.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.v(TAG, "OnSaveClick Started");
				//How many items are checked
				int numChecked = getCheckedCount();
				int i=0;
				int j=0;
		    	SharedPreferences mPrefs = getSharedPreferences(PREFS_NAME,0);
		        SharedPreferences.Editor ed = mPrefs.edit();
				if (numChecked>0){
					//there are items checked.  Write the count variable
					ed.putInt(GROUPSCOUNT, numChecked);
					while(i<adapter.getCount()){
						//Loop through all checkboxes to save the state
						if (adapter.checkState[i]){
							//save this group_ID as a KeyValue Pair
							j++;
							GroupCur.moveToPosition(i);
							//the GROUPSID is Appended with the item number j in this case
							ed.putInt(GROUPSID + j, GroupCur.getInt(0));
							Log.v(TAG,"Saving Group " + GroupCur.getInt(0));
						}
						i++;
					}
					ed.commit();
					finish();
				}
				else
				{
					//toast with message that nothing is checked.  Ask them to check something.  Do nothing.
					Log.v(TAG, "OnClick Group Save nothing Selected");
					Toast t = Toast.makeText(context, "Please Select at least 1 group to display", Toast.LENGTH_SHORT);
					t.show();
				}
				
			}

			private int getCheckedCount() {
				int count, i;
				count=0;
				i=0;
				if (GroupCur.getCount()>0){
					//loop through checked array to see count of checked
					while (i<adapter.getCount()){
						if (adapter.checkState[i]){
							count++;
						}
						i++;
					}
				}
				Log.v(TAG,"CheckedCount " + count);
				return count;
			}
		});
        
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

	
	private class CustomGroupAdapter extends SimpleCursorAdapter{
		public boolean[] checkState;
		ViewHolder viewholder;

		public CustomGroupAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
			super(context, layout, c, from, to);
			checkState = new boolean[c.getCount()];
	        //Initialize Checked State
	        int i=0;
	        int j=1;
	    	SharedPreferences mPrefs = getSharedPreferences(PREFS_NAME,0);
	    	int SavedGroupCount = mPrefs.getInt(GROUPSCOUNT, 0);
	    	Log.v(TAG,"SavedGroupCount " + SavedGroupCount);
	    	if (SavedGroupCount>0){
		    	//loop through all groups listed
		        while (i<c.getCount()){
		        	c.moveToPosition(i);
		        	j=1;
		        	//loop through all saved groups to see if this one is saved
		        	while (j<=SavedGroupCount){
		        		if(c.getInt(0)==mPrefs.getInt(GROUPSID+j, -1)){
		        			//This should be checked
		        			checkState[i]=true;
		        			Log.v(TAG,"Loading Saved Group " + mPrefs.getInt(GROUPSID+j, -1));
		        		}
		        		j++;
		        		
		        	}
		        	i++;
		        }
	    	}

	    

		
		}
		


		//class for caching the views in a row  
		 private class ViewHolder
		 {
		   CheckBox checkBox;
		 }
	 
	   
	 
		 @Override
		 public View getView(final int position, View convertView, ViewGroup parent) {
		 
		   if(convertView==null)
		    {
		   convertView=inflater.inflate(R.layout.groupselectlayout, null);
		   viewholder=new ViewHolder();
		 
		    //cache the views
		    viewholder.checkBox=(CheckBox) convertView.findViewById(R.id.groupNameChk);
		    
		     //link the cached views to the convertview
		    convertView.setTag(viewholder);
		    
		    //set initial CheckState
		    if (checkState[position]){
		    	viewholder.checkBox.setChecked(true);
		    }
		    }  
		  else
			   viewholder=(ViewHolder) convertView.getTag();
			 
			            
			//set the data to be displayed
		   	GroupCur.moveToPosition(position);
			  viewholder.checkBox.setText(GroupCur.getString(1));
			    
			   //VITAL PART!!! Set the state of the 
			   //CheckBox using the boolean array
			  viewholder.checkBox.setChecked(checkState[position]);
			     
			  
			   //for managing the state of the boolean
			   //array according to the state of the
			   //CheckBox 
			   viewholder.checkBox.setOnClickListener(new View.OnClickListener() {
		     
				   public void onClick(View v) {
				    if(((CheckBox)v).isChecked())
				     checkState[position]=true;
				    else
				     checkState[position]=false;
				      
				    }
			   });
			   
			   //return the view to be displayed
			   return convertView;
		  }
		 
	}
}

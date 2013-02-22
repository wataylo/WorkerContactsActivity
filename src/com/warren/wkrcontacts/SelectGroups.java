package com.warren.wkrcontacts;

import android.app.Activity;
import android.content.Context;
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
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;


public class SelectGroups extends Activity {

	LayoutInflater inflater;
	private final static String TAG = "WorkContactsActivitySelectGroups"; 
	private CustomGroupAdapter adapter;
	private Cursor GroupCur;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
    	inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_groups);
        ListView Groups = (ListView) findViewById(R.id.groupsLv);
        GroupCur = getAllGroups();
        String[] fieldsT = new String[] {
                ContactsContract.Groups.TITLE
        };
        int[] to = new int[] {R.id.groupNameChk};
        adapter = new CustomGroupAdapter(this, R.layout.groupselectlayout, GroupCur, fieldsT, to);
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

	
	private class CustomGroupAdapter extends SimpleCursorAdapter{
		public boolean[] checkState;
		ViewHolder viewholder;

		public CustomGroupAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
			super(context, layout, c, from, to);
			checkState = new boolean[c.getCount()];
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

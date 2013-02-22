package com.warren.wkrcontacts;

import org.acra.ACRA;
import org.acra.annotation.*;

@ReportsCrashes(formKey = "dGVLMk5Eb3k1czI3a2hNN1M1bExacXc6MQ") 
public class MyApplication extends android.app.Application {
	@Override
	  public void onCreate() {
	      super.onCreate();
	      // The following line triggers the initialization of ACRA
	      ACRA.init(this);
	  }
	
	
}

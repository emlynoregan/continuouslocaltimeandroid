package com.emlynoregan.continuouslocaltimeandroid;

import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.app.Activity;
import android.content.Context;
import android.widget.TextView;

public class MainActivity extends Activity 
{
	boolean _haveLongitude;
	double _longitude;
	
	TextView _txtTime;
	TextView _txtStats;
	
	LocationManager _lm;
	
	Timer _timer;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        _txtTime = (TextView)findViewById(R.id.txtTime);
        _txtStats = (TextView)findViewById(R.id.txtStats);

        _haveLongitude = false;

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
            	_haveLongitude = true;
                _longitude = location.getLongitude();
                DoUpdate();
            }

			@Override
			public void onProviderDisabled(String provider) {
                DoUpdate();
			}

			@Override
			public void onProviderEnabled(String provider) {
                DoUpdate();
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
                DoUpdate();
			}
        };
        
        
        
        _lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        
        Location llocation = _lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        
        if (llocation != null)
        {
        	_haveLongitude = true;
        	_longitude = llocation.getLongitude();
        }
        
        Criteria lcriteria = new Criteria();
        lcriteria.setAccuracy(Criteria.NO_REQUIREMENT);
        _lm.requestLocationUpdates(30000, (float) 50.0, lcriteria, locationListener, Looper.myLooper());
        
        DoUpdate();
     
        _timer = new Timer();
        _timer.schedule(new TimerTask() 
        	{
				@Override
				public void run() 
				{
					MainActivity.this.runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							DoUpdate();
						}
					});
				}
			},
        	0, 1000);
    }

    public void DoUpdate()
    {
    	if (_haveLongitude)
    	{
    		String lcontinuousTime = CalcContinuousTime();
    		_txtTime.setText(lcontinuousTime);
    		
    		_txtStats.setText("longitude=" + ((Double)_longitude).toString());
    	}
    	else
    	{
    		_txtTime.setText("Waiting for location");
    		_txtStats.setText("...");
    	}
    }

	private String CalcContinuousTime() 
	{
		long lcontinuousOffsetMilliseconds = Math.round((_longitude / 15) * 60 * 60 * 1000);

		DateFormat df = DateFormat.getTimeInstance();
		df.setTimeZone(TimeZone.getTimeZone("gmt"));
		
		long lfixedTime = (new Date()).getTime() + lcontinuousOffsetMilliseconds;
		String outputTime = df.format(new Date(lfixedTime));
		return outputTime;
	}
}

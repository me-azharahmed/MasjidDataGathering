package com.aze.datagathering;

import java.io.ByteArrayOutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;

import com.parse.ParseAnalytics;
import com.parse.ParseFile;
import com.parse.ParseObject;

public class MainActivity extends Activity implements LocationListener {

	private EditText edt_masjidName;
	private TimePicker tp_namazTime;
	private ImageView img_masjidImage;
	private Button btn_save;
	private Button btn_getCurrentLocation;
	private LocationManager locationManager;
	private String latitude = "";
	private String longitude = "";
	private final int CAMERA = 111;
	private String mMasjidName = "";
	private int mMasjidTimeHour = -1;
	private int mMasjidTimeMinutes = -1;
	private byte[] image;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_old);

		ParseAnalytics.trackAppOpenedInBackground(getIntent());

		edt_masjidName = (EditText) findViewById(R.id.edt_masjidName);
		tp_namazTime = (TimePicker) findViewById(R.id.tp_namazTime);
		img_masjidImage = (ImageView) findViewById(R.id.img_masjidImage);
		btn_save = (Button) findViewById(R.id.btn_save);
		btn_getCurrentLocation = (Button) findViewById(R.id.btn_getCurrentLocation);

		btn_save.setOnClickListener(mClickListener);
		btn_getCurrentLocation.setOnClickListener(mClickListener);
		img_masjidImage.setOnClickListener(mClickListener);

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
	}

	OnClickListener mClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			switch (v.getId()) {

			case R.id.btn_save:
				mMasjidName = edt_masjidName.getText().toString().trim();
				mMasjidTimeHour = tp_namazTime.getCurrentHour();
				mMasjidTimeMinutes = tp_namazTime.getCurrentMinute();

				if (image == null)
					showDialog();
				else
					UploadData();
				break;

			case R.id.img_masjidImage:
				saveFullImage();
				break;

			case R.id.btn_getCurrentLocation:
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, MainActivity.this);
				break;

			default:
				break;
			}

		}
	};

	private void saveFullImage() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(intent, CAMERA);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == CAMERA) {
				// Check if the result includes a thumbnail Bitmap
				if (data != null) {
					Bitmap bmp = (Bitmap) data.getExtras().get("data");
					img_masjidImage.setImageBitmap(bmp);
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
					image = stream.toByteArray();
				}
			}
		}
	}

	private void UploadData() {
		try {
			// Create the ParseFile
			ParseFile file = new ParseFile("masjid.png", image);
			// Upload the image into Parse Cloud
			file.saveInBackground();

			ParseObject mParseObject = new ParseObject("Masjid");
			mParseObject.put("masjid_name", mMasjidName);
			mParseObject.put("lat", latitude.equalsIgnoreCase("") ? 0.0 : latitude);
			mParseObject.put("long", longitude.equalsIgnoreCase("") ? 0.0 : longitude);
			mParseObject.put("time", mMasjidTimeHour + ":" + mMasjidTimeMinutes);
			mParseObject.put("image", file);

			// Create the class and the columns
			mParseObject.saveInBackground();

			// // Create the ParseFile
			// ParseFile file = new ParseFile("ic_launcher.png", image);
			// // Upload the image into Parse Cloud
			// file.saveInBackground();
			//
			// // Create a New Class called "ImageUpload" in Parse
			// ParseObject imgupload = new ParseObject("Masjid");
			//
			// // Create a column named "ImageName" and set the string
			// imgupload.put("masjid_name", "mMasjidName");
			// imgupload.put("lat", "23.00000");
			// imgupload.put("long", "54.00000");
			// imgupload.put("time", mMasjidTimeHour + ":" + mMasjidTimeMinutes);
			//
			// // Create a column named "ImageFile" and insert the image
			// imgupload.put("image", file);
			//
			// // Create the class and the columns
			// imgupload.saveInBackground();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void showDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

		builder.setMessage("Do you want to take snap of Masjid ?");
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				saveFullImage();
			}
		});

		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				UploadData();
			}
		});

		builder.show();
	}

	@Override
	public void onLocationChanged(Location location) {
		latitude = String.valueOf(location.getLatitude());
		longitude = String.valueOf(location.getLongitude());
	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.d("onProviderDisabled", "disable");
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.d("onProviderEnabled", "enable");
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.d("onStatusChanged", "status : " + status);
	}
}
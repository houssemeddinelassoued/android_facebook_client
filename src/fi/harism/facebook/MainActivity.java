package fi.harism.facebook;

import java.io.InputStream;
import java.net.URL;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

public class MainActivity extends Activity {
	
	MyRunnable runnable;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        runnable = new MyRunnable(this);
        
        new Thread() {
        	@Override
        	public void run() {
        		try {
        			Bundle b = new Bundle();
        			b.putString("fields", "id,name,picture");
        			b.putString("access_token", LoginActivity.facebook.getAccessToken());
        			
					String res = LoginActivity.facebook.request("me", b);
					JSONObject jsonObj = new JSONObject(res);
					
					String name = jsonObj.getString("name");
					
					URL url = new URL(jsonObj.getString("picture"));
					InputStream is = url.openStream();
					Bitmap bitmap = BitmapFactory.decodeStream(is);
					
					runnable.setNameBitmap(name, bitmap);
					runOnUiThread(runnable);
					
				} catch (Exception ex) {
				}
        	}
        }.start();
    }
	
    class MyRunnable implements Runnable {
    	MainActivity mainActivity;
    	String name;
    	Bitmap bitmap;
    	MyRunnable(MainActivity mainActivity) {
    		this.mainActivity = mainActivity;
    	}
    	public void setNameBitmap(String name, Bitmap bitmap) {
    		this.name = name;
    		this.bitmap = bitmap;
    	}
    	@Override
    	public void run() {
        	TextView tv = (TextView)findViewById(R.id.main_user_name);
        	tv.setText(name);
        	ImageView iv = (ImageView)findViewById(R.id.main_user_image);
        	iv.setImageBitmap(bitmap);
    	}
    }
    
}

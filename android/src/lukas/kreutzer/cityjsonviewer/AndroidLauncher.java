package lukas.kreutzer.cityjsonviewer;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;


public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Context context = (Context)this;
		Uri uri = getIntent().getParcelableExtra("CityJSONUri");
		Viewer viewer = new Viewer(uri, context);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(viewer, config);
	}
}

package lukas.kreutzer.cityjsonviewer;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;


import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.Context;
import android.content.ContentResolver;

import org.citygml4j.cityjson.CityJSON;

import java.io.IOException;


public class MainMenu extends AppCompatActivity {

    private Uri uri;
    private String uriString;
    TextView showuri;
    Button startViewer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        showuri = (TextView) findViewById(R.id.showuri);
        startViewer = (Button) findViewById(R.id.start);
        startViewer.setVisibility(View.INVISIBLE);
    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        assert data != null;
                        Uri uri1 = data.getData();
                        setUri(uri1);
                        setUriString();
                        showuri.setText(uriString);
                        startViewer.setVisibility(View.VISIBLE);
                    }
                }
            }
    );

    public void openFileDialog(View view) {
        Intent data = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        data.setType("application/json");
        data = Intent.createChooser(data, "Choose a File");
        activityResultLauncher.launch(data);
    }

    public void startViewerApp(View view) {
        Intent intent = new Intent(MainMenu.this, AndroidLauncher.class);
        intent.putExtra("CityJSONUri", uri);
        startActivity(intent);
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUriString() {
        uriString = getUri().toString();
    }
}

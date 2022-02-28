package lukas.kreutzer.cityjsonviewer;

import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import org.citygml4j.cityjson.CityJSON;
import org.citygml4j.cityjson.CityJSONAdapter;
import org.citygml4j.cityjson.CityJSONTypeAdapterFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.content.ContentResolver;



public class CityJSONParser {
    private Uri uri;
    Context context;
    InputStream inputStream;

    public CityJSONParser(Uri uri, Context context) {
        this.uri = uri;
        this.context = context;
    }

    public CityJSON parse() throws IOException {
        JsonReader reader = new JsonReader(
                new InputStreamReader(inputStream =
                        context.getContentResolver().openInputStream(uri)));

        Gson gson = new GsonBuilder().registerTypeAdapterFactory(new CityJSONTypeAdapterFactory()).create();

        CityJSONAdapter adapter = new CityJSONAdapter(gson);

        CityJSON cityJson = adapter.read(reader);
        return cityJson;
    }

}

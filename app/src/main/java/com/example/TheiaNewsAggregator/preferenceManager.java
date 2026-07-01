import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

public class preferenceManager {

    private static final String preferenceNames = "prefs";
    private static final String keys = "keys";

    // Save the HashMap to SharedPreferences
    public static void saveCollections(Context context, HashMap<String, ArrayList<String>> collections) {
        SharedPreferences prefs = context.getSharedPreferences(preferenceNames, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String json = new Gson().toJson(collections);  // Convert to JSON string
        editor.putString(keys, json);
        editor.apply(); // apply() is async and safe; use commit() if you need it done immediately
    }

    // Load the HashMap from SharedPreferences
    public static HashMap<String, ArrayList<String>> loadCollections(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(preferenceNames, Context.MODE_PRIVATE);
        String json = prefs.getString(keys, null);

        if (json == null) {
            return new HashMap<>(); // Return empty map if nothing saved yet
        }

        Type type = new TypeToken<HashMap<String, ArrayList<String>>>() {}.getType();
        return new Gson().fromJson(json, type); // Convert back from JSON string
    }
}
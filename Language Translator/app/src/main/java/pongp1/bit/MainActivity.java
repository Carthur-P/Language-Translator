package pongp1.bit;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    protected TextToSpeech tts;
    protected Spinner translatedSpinner;
    protected EditText originalText;
    protected TextView translatedText;
    protected ImageButton speakButton;
    protected Button translateButton;
    protected LinearLayout screen;
    protected ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupSpinner();
        tts = new TextToSpeech(this, new TextToSpeechInit());
        originalText = findViewById(R.id.editText1);
        translatedText = findViewById(R.id.textView4);
        speakButton = findViewById(R.id.speakButton);
        speakButton.setOnClickListener(new SpeakButtonClickHandler());
        translateButton = findViewById(R.id.button1);
        translateButton.setOnClickListener(new TranslateButtonClickHandler());
        screen = findViewById(R.id.mainScreen);
        screen.setOnClickListener(new ScreenClickHandler());
        progressDialog = new ProgressDialog(MainActivity.this);
    }

    protected void setupSpinner(){
        String[] translatedLanguage = {
                "Select Language:",
                "English",
                "French",
                "Spanish",
                "German",
                "Italian"
        };

        translatedSpinner = findViewById(R.id.spinner2);
        translatedSpinner.setAdapter(new ArrayAdapter(MainActivity.this, R.layout.support_simple_spinner_dropdown_item, translatedLanguage));
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public class TextToSpeechInit implements TextToSpeech.OnInitListener{
        @Override
        public void onInit(int status) {
            if(status == TextToSpeech.SUCCESS){
                int result = tts.setLanguage(Locale.US);
                tts.setSpeechRate((float)0.8);

                if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                    Log.i("Error", "Language not supported or missing data");
                }
            }
        }
    }

    public class ScreenClickHandler implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            hideKeyboard(MainActivity.this);
        }
    }

    public class TranslateButtonClickHandler implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            String languageCode = null;
            String translatedLanguageSelected = translatedSpinner.getSelectedItem().toString();
           if(translatedLanguageSelected.equals("Select Language:")){
               Toast.makeText(MainActivity.this, "Please select a language preference", Toast.LENGTH_LONG).show();
           } else{
               switch(translatedLanguageSelected){
                   case "English":
                       languageCode = "en";
                       break;

                   case "French":
                       languageCode = "fr";
                       break;

                   case "Spanish":
                       languageCode = "es";
                       break;

                   case "German":
                       languageCode = "de";
                       break;

                   case "Italian":
                       languageCode = "it";
                       break;
               }
               getAPIData thread = new getAPIData();
               thread.execute(originalText.getText().toString(), languageCode);
           }
        }
    }

    public class SpeakButtonClickHandler implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            String translatedLanguageSelected = translatedSpinner.getSelectedItem().toString();
            switch(translatedLanguageSelected){
                case "English":
                    tts.setLanguage(Locale.US);
                    break;

                case "French":
                    tts.setLanguage(Locale.FRENCH);
                    break;

                case "Spanish":
                    tts.setLanguage(new Locale("spa", "ESP"));
                    break;

                case "German":
                    tts.setLanguage(Locale.GERMAN);
                    break;

                case "Italian":
                    tts.setLanguage(Locale.ITALIAN);
                    break;

                default:
                    tts.setLanguage(Locale.US);
                    break;
            }
            tts.speak(translatedText.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    class getAPIData extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... words) {
            String word = null;
            try{
                URL urlObject = new URL("https://translate.yandex.net/api/v1.5/tr.json/translate?key=trnsl.1.1.20190528T031652Z.482b8ef596d4fb28.f55592a1268ae66faf5206ef0a1491196acd092a&text=" + words[0] + "&lang=" + words[1] + "&options=1");
                HttpURLConnection connection  = (HttpURLConnection) urlObject.openConnection();
                connection.connect();
                int code = connection.getResponseCode();
                if(code == 200){
                    InputStream is = connection.getInputStream();
                    InputStreamReader ir = new InputStreamReader(is);
                    BufferedReader reader = new BufferedReader(ir);

                    String currentLine;
                    StringBuilder stringBuilder = new StringBuilder();
                    while ((currentLine = reader.readLine()) != null) {
                        stringBuilder.append(currentLine);
                    }

                    JSONObject data = new JSONObject(stringBuilder.toString());
                    JSONArray translatedWords = data.getJSONArray("text");
                    word = translatedWords.getString(0);
                    //Log.i("Translated word: ", word);
                }
            } catch (Exception e){
                Log.i("Error: ", e.getMessage());
            }
            return word;
        }

        @Override
        protected void onPostExecute(String word) {
            translatedText.setText(word);
            progressDialog.cancel();
        }

        @Override
        protected void onPreExecute(){
            progressDialog.setTitle("Please wait");
            progressDialog.setMessage("translating your text ... ");
            progressDialog.setIndeterminate(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
        }
    }
}
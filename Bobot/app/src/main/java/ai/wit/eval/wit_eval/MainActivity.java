package ai.wit.eval.wit_eval;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.ArrayList;

import ai.wit.sdk.IWitListener;
import ai.wit.sdk.Wit;
import ai.wit.sdk.model.WitOutcome;


import java.util.ArrayList;
import java.util.Locale;
import android.util.Log;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends ActionBarActivity implements IWitListener {
    //
    private ImageButton btnSpeak;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private  String text;
    //
    Wit _wit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        super.onCreate(savedInstanceState);
        View view = this.getWindow().getDecorView();
        this.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.mytitle);
        setContentView(R.layout.activity_main);

        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak1);
        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });
    }

    //
    private void promptSpeechInput() {


        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    text=result.get(0);
                    String accessToken = "4XKLM7W7TELEAXKBKNYXOPCZF5AXGMRJ";
                    _wit = new Wit(accessToken, this);
                    _wit.enableContextLocation(getApplicationContext());
                    _wit.captureTextIntent(text);
                   // txtSpeechInput.setText(new_string);
                }
                break;
            }

        }
    }
//

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void toggle(View v) {
        try {
            _wit.toggleListening();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void witDidGraspIntent(ArrayList<WitOutcome> witOutcomes, String messageId, Error error) {
        TextView jsonView = (TextView) findViewById(R.id.jsonView);
        jsonView.setMovementMethod(new ScrollingMovementMethod());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        if (error != null) {
            jsonView.setText(error.getLocalizedMessage());
            return;
        }
        String jsonOutput = gson.toJson(witOutcomes);

        Log.d("Raw Output:-",jsonOutput.toString());

        String intent="",amount="",sub="";
        try
        {
            JSONArray raw_output = new JSONArray(jsonOutput);
            JSONObject obj = raw_output.getJSONObject(0);
            intent = "Intent = " + obj.get("intent");
            Log.d("Intent:-",intent);
            if(obj.get("intent")=="get_grocery") {
                amount = "Amount = " + obj.getJSONObject("entities").getJSONArray("number").getJSONObject(0).get("value");
                sub = "Message = " + obj.getJSONObject("entities").getJSONArray("message_subject").getJSONObject(0).get("value");
            }
            else
            {
                amount = "Amount = " + obj.getJSONObject("entities").getJSONArray("amount_of_money").getJSONObject(0).get("value");
                sub = "Message = " + obj.getJSONObject("entities").getJSONArray("phone_number").getJSONObject(0).get("value");
            }
                jsonOutput = intent + "\n" + amount + "\n" + sub;
        }
        catch(JSONException e)
        {
            jsonOutput = "Intent not Recognised" + intent + amount + sub;
            e.printStackTrace();
        }
        jsonView.setText(jsonOutput);
        ((TextView) findViewById(R.id.txtText)).setText("Done!");
    }

    @Override
    public void witDidStartListening() {
        ((TextView) findViewById(R.id.txtText)).setText("Witting...");
    }

    @Override
    public void witDidStopListening() {
        ((TextView) findViewById(R.id.txtText)).setText("Processing...");
    }

    @Override
    public void witActivityDetectorStarted() {
        ((TextView) findViewById(R.id.txtText)).setText("Listening");
    }

    @Override
    public String witGenerateMessageId() {
        return null;
    }

    public static class PlaceholderFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            return inflater.inflate(R.layout.wit_button, container, false);
        }
    }

}

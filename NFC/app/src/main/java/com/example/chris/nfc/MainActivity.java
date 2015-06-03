package com.example.chris.nfc;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


public class MainActivity extends Activity {

    private TextView mTextView;
    private Tag mTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.textview);
        mTextView.setText("");
        Intent intent = getIntent();
        mTag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (mTag != null) {
            showTechAndId();
            handleIntent(intent);
        }
    }

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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void handleIntent(Intent intent) {
        switch (intent.getAction()) {
            case NfcAdapter.ACTION_NDEF_DISCOVERED:
                handleNDEF(intent);
                break;
            case NfcAdapter.ACTION_TECH_DISCOVERED:
                break;
            case NfcAdapter.ACTION_TAG_DISCOVERED:
                break;
        }
    }

    private void handleNDEF(Intent intent) {
        TextView data_title_view = (TextView) findViewById(R.id.data_title);
        data_title_view.setText("NDEF Data");
        NdefMessage msgs[];
        // from https://developer.android.com/guide/topics/connectivity/nfc/nfc.html#obtain-info
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (rawMsgs != null) {
            msgs = new NdefMessage[rawMsgs.length];
            for (int i = 0; i < rawMsgs.length; i++) {
                msgs[i] = (NdefMessage) rawMsgs[i];
            }

            for (int i = 0; i < msgs.length; i++) {
                for (int j = 0; j < msgs[i].getRecords().length; j++) {
                    NdefRecord record = msgs[i].getRecords()[j];
                    String mimeType = record.toMimeType();
                    mTextView.append("Mime type: " + mimeType + "\n\n");
                    if (mimeType.equals("text/plain")) {
                        try {
                            String payload = new String(record.getPayload());
                            mTextView.append(payload);
                        } catch (Exception e) {
                            mTextView.append("Could not read text.");
                        }
                    } else {
                        mTextView.append("Content type not supported.");
                    }
                    mTextView.append("\n\n");
                }
            }
        }
    }

    private void showTechAndId() {
        TextView id_view = (TextView) findViewById(R.id.id);
        id_view.setText(toHexString(mTag.getId()));
        TextView tech_view = (TextView) findViewById(R.id.tech);
        tech_view.setText("");
        String techs[] = mTag.getTechList();
        for (String tech : techs) {
            tech_view.append(tech + "\n");
        }
    }

    public void showRawData(View view) {
        mTextView.setText(mTag.toString());
    }

    private String toHexString(byte[] bytes) {
        String string = "";
        System.out.println(bytes.length);
        for (byte b : bytes) {
            string += String.format("%02x", b);
        }
        return string;
    }
}

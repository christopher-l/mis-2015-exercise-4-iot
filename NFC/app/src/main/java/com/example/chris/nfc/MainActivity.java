package com.example.chris.nfc;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.method.ScrollingMovementMethod;
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
        mTextView.setMovementMethod(new ScrollingMovementMethod());
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
        Ndef ndef = Ndef.get(mTag);
        if (ndef != null) {
            handleNDEF(ndef);
        }
        /*
        switch (intent.getAction()) {
            case NfcAdapter.ACTION_NDEF_DISCOVERED:
                handleNDEF(intent);
                break;
            case NfcAdapter.ACTION_TECH_DISCOVERED:
                break;
            case NfcAdapter.ACTION_TAG_DISCOVERED:
                break;
        }
        */
    }

    private void print_payload(byte payload[]) {
        char status = (char) payload[0];
        boolean isUtf8 = (status & 0x80) != 0;
        int langLength = status & 0x7F;
        if (langLength > 0) {
            byte langBytes[] = new byte[langLength];
            System.arraycopy(payload, 1, langBytes, 0, langLength);
            mTextView.append("Language: " + new String(langBytes) + "\n");
        }
        int textLength = payload.length - langLength - 1;
        byte textBytes[] = new byte[textLength];
        System.arraycopy(payload, 1 + langLength, textBytes, 0, textLength);
        mTextView.append("\n" + new String(textBytes));
    }

    private void handleNDEF(Ndef ndef) {
        TextView data_title_view = (TextView) findViewById(R.id.data_title);
        data_title_view.setText("NDEF Data");
        try {

            ndef.connect();
            NdefMessage message = ndef.getNdefMessage();
            mTextView.setText("");

            for (int i = 0; i < message.getRecords().length; i++) {
                NdefRecord record = message.getRecords()[i];
                String mimeType = record.toMimeType();
                mTextView.append("Mime type: " + mimeType + "\n");
                if (mimeType != null) {
                    try {
                        print_payload(record.getPayload());
                    } catch (Exception e) {
                        mTextView.append("\nCould not read content.");
                    }

                    mTextView.append("\n\n");
                }
                mTextView.append(record.toUri().toString());
                mTextView.append("\n\n");
            }

        } catch (Exception e) {
        } finally {
            try {
                ndef.close();
            } catch (Exception e) {
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
        TextView data_title_view = (TextView) findViewById(R.id.data_title);
        data_title_view.setText("Raw Data");
        mTextView.setText("");

        MifareUltralight mifare = MifareUltralight.get(mTag);
        if (mifare != null) {

            int type = mifare.getType();
            int length = type == mifare.TYPE_ULTRALIGHT_C ? 0x2C : 0x10;

            try {

                mifare.connect();

                for (int i = 0; i < length; i+=4) {
                    byte[] b = mifare.readPages(i);
                    mTextView.append(new String(b));
                }

                mTextView.append("\n\n");

                for (int i = 0; i < length; i+=4) {
                    byte[] b = mifare.readPages(i);
                    mTextView.append(toHexString(b));
                }

            } catch (Exception e) {
                mTextView.setText("Could not obtain raw data. Did you remove the NFC tag?");
            } finally {
                try {
                    mifare.close();
                } catch (Exception e) {
                }
            }
        } else {
            mTextView.setText("Cannot obtain raw data from tags that don't support Mifare Ultralight.");
        }
    }

    private String toHexString(byte[] bytes) {
        String string = "";
        for (byte b : bytes) {
            string += String.format("%02x", b);
        }
        return string;
    }
}

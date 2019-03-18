package com.example.nfcreaderapp;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nfcreaderapp.comm.DumpTagData;
import com.example.nfcreaderapp.conn.UbicastHttpConn;
import com.example.nfcreaderapp.parser.NdefMessageParser;
import com.example.nfcreaderapp.record.ParsedNdefRecord;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    NfcAdapter nfcAdapter = null;
    PendingIntent pendingIntent = null;
    TextView text = null;

    UbicastHttpConn postConn = null;
    UbicastHttpConn getConn = null;
    public static Handler handler; //宣告成static讓service可以直接使用
    private Button postBtn;
    private Button getBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text = (TextView) findViewById(R.id.text);

        postBtn = (Button) findViewById(R.id.btnPost);
        getBtn = (Button) findViewById(R.id.btnGet);

        //讓多個Button共用一個Listener，在Listener中再去設定各按鈕要做的事
        postBtn.setOnClickListener(this);
        getBtn.setOnClickListener(this);


        //接收service傳出Post的到的回傳訊息，並透過Toast顯示出來
        handler = new Handler(){
            public void handleMessage(Message msg){
                switch (msg.what){
                    case 1: //POST
                        String ss = (String)msg.obj;
                        Toast.makeText(MainActivity.this, "POST:" + ss,Toast.LENGTH_LONG).show();
                        break;
                    case 2://GET
                        String as = (String)msg.obj;
                        Toast.makeText(MainActivity.this, "GET:" + as,Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };

        /*
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            Toast.makeText(this, "No NFC", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, this.getClass())
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
       */
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (nfcAdapter != null) {
            if (!nfcAdapter.isEnabled())
                showWirelessSettings();

            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }

    private void showWirelessSettings() {
        Toast.makeText(this, "You need to enable NFC", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        startActivity(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        resolveIntent(intent);
    }

    private void resolveIntent(Intent intent) {
        String action = intent.getAction();

        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs;

            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];

                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }

            } else {
                byte[] empty = new byte[0];
                byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
                Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                byte[] payload = DumpTagData.dumpTagData(tag).getBytes();
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, id, payload);
                NdefMessage msg = new NdefMessage(new NdefRecord[] {record});
                msgs = new NdefMessage[] {msg};
            }

            displayMsgs(msgs);
        }
    }

    private void displayMsgs(NdefMessage[] msgs) {
        if (msgs == null || msgs.length == 0)
            return;

        StringBuilder builder = new StringBuilder();
        List<ParsedNdefRecord> records = NdefMessageParser.parse(msgs[0]);
        final int size = records.size();

        for (int i = 0; i < size; i++) {
            ParsedNdefRecord record = records.get(i);
            String str = record.str();
            builder.append(str).append("\n");
        }

        text.setText(builder.toString());
    }

    @Override
    public void onClick(View v) {
        StringBuffer body = new StringBuffer("");
        Toast.makeText(this, "onClick", Toast.LENGTH_LONG).show();
        switch(v.getId()){
            case R.id.btnPost:
                Toast.makeText(this, "Post Click", Toast.LENGTH_SHORT).show();
                body.append("action=bcv"); //bcv or preserveCodeCheck
                body.append("&appId=EINV9201710073426");
                body.append("&barCode=kkkk");
                //body.append("&pCode=919");
                body.append("&TxID=kkkXSAFAFEFEQFAEFAEFEFEAE1234124");
                body.append("&version=1.0");
                postConn = new UbicastHttpConn();
                postConn.request(
                        UbicastHttpConn.Method.POST.value(),
                        "https://wwwtest-vc.einvoice.nat.gov.tw/BIZAPIVAN/biz",
                        body.toString(),
                        UbicastHttpConn.ContentType.OTHER.value(),
                        this);
                break;
            case R.id.btnGet:
                Toast.makeText(this, "Get Click", Toast.LENGTH_SHORT).show();
                body.append("page=1");
                body.append("&perPageRows=10");
                postConn = new UbicastHttpConn();
                postConn.request(
                        UbicastHttpConn.Method.GET.value(),
                        "http://192.168.100.159/api/display/bestMembers/TW?" ,
                        body.toString(),
                        UbicastHttpConn.ContentType.JSON.value(),
                        this);
                break;
            default:
                break;
        }
    }
}

package com.example.fatih.wirelesscomchat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ChatActivity extends AppCompatActivity {

    TextView receivedText;
    EditText yourMessage;
    Button send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        receivedText = (TextView) findViewById(R.id.text_incoming);
        yourMessage = (EditText) findViewById(R.id.text_send);
        send = (Button) findViewById(R.id.btn_send);

    }
}

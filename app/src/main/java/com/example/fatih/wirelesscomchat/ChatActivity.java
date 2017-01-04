package com.example.fatih.wirelesscomchat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChatActivity extends AppCompatActivity {

    private ServerSocket serverSocket;
    Handler updateConversationHandler;
    Thread serverThread = null;
    public static final int SERVERPORT = 6000;
    private static String SERVER_IP;
    private Socket socket;


    TextView receivedText;
    EditText yourMessage;
    Button send;
    boolean owner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intent = getIntent();

        owner = intent.getBooleanExtra("Owner?", false);
        SERVER_IP = intent.getStringExtra("Owner Address");
        receivedText = (TextView) findViewById(R.id.text_incoming);
        yourMessage = (EditText) findViewById(R.id.text_send);
        send = (Button) findViewById(R.id.btn_send);

        updateConversationHandler = new Handler();

        if (owner) {
            this.serverThread = new Thread(new ServerThread());
            this.serverThread.start();
        } else {
            new Thread(new ClientThread()).start();
        }

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String str = yourMessage.getText().toString();
                    PrintWriter out = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream()
                            )), true);
                    out.println(str);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ServerThread implements Runnable {
        @Override
        public void run() {
            Socket socket = null;
            try {
                serverSocket = new ServerSocket(SERVERPORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    socket = serverSocket.accept();
                    CommunicationThread commThread = new CommunicationThread(socket);
                    new Thread(commThread).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class CommunicationThread implements Runnable {
        private Socket clientSocket;
        private BufferedReader input;

        public CommunicationThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
            try {
                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String read = input.readLine();
                    updateConversationHandler.post(new UpdateUIThread(read));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class UpdateUIThread implements Runnable {
        private String msg;

        public UpdateUIThread(String str) {
            this.msg = str;
        }

        @Override
        public void run() {
            receivedText.setText(receivedText.getText().toString() + "Gelen Mesaj: " + msg + "\n");
        }
    }

    class ClientThread implements Runnable {
        @Override
        public void run() {
            try {
                InetAddress serverAddress = InetAddress.getByName(SERVER_IP);
                socket = new Socket(serverAddress, SERVERPORT);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

package com.temas.selectos.bluetootharduino;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    private final int handlerState=0;
    private String direccion;
    private BluetoothAdapter btAdaptador;
    private BluetoothSocket btSocket=null;
    private static final UUID btUUID=UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    //private static final UUID btUUID=UUID.randomUUID();
    private Handler hBTcomunica;
    private Switch swPrendeApaga;
    private ComunicaThread comunicaThreadBT;
    private TextView txtvDato;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        swPrendeApaga= findViewById(R.id.swPrendeApaga);
        btAdaptador= BluetoothAdapter.getDefaultAdapter();
        txtvDato= findViewById(R.id.txtvDato);

        hBTcomunica = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                Log.d("Mensaje","Entrando al manejador");
                if( msg.what == handlerState)
                {
                    Log.d("Menwhat","Coinciden los mensajes");
                    char Caracter= (char)msg.obj;
                    if(Caracter == 'C'){
                        txtvDato.setText("Se está enviando una C");
                    }
                    if( Caracter == 'D'){
                        txtvDato.setText("Se está enviando una D");
                    }
                }
            }

        };



        swPrendeApaga.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    comunicaThreadBT.write("A");
                    Toast.makeText(getApplicationContext(),"Prendido",Toast.LENGTH_SHORT).show();
                }
                else {
                    comunicaThreadBT.write("B");
                    Toast.makeText(getApplicationContext(),"Apagado",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
        Bundle bundleextras= getIntent().getExtras();
        direccion = bundleextras.getString(getResources().getString(R.string.str_direccion_dispositivo));
        BluetoothDevice dispositivo=btAdaptador.getRemoteDevice(direccion);

        try {

            btSocket= dispositivo.createInsecureRfcommSocketToServiceRecord(btUUID);
            Toast.makeText(getApplicationContext(),"Direccion:" +direccion,Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"Falló la creación de soccket",Toast.LENGTH_SHORT).show();
        }
        try {
            btSocket.connect();

        } catch (IOException e) {
            Log.d("ErrorContect","Error de conexión: " +e.toString());
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"Error al conectar con dispositivo" ,Toast.LENGTH_LONG).show();

            try {
                btSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        comunicaThreadBT= new ComunicaThread(btSocket);
       // comunicaThreadBT.run();
        comunicaThreadBT.start();



    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            btSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Comunicación con Bluetooth por medio del protocolo serie
    private class ComunicaThread extends Thread{
        private  InputStream minputStream;
        private  OutputStream moutputStream;


        private ComunicaThread(BluetoothSocket socket) {
            InputStream tmpIn= null;
            OutputStream tmpOut= null;

            try {

                tmpIn= socket.getInputStream();
                tmpOut= socket.getOutputStream();
            }
            catch (IOException e)
            {
                Log.d("eSocket","Error: " +e.toString());
            }
            this.minputStream= tmpIn;
            this.moutputStream= tmpOut;
        }

        public void run(){

            byte[] buffer= new byte[1];
            while (true){
                Log.d("Run","Método que lee la entrada del puerto serie "  );
                try {
                    minputStream.read(buffer);
                    char ch=(char)buffer[0];
                    Log.d("Run","Método que lee la entrada del puerto serie: " +ch );

                    hBTcomunica.obtainMessage(handlerState,ch).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }

        }

        public void write(String input)  {

            try {
                moutputStream.write(input.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
               // finish();
            }
        }


    }

}

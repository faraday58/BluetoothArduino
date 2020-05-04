package com.temas.selectos.bluetootharduino;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {
    private String direccion;
    private BluetoothAdapter btAdaptador;
    private BluetoothSocket btSocket=null;
    //private static final UUID btUUID=UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final UUID btUUID=UUID.randomUUID();
    final int handlerState=0;
    private Handler hBTcomunica;
    private Switch swPrendeApaga;
    private ComunicaThread comunicaThreadBT;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        swPrendeApaga= findViewById(R.id.swPrendeApaga);
        btAdaptador= BluetoothAdapter.getDefaultAdapter();

        swPrendeApaga.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    comunicaThreadBT.write("A");
                    Toast.makeText(getApplicationContext(),"Prendido",Toast.LENGTH_SHORT).show();
                }
                else {
                    comunicaThreadBT.write("A");
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
            Toast.makeText(getApplicationContext(),"Error al conectar con dispositivo",Toast.LENGTH_LONG).show();

            try {
                btSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        comunicaThreadBT= new ComunicaThread(btSocket);
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

    //Comunicación con Blutooth por medio del protocolo serie
    private class ComunicaThread extends Thread{
        private  InputStream minputStream;
        private  OutputStream moutputStream;


        private ComunicaThread(BluetoothSocket socket) {
            try {
                this.minputStream= socket.getInputStream();
                this.moutputStream= socket.getOutputStream();
            }
            catch (IOException e)
            {
                Log.d("eSocket","Error: " +e.toString());
            }
        }

        public void run(){
            byte[] buffer= new byte[256];
            int bytes=0;
            while (true){
                try {
                    bytes=minputStream.read(buffer);
                    String LMensaje= new String(buffer,0,bytes);
                    hBTcomunica.obtainMessage(handlerState,bytes,-1,LMensaje).sendToTarget();
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

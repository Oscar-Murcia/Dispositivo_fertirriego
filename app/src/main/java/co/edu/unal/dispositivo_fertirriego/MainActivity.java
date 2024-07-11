package co.edu.unal.dispositivo_fertirriego;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import co.edu.unal.dispositivo_fertirriego.R;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

import co.edu.unal.dispositivo_fertirriego.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    //private Board1_Service mService;

    TextView textMens;

    EditText editText;

    ImageButton btn_envio;

    Handler bluetoothIn;
    final int handlerState = 0;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder dataStringIN = new StringBuilder();
    private ConnectedThread MyConexionBT;
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String address = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        btn_envio = findViewById(R.id.btnPlay);
        editText = findViewById(R.id.editTextText);
        textMens = findViewById(R.id.textMens);

        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //replaceFragment(new Inicio_Fragment());

        binding.navigationView.setOnItemSelectedListener(item -> {


                if (item.getItemId() == R.id.btn_inicio) {
                    replaceFragment(new Inicio_Fragment());
                } else if (item.getItemId() == R.id.btn_bt_conexion) {
                    //replaceFragment(new Bt_Fragment());
                    Intent intent = new Intent(MainActivity.this, DispositivosVinculados.class);
                    startActivity(intent);
                    //setContentView(R.layout.activity_dispositivos_vinculados);
                } else if (item.getItemId() == R.id.btn_firebase) {
                    replaceFragment(new Firebase_Fragment());
                }

            return true;
        });

        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {
                    //interaccion datos de ingreso
                    String Dato = msg.obj.toString();
                    byte[] b = Dato.getBytes();
                    String buff2 = null;
                    try {
                        buff2 = new String(StandardCharsets.UTF_8.encode(Arrays.toString(b)).array());
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            //Base64.getDecoder().decode(buff2.getBytes());
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("error:", e.getMessage());
                    }

                    buff2 = buff2.substring(1);
                    int buff4 = buff2.length();
                    buff4 = buff4 - 1;
                    buff2 = buff2.substring(0, buff4);
                    //buff2 = buff2.substring(buff2.length()-1);
                    //String buff = String.valueOf(Dato);
                    //buff = String.valueOf(textMens.getText())+String.valueOf(Dato);
                    textMens.setText(textMens.getText().toString()+buff2);

                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        VerificarEstadoBT();







    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= 31) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 100);
                return btSocket;
            }

            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //return btSocket;
        }
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    @Override
    public void onResume() {
        super.onResume();

        Intent intent = getIntent();
        address = intent.getStringExtra(DispositivosVinculados.EXTRA_DEVICE_ADDRESS);
        //seteo direccion MAC
        BluetoothDevice device = btAdapter.getRemoteDevice(address);
        //BluetoothSocket tmp = null;

        try {
            btSocket = createBluetoothSocket(device);
            Toast.makeText(getBaseContext(), "Socket creado", Toast.LENGTH_LONG).show();
            //tmp = device.createRfcommSocketToServiceRecord(device);

        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "laa creacion del socket fallo", Toast.LENGTH_LONG).show();
        }


        //establecemos la conexión con el socket BT
        try {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= 31) {
                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 100);
                    return;
                }

                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                //return;
            }
            btSocket.connect();
            boolean prue = btSocket.isConnected();
            if (prue==true){
                Toast.makeText(getBaseContext(), "conectado", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(getBaseContext(), "conexion fallo", Toast.LENGTH_LONG).show();

            }
        } catch (IOException e) {
            try {
                Toast.makeText(getBaseContext(), "conexion fallo", Toast.LENGTH_LONG).show();
                btSocket.close();
            } catch (IOException e2) {
            }
        }
        MyConexionBT = new ConnectedThread(btSocket);
        MyConexionBT.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        //permite que no se deje abierto el socket al salir de la app
        try {
            btSocket.close();
        } catch (IOException e2) {
        }
    }

    //Comprueba que el dispositivo BT esta disponible y solicita que se active si esta desactivado

    private void VerificarEstadoBT() {

        if (btAdapter == null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta BT", Toast.LENGTH_SHORT).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {


                    if (Build.VERSION.SDK_INT >= 31) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 100);
                        return;
                    }
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    //return;
                }
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    //crea la clase del evento de conexion
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;

        }

        public void run() {
            byte[] byte_in = new byte[1];
            //modo escucha
            while (true) {
                try {
                    mmInStream.read(byte_in);
                    char ch = (char) byte_in[0];
                    bluetoothIn.obtainMessage(handlerState, ch).sendToTarget();
                } catch (IOException e) {
                    break;
                }

            }
        }

        //envio de trama
        public void write(String input) {
            try {
                mmOutStream.write(input.getBytes());
            } catch (IOException e) {
                //si no se envian datos se cierra la conexión
                Toast.makeText(getBaseContext(), "La conexion fallo", Toast.LENGTH_LONG).show();
                finish();
            }
        }

    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.commit();
    }
}
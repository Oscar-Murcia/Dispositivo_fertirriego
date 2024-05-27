package co.edu.unal.dispositivo_fertirriego;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import co.edu.unal.dispositivo_fertirriego.R;
import android.os.Bundle;

import co.edu.unal.dispositivo_fertirriego.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new Inicio_Fragment());

        binding.navigationView.setOnItemSelectedListener(item -> {


                if (item.getItemId() == R.id.btn_inicio) {
                    replaceFragment(new Inicio_Fragment());
                } else if (item.getItemId() == R.id.btn_bt_conexion) {
                    replaceFragment(new Bt_Fragment());
                } else if (item.getItemId() == R.id.btn_firebase) {
                    replaceFragment(new Firebase_Fragment());
                }

            return true;
        });
    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.commit();
    }
}
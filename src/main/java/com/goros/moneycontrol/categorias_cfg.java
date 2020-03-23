package com.goros.moneycontrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class categorias_cfg extends AppCompatActivity {

    private ArrayList<String> categorias;
    private ListView listCategorias;
    private EditText etNuevaCat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categorias_cfg);
        listCategorias = (ListView)findViewById(R.id.listCategorias);
        etNuevaCat = (EditText)findViewById(R.id.etNuevaCat);
        categorias = new ArrayList<>();
        llenarListaCategorias();

        listCategorias.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder dialogo1 = new AlertDialog.Builder(categorias_cfg.this);
                dialogo1.setTitle(getString(R.string.atencion));
                dialogo1.setMessage(getString(R.string.eliminarCategoria));
                dialogo1.setCancelable(false);
                dialogo1.setPositiveButton(getString(R.string.confirmar), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogo1, int id) {
                        eliminarCategoria(position);
                    }
                });

                dialogo1.setNegativeButton(getString(R.string.cancelar), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogo1, int id) {
                    }
                });
                dialogo1.show();

                return false;
            }
        });

    }

    public void llenarListaCategorias() {
       // String[] archivos = fileList();
        categorias.clear();
        try {
               InputStreamReader archivo = new InputStreamReader(openFileInput("categorias.txt"));
               BufferedReader br = new BufferedReader(archivo);
               String linea = br.readLine();
               while (linea != null) {
                   categorias.add(linea);
                   linea = br.readLine();
               }
               br.close();
               archivo.close();
        }catch (IOException e) {}

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,categorias);
        listCategorias.setAdapter(adapter);
    }

    public void onActionGrabarCat(View view){
        String nuevaCategoria = etNuevaCat.getText().toString();
        if(nuevaCategoria.isEmpty()){
            Toast.makeText(this,getString(R.string.toastCategoriaVacia), Toast.LENGTH_SHORT).show();
            return;
        }
        String categoriasString = "";
        categorias.add(nuevaCategoria);
        try {
            OutputStreamWriter archivo = new OutputStreamWriter(openFileOutput(
                    "categorias.txt", Activity.MODE_PRIVATE));

            for (int i = 0; i < categorias.size(); i++) {
                categoriasString += categorias.get(i) + "\n";
            }
            archivo.write(categoriasString);
            archivo.flush();
            archivo.close();
        } catch (IOException e) {}
        etNuevaCat.setText("");
        llenarListaCategorias();

    }

    public void eliminarCategoria(int i){

        //lleno el vector con las categorias, y luego elimino la posición pasada en el argumento
        categorias.clear();
        try {
            InputStreamReader archivo = new InputStreamReader(openFileInput("categorias.txt"));
            BufferedReader br = new BufferedReader(archivo);
            String linea = br.readLine();
            while (linea != null) {
                categorias.add(linea);
                linea = br.readLine();
            }
            br.close();
            archivo.close();
        }catch (IOException e) {}
        categorias.remove(i);

        //reescribo el archivo de texto con el nuevo vector resultante
        String categoriasString = "";
        try {
            OutputStreamWriter archivo = new OutputStreamWriter(openFileOutput(
                    "categorias.txt", Activity.MODE_PRIVATE));

            for (int j = 0; j < categorias.size(); j++) {
                categoriasString += categorias.get(j) + "\n";
            }
            archivo.write(categoriasString);
            archivo.flush();
            archivo.close();
        } catch (IOException e) {}
        etNuevaCat.setText("");
        llenarListaCategorias();
    }
}

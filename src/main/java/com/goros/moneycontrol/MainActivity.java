package com.goros.moneycontrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText et_importe;
    private Spinner spinnerCat;
    private ListView listGastos; //List view personalizado -> https://www.youtube.com/watch?v=V7tPLlnA5Ms
    private TextView tvMesActual, tvMesAnterior;
    private AdaptadorList adaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et_importe = (EditText)findViewById(R.id.txtImporte);
        spinnerCat = (Spinner)findViewById(R.id.spinnerCat);
        listGastos = (ListView)findViewById(R.id.listGastos);
        tvMesActual = (TextView)findViewById(R.id.tvMesActual);
        tvMesAnterior = (TextView)findViewById(R.id.tvMesAnterior);

        llenarSpinnerCategorias();
        llenarListaGastos();

        listGastos.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder dialogo1 = new AlertDialog.Builder(MainActivity.this);
                dialogo1.setTitle(getString(R.string.atencion));
                dialogo1.setMessage(getString(R.string.eliminarGasto));
                dialogo1.setCancelable(false);
                dialogo1.setPositiveButton(getString(R.string.confirmar), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogo1, int id) {
                        eliminarGasto(position);
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

    @Override
    public void onResume(){
        super.onResume();
        llenarSpinnerCategorias();
    }

    public void onActionCfgCategoria(View view){
        Intent i = new Intent(this, categorias_cfg.class);
        startActivity(i);
    }

    public void onActionListaDetalle(View view){
        Intent i = new Intent(this, lista_detalle.class);
        startActivity(i);
    }

    public void eliminarGasto(int i){
        Item gasto = (Item) (listGastos.getItemAtPosition(i));
        String fecha = gasto.getFecha().substring(1,15);
        String categoria = gasto.getCategoria();
        String importe = gasto.getImporte().substring(1);

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this,"administracion",null,1);
        SQLiteDatabase baseDeDatos = admin.getWritableDatabase();
        baseDeDatos.delete("gastos", "fecha = '"+fecha+"' and categoria = '"+categoria+"' and importe = "+importe, null);
        llenarListaGastos();
    }

    public void onActionGrabar(View view) {
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this,"administracion", null, 1);
        SQLiteDatabase baseDeDatos = admin.getWritableDatabase();

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yy HH:mm");
        String fechaActualString = dateFormat.format(new Date());
        String categoria = spinnerCat.getSelectedItem().toString();
        String importe = et_importe.getText().toString();
        DecimalFormat dosDecimales = new DecimalFormat("0.00");

        if(importe.isEmpty()){
            Toast.makeText(this, getString(R.string.toastImporteVacio), Toast.LENGTH_SHORT).show();
        }else{
            Float importeFloat = Float.parseFloat(importe);
            importe = dosDecimales.format(importeFloat);
            ContentValues registro = new ContentValues();

            registro.put("fecha",fechaActualString);
            registro.put("importe",importe);
            registro.put("categoria", categoria);

            baseDeDatos.insert("gastos",null,registro);
            baseDeDatos.close();

            et_importe.setText("");
            Toast.makeText(this, getString(R.string.toastGrabado), Toast.LENGTH_SHORT).show();
            llenarListaGastos();
        }
    }

    public void llenarSpinnerCategorias(){
        String[] archivos = fileList();
        ArrayList<String> categorias = new ArrayList<>();
        if (existeArchivo(archivos, "categorias.txt")) {
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
            } catch (IOException e) {}
        }else{
            try {
                OutputStreamWriter archivo = new OutputStreamWriter(openFileOutput(
                        "categorias.txt", Activity.MODE_PRIVATE));
                archivo.write("Normales");
                archivo.flush();
                archivo.close();
                categorias.add("Normales");
            } catch (IOException e) {}
        }

        ArrayAdapter <String>adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, categorias);
        spinnerCat.setAdapter(adapter);
    }

    public boolean existeArchivo(String[] archivos, String archbusca){
        for (int i = 0; i < archivos.length; i++) {
            if(archbusca.equals(archivos[i])) return true;
        }
        return false;
    }

    public void llenarListaGastos(){
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this,"administracion", null, 1);
        SQLiteDatabase baseDeDatos = admin.getWritableDatabase();

        Cursor filas = baseDeDatos.rawQuery("select fecha, importe, categoria from gastos", null);

        ArrayList<Item> gastos = new ArrayList<>();

        Float importeMesActual = 0f;
        Float importeMesAnterior = 0f;
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yy");
        String fechaActualString = dateFormat.format(new Date());
        int mesActual = Integer.parseInt(fechaActualString.substring(3,5));
        int anioActual = Integer.parseInt(fechaActualString.substring(6));
        int mesAnterior = 0;
        int anioAnterior = 0;

        if(mesActual == 1){
            mesAnterior = 12;
            anioAnterior = anioActual - 1;
        }else{
            mesAnterior = mesActual - 1;
            anioAnterior = anioActual;
        }

        for (int i = filas.getCount()-1; i >= 0; i--) {
            filas.moveToPosition(i);
            String tmp_fecha = filas.getString(0);
            String tmp_importe = filas.getString(1);
            String tmp_categoria = filas.getString(2);


            int tmp_mes = Integer.parseInt(tmp_fecha.substring(3,5));
            int tmp_anio = Integer.parseInt(tmp_fecha.substring(6,8));

            if(tmp_mes == mesActual && tmp_anio == anioActual){
                importeMesActual += Float.parseFloat(tmp_importe);
            }

            if(tmp_mes == mesAnterior && tmp_anio == anioAnterior){
                importeMesAnterior += Float.parseFloat(tmp_importe);
            }
            Item itemGasto = new Item("["+tmp_fecha+"]",tmp_categoria,"$"+tmp_importe);
            gastos.add(itemGasto);
        }
        baseDeDatos.close();

        DecimalFormat dosDecimales = new DecimalFormat("0.00");

        tvMesActual.setText("$"+dosDecimales.format(importeMesActual));
        tvMesAnterior.setText("$"+dosDecimales.format(importeMesAnterior));
        adaptador = new AdaptadorList(this, gastos);
        listGastos.setAdapter(adaptador);
    }
}

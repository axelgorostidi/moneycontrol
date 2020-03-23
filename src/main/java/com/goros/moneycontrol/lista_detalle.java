package com.goros.moneycontrol;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class lista_detalle extends AppCompatActivity {

    private EditText etImporteMax, etImporteMin, etFechaInicial, etFechaFinal;
    private Spinner spinnerCat;
    private ListView listGastos;
    private AdaptadorList adaptador;
    private TextView tvTotal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_detalle);

        etImporteMax = (EditText)findViewById(R.id.etImporteMax);
        etImporteMin = (EditText)findViewById(R.id.etImporteMin);
        spinnerCat = (Spinner)findViewById(R.id.spinnerCat);
        listGastos = (ListView)findViewById(R.id.listGastos);
        etFechaInicial = (EditText)findViewById(R.id.etFechaInicial);
        etFechaFinal = (EditText)findViewById(R.id.etFechaFinal);
        tvTotal = (TextView)findViewById(R.id.tvTotal);

        llenarSpinnerCat();
        llenarListaGastos();
    }

    public void  llenarSpinnerCat(){
        ArrayList<String> categorias = new ArrayList<>();
        categorias.add("");
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

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, categorias);
        spinnerCat.setAdapter(adapter);
    }

    public void aplicarFiltro(View view){
        llenarListaGastos();
    }

    public void limpiar(View view){
        etImporteMin.setText("");
        etImporteMax.setText("");
        spinnerCat.setSelection(0);
        etFechaFinal.setText("");
        etFechaInicial.setText("");
        llenarListaGastos();
    }

    public void llenarListaGastos(){
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this,"administracion", null, 1);
        SQLiteDatabase baseDeDatos = admin.getWritableDatabase();
        String impMaximo = etImporteMax.getText().toString();
        String impMinimo = etImporteMin.getText().toString();
        String categoria = spinnerCat.getSelectedItem().toString();
        String fechaInicial = etFechaInicial.getText().toString();
        String fechaFinal = etFechaFinal.getText().toString();
        Float total = 0f;

        if(fechaInicial.length()>1){
            fechaInicial = fechaInicial.substring(12,14)+fechaInicial.substring(5,7)+fechaInicial.substring(0,2);
        }

        if(fechaFinal.length()>1){
            fechaFinal = fechaFinal.substring(12,14)+fechaFinal.substring(5,7)+fechaFinal.substring(0,2);
        }

        String query = "select fecha, importe, categoria from gastos where importe <> -9999";
        if(!impMaximo.isEmpty()){
            query += " and importe <= "+impMaximo;
        }
        if(!impMinimo.isEmpty()){
            query += " and importe >= "+impMinimo;
        }
        if(!categoria.isEmpty()){
            query += " and categoria = "+"'"+categoria+"'";
        }
        if(fechaInicial.length()>1){
            query += " and (substr(fecha,7,2)||substr(fecha,4,2)||substr(fecha,1,2)) >= "+"'"+fechaInicial+"'";
        }
        if(fechaFinal.length()>1){
            query += " and (substr(fecha,7,2)||substr(fecha,4,2)||substr(fecha,1,2)) <= "+"'"+fechaFinal+"'";
        }
        Cursor filas = baseDeDatos.rawQuery(query, null);

        ArrayList<Item> gastos = new ArrayList<>();

        for (int i = filas.getCount()-1; i >= 0; i--) {
            filas.moveToPosition(i);
            String tmp_fecha = filas.getString(0);
            String tmp_importe = filas.getString(1);
            String tmp_categoria = filas.getString(2);
            Item itemGasto = new Item("["+tmp_fecha+"]",tmp_categoria,"$"+tmp_importe);
            gastos.add(itemGasto);
            total += Float.parseFloat(tmp_importe);
        }
        baseDeDatos.close();
        tvTotal.setText("$"+total.toString());
        adaptador = new AdaptadorList(this, gastos);
        listGastos.setAdapter(adaptador);
    }

    public void onClickFechaInicial(View view){
        showDatePickerDialog(1);
    }

    public void onClickFechaFinal(View view){
        showDatePickerDialog(2);
    }

    private void showDatePickerDialog(int i) {
        if(i == 1){
            DatePickerFragment newFragment = DatePickerFragment.newInstance(new DatePickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                    // +1 porque enero es 0
                    String strDay = day+"";
                    String strMonth = month+"";
                    if(day < 10){
                        strDay = "0"+day;
                    }
                    if(month < 10){
                        strMonth = "0"+(month+1);
                    }

                    final String selectedDate = strDay + " / " + strMonth + " / " + year;
                    etFechaInicial.setText(selectedDate);
                }
            });
            newFragment.show(getSupportFragmentManager(), "datePicker");
        }else{
            DatePickerFragment newFragment = DatePickerFragment.newInstance(new DatePickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                    // +1 porque enero es 0
                    String strDay = day+"";
                    String strMonth = month+"";
                    if(day < 10){
                        strDay = "0"+day;
                    }
                    if(month < 10){
                        strMonth = "0"+(month+1);
                    }
                    final String selectedDate = strDay + " / " + strMonth + " / " + year;
                    etFechaFinal.setText(selectedDate);
                }
            });
            newFragment.show(getSupportFragmentManager(), "datePicker");
        }
    }
}

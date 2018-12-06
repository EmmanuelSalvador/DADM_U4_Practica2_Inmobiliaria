package mx.edu.ittepic.dadm_u4_practica2_inmobiliaria;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Calendar;

public class Main2Activity extends AppCompatActivity {
    BaseDatos base;
    EditText identificacion, domicilio, precioVenta, precioRenta, fechaTransaccion;
    Spinner idpropietario;
    Button insertar, consultar, eliminar, actualizar, regesar;
    private static final int DATE_ID = 0;
    int anio, mes, dia, anioActual, mesActual, diaActual;
    Calendar calendario = Calendar.getInstance();
    String[] nombres;
    String[] id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        base = new BaseDatos(this, "INMOBILIARIA", null, 1);
        identificacion = findViewById(R.id.idinmueble);
        domicilio = findViewById(R.id.domicilio);
        precioVenta = findViewById(R.id.precioVenta);
        precioRenta = findViewById(R.id.precioRenta);
        fechaTransaccion = findViewById(R.id.fechaTransaccion);
        idpropietario=findViewById(R.id.listaPropietarios);
        fechaTransaccion.setInputType(InputType.TYPE_NULL);
        regesar = findViewById(R.id.regresar);

        anioActual = calendario.get(Calendar.YEAR);
        mesActual = calendario.get(Calendar.MONTH);
        diaActual = calendario.get(Calendar.DAY_OF_MONTH);

        try {
            SQLiteDatabase tabla = base.getWritableDatabase();
            String SQL = "SELECT IDP, NOMBRE FROM PROPIETARIO ORDER BY NOMBRE";
            Cursor fila = tabla.rawQuery(SQL, null);
            if(fila.moveToFirst()!=false &&fila.getCount()>0){
                id = new String[fila.getCount()];
                nombres = new String[fila.getCount()];
                for(int i=0; i<fila.getCount(); i++){
                    id[i]=fila.getString(0);
                    nombres[i] = fila.getString(1);
                    fila.moveToNext();
                }
            }
            ArrayAdapter<String> adaptador = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, nombres);
            idpropietario.setAdapter(adaptador);
        } catch (SQLiteException e){
            Toast.makeText(this, ""+e, Toast.LENGTH_SHORT).show();
        }

        insertar = findViewById(R.id.btnInsertar);
        consultar = findViewById(R.id.btnConsultar);
        eliminar = findViewById(R.id.btnEliminar);
        actualizar = findViewById(R.id.btnActualizar);

        fechaTransaccion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DATE_ID);
            }
        });

        insertar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertarValor();
            }
        });

        consultar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pedirID(1); //Contendrá el AlertDialog
            }
        });

        eliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pedirID(2);
            }
        });

        actualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (actualizar.getText().toString().startsWith("CONFIRMAR ACTUALIZACION")){
                    invocarConfirmacionActualizacion();
                }else{
                    pedirID(3);
                }
            }
        });
        regesar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent regresar = new Intent (Main2Activity.this, MainActivity.class);
                startActivity(regresar);
            }
        });
    }

    private void insertarValor() {
        try{
            SQLiteDatabase tabla = base.getWritableDatabase();
            String SQL = "INSERT INTO INMUEBLE VALUES ("+identificacion.getText().toString()+", '"+domicilio.getText().toString()+"', "+precioVenta.getText().toString()+", "+precioRenta.getText().toString()+", '"+fechaTransaccion.getText().toString()+"', "+id[idpropietario.getSelectedItemPosition()]+")";
            tabla.execSQL(SQL);
            Toast.makeText(this,"Si se pudo", Toast.LENGTH_LONG).show();

            tabla.close();
            vaciarCampos();

        }catch (SQLiteException e){
            Toast.makeText(this,"No se pudo" +e, Toast.LENGTH_LONG).show();
        }
    }
    private void invocarConfirmacionActualizacion() {

        AlertDialog.Builder confir = new AlertDialog.Builder(this);
        confir.setTitle("IMPORTNATE").setMessage("estas seguro que deseas aplicar cambios")
                .setPositiveButton("si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        aplicarActualizar();
                        dialog.dismiss();
                    }
                }).setNegativeButton("cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                habilitarBotonesYLimpiarCampos();
                dialog.cancel();
            }
        }).show();
    }

    private void habilitarBotonesYLimpiarCampos(){
        identificacion.setText("");
        domicilio.setText("");
        precioVenta.setText("");
        precioRenta.setText("");
        fechaTransaccion.setText("");
        insertar.setEnabled(true);
        consultar.setEnabled(true);
        eliminar.setEnabled(true);
        actualizar.setText("ACTUALIZAR");
        identificacion.setEnabled(true);
    }
    private void aplicarActualizar() {

        try{
            SQLiteDatabase tabla = base.getWritableDatabase();

            String SQL= "UPDATE INMUEBLE SET DOMICILIO='"+domicilio.getText().toString()+"', PRECIOVENTA="+precioVenta.getText().toString()+", PRECIORENTA="+precioRenta.getText().toString()+", FECHATRANSACCION='"+fechaTransaccion.getText().toString()+"', IDP="+id[idpropietario.getSelectedItemPosition()]+" WHERE IDINMUEBLE="+identificacion.getText().toString();
            tabla.execSQL(SQL);
            tabla.close();
            Toast.makeText(this,"Se actualizó",Toast.LENGTH_LONG).show();

        }catch (SQLiteException e){
            Toast.makeText(this,"No se pudo actualizar",Toast.LENGTH_LONG).show();
        }
        habilitarBotonesYLimpiarCampos();
    }

    private void pedirID(final int origen) {

        final EditText pidoID = new EditText(this);
        String mensaje ="", mensajeButton = null;
        pidoID.setInputType(InputType.TYPE_CLASS_NUMBER);

        if(origen==1){
            mensaje = "ESCRIBE ID A BUSCAR";
            mensajeButton = "BUSCAR";
        }

        if(origen==2){
            mensaje = "ESCRIBE EL ID A ELIMINAR";
            mensajeButton = "Eliminar";
        }

        if(origen==3){
            mensaje= "ESCRIBE EL ID A MODIFICAR";
            mensajeButton = "Modificar";
        }

        pidoID.setHint(mensaje);

        AlertDialog.Builder alerta = new AlertDialog.Builder(this);

        alerta.setTitle("ATENCIÓN").setMessage(mensaje)
                .setView(pidoID)
                .setPositiveButton(mensajeButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(pidoID.getText().toString().isEmpty()){
                            Toast.makeText(Main2Activity.this,"DEBES ESCRIBIR VALOR", Toast.LENGTH_LONG).show();
                            return;
                        }
                        buscarDato(pidoID.getText().toString(), origen);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("CANCELAR",null).show();
    }

    private void buscarDato(String idBuscar, int origen) {

        try{

            SQLiteDatabase tabla = base.getReadableDatabase();

            String SQL = "SELECT * FROM INMUEBLE WHERE IDINMUEBLE="+idBuscar;

            Cursor resultado = tabla.rawQuery(SQL, null);

            if(resultado.moveToFirst()){
                //Si hay resultado

                identificacion.setText(resultado.getString(0));
                domicilio.setText(resultado.getString(1));
                precioVenta.setText(resultado.getString(2));
                precioRenta.setText(resultado.getString(3));
                fechaTransaccion.setText(resultado.getString(4));
                idpropietario.setSelection(Integer.parseInt(resultado.getString(5))-1);

                if(origen==2){
                    //Esto siginifica que el resultó  para borrar
                    String dato = idBuscar+"&"+resultado.getString(1)+"&"+resultado.getString(2)+"&"+resultado.getString(3);
                    invocarConfirmacionEliminar(dato);
                    return;
                }

                if(origen==3){
                    //modificar
                    insertar.setEnabled(false);
                    consultar.setEnabled(false);
                    eliminar.setEnabled(false);
                    actualizar.setText("CONFIRMAR ACTUALIZACION");
                    identificacion.setEnabled(false);
                }

            }else {
                //No hay resultado
                Toast.makeText(this,"No se encontró resultado", Toast.LENGTH_LONG).show();
            }

            tabla.close();

        }catch (SQLiteException e){
            Toast.makeText(this,"ERROR: No se pudo "+e , Toast.LENGTH_LONG).show();
        }
    }

    private void invocarConfirmacionEliminar(String dato) {

        String datos[] = dato.split("&");
        final String id = datos[0];
        String domicilio = datos[1];

        AlertDialog.Builder alerta = new AlertDialog.Builder(this);
        alerta.setTitle("Importante").setMessage("¿Seguro que deseas eliminar este inmueble ubicado en el domicilio "+domicilio+"?")
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        eliminarDato(id);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("No",null).show();


    }

    private void eliminarDato(String idEliminar) {

        try {
            SQLiteDatabase tabla = base.getWritableDatabase();
            String SQL = "DELETE FROM INMUEBLE WHERE IDINMUEBLE="+idEliminar;
            tabla.execSQL(SQL);
            tabla.close();

            Toast.makeText(this,"ELIMINADO", Toast.LENGTH_LONG).show();

            vaciarCampos();


        }catch (SQLiteException e){

            Toast.makeText(this,"ERROR: No se pudo eliminar", Toast.LENGTH_LONG).show();

        }

    }

    private void vaciarCampos() {

        identificacion.setText(null);
        domicilio.setText(null);
        precioVenta.setText(null);
        precioRenta.setText(null);
        fechaTransaccion.setText(null);
        //idpropietario.setText(null);
    }
    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    anio = year;
                    mes = monthOfYear;
                    dia = dayOfMonth;
                    fechaTransaccion.setText(anio + "/" + (mes+1) + "/" + dia);
                }
            };
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_ID:
                return new DatePickerDialog(this, mDateSetListener, anioActual, mesActual, diaActual);
        }
        return null;
    }
}
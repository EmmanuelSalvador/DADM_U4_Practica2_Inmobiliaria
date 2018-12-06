package mx.edu.ittepic.dadm_u4_practica2_inmobiliaria;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    BaseDatos base;
    Button insertar, consultar, eliminar, actualizar, inmueble;
    EditText identificador, nombre, domicilio, telefono;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        base = new BaseDatos(this,"INMOBILIARIA", null,1);
        insertar = findViewById(R.id.btnInsertar);
        consultar = findViewById(R.id.btnConsultar);
        eliminar = findViewById(R.id.btnEliminar);
        actualizar = findViewById(R.id.btnActualizar);
        identificador = findViewById(R.id.idp);
        nombre = findViewById(R.id.nombrePropietario);
        domicilio = findViewById(R.id.domicilioPropietario);
        telefono = findViewById(R.id.telefonoPropietario);
        inmueble = findViewById(R.id.inmueble);

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
        inmueble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent inmueble = new Intent(MainActivity.this, Main2Activity.class);
                startActivity(inmueble);
            }
        });
    }

    private void insertarValor() {
        try{
            SQLiteDatabase tabla = base.getWritableDatabase();
            String SQL = "INSERT INTO PROPIETARIO VALUES("+identificador.getText().toString()+",'"+nombre.getText().toString()+"','"+domicilio.getText().toString()+"','"+telefono.getText().toString()+"')";

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
        identificador.setText("");
        nombre.setText("");
        domicilio.setText("");
        telefono.setText("");
        insertar.setEnabled(true);
        consultar.setEnabled(true);
        eliminar.setEnabled(true);
        actualizar.setText("ACTUALIZAR");
        identificador.setEnabled(true);
    }
    private void aplicarActualizar() {

        try{
            SQLiteDatabase tabla = base.getWritableDatabase();

            String SQL= "UPDATE PROPIETARIO SET NOMBRE='"+nombre.getText().toString()+"', DOMICILIO='"+domicilio.getText().toString()+"', TELEFONO='"+telefono.getText().toString()+"' WHERE IDP="+identificador.getText().toString();
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
            mensajeButton = "Eliminr";
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
                            Toast.makeText(MainActivity.this,"DEBES ESCRIBIR VALOR", Toast.LENGTH_LONG).show();
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

            String SQL = "SELECT * FROM PROPIETARIO WHERE IDP="+idBuscar;

            Cursor resultado = tabla.rawQuery(SQL, null);

            if(resultado.moveToFirst()){
                //Si hay resultado

                identificador.setText(resultado.getString(0));
                nombre.setText(resultado.getString(1));
                domicilio.setText(resultado.getString(2));
                telefono.setText(resultado.getString(3));

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
                    identificador.setEnabled(false);
                }

            }else {
                //No hay resultado
                Toast.makeText(this,"No se encontró resultado", Toast.LENGTH_LONG).show();
            }

            tabla.close();

        }catch (SQLiteException e){
            Toast.makeText(this,"ERROR: No se pudo", Toast.LENGTH_LONG).show();
        }
    }

    private void invocarConfirmacionEliminar(String dato) {

        String datos[] = dato.split("&");
        final String id = datos[0];
        String nombre = datos[1];

        AlertDialog.Builder alerta = new AlertDialog.Builder(this);
        alerta.setTitle("IMPORTANTE").setMessage("¿Seguro que deseas eliminar al propietario "+nombre+"?")
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
            String SQL = "DELETE FROM PROPIETARIO WHERE IDP="+idEliminar;
            tabla.execSQL(SQL);
            tabla.close();

            Toast.makeText(this,"ELIMINADO", Toast.LENGTH_LONG).show();

            vaciarCampos();


        }catch (SQLiteException e){

            Toast.makeText(this,"ERROR: No se pudo eliminar", Toast.LENGTH_LONG).show();

        }

    }

    private void vaciarCampos() {

        identificador.setText(null);
        nombre.setText(null);
        domicilio.setText(null);
        telefono.setText(null);
    }
}

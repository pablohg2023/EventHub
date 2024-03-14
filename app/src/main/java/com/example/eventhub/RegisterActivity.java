package com.example.eventhub;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.core.Tag;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    Usuario usuario;

    private EditText editNombre, editFechaNac, editApellidos,
            editRol, editPassword, editPasswordRepeat, editMail;
    private ProgressBar progressBar;
    private RadioGroup radioGroupRol;
    private RadioButton radioButtonRol;

    private String txtNombre, txtFechaNac, txtApellidos, txtRol, txtPassword, txtPasswordRepeat, txtMail;

    private DatePickerDialog picker;

    FirebaseAuth auth;
    DatabaseReference databaseReference;

    TextView tengoCuenta;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("usuarios");
        progressDialog = new ProgressDialog(RegisterActivity.this);
        progressDialog.setTitle("Espere por favor");
        progressDialog.setCanceledOnTouchOutside(false);


        progressBar = findViewById(R.id.progrssBar);
        editNombre = findViewById(R.id.editName);
        editApellidos = findViewById(R.id.editApellidos);
        editFechaNac = findViewById(R.id.editFechaNacimiento);
        editPassword = findViewById(R.id.editPassword);
        editPasswordRepeat = findViewById(R.id.editPasswordRepeat);
        editMail = findViewById(R.id.editEmail);
        tengoCuenta = findViewById(R.id.txtTengoCuenta);


        // Radiobutton
        radioGroupRol = findViewById(R.id.rdbtnRol);
        radioGroupRol.clearCheck();

        tengoCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });

        editFechaNac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int dia = calendar.get(Calendar.DAY_OF_MONTH);
                int mes = calendar.get(Calendar.MONTH);
                int anio = calendar.get(Calendar.YEAR);

                picker = new DatePickerDialog(RegisterActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        editFechaNac.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, anio, mes, dia);
                picker.show();
            }
        });

        Button buttonRegister = findViewById(R.id.btnRegistrar);
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int seleccionar = radioGroupRol.getCheckedRadioButtonId();
                radioButtonRol = findViewById(seleccionar);

                txtNombre = editNombre.getText().toString();
                txtApellidos = editApellidos.getText().toString();
                txtFechaNac = editFechaNac.getText().toString();
                txtMail = editMail.getText().toString();
                txtPassword = editPassword.getText().toString();
                txtPasswordRepeat = editPasswordRepeat.getText().toString();


                if (TextUtils.isEmpty(txtNombre)) {
                    editNombre.setError("Se requiere el nombre");
                    editNombre.requestFocus();
                } else if (TextUtils.isEmpty(txtApellidos)) {
                    editApellidos.setError("Se requieren los apellidos");
                    editApellidos.requestFocus();
                } else if (TextUtils.isEmpty(txtMail)) {
                    editMail.setError("Se requieren el email");
                    editMail.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(txtMail).matches()) {
                    editMail.setError("Se requieren email correcto");
                    editMail.requestFocus();
                } else if (TextUtils.isEmpty(txtFechaNac)) {
                    editFechaNac.setError("Fecha de nacimiento necesario");
                    editFechaNac.requestFocus();
                } else if (radioGroupRol.getCheckedRadioButtonId() == -1) {
                    editRol.setError("Rol necesario");
                    editRol.requestFocus();
                } else if (TextUtils.isEmpty(txtPassword)) {
                    editPassword.setError("Es necesario la contraseña");
                    editPassword.requestFocus();
                } else if (txtPassword.length() < 6) {
                    editPassword.setError("La contraseña debe tener minimo 6 caracteres");
                    editPassword.requestFocus();
                } else if (TextUtils.isEmpty(txtPasswordRepeat)) {
                    editPasswordRepeat.setError("Es necesario confirmar la contraseña");
                    editPasswordRepeat.requestFocus();
                } else if (!txtPassword.equals(txtPasswordRepeat)) {
                    editPasswordRepeat.setError("Es necesario que coincidan las contraseñas");
                    editPasswordRepeat.requestFocus();

                    editPassword.clearComposingText();
                    editPasswordRepeat.clearComposingText();
                } else {
                    if (validarFechaNacimiento()) {
                        txtRol = radioButtonRol.getText().toString();
                        progressBar.setVisibility(View.VISIBLE);
                        registrarUsuario();
                    }
                }
            }
        });

    }

    private void registrarUsuario() {

        auth.createUserWithEmailAndPassword(txtMail, txtPassword).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                //
                GuardarInformacion();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void GuardarInformacion() {
        progressDialog.setMessage("Guandando su informacion");
        progressDialog.dismiss();

        String uid = auth.getUid();

        HashMap<String, String> Datos = new HashMap<>();
        Datos.put("uid", uid);
        Datos.put("correo", txtMail);
        Datos.put("nombre", txtNombre);
        Datos.put("apellidos", txtApellidos);
        Datos.put("password", txtPassword);
        Datos.put("rol", txtRol);
        Datos.put("fecNacimiento" , txtFechaNac);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Usuarios");
        databaseReference.child(uid)
                .setValue(Datos)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Cuenta creada exitosamente", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private boolean validarFechaNacimiento() {
        boolean isValid = true;

        Calendar fechaNacimiento = obtenerFechaSeleccionada(editFechaNac.getText().toString());
        if (fechaNacimiento == null) {
            Toast.makeText(RegisterActivity.this, "Fecha de nacimiento inválida", Toast.LENGTH_LONG).show();
            isValid = false;
        } else {
            // Calcular la edad
            Calendar hoy = Calendar.getInstance();
            int edad = hoy.get(Calendar.YEAR) - fechaNacimiento.get(Calendar.YEAR);
            if (hoy.get(Calendar.MONTH) < fechaNacimiento.get(Calendar.MONTH) ||
                    (hoy.get(Calendar.MONTH) == fechaNacimiento.get(Calendar.MONTH) &&
                            hoy.get(Calendar.DAY_OF_MONTH) < fechaNacimiento.get(Calendar.DAY_OF_MONTH))) {
                edad--;
            }

            int edadMinima = txtRol.equals("Cliente") ? 16 : 18;
            if (edad < edadMinima) {
                String mensaje = txtRol.equals("Cliente") ? "Debe tener al menos 16 años para registrarse como cliente" :
                        "Debe tener al menos 18 años para registrarse como organizador de eventos";
                Toast.makeText(RegisterActivity.this, mensaje, Toast.LENGTH_LONG).show();
                isValid = false;
            }
        }

        return isValid;
    }

    private Calendar obtenerFechaSeleccionada(String fecha) {
        try {
            String[] partes = fecha.split("/");
            int dia = Integer.parseInt(partes[0]);
            int mes = Integer.parseInt(partes[1]) - 1; // Restar 1 porque los meses en Calendar comienzan desde 0
            int anio = Integer.parseInt(partes[2]);
            return new GregorianCalendar(anio, mes, dia);
        } catch (Exception e) {
            return null;
        }
    }

}
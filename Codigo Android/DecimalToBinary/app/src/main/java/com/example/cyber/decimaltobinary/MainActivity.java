package com.example.cyber.decimaltobinary;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private EditText editTxtDecimal;
    private TextView txtBin,txtOct,txtHex;
    private Button btnConectar,btnEnviar;
    private CheckBox enviarAutomaticamente;

    // Requisição para Activity de ativação do Bluetooth
    // Se numero for maior > 0,este codigo sera devolvido em onActivityResult()
    private static final int REQUEST_ENABLE_BT = 1;
    // Requisiçãoo para Activity para inciar tela do aplicativos pareados,
    // que se houver ou nao aplicativo pareado retornara para onActivityResult()
    // e realizara as devidas ações conforme a resposta
    public static final int SELECT_PAIRED_DEVICE = 2;

    // BluetoothAdapter é comando de entrada padrão paras todas interações com bluetooth
    private BluetoothAdapter bluetoothPadrao = null;

    //Usaod para conectar com o dispositivo bluetooth
    BluetoothThread btt;
    Handler writeHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Obtem o bluetooth padrao do aparelho celular
        bluetoothPadrao = BluetoothAdapter.getDefaultAdapter();

        editTxtDecimal = (EditText) findViewById(R.id.inboxdec);
        editTxtDecimal.setFilters(new InputFilter[]{new InputFilterMinMax("0", "255")});
        txtBin = (TextView) findViewById(R.id.txtBin);
        txtOct = (TextView) findViewById(R.id.txtOct);
        txtHex = (TextView) findViewById(R.id.txtHex);

        btnConectar = (Button) findViewById(R.id.btnConectar);
        btnEnviar = (Button) findViewById(R.id.btnEnviar);
        enviarAutomaticamente = (CheckBox)findViewById(R.id.ckbEnviarAutomaticamente);
    }

    @Override
    protected void onResume() {
        super.onResume();

        editTxtDecimal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (editTxtDecimal.getText().toString().equals("")) {
                    txtBin.setText("Binario: ");
                    txtOct.setText("Octal: ");
                    txtHex.setText("Hexadecimal:");
                }else{
                    txtBin.setText("Binario: " + Integer.toBinaryString(Integer.parseInt(editTxtDecimal.getText().toString())));
                    txtOct.setText("Octal: " + Integer.toOctalString(Integer.parseInt(editTxtDecimal.getText().toString())));
                    txtHex.setText("Hexadecimal: " + Integer.toHexString(Integer.parseInt(editTxtDecimal.getText().toString())));

                    if(enviarAutomaticamente.isChecked()){
                        enviarBinario();
                    }

                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btnConectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothPadrao == null) {
                    Toast.makeText(getApplicationContext(), "Dispostivo não possui Bluetooth", Toast.LENGTH_LONG).show();
                } else {
                    if (!bluetoothPadrao.isEnabled()) {
                        Intent novoIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(novoIntent, REQUEST_ENABLE_BT);
                    } else {
                        listaDeDispositivos();
                    }
                }

            }
        });

        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTxtDecimal.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Não ha binario para enviar", Toast.LENGTH_LONG).show();
                }else{
                    enviarBinario();
                }
            }
        });
    }

    public void enviarBinario(){
        if (btt != null) {
            Message msg = Message.obtain();
            msg.obj = Integer.toBinaryString(Integer.parseInt(editTxtDecimal.getText().toString()));
            writeHandler.sendMessage(msg);
        }
    }

    @Override
    public void onStop() {
        super.onStop();;
        interromperBluetooth();

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            // Retrono do pedido de ativação do Bluetooth
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(getApplicationContext(), "Bluetooth Ativado XD", Toast.LENGTH_LONG).show();
                    listaDeDispositivos();
                } else {
                    Toast.makeText(getApplicationContext(), "Voce precisa ativar o bluetooth ", Toast.LENGTH_LONG).show();
                }
                break;
            case SELECT_PAIRED_DEVICE:
                if (resultCode == RESULT_OK) {
                    if (btt == null) {
                        btt = new BluetoothThread(data.getStringExtra("btDevAddress"), new Handler() {
                            @Override
                            public void handleMessage(Message message) {
                                String s = (String) message.obj;
                                //Em tempo de execução compara as mensagens recebidas pelo gerenciador de conexão
                                if (s.equals("CONNECTED")) {
                                    btnConectar.setText("Desconectar");
                                    btnConectar.setEnabled(true);
                                    Toast.makeText(getApplicationContext(), "Conectado", Toast.LENGTH_LONG).show();
                                    btnEnviar.setEnabled(true);
                                } else if (s.equals("DISCONNECTED")) {
                                    Toast.makeText(getApplicationContext(), "Desconectado", Toast.LENGTH_LONG).show();
                                    interromperBluetooth();
                                } else if (s.equals("CONNECTION FAILED")) {
                                    Toast.makeText(getApplicationContext(), "Falha na conexao", Toast.LENGTH_LONG).show();
                                    interromperBluetooth();
                                }
                            }
                        });
                    }

                    if (btt != null) {
                        // Busca o handler usado para mandar mensagens
                        writeHandler = btt.getWriteHandler();

                        // Roda o thread
                        btt.start();

                        btnConectar.setText("Conectando...");
                        btnConectar.setEnabled(false);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Nenhum dispositivo Selecionado", Toast.LENGTH_LONG).show();
                }
                break;

        }
    };

    public void listaDeDispositivos() {
        if (bluetoothPadrao.isEnabled()) {
            if (btt == null) {
                Intent searchPairedDevicesIntent = new Intent(this, PairedDevices.class);
                startActivityForResult(searchPairedDevicesIntent, SELECT_PAIRED_DEVICE);
            } else {
                interromperBluetooth();
            }
        }
    }
    public void interromperBluetooth(){
        if(btt != null){
            btnConectar.setText("Conectar");
            btt.interrupt();
            btt = null;
            btnConectar.setEnabled(true);
            btnEnviar.setEnabled(false);
        }

    }

}

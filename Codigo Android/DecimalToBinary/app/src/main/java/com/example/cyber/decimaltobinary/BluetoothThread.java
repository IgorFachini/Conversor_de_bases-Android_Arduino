package com.example.cyber.decimaltobinary;

        import android.annotation.SuppressLint;
        import android.bluetooth.BluetoothAdapter;
        import android.bluetooth.BluetoothDevice;
        import android.bluetooth.BluetoothSocket;

        import java.io.InputStream;
        import java.io.OutputStream;

        import android.os.Handler;
        import android.os.Message;

        import android.util.Log;

        import java.util.UUID;

@SuppressLint({ "DefaultLocale", "HandlerLeak" })
public class BluetoothThread extends Thread {

    // Tag for logging
    private static final String TAG = "BluetoothThread";

    // Delimiter used to separate messages
    private static final char DELIMITER = ';';

    // UUID especifica um protocolo para comunicacaoo serial Bluetooth genérica
    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Ira armazenar o endereço MAC do disposivo Bluetooth
    private final String address;

    // BluetoothSocket cria um ponto de conexão que permite trocar dados com outro
    // disposivo bleutooth atraves do ImputStream() e OutputStream()
    public BluetoothSocket socket;
    private OutputStream outStream;

    // Handlers usado para passar dados entre threads
    private final Handler readHandler;
    private final Handler writeHandler;
    /**
     * Construtor da classe, recebe o endereço MAC do dispositivo bluetooth
     * e um Handler para as mensagens recebidas.
     *
     */
    public BluetoothThread(String address, Handler handler) {

        this.address = address.toUpperCase();
        this.readHandler = handler;

        writeHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                write((String) message.obj);
            }
        };
    }
    /**
     * Devolve o manipulador(Handler) de escrita para a conexão. As mensagens
     * recebidas por este manipulador(handler) sera escrito no Bluetooth socket.
     */
    public Handler getWriteHandler() {
        return writeHandler;
    }

    /**
     * Conectar o Bluetooth socket, ou lançar uma exeção se ele falhar.
     */
    private void connect() throws Exception {

        Log.i(TAG, "Attempting connection to " + address + "...");

        // busca o dispositivo padrao do aparelho
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

        // Find the remote device
        BluetoothDevice remoteDevice = adapter.getRemoteDevice(address);

        // Create a socket with the remote device using this protocol
        socket = remoteDevice.createRfcommSocketToServiceRecord(uuid);

        // Make sure Bluetooth adapter is not in discovery mode
        adapter.cancelDiscovery();

        // Connect to the socket
        socket.connect();

        // Get input and output streams from the socket
        outStream = socket.getOutputStream();
        Log.i(TAG, "Connected successfully to " + address + ".");
    }

    /**
     * Disconnect the streams and socket.
     */
    private void disconnect() {
        if (outStream != null) {
            try {
                outStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * Write data to the socket.
     */
    private void write(String s) {

        try {
            // Add the delimiter
             s += DELIMITER;
            byte[] msgBuffer = s.getBytes();
            outStream.write(msgBuffer);
            Log.i(TAG, "[SENT] " + s);
        } catch (Exception e) {
            Log.e(TAG, "Write failed!", e);
        }
    }

    /**
     * Pass a message to the read handler.
     */
    private void sendToReadHandler(String s) {
        Message msg = Message.obtain();
        msg.obj = s;
        readHandler.sendMessage(msg);
        Log.i(TAG, "[RECV] " + s);
    }

    /**
     * Entry point when thread.start() is called.
     */
    public void run() {

        // Attempt to connect and exit the thread if it failed
        try {
            connect();
            sendToReadHandler("CONNECTED");
        } catch (Exception e) {
            Log.e(TAG, "Failed to connect!", e);
            sendToReadHandler("CONNECTION FAILED");
            disconnect();
            return;
        }

        // Loop continuously, reading data, until thread.interrupt() is called
        while (!this.isInterrupted()) {

            // Make sure things haven't gone wrong
            if (outStream == null) {
                Log.e(TAG, "Lost bluetooth connection!");
                break;
            }
        }
        // Se thread é interrompido, ele fecha a conexão
        disconnect();
        sendToReadHandler("DISCONNECTED");
    }
}

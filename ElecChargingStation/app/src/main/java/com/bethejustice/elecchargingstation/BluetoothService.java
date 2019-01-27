package com.bethejustice.elecchargingstation;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class BluetoothService {
    // Debugging
    private static final String TAG = "BluetoothService";

    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    // Intent request code
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_NOTDEVICE = 3;
    public static final int MESSAGE_CONNECT = 4;
    public static final int MESSAGE_FAIL = 5;

    public static final String SERVICE_HANDLER_MSG_KEY_DEVICE_NAME = "device_name";
    public static final String SERVICE_HANDLER_MSG_KEY_DEVICE_ADDRESS = "device_address";
    public static final String SERVICE_HANDLER_MSG_KEY_TOAST = "toast";

    // RFCOMM Protocol
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter btAdapter;

    private Activity mActivity;
    private Handler mHandler;

    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread; // ������ �ٽ�
    private ConnectedThread mConnectedThread; // ������ �ٽ�

    private int mState;

    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private static final long RECONNECT_DELAY_MAX = 60*60*1000;
    private long mReconnectDelay = 15*1000;
    private Timer mConnectTimer = null;
    private boolean mIsServiceStopped = false;

    // Constructors
    public BluetoothService(Activity ac, Handler h) {
        mActivity = ac;
        mHandler = h;

        // BluetoothAdapter ���
        btAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
        if(mState == STATE_CONNECTED)

        if(mState == STATE_CONNECTED)
            cancelRetryConnect();

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    // Bluetooth ���� get
    public synchronized int getState() {

        return mState;
    }

    /**
     * Check the Bluetooth support
     *
     * @return boolean
     */
    public boolean getDeviceState() {
        Log.i(TAG, "Check the Bluetooth support");

        if (btAdapter == null) {
            Log.d(TAG, "Bluetooth is not available");

            return false;

        } else {
            Log.d(TAG, "Bluetooth is available");

            return true;
        }
    }

    /**
     * Check the enabled Bluetooth
     */
    public void enableBluetooth() {
        Log.i(TAG, "Check the enabled Bluetooth");

        if (btAdapter.isEnabled()) {
            // ����� ������� ���°� On�� ���
            Log.d(TAG, "Bluetooth Enable Now");

            // Next Step
            scanDevice();
        } else {
            // ����� ������� ���°� Off�� ���
            Log.d(TAG, "Bluetooth Enable Request");

            Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mActivity.startActivityForResult(i, REQUEST_ENABLE_BT);
        }
    }

    /**
     * Available device search
     */
    public void scanDevice() {
        Log.d(TAG, "Scan Device");

        Intent serverIntent = new Intent(mActivity, DeviceListActivity.class);
        mActivity.startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }

    /**
     * after scanning and get device info
     *
     * @param data
     */
    public void getDeviceInfo(Intent data) {
        // Get the device MAC address
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        System.out.println(address);
        // Get the BluetoothDevice object
        // BluetoothDevice device = btAdapter.getRemoteDevice(address);
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        Log.d(TAG, "Get Device Info \n" + "address : " + address);
        connect(device);
    }


    public synchronized void start() {
        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {

            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
        setState(STATE_LISTEN);
        mIsServiceStopped = false;

    }

    // ConnectThread �ʱ�ȭ device�� ��� ���� ����
    public synchronized void connect(BluetoothDevice device) {
        Log.d(TAG, "connect to: " + device);

        if (mState == STATE_CONNECTED){
            return;
        }

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    // ConnectedThread �ʱ�ȭ
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        Log.d(TAG, "connected");

        // Cancel the thread that completed the connection
        if (mConnectThread != null) { mConnectThread.cancel(); mConnectThread = null; }
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) { mConnectedThread.cancel(); mConnectedThread = null; }

        if (mAcceptThread != null) { mAcceptThread.cancel(); mAcceptThread = null; }

        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // Start the thread to manage the connection and perform transmissions
        /*Message msg = mHandler.obtainMessage(MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(SERVICE_HANDLER_MSG_KEY_DEVICE_ADDRESS, device.getAddress());
        bundle.putString(SERVICE_HANDLER_MSG_KEY_DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);*/

        setState(STATE_CONNECTED);
    }

    // ��� thread stop
    public synchronized void stop() {
        Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_NONE);
        mIsServiceStopped = true;
        cancelRetryConnect();
    }

    // ���� ���� �κ�(������ �κ�)
    public void write(byte[] out) { // Create temporary object
        ConnectedThread r; // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED)
                return;
            r = mConnectedThread;
        } // Perform the write unsynchronized
        r.write(out);
    }

    // ���� ����������
    private void connectionFailed() {
        Message msg = mHandler.obtainMessage(MESSAGE_FAIL);
        Bundle bundle = new Bundle();
        bundle.putString("fail", "블루투스를 연결 할 수 없습니다.");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        setState(STATE_LISTEN);
        reserveRetryConnect();
    }

    // ������ �Ҿ��� ��
    private void connectionLost() {
        setState(STATE_LISTEN);
        reserveRetryConnect();
    }

    private void reserveRetryConnect() {
        if(mIsServiceStopped)
            return;

        mReconnectDelay = mReconnectDelay * 2;
        if(mReconnectDelay > RECONNECT_DELAY_MAX)
            mReconnectDelay = RECONNECT_DELAY_MAX;

        if(mConnectTimer != null) {
            try {
                mConnectTimer.cancel();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
        mConnectTimer = new Timer();
        mConnectTimer.schedule(new ConnectTimerTask(), mReconnectDelay);
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            btAdapter.cancelDiscovery();

            // BluetoothSocket ���� �õ�
            try {
                // BluetoothSocket ���� �õ��� ���� return ���� succes �Ǵ� exception�̴�.
                mmSocket.connect();
                Log.d(TAG, "Connect Success");

            } catch (IOException e) {
                connectionFailed();
                Log.d(TAG, "Connect Fail");

                // socket�� �ݴ´�.
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG,
                            "unable to close() socket during connection failure",
                            e2);
                }
                // ������? Ȥ�� ���� �������� �޼ҵ带 ȣ���Ѵ�.
                BluetoothService.this.start();
                return;
            }

            // ConnectThread Ŭ������ reset�Ѵ�.
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }

            // ConnectThread�� �����Ѵ�.
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        char mCharDelimiter = '\n';

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // BluetoothSocket�� inputstream �� outputstream�� ��´�.
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int readBufferPosition = 0;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    int byteAvailable = mmInStream.available();   // 수신 데이터 확인
                    if (byteAvailable > 0) {                        // 데이터가 수신된 경우.
                        byte[] packetBytes = new byte[byteAvailable];
                        // read(buf[]) : 입력스트림에서 buf[] 크기만큼 읽어서 저장 없을 경우에 -1 리턴.
                        mmInStream.read(packetBytes);
                        for (int i = 0; i < byteAvailable; i++) {
                            byte b = packetBytes[i];
                            if (b == mCharDelimiter) {
                                byte[] encodedBytes = new byte[readBufferPosition];
                                //  System.arraycopy(복사할 배열, 복사시작점, 복사된 배열, 붙이기 시작점, 복사할 개수)
                                //  readBuffer 배열을 처음 부터 끝까지 encodedBytes 배열로 복사.
                                System.arraycopy(buffer, 0, encodedBytes, 0, encodedBytes.length);

                                // data : 읽어온 데이터 (String 값)
                                final String data = new String(encodedBytes, "US-ASCII");
                                final String str[] = data.split(" ");
                                readBufferPosition = 0;
                                if(str.length>1) {
                                    Message msg = mHandler.obtainMessage(MESSAGE_CONNECT);
                                    Bundle bundle = new Bundle();
                                    bundle.putString("connect", "블루투스가 연결되었습니다.");
                                    bundle.putString("available", str[0]);
                                    bundle.putString("velocity", str[1]);
                                    msg.setData(bundle);
                                    mHandler.sendMessage(msg);
                                }
                            } else {
                                buffer[readBufferPosition++] = b;
                            }
                        }
                    }
                } catch (IOException e) {
                    Message msg = mHandler.obtainMessage(MESSAGE_NOTDEVICE);
                    Bundle bundle = new Bundle();
                    bundle.putString("disconnected", "장치가 없습니다.");
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }

        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer
         *            The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                // ���� ���� �κ�(���� ������)
                mmOutStream.write(buffer);

            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }


    }

    private void cancelRetryConnect() {
        if(mConnectTimer != null) {
            try {
                mConnectTimer.cancel();
                mConnectTimer.purge();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            mConnectTimer = null;
            mReconnectDelay = 15*1000;
        }
    }

    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            try {
                tmp = btAdapter.listenUsingRfcommWithServiceRecord(TAG, MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "listen() failed" + e.toString());
            }
            mmServerSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "BEGIN mAcceptThread" + this);
            setName("AcceptThread");
            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    //if(mmServerSocket != null)
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "accept() failed", e);
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (BluetoothService.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // Situation normal. Start the connected thread.
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
            Log.i(TAG, "END mAcceptThread");
        }

        public void cancel() {
            Log.d(TAG, "cancel " + this);
            try {
                if(mmServerSocket != null)
                    mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of server failed" + e.toString());
            }
        }
    }	// End of class AcceptThread

    private class ConnectTimerTask extends TimerTask {
        public ConnectTimerTask() {}

        public void run() {
            if(mIsServiceStopped)
                return;

            mHandler.post(new Runnable() {
                public void run() {
                    if(getState() == STATE_CONNECTED || getState() == STATE_CONNECTING)
                        return;

                    Log.d(TAG, "ConnectTimerTask :: Retry connect()");

                    ConnectionInfo cInfo = ConnectionInfo.getInstance(null);
                    if(cInfo != null) {
                        String addrs = cInfo.getDeviceAddress();
                        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
                        if(ba != null && addrs != null) {
                            BluetoothDevice device = ba.getRemoteDevice(addrs);

                            if(device != null) {
                                connect(device);
                            }
                        }
                    }
                }	// End of run()
            });
        }
    }

}

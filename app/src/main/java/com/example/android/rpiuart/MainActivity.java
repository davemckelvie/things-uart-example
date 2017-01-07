package com.example.android.rpiuart;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class MainActivity extends Activity {

  public static final String TAG = MainActivity.class.getSimpleName();
  private static final String UART_NAME = "/dev/ttyUSB0";
  private static final long WRITE_DELAY = 5000;
  SerialPort serialPort;
  InputStream inputStream;
  OutputStream outputStream;
  private ReadThread readThread;
  Handler handler = new Handler();
  WriteRunner writeRunner = new WriteRunner();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

      try {
          initSerialPort();
      } catch (IOException e) {
          //
      }
    readThread = new ReadThread();
    readThread.start();
    handler.postDelayed(writeRunner, WRITE_DELAY);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (readThread != null) {
      readThread.interrupt();
      readThread = null;
    }
    if (serialPort != null) {
      serialPort.close();
    }
  }

  private void initSerialPort() throws IOException {
    serialPort = new SerialPort(new File(UART_NAME), 115200, 0);
    inputStream = serialPort.getInputStream();
    outputStream = serialPort.getOutputStream();
  }



  private class ReadThread extends Thread {

    @Override
    public void run() {
      super.run();
      while(!isInterrupted()) {
        int size;
        try {
          byte[] buffer = new byte[64];
          if (inputStream == null) return;
          size = inputStream.read(buffer);
          if (size > 0) {
            onDataReceived(buffer, size);
          }
        } catch (IOException e) {
          e.printStackTrace();
          return;
        }
      }
    }
  }

  private void onDataReceived(byte[] buffer, int size) {
    Log.d(TAG, "onDataReceived: " + new String(buffer, 0, size));
  }

  private class WriteRunner implements Runnable {

    @Override
    public void run() {
      if (outputStream != null) {
        try {
          Log.d(TAG, "run: writing to UART");
          outputStream.write("Hello there UART!".getBytes());
        } catch (IOException e) {
          //
        }
      }
      handler.postDelayed(this, WRITE_DELAY);
    }
  }

}

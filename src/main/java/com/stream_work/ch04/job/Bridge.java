package com.stream_work.ch04.job;

import java.net.*;
import java.io.*;
import java.util.List;

import com.stream_work.ch04.api.Event;
import com.stream_work.ch04.api.EventCollector;
import com.stream_work.ch04.api.Source;

class Bridge extends Source {
  private static final long serialVersionUID = 4914193100808497571L;

  private final int portBase;
  private int instance = 0;
  private BufferedReader reader = null;

  public Bridge(String name, int parallelism, int port) {
    super(name, parallelism);

    this.portBase = port;
  }

  @Override
  public void setupInstance(int instance) {
    this.instance = instance;
    reader = setupSocketReader(portBase + instance);
  }

  @Override
  public void getEvents(EventCollector eventCollector) {
    try {
      String vehicle = reader.readLine();
      if (vehicle == null) {
        // Exit when user closes the server.
        System.exit(0);
      }
      eventCollector.add("default", new VehicleEvent(vehicle));
      eventCollector.add("clone", new VehicleEvent(vehicle + " clone"));

      System.out.println("bridge :: instance " + instance + " --> " + vehicle);
    } catch (IOException e) {
      System.out.println("Failed to read input: " + e);
    }
  }

  private BufferedReader setupSocketReader(int port) {
    try {
      Socket socket = new Socket("localhost", port);
      InputStream input = socket.getInputStream();
      return new BufferedReader(new InputStreamReader(input));
    } catch (UnknownHostException e) {
      e.printStackTrace();
      System.exit(0);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(0);
    }
    return null;
  }
}

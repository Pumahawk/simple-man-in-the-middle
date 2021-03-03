package com.pumahawk.utils.mandinthemiddle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class ManInTheMiddle {
	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
	
		ParamsConfigurations conf = ParamsConfigurations.readParams(args);
		
		ParamsConfigurations.validateConf(conf);
		
		if (conf.isHelp() || conf.isError()) {
			helpOrErrorAndExit(conf);
		} else {
			while (true) {
				manInTheMiddle(conf);
			}
		}
		
	}

	public static void readAndWrite(boolean log, InputStream in, OutputStream out) throws IOException {
		for (int i = in.read(); i != -1; i = in.read()) {
			if (log) {
				System.out.write(i);
			}
			out.write(i);
		}
	}
	
	public static void manInTheMiddle(ParamsConfigurations conf) throws IOException, InterruptedException {
		ServerSocket ss = null;
		Socket serv = null;
		Socket conn = null;
		try {
			ss = new ServerSocket(conf.getLocalPort());
			conn = ss.accept();
	
			InputStream inServer = conn.getInputStream();
			OutputStream outServer = conn.getOutputStream();
	
			serv = new Socket(conf.getDestinationHost(), conf.getDestinationPort());
	
			InputStream servIn = serv.getInputStream();
			OutputStream servOut = serv.getOutputStream();
			
			Thread reader = new Thread(() -> {
				try {
					readAndWrite(conf.isLogFromClient(), inServer, servOut);
				} catch (IOException e) {}
			});
			reader.start();
			readAndWrite(conf.isLogFromServer(), servIn, outServer);
		} finally {
			if (ss != null) {
				ss.close();
			}
			if (conn != null) {
				conn.close();
			}
			if (serv != null) {
				serv.close();
			}
		}
	}
	
	public static void helpOrErrorAndExit(ParamsConfigurations conf) {
		try (PrintStream out = conf.isError() ? System.err : System.out) {
			out.println("jmiddle [--log-all] [--log-from-client] [--log-from-server] -p <port> -H <remote-host> -P <remote-host>");
			System.exit(!conf.isError() ? 1 : 0);
		}
	}
	
	public static class ParamsConfigurations {
		
		private boolean help = false;
		private boolean error = false;
	
		private int localPort = -1;
		private int destinationPort = -1;
		private String destinationHost = null;
		private boolean logFromClient = false;
		private boolean logFromServer = false;
		
		public static ParamsConfigurations readParams(String[] args) {

			ParamsConfigurations conf = new ParamsConfigurations();
			
			if (args.length == 0) {
				conf.setError(true);
				return conf;
			}

			try {
				for (int i = 0; i < args.length; i++) {
					if ("-h".equals(args[i]) || "--help".equals(args[i])) {
						conf.setHelp(true);
						return conf;
					} else if ("-p".equals(args[i])) {
						conf.setLocalPort(Integer.valueOf(args[++i]));
					} else if ("-P".equals(args[i])) {
						conf.setDestinationPort(Integer.valueOf(args[++i]));
					} else if ("-H".equals(args[i])) {
						conf.setDestinationHost(args[++i]);
					} else if ("--log-all".equals(args[i])) {
						conf.setLogFromClient(true);
						conf.setLogFromServer(true);
					} else if ("--log-from-client".equals(args[i])) {
						conf.setLogFromClient(true);
					} else if ("--log-from-server".equals(args[i])) {
						conf.setLogFromServer(true);
					} else {
						conf.setError(true);
						return conf;
					}
				}
			} catch (Exception e) {
				conf.setError(true);
				return conf;
			}
			return conf;
		}
		
		public static void validateConf(ParamsConfigurations conf) {
			if (!conf.isHelp() && (conf.getDestinationPort() < 0 || conf.getLocalPort() < 0 || conf.getDestinationHost() == null)) {
				conf.setError(true);
			}
		}
		
		public boolean isError() {
			return error;
		}
		
		public void setError(boolean error) {
			this.error = error;
		}
		
		public boolean isHelp() {
			return help;
		}
		
		public void setHelp(boolean help) {
			this.help = help;
		}

		public int getLocalPort() {
			return localPort;
		}
		public void setLocalPort(int localPort) {
			this.localPort = localPort;
		}
		public int getDestinationPort() {
			return destinationPort;
		}
		public void setDestinationPort(int destinationHost) {
			this.destinationPort = destinationHost;
		}

		public String setDestinationHost() {
			return destinationHost;
		}

		public void setDestinationHost(String destinationHost) {
			this.destinationHost = destinationHost;
		}
		
		public String getDestinationHost() {
			return destinationHost;
		}

		public boolean isLogFromClient() {
			return logFromClient;
		}

		public void setLogFromClient(boolean logFromClient) {
			this.logFromClient = logFromClient;
		}

		public boolean isLogFromServer() {
			return logFromServer;
		}

		public void setLogFromServer(boolean logFromServer) {
			this.logFromServer = logFromServer;
		}

	}
}

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;

public class Server{
	static public int BUFSIZ = 1024; // 缓冲区最大字节数
	static public int CONNECT_RETRIES = 5; // 尝试与目标主机连接次数
	static public int CONNECT_PAUSE = 5; // 每次建立连接的间隔时间
	static public int TIMEOUT = 50; // 每次尝试连接的最大时间

	static public String serverAddr = "yourProxyServerAddr"; 
	static public int serverPort = yourProxyServerPort;
	public static void main(String[] args) {
		(new Server()).run();
	}

 
	public void run() {
		try {

			Socket socket;
			int httpPort = 9093;
			ServerSocket serverSocket = new ServerSocket(9093);
			Log.debug("代理服务器启动，监听端口：" + httpPort);
			
			try {
				while ((socket = serverSocket.accept()) != null) {
					(new Handler(socket)).start();
				}
			} catch (IOException e) {
				e.printStackTrace(); // TODO: implement catch
			}
		} catch (IOException e) {
			e.printStackTrace(); // TODO: implement catch
			return;
		}
	}

	public static class Handler extends Thread {

		private final Socket clientSocket;
		
		private  ExecutorService cachedThreadPool = Executors.newCachedThreadPool();  

		public Handler(Socket clientSocket) {
			this.clientSocket = clientSocket;
		}

		@Override
		public void run() {

			String request;
			try {
				request = readHeader(clientSocket);
				Log.debug(request);
 
				Socket forwardSocket =  SSLContext.getDefault().getSocketFactory().createSocket(serverAddr, serverPort);;
 
		      	forwardSocket.getOutputStream().write(request.getBytes());
				pipe(clientSocket,forwardSocket);

			} catch (IOException | NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		private String readHeader(Socket socket) throws IOException {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

			int next;
		    int currPos = -1;

			while ((next = socket.getInputStream().read()) != -1) {
				currPos++;
				byteArrayOutputStream.write(next);

		 		if (currPos >= 3 && next == 0x0a) {
					byte[] buffer = byteArrayOutputStream.toByteArray();

					if (buffer[currPos - 1] == 0x0d
							|| buffer[currPos - 2] == 0x0a
							|| buffer[currPos - 3] == 0x0d) {
						
						break;
					}
				}
			}

			String reqHeader = byteArrayOutputStream.toString("ISO-8859-1");
			return reqHeader;
		}
		
		private void closeSocket(final Socket clientSocket,
				final Socket chainSocket, final PipeStatus pipeStatus)
				throws IOException {
			if(pipeStatus.isClientReadDone() && pipeStatus.isClientWriteDone()){
				clientSocket.close();
				chainSocket.close();
				
				Log.debug("Done close...");
			}
		}

		private void pipe(final Socket clientSocket, final Socket chainSocket) {

			  final PipeStatus  pipeStatus = new PipeStatus();
 
			
			cachedThreadPool.execute(new Runnable(){
				 public void run() {
			        	int length;
						byte bytes[] = new byte[BUFSIZ];
						  
						InputStream cis;
						try {
							cis = clientSocket.getInputStream();
							OutputStream sos = chainSocket.getOutputStream();
							
							while(true){
					        	if ((length = cis.read(bytes)) > 0) {

									sos.write(bytes, 0, length);

								} 
					        	
								if(length == -1){
									pipeStatus.setClientReadDone(true);
									
									closeSocket(clientSocket,chainSocket,pipeStatus);
									break;
								}
							}

				        	
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			}}
			);
			     
			cachedThreadPool.execute(new Runnable(){
			     public void run() {
			        	int length;
						byte bytes[] = new byte[BUFSIZ];
						  
						InputStream sis;
						try {
							sis = chainSocket.getInputStream();
							OutputStream cos = clientSocket.getOutputStream();
							
							while(true){
								if ((length = sis.read(bytes)) > 0) {
									cos.write(bytes, 0, length);

								}
								
								
								if(length == -1){
									pipeStatus.setClientWriteDone(true);
									closeSocket(clientSocket,chainSocket,pipeStatus);
									break;
								}
							}

						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
			        }
			});
			    
		}
	}
}
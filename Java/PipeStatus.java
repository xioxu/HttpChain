import java.net.Proxy;

import sun.net.www.http.HttpClient;

import com.sun.net.httpserver.HttpServer;

public class PipeStatus {

	private boolean clientReadDone;
	private boolean clientWriteDone;

	public boolean isClientReadDone() {
		return clientReadDone;
	}

	public void setClientReadDone(boolean clientReadDone) {
		this.clientReadDone = clientReadDone;
	}

	public boolean isClientWriteDone() {
		return clientWriteDone;
	}

	public void setClientWriteDone(boolean clientWriteDone) {
		this.clientWriteDone = clientWriteDone;
	}

}

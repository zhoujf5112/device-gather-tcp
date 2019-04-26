package com.cnten.handler;

import com.cnten.resolve.AbstractResolve;
import com.cnten.utils.HexUtil;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

/**
 * Created by XHD on 2018/3/15.
 */
public class CntenAsyncHandler implements Handler<Buffer> {
	private NetSocket socket = null;
	private AbstractResolve abstractResolve = null;
	public CntenAsyncHandler(NetSocket socket,AbstractResolve abstractResolve) {
		this.socket = socket;
		this.abstractResolve = abstractResolve;
	}

	@Override
	public void handle(Buffer event) {
		String value = event.getString(0, event.length());
//		String value = HexUtil.encodeHexStr(event.getBytes(), false);
		abstractResolve.resolve(value,socket);
		//socket.close();
	}
}

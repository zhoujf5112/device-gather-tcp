package com.cnten.resolve;

import io.vertx.core.net.NetSocket;

public interface AbstractResolve {
     void resolve(String data,NetSocket socket);
}

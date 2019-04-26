/*
 * Copyright 2017 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.cnten.vertx;

import com.cnten.resolve.impl.DeviceResolve;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.cnten.handler.CntenAsyncHandler;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;


@Component
public class SpringVerticleManager extends AbstractVerticle {
	@Value("${vertx.http.port}")
	private int httpPort;
	@Value("${vertx.http.address}")
	private String address;

	/**
	 * A standard verticle, consuming the {@link ResolveManager} over the event
	 * bus to expose a REST API.
	 *
	 * @author Thomas Segismont
	 */
	@Autowired
	private DeviceResolve datangResolve;
	@Override
	public void start(Future<Void> startFuture) throws Exception {
		vertx.createNetServer().connectHandler(socket -> {
			socket.handler(new CntenAsyncHandler(socket, datangResolve));
		}).listen(httpPort, address, res -> {
			if (res.succeeded()) {
				System.out.println("test ========================: " + httpPort);
			} else {
				System.out.println("socket Failed to bind!");
			}
		});
	}

}

package com.albin.mqtt;

import java.net.InetSocketAddress;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import com.albin.mqtt.message.ConnectMessage;
import com.albin.mqtt.message.DisconnectMessage;
import com.albin.mqtt.message.PingReqMessage;
import com.albin.mqtt.message.PublishMessage;
import com.albin.mqtt.message.QoS;
import com.albin.mqtt.message.SubscribeMessage;
import com.albin.mqtt.message.UnsubscribeMessage;
import com.albin.mqtt.netty.MqttMessageDecoder;
import com.albin.mqtt.netty.MqttMessageEncoder;
import com.albin.mqtt.netty.MqttMessageHandler;

public class IsidoreyClient {
	
	private Channel channel;
	private ClientBootstrap bootstrap;
	private MqttListener listener;
	private MqttMessageHandler handler;
	private Timer timer;
	private final ConnConfig config;
	
	public IsidoreyClient(ConnConfig config){
		this.config = config;
	}

	
	public void setListener(MqttListener listener) {
		this.listener = listener;
		if (handler != null) {
			handler.setListener(listener);
		}
	}

	public void connect(){
		connect(config.getClientId(), config.getHost(), 
				config.getPort(), config.getKeepAlive(),
				config.getUsername(), config.getPassword());
	}
	
	public void connect(String id, String host, int port, int keepAlive, 
			String username, String password) {
		try {
			bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(
					Executors.newCachedThreadPool(),
					Executors.newCachedThreadPool()));

			handler = new MqttMessageHandler();
			handler.setListener(listener);
			bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

				public ChannelPipeline getPipeline() throws Exception {
					return Channels.pipeline(new MqttMessageEncoder(),
							new MqttMessageDecoder(), handler);
				}
			});

			bootstrap.setOption("tcpNoDelay", true);
			bootstrap.setOption("keepAlive", true);

			ChannelFuture future = bootstrap.connect(new InetSocketAddress(
					host, port));

			channel = future.awaitUninterruptibly().getChannel();
			if (!future.isSuccess()) {
				future.getCause().printStackTrace();
				bootstrap.releaseExternalResources();
				return; 
			}
			
			ConnectMessage cm = new ConnectMessage(id, true, keepAlive);
			if(username != null  && password != null){
				cm.setCredentials(username, password);
			}
			channel.write(cm);
			
			establishKeepAlive(keepAlive);
			// TODO: Should probably wait for the ConnAck message
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void establishKeepAlive(int seconds) {
		int keepAliveMillis = seconds * 1000;
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				ping();
			}
		}, keepAliveMillis, keepAliveMillis);

	}
	
	private void reconnect() {
		System.err.println("Reconnecting Client");
		connect();
		subscribeToConfigTopics();
	}

	public void subscribeToConfigTopics(){
		for(Entry<String, QoS> entry : config.getSubscribedTopics().entrySet()){
			System.err.println("Resubscribing on topic: "+ entry.getKey());
			subscribe(entry.getKey(), entry.getValue());
		}
	}
	
	public void disconnect() {
		channel.write(new DisconnectMessage()).awaitUninterruptibly();
		channel.close().awaitUninterruptibly();
		bootstrap.releaseExternalResources();
	}

	
	public void subscribe(String topic, QoS qos) {
		channel.write(new SubscribeMessage(topic, qos));
	}


	public void unsubscribe(String topic) {
		channel.write(new UnsubscribeMessage(topic));
	}


	public void publish(String topic, String msg, QoS qos) {
		channel.write(new PublishMessage(topic, msg, qos));
	}

	
	public void ping() {
		if(!channel.isConnected()){
			System.err.println("MQtt Channel disconnected");
			timer.cancel();
			reconnect();
		}
		channel.write(new PingReqMessage());
	}


}

package com.albin.mqtt;

import java.util.HashMap;
import java.util.Map;

import com.albin.mqtt.message.QoS;

public class ConnConfig {

	private String clientId;
	private String host;
	private int port;
	private int keepAlive;
	private String username;
	private String password;
	private Map<String, QoS> subscribedTopics = new HashMap<String, QoS>();

	
	
	public String getClientId() {
		return clientId;
	}public ConnConfig(String clientId, String host, int port, int keepAlive,
			String username, String password) {
		super();
		this.clientId = clientId;
		this.host = host;
		this.port = port;
		this.keepAlive = keepAlive;
		this.username = username;
		this.password = password;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public int getKeepAlive() {
		return keepAlive;
	}
	public void setKeepAlive(int keepAlive) {
		this.keepAlive = keepAlive;
	}
	public Map<String, QoS> getSubscribedTopics() {
		return subscribedTopics;
	}
	public void setSubscribedTopics(Map<String, QoS> subscribedTopics) {
		this.subscribedTopics = subscribedTopics;
	}
	public void addSubscribedTopic(String topic, QoS qos){
		subscribedTopics.put(topic, qos);
	}
	
	
	
	
}

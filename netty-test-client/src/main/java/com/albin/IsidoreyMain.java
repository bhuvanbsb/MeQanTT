package com.albin;

import com.albin.mqtt.ConnConfig;
import com.albin.mqtt.IsidoreyClient;
import com.albin.mqtt.MqttListener;
import com.albin.mqtt.message.QoS;

public class IsidoreyMain {

	private static IsidoreyClient client;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		ConnConfig config = new ConnConfig("",
				"broker.isidorey.net", 1883, 30, "",
				"");

		config.addSubscribedTopic("#", QoS.AT_MOST_ONCE);

		client = new IsidoreyClient(config);
		client.setListener(new EchoListener());

		client.connect();
		client.subscribeToConfigTopics();
	}

	private static class EchoListener implements MqttListener {

		public void disconnected() {
			System.err.println("DISCONNECTED");
		}

		public void publishArrived(String topic, byte[] data) {
			String payload = new String(data);
			System.out.println("[" + topic + "]: " + payload);
		
			
		}

	}

}

/*
Name: Fawaz Kserawi - fawazafk@gmail.com
This is a secure mqtt publish/ subscribe that may get an RSA key file from a third party sftp server to encrypt/decrypt published messages. all mqtt packets are placed inside websocket packets with tls encryption for additional network security
 */
package mqtt.client;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Scanner;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import static mqtt.client.CryptoUtilsRSA.*;


public class IoTServicesSample implements MqttCallback {


	public static MqttAsyncClient sampleClient;
	public static String logTopic;
	public static MqttMessage message=new MqttMessage();
	public static Scanner sc = new Scanner(System.in);
	public static MqttConnectOptions connOpts;
	public static String broker,str,pubMsg=" ",decryptedString;
	public static File pKeyFile;
	public static  long startTime,endTime,processingTime;

	public static void main(String[] args) throws IOException {


		while (!(new File("pub").exists())) {
		try {

			if (!(new File("pub").exists())) {
				ChannelSftp csftp=new ChannelSftp();
				System.out.println("Trying to get pub key file from sftp");
				System.out.println("Input username for SFTP Authentication: ");
				str=sc.nextLine();
				csftp.username=str;
				System.out.println("Input password for SFTP Authentication: ");
				str=sc.nextLine();
				csftp.password=str;
				csftp.copyPubFile();


			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("File not found");
		}}
		if ((new File("pub").exists())) {
			System.out.println("Key file found");
			pKeyFile= new File("pub");
			publicKey=readFile(pKeyFile);
		}

		String deviceId = "myid"; //F: DeviceID=client id
		String oauthToken = "234";
		String msg = "Message From new wss testing";
		try {
			String dataTopic =  deviceId;
			logTopic =  deviceId;
			broker = "wss://192.168.10.10:8884"; //F:
			System.out.println("Input device Id: ");
			deviceId=sc.nextLine();

			System.out.println("input option 1 to Subscribe(default Enter) 2 to Publish 3 to change Broker/deviceId/ ");
			str=sc.nextLine();
			// Change parameters
			if (str.equals("3")) {
				System.out.println("Input broker Enter for default:"+ broker);
				broker=sc.nextLine();
				if (broker.equals("")) {
					broker="wss://192.168.10.10:8884";
				}
				System.out.println("Input topic Enter for default: "+ logTopic);
				logTopic=sc.nextLine();
				if (logTopic.equals("")) {
					logTopic="myid";
				}
				System.out.println("Input device Id Enter for default:"+ deviceId);
				deviceId=sc.nextLine();
				if (deviceId.equals("")) {
					deviceId="myid";
				}
				System.out.println("input 1 to subscribe or 2 to publish");
				str=sc.nextLine();
			}
			System.out.println("broker is: " +broker +" Device: " + deviceId);
//			sampleClient = new MqttClient(broker, deviceId, new MemoryPersistence());
			sampleClient = new MqttAsyncClient(broker, deviceId, new MemoryPersistence());
			connOpts = new MqttConnectOptions();
			connOpts.setUserName(deviceId);
			connOpts.setPassword(oauthToken.toCharArray());
			connOpts.setSocketFactory(getTrustAllSocketFactory());
			connOpts.setCleanSession(true);
//			sampleClient.connect(connOpts)

			if (str.equals("1") || str.equals("")) {
//				System.out.println("Subscribinf");
				Subscribe();
			}
			if (str.equals("2")) {
				Publish();
			}
			System.out.println("Enter to quit");
			sc.nextLine();

			sampleClient.disconnect();
			System.out.println("Disconnected");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private static SSLSocketFactory getTrustAllSocketFactory()
	throws GeneralSecurityException {
		SSLContext sslcontext = SSLContext.getInstance("TLS");
		sslcontext.init(null, new TrustManager[] { new X509TrustManager() {
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
			@Override
			public void checkClientTrusted(X509Certificate[] arg0, String arg1)
			throws CertificateException {
			}
			@Override
			public void checkServerTrusted(X509Certificate[] arg0, String arg1)
			throws CertificateException {
			}
		} }, new java.security.SecureRandom());
		return sslcontext.getSocketFactory();
	}

	public static void Publish() {
		try {
			String encryptedString="";
			MqttMessage message;
			sampleClient.connect(connOpts);
			Thread.sleep(1000);
			while (!pubMsg.equals("q")) {
				System.out.print("Input line to publish (q) to quit: ");
				pubMsg = sc.nextLine();
				startTime = System.currentTimeMillis();
				encryptedString = Base64.getEncoder().encodeToString(encrypt(pubMsg, publicKey));
				endTime = System.currentTimeMillis();
				System.out.println("Time in encryption:" + (endTime-startTime));
//				System.out.println("Publishing message on topic \"" + logTopic + "\": " + pubMsg);
				message = new MqttMessage(encryptedString.getBytes());
				message.setQos(1);
				sampleClient.publish(logTopic, message);
				System.out.println("Published message on topic \"" + logTopic + "\": " + pubMsg);
			}
			//sampleClient.disconnect();
		} catch (MqttException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void Subscribe()  {
		try {
			sampleClient.setCallback(new IoTServicesSample());
			sampleClient.connect(connOpts);
			Thread.sleep(1000);
			System.out.println("Subscribing to topic: " + logTopic);
			sampleClient.subscribe(logTopic,1);
//			String msg = sc.next();

		} catch (MqttException e) {
			e.printStackTrace();
			System.out.println("msg " + e.getMessage());
			System.out.println("loc " + e.getLocalizedMessage());
			System.out.println("cause " + e.getCause());
			System.out.println("excep " + e);
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void connectionLost(Throwable arg0) {
		System.err.println("connection lost");
	}
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		decryptedString = CryptoUtilsRSA.decrypt(new String(message.getPayload()), privateKey);
		System.out.println("from " + topic +": "+ decryptedString);
//		System.out.println("from " + topic +": "+ new String(message.getPayload()));
	}
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		System.err.println("delivery complete");
	}


}
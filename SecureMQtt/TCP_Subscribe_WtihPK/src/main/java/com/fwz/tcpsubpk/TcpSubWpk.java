/*
Name: Fawaz Kserawi ID: fawazafk@gmail.com
This is a secure mqtt subscriber that gets an RSA key file from a third party sftp server to decrypt published messages
 */
package com.fwz.tcpsubpk;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static com.fwz.tcpsubpk.CryptoUtilsRSA.*;

/**
 *
 * @author User
 */
public class TcpSubWpk implements MqttCallback {

    private final int qos = 1;
    private static String broker = "tcp://192.168.10.10:1883",str;
    private static String topic = "myid";
    private static String content = "Hello";
    private static String clientId = "tcpsubid";
    private MqttClient client;

    public static boolean usePub=false;
    public static File pKeyFile;
    public static String decryptedString="";


    public static void main(String[] args) throws MqttException, URISyntaxException, IOException {
        Scanner sc = new Scanner(System.in);
        System.out.println("Use pub key decryption? y/n");
        str=sc.nextLine();
        if (str.equals("y")) {
            usePub=true;
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
                pKeyFile = new File("pub");
                publicKey = readFile(pKeyFile);
            }
        } else {
            usePub=false;
        }

        //PubSubFawaz.Publish(content, topic, broker, clientId);
        //  SubJFrame sFrame= new SubJFrame();
        System.out.println("Input Device Id:");
        clientId=sc.nextLine();
        System.out.println("Input Broker Address. Input (Return) to choose default broker: " + broker + "\nTo Change To WS use: ws://192.168.10.10:1884");
        str = sc.nextLine();
        broker=str;
        if (str.equals("")) {
            broker = "tcp://192.168.10.10:1883";
        }


        TcpSubWpk s = new TcpSubWpk();
        System.out.println("Listening to Broker: " + broker);
    }
    public TcpSubWpk() throws MqttException {
        String host = broker;
        MqttConnectOptions conOpt = new MqttConnectOptions();
        conOpt.setCleanSession(true);
        this.client = new MqttClient(host, clientId, new MemoryPersistence());
        this.client.setCallback(this);
        this.client.connect(conOpt);
        this.client.subscribe(this.topic, qos);
    }

    public void connectionLost(Throwable cause) {
        System.out.println("Connection lost because: " + cause);
        System.exit(1);
    }
    public void deliveryComplete(IMqttDeliveryToken token) {
    }
    public void messageArrived(String topic, MqttMessage message) throws MqttException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException {
        if (usePub==true) {
            decryptedString = CryptoUtilsRSA.decrypt(new String(message.getPayload()), privateKey);
            System.out.println("from " + topic + "(Decrypted): " + decryptedString);
        } else {

//            System.out.println(String.format("from " + topic + " : ", new String(message.getPayload())));  //F: PRINT OUT THE RECIEVED COMMUNICATION
            System.out.println("from " + topic + " : " + new String(message.getPayload()));  //F: PRINT OUT THE RECIEVED COMMUNICATION
        }
    }
}

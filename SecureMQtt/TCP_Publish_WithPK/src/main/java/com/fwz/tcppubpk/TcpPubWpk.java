/*
Name: Fawaz Kserawi fawazafk@gmail.com
This is a secure mqtt subscriber that gets an RSA key file from a third party sftp server to encrypt published messages
 */
package com.fwz.tcppubpk;

import java.io.File;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Scanner;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static  com.fwz.tcppubpk.CryptoUtilsRSA.*;

/**
 *
 * @author User
 */
public class TcpPubWpk {

    private final int qos = 1;
    private static String broker = "tcp://192.168.10.10:1883",str;
    private static String topic = "myid";
    private static String content = "Hello";
    private static String clientId = "tcppubid";
    private MqttClient client;

    public static boolean usePub=false;
    public static File pKeyFile;
    public static String decryptedString="";

    public static void main(String[] args) throws MqttException, URISyntaxException {
        Scanner sc = new Scanner(System.in);
        System.out.println("Use pub key decryption? y/n");
        str=sc.nextLine();
        if (str.equals("y")) {
            usePub=true;
            try {
                if (!(new File("pub").exists())) {
                    ChannelSftp csftp = new ChannelSftp();
                    System.out.println("File not found, Copying from sftp");
                    csftp.copyPubFile();
                }
                if ((new File("pub").exists())) {
                    System.out.println("Key file found");
                    pKeyFile = new File("pub");
                    publicKey = readFile(pKeyFile);
                }

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("File not found");
            }
        } else {
            usePub=false;
        }
        System.out.println("Input Device Id:");
        clientId=sc.nextLine();

        System.out.println("Input Broker Address. Input (Enter) to choose default broker: "+ broker + "\nTo Change To WebSocket use: ws://192.168.10.10:1884");
        str = sc.nextLine();
        broker=str;
        if (str.equals("")) {
            broker = "tcp://192.168.10.10:1883";
        }

        while (!content.equals("q")) {
           System.out.print("Input Message for "+ broker + " q to exit: ");
           content=sc.next();
           TcpPubWpk.Publish(content, topic, broker, clientId);
        }
        System.exit(0);
    }

    public static void Publish(String content, String topic, String broker, String clientId) {
        String encryptedString="";
        int qos = 1;
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            sampleClient.connect(connOpts);
            if (usePub==true) {
                encryptedString = Base64.getEncoder().encodeToString(encrypt(content, publicKey));
                content=encryptedString;
            }

            MqttMessage message = new MqttMessage(content.getBytes());

            message.setQos(qos);


            sampleClient.publish(topic, message);
            System.out.println("Message published");
            sampleClient.disconnect();
        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
    }

}
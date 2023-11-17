package com.ieseljust.psp.client.communications;

import com.ieseljust.psp.client.CurrentConfig;
import com.ieseljust.psp.client.Message;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;

public class communicationManager {

    public static JSONObject sendServer(String msg) throws IOException {
        JSONObject response = new JSONObject();

        String serverAddress = "127.0.0.1";
        int serverPort = 9999;

        try (Socket socket = new Socket(serverAddress, serverPort);
             OutputStream output = socket.getOutputStream();
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output));
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("Conexión establecida: ");

            // Envía el mensaje al servidor
            writer.write(msg + "\n"); // Añade un salto de línea al mensaje para marcar su final
            writer.flush();

            // Lee la respuesta del servidor
            StringBuilder responseString = new StringBuilder();
            String line=reader.readLine();
            
            /*while ((line = reader.readLine()) != null) {
                responseString.append(line);
            }*/

            // Convierte la respuesta en un objeto JSON
            response = new JSONObject(line);

        } catch (IOException | JSONException e) {
            System.out.println("Error de comunicación: " + e.getMessage());
        }

        return response;
    }

    public static void connect() throws JSONException, communicationManagerException, IOException {
        JSONObject registrationMessage = new JSONObject();
        registrationMessage.put("command", "register");
        registrationMessage.put("user", CurrentConfig.username());
        registrationMessage.put("listenPort", CurrentConfig.listenPort());

        System.out.println("Conectando al servidor...");
        JSONObject response = sendServer(registrationMessage.toString());
        System.out.println(response.toString());
    }

    public static void sendMessage(Message m) throws communicationManagerException, IOException {
        sendServer(m.toJSONCommand().toString());

    }
}



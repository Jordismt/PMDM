package com.ieseljust.psp.client.communications;

import com.ieseljust.psp.client.CurrentConfig;
import com.ieseljust.psp.client.Message;
import com.ieseljust.psp.client.ViewModel;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import org.json.*;

public class serverListener implements Runnable {

    private ViewModel vm;

    public serverListener(ViewModel vm) {
        this.vm = vm;
    }

    @Override
    public void run() {
        try (ServerSocket listener = new ServerSocket(0)) {
            CurrentConfig.setlistenPort(listener.getLocalPort());

            while (true) {
                Socket clientSocket = listener.accept();
                handleClientRequest(clientSocket);
            }
        } catch (IOException e) {
            System.out.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }

    private void handleClientRequest(Socket clientSocket) {
        try (
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))
        ) {
            StringBuilder message = new StringBuilder();
            String line=reader.readLine();
            
            message.append(line);
            

            JSONObject jsonMessage = new JSONObject(message.toString());
            String messageType = jsonMessage.getString("type");

            if ("userlist".equals(messageType)) {
                JSONArray userList = jsonMessage.getJSONArray("content");
                ArrayList<String> users = new ArrayList<>();
                for (int i = 0; i < userList.length(); i++) {
                    users.add(userList.getString(i));
                }
                vm.updateUserList(users);
            } else if ("message".equals(messageType)) {
                String username = jsonMessage.getString("user");
                String messageContent = jsonMessage.getString("content");
                Message msg = new Message(username, messageContent);
                vm.addMessage(msg);
                // Procesar el mensaje y actualizar el ViewModel según sea necesario
            }

            writer.write("{'status' : 'ok'}"); // Enviar respuesta al cliente
            writer.newLine(); // Agregar una nueva línea para indicar el final del mensaje
            writer.flush();
        } catch (IOException | JSONException e) {
            System.out.println("Error al manejar la conexión entrante: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Error al cerrar el socket del cliente: " + e.getMessage());
            }
        }
    }
}


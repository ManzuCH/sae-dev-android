package com.example.myapplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class LectureClasse {
    private String hostname;
    private int port;

    public LectureClasse(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public String recupererListeEtudiants(String nomGroupe) {
        StringBuilder resultat = new StringBuilder();

        try {
            Socket socket = new Socket(hostname, port);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );

            out.println(nomGroupe);

            String ligne;
            while ((ligne = in.readLine()) != null) {
                resultat.append(ligne).append("\n");
            }

            in.close();
            out.close();
            socket.close();

        } catch (IOException e) {
            return "Erreur de connexion : " + e.getMessage();
        }

        return resultat.toString();
    }

    public String envoyerPresences(String nomGroupe,
                                   ArrayList<String> etudiants,
                                   ArrayList<Boolean> presents) {
        StringBuilder resultat = new StringBuilder();

        try {
            Socket socket = new Socket(hostname, port);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );

            out.println("PRESENCE;" + nomGroupe);

            for (int i = 0; i < etudiants.size(); i++) {
                String status;

                if (presents.get(i)) {
                    status = "présent";
                } else {
                    status = "absent";
                }

                out.println(etudiants.get(i) + " : " + status);
            }

            out.println("FIN");

            String ligne;
            while ((ligne = in.readLine()) != null) {
                resultat.append(ligne).append("\n");
            }

            in.close();
            out.close();
            socket.close();

        } catch (IOException e) {
            return "Erreur de connexion : " + e.getMessage();
        }

        return resultat.toString();
    }
}
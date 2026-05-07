package com.example.myapplication;

import java.util.ArrayList;

public class EnvoiPresenceThread extends Thread {
    private String hostname;
    private int port;
    private String nomGroupe;
    private ArrayList<String> etudiants;
    private ArrayList<Boolean> presents;
    private TaskListener listener;

    public EnvoiPresenceThread(String hostname,
                               int port,
                               String nomGroupe,
                               ArrayList<String> etudiants,
                               ArrayList<Boolean> presents,
                               TaskListener listener) {
        this.hostname = hostname;
        this.port = port;
        this.nomGroupe = nomGroupe;
        this.etudiants = etudiants;
        this.presents = presents;
        this.listener = listener;
    }

    @Override
    public void run() {
        LectureClasse explorateur = new LectureClasse(hostname, port);

        String result = explorateur.envoyerPresences(
                nomGroupe,
                etudiants,
                presents
        );

        listener.onTaskComplete("ENVOI_PRESENCE\n" + result);
    }
}
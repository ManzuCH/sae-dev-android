package com.example.myapplication;

public class MonThread extends Thread {
    private String hostname;
    private int port;
    private String nomGroupe;
    private TaskListener listener;

    public MonThread(String hostname, int port, String nomGroupe, TaskListener listener) {
        this.hostname = hostname;
        this.port = port;
        this.nomGroupe = nomGroupe;
        this.listener = listener;
    }

    @Override
    public void run() {
        LectureClasse explorateur = new LectureClasse(hostname, port);
        String result = explorateur.recupererListeEtudiants(nomGroupe);
        listener.onTaskComplete(result);
    }
}
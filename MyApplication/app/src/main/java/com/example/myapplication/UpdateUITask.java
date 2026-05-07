package com.example.myapplication;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

public class UpdateUITask implements Runnable {
    private TextView textView;
    private ProgressBar progressBar;
    private ListView listView;
    private ArrayList<String> lignes;
    private ArrayList<Boolean> presents;
    private ArrayAdapter<String> adapter;
    private String result;

    public UpdateUITask(TextView textView,
                        ProgressBar progressBar,
                        ListView listView,
                        ArrayList<String> lignes,
                        ArrayList<Boolean> presents,
                        ArrayAdapter<String> adapter,
                        String result) {
        this.textView = textView;
        this.progressBar = progressBar;
        this.listView = listView;
        this.lignes = lignes;
        this.presents = presents;
        this.adapter = adapter;
        this.result = result;
    }

    @Override
    public void run() {
        lignes.clear();
        presents.clear();

        if (result == null || result.length() == 0) {
            lignes.add("Aucune donnée reçue.");
            presents.add(false);
            textView.setText("Réponse vide du serveur.");
        } else {
            String[] tableauLignes = result.split("\n");

            for (int i = 0; i < tableauLignes.length; i++) {
                String ligne = tableauLignes[i].trim();

                if (ligne.length() > 0) {
                    String ligneMinuscule = ligne.toLowerCase();

                    if (ligne.startsWith("Erreur")) {
                        lignes.add(ligne);
                        presents.add(false);
                    } else if (ligneMinuscule.contains("nom") && ligneMinuscule.contains("tp")) {
                        // On ignore l'en-tête du CSV
                    } else {
                        String[] colonnes = ligne.split(";", -1);

                        if (colonnes.length >= 8) {
                            String nom = colonnes[4].trim();
                            String prenom = colonnes[6].trim();
                            String groupeTP = colonnes[colonnes.length - 1].trim();

                            String affichage = nom + " " + prenom + " (" + groupeTP + ")";

                            lignes.add(affichage);
                            presents.add(true);
                        }
                    }
                }
            }

            textView.setText("Décochez les absents :");
        }

        adapter.notifyDataSetChanged();

        for (int i = 0; i < presents.size(); i++) {
            listView.setItemChecked(i, presents.get(i));
        }

        progressBar.setVisibility(View.GONE);
    }
}
package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements TaskListener, AdapterView.OnItemClickListener {

    private TextView textView;
    private ProgressBar progressBar;
    private ListView listView;

    private ArrayList<String> lignesCSV;
    private ArrayList<Boolean> presents;
    private ArrayAdapter<String> adapter;

    private String groupeCourant;

    private final String SERVER_IP = "10.0.2.2";
    private final int SERVER_PORT = 5001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.messageID);
        progressBar = findViewById(R.id.progressBarID);
        listView = findViewById(R.id.listViewID);

        lignesCSV = new ArrayList<>();
        presents = new ArrayList<>();
        groupeCourant = "";

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_multiple_choice,
                lignesCSV
        );

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        progressBar.setVisibility(View.GONE);
        textView.setText("Sélectionnez une classe :");
    }

    public void reagir(View view) {
        Button bouton = (Button) view;
        groupeCourant = bouton.getText().toString();

        textView.setText("Chargement de " + groupeCourant + ".csv...");
        progressBar.setVisibility(View.VISIBLE);

        lignesCSV.clear();
        presents.clear();
        adapter.notifyDataSetChanged();

        MonThread thread = new MonThread(
                SERVER_IP,
                SERVER_PORT,
                groupeCourant,
                this
        );

        thread.start();
    }

    public void envoyerPresences(View view) {
        if (groupeCourant.length() == 0) {
            textView.setText("Sélectionnez d'abord une classe.");
        } else if (lignesCSV.size() == 0) {
            textView.setText("Aucune liste à envoyer.");
        } else {
            textView.setText("Envoi des présences au serveur...");
            progressBar.setVisibility(View.VISIBLE);

            EnvoiPresenceThread thread = new EnvoiPresenceThread(
                    SERVER_IP,
                    SERVER_PORT,
                    groupeCourant,
                    lignesCSV,
                    presents,
                    this
            );

            thread.start();
        }
    }

    @Override
    public void onTaskComplete(String result) {
        if (result.startsWith("ENVOI_PRESENCE")) {
            String message = result.replace("ENVOI_PRESENCE\n", "");

            UpdateMessageTask updateMessageTask = new UpdateMessageTask(
                    textView,
                    progressBar,
                    message
            );

            runOnUiThread(updateMessageTask);
        } else {
            UpdateUITask updateUITask = new UpdateUITask(
                    textView,
                    progressBar,
                    listView,
                    lignesCSV,
                    presents,
                    adapter,
                    result
            );

            runOnUiThread(updateUITask);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        boolean isChecked = listView.isItemChecked(position);

        presents.set(position, isChecked);

        String nom = lignesCSV.get(position);
        String status;

        if (isChecked) {
            status = "présent";
        } else {
            status = "absent";
        }

        textView.setText(nom + " : " + status);
        System.out.printf("L'étudiant %s est %s\n", nom, status);
    }
}
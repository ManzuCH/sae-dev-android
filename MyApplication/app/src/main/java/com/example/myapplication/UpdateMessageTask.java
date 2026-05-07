package com.example.myapplication;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class UpdateMessageTask implements Runnable {
    private TextView textView;
    private ProgressBar progressBar;
    private String message;

    public UpdateMessageTask(TextView textView,
                             ProgressBar progressBar,
                             String message) {
        this.textView = textView;
        this.progressBar = progressBar;
        this.message = message;
    }

    @Override
    public void run() {
        textView.setText(message);
        progressBar.setVisibility(View.GONE);
    }
}
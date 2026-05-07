import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private TextView textView;
    private final String SERVER_IP = "10.0.2.2";
    private final int SERVER_PORT = 5001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.messageID);
    }

    public void reagir(View view) {
        Button b = (Button) view;
        String nomClasseChoisie = b.getText().toString();

        LectureClasse explorateur = new LectureClasse(SERVER_IP, SERVER_PORT);
        String resultat = explorateur.recupererListeEtudiants(nomClasseChoisie);

        textView.setText(resultat);
    }
}

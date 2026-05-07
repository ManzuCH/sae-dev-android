import java.io.*;
import java.net.*;

public class LectureClasse {
    private String hostname;
    private int port;

    public LectureClasse(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public String recupererListeEtudiants(String nomGroupe) {
        StringBuilder resultat = new StringBuilder();

        try (Socket socket = new Socket(hostname, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // On envoie le nom du groupe (ex: RT1FI) au serveur C
            out.println(nomGroupe);

            // On lit la réponse du serveur ligne par ligne
            String ligne;
            while ((ligne = in.readLine()) != null) {
                resultat.append(ligne).append("\n");
            }

        } catch (IOException e) {
            return "Erreur de connexion : " + e.getMessage();
        }

        return resultat.toString();
    }
}


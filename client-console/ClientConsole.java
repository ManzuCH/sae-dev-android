import java.util.ArrayList;
import java.util.Scanner;

public class ClientConsole {

    public static String formaterLigneCSV(String ligne) {
        String lignePropre = ligne.trim();

        if (lignePropre.length() == 0) {
            return "";
        }

        if (lignePropre.startsWith("Erreur")) {
            return lignePropre;
        }

        String ligneMinuscule = lignePropre.toLowerCase();

        if (ligneMinuscule.contains("nom") && ligneMinuscule.contains("tp")) {
            return "";
        }

        String[] colonnes = lignePropre.split(";", -1);

        if (colonnes.length >= 8) {
            String nom = colonnes[4].trim();
            String prenom = colonnes[6].trim();
            String groupeTP = colonnes[colonnes.length - 1].trim();

            return nom + " " + prenom + " (" + groupeTP + ")";
        }

        return lignePropre;
    }

    public static ArrayList<String> construireListeEtudiants(String listeBrute) {
        ArrayList<String> etudiants = new ArrayList<>();
        String[] lignes = listeBrute.split("\n");

        for (int i = 0; i < lignes.length; i++) {
            String ligneFormatee = formaterLigneCSV(lignes[i]);

            if (ligneFormatee.length() > 0) {
                etudiants.add(ligneFormatee);
            }
        }

        return etudiants;
    }

    public static void main(String[] args) {
        String host = "localhost";
        int port = 5001;

        LectureClasse explorateur = new LectureClasse(host, port);
        Scanner sc = new Scanner(System.in);

        System.out.println("=== CLIENT CONSOLE JAVA ===");
        System.out.print("Entrez le nom du groupe (ex: RT1FI, RT1FA, RT2FI, RT2FA) : ");
        String groupe = sc.nextLine();

        String listeBrute = explorateur.recupererListeEtudiants(groupe);
        ArrayList<String> etudiants = construireListeEtudiants(listeBrute);

        if (etudiants.size() == 0) {
            System.out.println("Aucun étudiant reçu.");
        } else if (etudiants.get(0).startsWith("Erreur")) {
            System.out.println(etudiants.get(0));
        } else {
            Presence presence = new Presence(etudiants);

            presence.faireAppel(sc);
            presence.afficherBilan();

            System.out.println();
            System.out.println("Envoi des présences au serveur...");

            String reponseServeur = explorateur.envoyerPresences(
                    groupe,
                    presence.getEtudiants(),
                    presence.getPresents()
            );

            System.out.println(reponseServeur);
        }

        sc.close();
    }
}
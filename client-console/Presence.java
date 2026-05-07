import java.util.ArrayList;
import java.util.Scanner;

public class Presence {
    private ArrayList<String> etudiants;
    private ArrayList<Boolean> presents;

    public Presence(ArrayList<String> etudiants) {
        this.etudiants = etudiants;
        this.presents = new ArrayList<>();

        for (int i = 0; i < etudiants.size(); i++) {
            presents.add(true);
        }
    }

    public void faireAppel(Scanner sc) {
        System.out.println();
        System.out.println("=== APPEL DES ETUDIANTS ===");
        System.out.println("Répondez y pour présent ou n pour absent.");

        for (int i = 0; i < etudiants.size(); i++) {
            System.out.print(etudiants.get(i) + " présent ? y/n : ");
            String reponse = sc.nextLine();

            if (reponse.equals("n") || reponse.equals("N")) {
                presents.set(i, false);
            } else {
                presents.set(i, true);
            }

            System.out.printf("L'étudiant %s est %s\n", etudiants.get(i), getStatut(i));
        }
    }

    public void afficherBilan() {
        System.out.println();
        System.out.println("=== BILAN DES PRESENCES ===");

        for (int i = 0; i < etudiants.size(); i++) {
            System.out.println(etudiants.get(i) + " : " + getStatut(i));
        }

        System.out.println("===========================");
    }

    public ArrayList<String> getEtudiants() {
        return etudiants;
    }

    public ArrayList<Boolean> getPresents() {
        return presents;
    }

    private String getStatut(int index) {
        if (presents.get(index)) {
            return "présent";
        } else {
            return "absent";
        }
    }
}
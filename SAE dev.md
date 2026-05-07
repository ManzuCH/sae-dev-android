# SAE dev

Ce projet implante une architecture Client/Serveur permettant de récupérer des fichiers CSV stockés sur un serveur Linux depuis un client console (PC) ou une application mobile (Android).

## Programe Serveur

### simpleServeurSocket.c

```c
/* simpleServerSocket.c */
#include <stdio.h>
#include <stdlib.h>
#include <string.h> 
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>

int main( int argc, char *argv[] )
{
    int sockfd, newsockfd, portno;
    unsigned int clilen;
    char buffer[256];
    char filepath[300]; 
    struct sockaddr_in serv_addr, cli_addr;
    int n;
    FILE *fp;

    sockfd = socket(PF_INET, SOCK_STREAM, 0);
    bzero((char *) &serv_addr, sizeof(serv_addr));
    portno = 5001;

    serv_addr.sin_family = AF_INET;
    serv_addr.sin_addr.s_addr = INADDR_ANY;
    serv_addr.sin_port = htons(portno);

    bind(sockfd, (struct sockaddr *) &serv_addr, sizeof(serv_addr));
    listen(sockfd,5);
    clilen = sizeof(cli_addr);

    while (1)
    {
        newsockfd = accept(sockfd, (struct sockaddr *)&cli_addr, &clilen);

        bzero(buffer, 256);
        n = read(newsockfd, buffer, 255);
        
        // Nettoyage du nom de fichier reçu
        buffer[strcspn(buffer, "\n\r")] = 0;
        
        snprintf(filepath, sizeof(filepath), "CSV/%s.csv", buffer);
        printf("Envoi du fichier : %s\n", filepath);

        fp = fopen(filepath, "r");
        if (fp == NULL) {
            write(newsockfd, "Erreur : Fichier introuvable.\n", 30);
        } else {
            bzero(buffer, 256);
            // On lit le fichier et on l'envoie morceau par morceau
            while (fgets(buffer, 255, fp) != NULL) {
                write(newsockfd, buffer, strlen(buffer));
                bzero(buffer, 256);
            }
            fclose(fp);
        }

        // CRITIQUE : Fermer le socket client pour dire "J'ai fini"
        close(newsockfd); 
    }

    close(sockfd);
    return 0;
}
```

Le serveur est le point d'entrée. Il écoute sur le port **5001** et attend un nom de groupe.

- **Gestion des fichiers** : Il cherche les fichiers dans un sous-dossier nommé `CSV/`.
- **Protocole** :
  1. Reçoit le nom (ex: "RT1FI").
  2. Nettoie les caractères de fin de ligne (`\n`, `\r`).
  3. Tente d'ouvrir `CSV/RT1FI.csv`.
  4. Envoie le contenu ligne par ligne ou un message d'erreur si le fichier est absent.
- **Point Critique** : La commande `close(newsockfd)` en fin de boucle est essentielle pour signaler au client Java que le transfert est terminé, ce qui débloque la lecture (`in.readLine() == null`).



## Programe Client

### classe global :

#### LectureClasse.java

```java
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

```

C'est le "moteur" réseau réutilisable.

- **Encapsulation** : Cette classe masque la complexité des Sockets.
- **Méthode `recupererListeEtudiants`** :
  - Ouvre une connexion vers l'hôte et le port spécifiés.
  - Envoie le nom du groupe via un `PrintWriter`.
  - Récupère la réponse via un `BufferedReader` et reconstruit la chaîne complète avec un `StringBuilder`.

### Programe CLI :

#### ClientConsole.java 

```java
import java.util.Scanner;

public class ClientConsole {
    public static void main(String[] args) {
        
            // Configuration du serveur
        String host = "localhost";
        int port = 5001; // Remplace par le port de ton serveur C

        // Instance de notre classe de service
        LectureClasse explorateur = new LectureClasse(host, port);
        Scanner sc = new Scanner(System.in);

        System.out.println("=== CLIENT CONSOLE JAVA ===");
        System.out.print("Entrez le nom du groupe (ex: RT1FI, RT2FI) : ");
        String groupe = sc.nextLine();

        // Appel de la logique
        String liste = explorateur.recupererListeEtudiants(groupe);

        System.out.println("\n--- LISTE REÇUE ---");
        System.out.println(liste);
        System.out.println("-------------------");
    }
}
```

Utilisé pour tester le protocole rapidement sur Linux/PC.

- **Hôte** : `localhost` (puisque le client et le serveur tournent sur la même machine).
- **Fonctionnement** : Lit le choix de l'utilisateur via `Scanner`, appelle le moteur Java et affiche le résultat brut.

### Programe Telephone : 

#### MainActivity.java

```java
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
```

#### activity_main.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <TextView
        android:id="@+id/messageID"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="32dp"
        android:text="Sélectionnez une classe :"
        android:textAlignment="center"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <Button
        android:id="@+id/btnRT1FI"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        android:text="RT1FI"
        android:onClick="reagir"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/messageID" />

    <Button
        android:id="@+id/btnRT1FA"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:text="RT1FA"
        android:onClick="reagir"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/btnRT1FI" />

    <Button
        android:id="@+id/btnRT2FI"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:text="RT2FI"
        android:onClick="reagir"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/btnRT1FA" />

    <Button
        android:id="@+id/btnRT2FA"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:text="RT2FA"
        android:onClick="reagir"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/btnRT2FI" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

Application mobile développée sous **Android Studio**.

- **Configuration Réseau Spécifique** :

  - **IP `10.0.2.2`** : Cette adresse IP spéciale est obligatoire pour que l'émulateur puisse contacter le serveur tournant sur l'ordinateur hôte.

  - **Permission INTERNET** : Requis dans le fichier `AndroidManifest.xml`.

  - **StrictMode** : Par défaut, Android interdit les opérations réseau sur le thread principal (UI Thread) pour ne pas figer l'écran. La politique `permitAll()` est utilisée ici pour simplifier les tests de la SAE.

    

- **Interface Dynamique** : La méthode `reagir(View view)` est générique. Elle récupère le texte du bouton cliqué (ex: "RT2FI") pour demander le fichier correspondant au serveur.



## Procédure de déploiement

1. **Serveur** :

   - Créer le dossier `CSV/` et y placer les fichiers `.csv`.
   - Compiler : `gcc simpleServerSocket.c -o simpleServerSocket`.
   - Lancer : `./simpleServerSocket`.

2. **CLI**

   - `java ClientConsole.java`

3. **Android** :

   - Ajouter `LectureClasse.java` dans le même package que `MainActivity`.

   - Vérifier le port (5001) et l'IP (10.0.2.2).

   - Lancer sur un périphérique virtuel (ex: Nexus 5 API 30).

     

     

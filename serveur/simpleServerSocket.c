/* simpleServerSocket.c */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>

int main(int argc, char *argv[])
{
    int sockfd, newsockfd, portno;
    unsigned int clilen;
    char buffer[256];
    char filepath[300];
    char groupe[100];
    struct sockaddr_in serv_addr, cli_addr;
    FILE *fp;
    FILE *client;

    sockfd = socket(PF_INET, SOCK_STREAM, 0);

    bzero((char *) &serv_addr, sizeof(serv_addr));
    portno = 5001;

    serv_addr.sin_family = AF_INET;
    serv_addr.sin_addr.s_addr = INADDR_ANY;
    serv_addr.sin_port = htons(portno);

    bind(sockfd, (struct sockaddr *) &serv_addr, sizeof(serv_addr));
    listen(sockfd, 5);

    clilen = sizeof(cli_addr);

    while (1)
    {
        newsockfd = accept(sockfd, (struct sockaddr *)&cli_addr, &clilen);

        client = fdopen(newsockfd, "r+");

        if (client == NULL) {
            close(newsockfd);
            continue;
        }

        bzero(buffer, 256);

        if (fgets(buffer, 255, client) != NULL) {
            buffer[strcspn(buffer, "\n\r")] = 0;

            if (strncmp(buffer, "PRESENCE;", 9) == 0) {
                strcpy(groupe, buffer + 9);

                snprintf(filepath, sizeof(filepath), "presences_%s.txt", groupe);
                printf("Reception des presences : %s\n", filepath);

                fp = fopen(filepath, "w");

                if (fp == NULL) {
                    fprintf(client, "Erreur serveur : impossible d'enregistrer les presences.\n");
                } else {
                    fprintf(fp, "Presences du groupe %s\n", groupe);
                    fprintf(fp, "--------------------------\n");

                    bzero(buffer, 256);

                    while (fgets(buffer, 255, client) != NULL) {
                        buffer[strcspn(buffer, "\n\r")] = 0;

                        if (strcmp(buffer, "FIN") == 0) {
                            break;
                        }

                        fprintf(fp, "%s\n", buffer);
                        bzero(buffer, 256);
                    }

                    fclose(fp);
                    fprintf(client, "OK : presences enregistrees dans %s\n", filepath);
                }

                fflush(client);
            } else {
                snprintf(filepath, sizeof(filepath), "CSV/%s.csv", buffer);
                printf("Envoi du fichier : %s\n", filepath);

                fp = fopen(filepath, "r");

                if (fp == NULL) {
                    fprintf(client, "Erreur : Fichier introuvable.\n");
                } else {
                    bzero(buffer, 256);

                    while (fgets(buffer, 255, fp) != NULL) {
                        fprintf(client, "%s", buffer);
                        bzero(buffer, 256);
                    }

                    fclose(fp);
                }

                fflush(client);
            }
        }

        fclose(client);
    }

    close(sockfd);
    return 0;
}
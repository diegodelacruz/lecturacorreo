/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zaimella;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;

/**
 *
 * @author Diego de la Cruz <www.zaimella.com>
 */
public class GmailInbox {

    private Message[] mensajesNoLeidos;
    String saveDirectory = "D:\\CargasMasivas\\descarga";
    SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy_MM_dd");

    public static void main(String[] args) {

        GmailInbox gmail = new GmailInbox();
        gmail.LeerCorreo();

    }

    public void LeerCorreo() {

        Properties props = new Properties();

        try {
            props.load(new FileInputStream(new File("configuracion.properties")));
            Session session = Session.getDefaultInstance(props, null);

            Store store = session.getStore("imaps");
//            store.connect("smtp.gmail.com", "ddelacruz@zaimella.com", "rurouni-kenshin");
            store.connect("smtp.gmail.com", "info@zaimella.com", "Zaimella0000");

            Folder sellout = store.getFolder("SELLOUT");
            sellout.open(Folder.READ_WRITE);

            Flags leido = new Flags(Flags.Flag.SEEN);
            FlagTerm noLeido = new FlagTerm(leido, false);

            int messageCount = sellout.getMessageCount();
            mensajesNoLeidos = sellout.search(noLeido);

            System.out.println("Total Mensajes SO: " + messageCount);
            String cliente = "";

            for (Message correoSO : mensajesNoLeidos) {
                String emisor = ((InternetAddress) correoSO.getFrom()[0]).getAddress();
                //String sentDate = correoSO.getSentDate().toString();
                String sentDate = formatoFecha.format(correoSO.getSentDate());
                String contentType = correoSO.getContentType();
                String messageContent = "";
                String attachFiles = "";

                if (emisor.contains("@corporaciongpf.com")) {
                    cliente = "farcomed_corte ";
                } else if (emisor.contains("@gerardoortiz.com")) {
                    cliente = "go_corte ";
                } else if (emisor.contains("@farmaenlace.com")) {
                    cliente = "farmaenlace_corte ";
                } else if (emisor.contains("@mega-santamaria.com")) {
                    cliente = "santamaria_corte ";
                } else if (emisor.contains("@grupodifare.com")) {
                    cliente = "distrigen_corte ";
                }

                cliente = cliente + sentDate + " ";

                System.out.print("Remitente: " + emisor + ", Asunto: " + correoSO.getSubject());

                if (contentType.contains("multipart")) {
                    Multipart multiPart = (Multipart) correoSO.getContent();
                    int numberOfParts = multiPart.getCount();
                    for (int partCount = 0; partCount < numberOfParts; partCount++) {
                        MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
                        if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                            // this part is attachment
                            String fileName = part.getFileName();
                            attachFiles += fileName + ", ";
                            System.out.println(", Archivo: " + fileName.toLowerCase());
                            part.saveFile(saveDirectory + File.separator + cliente + fileName.toLowerCase().replace("zaimella", "").replace("  ", "_").replace(" ", "_").replace("___", "_").replace("__", "_"));
//                            part.saveFile(saveDirectory + File.separator + fileName);
                        } else {
                            // this part may be the message content
                            messageContent = part.getContent().toString();
                        }
                    }

                    if (attachFiles.length() > 1) {
                        attachFiles = attachFiles.substring(0, attachFiles.length() - 2);
                    }
                } else if (contentType.contains("text/plain")
                        || contentType.contains("text/html")) {
                    Object content = correoSO.getContent();
                    if (content != null) {
                        messageContent = content.toString();
                    }
                }
                correoSO.setFlag(Flags.Flag.SEEN, true);
            }

            sellout.close(true);
            store.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

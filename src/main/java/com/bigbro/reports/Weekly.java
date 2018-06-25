package com.bigbro.reports;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by market6 on 15.05.2017.
 */
public class Weekly {

    private final String SESS;

    public Weekly(String sess) {
        this.SESS = sess;
    }

    public void downloadDatas(Map<Integer, String> cityMap, LocalDate startDate, LocalDate endDate) throws InterruptedException, IOException {

        downloadAndRenameFile(cityMap, startDate, endDate);

        Map<Integer, List<Integer>> clientsMap = getClientsCount(cityMap, startDate, endDate);

        File clientStat = new File("C:\\Подработка\\Еженедельные\\clientStat.txt");

        if (clientStat.exists()) clientStat.delete();

        System.out.println("Запись в файл");
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(clientStat), "UTF-8"));

        for (Map.Entry<Integer, List<Integer>> pair : clientsMap.entrySet()) {
            int id = pair.getKey();
            List<Integer> clients = pair.getValue();
            String city = id + ";" + clients.get(0) + ";" + clients.get(1);
            writer.write(city);
            writer.newLine();
        }

        writer.flush();
        writer.close();

        System.out.println("==========  ГОТОВО  ===========");
    }

    private void downloadAndRenameFile(Map<Integer, String> cityMap, LocalDate startDate, LocalDate endDate) throws InterruptedException, IOException {
        String linkSchema = "https://" + Selenium.getSiteName() + "/finances_reports/period_to_csv/%d?start_date=%s&end_date=%s";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        System.out.println("Выгрузка файлов с данными");
        int i = 1;
        for (Map.Entry<Integer, String> pair : cityMap.entrySet()) {
            int id = pair.getKey();
            String name = pair.getValue();

            System.out.println(i + ": " + name);
            i++;

            String fullLink = String.format(linkSchema, id, startDate.format(formatter), endDate.format(formatter));

            URL url = new URL(fullLink);
            URLConnection connection = url.openConnection();

            connection.addRequestProperty("Cookie", "auth=" + SESS);

            InputStream in = connection.getInputStream();

            File destFile = new File("C:/Подработка/Еженедельные/" + id + "," + name + ".csv");

            FileOutputStream out = new FileOutputStream(destFile);

            byte[] buffer = new byte[1024];
            int len = in.read(buffer);

            while (len != -1) {
                out.write(buffer, 0, len);
                len = in.read(buffer);
            }

            in.close();
            out.close();
        }
    }


    private Map<Integer, List<Integer>> getClientsCount(Map<Integer, String> cityMap, LocalDate startDate, LocalDate endDate) throws InterruptedException, IOException {
        String linkSchema = "https://" + Selenium.getSiteName() + "/analytics/%d?start_date=%s&end_date=%s&master_id=0&user_id=0\n";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        Map<Integer, List<Integer>> result = new HashMap<>();

        System.out.println("Выгрузка по клиентам");
        int i = 1;
        for (Map.Entry<Integer, String> pair : cityMap.entrySet()) {
            int id = pair.getKey();
            String name = pair.getValue();

            String fullLink = String.format(linkSchema, id, startDate.format(formatter), endDate.format(formatter));

            Document document = Jsoup.connect(fullLink)
                    .cookie("auth", SESS)
                    .get();

            System.out.print(i + ": " + name + ": ");
            i++;

            String clientS = "0";

            Element clientElem = document.getElementsContainingOwnText("Завершенных")
                    .first();
            if (clientElem != null) {
                clientS = clientElem.parent()
                        .parent()
                        .getElementsByTag("h1")
                        .text();
            }
            System.out.print(clientS + ", ");

            String repClientS = "0";

            Element repClientElem = document.getElementsContainingOwnText("Повторные визиты")
                    .first();
            if (clientElem != null) {
                repClientS = repClientElem.previousElementSibling()
                        .text();
            }
            System.out.println(repClientS);

            int clients = Integer.parseInt(clientS);
            int newClients = clients - Integer.parseInt(repClientS);
            List<Integer> clientList = new ArrayList<>();
            clientList.add(clients);
            clientList.add(newClients);
            result.put(id, clientList);
        }
        return result;
    }
}

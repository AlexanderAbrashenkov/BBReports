package com.bigbro.reports;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

        Map<String, List<Integer>> clientsMap = getClientsCount(cityMap, startDate, endDate);

        File clientStat = new File("C:\\Подработка\\Еженедельные\\clientStat.txt");

        if (clientStat.exists()) clientStat.delete();

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(clientStat)));

        for (Map.Entry<String, List<Integer>> pair : clientsMap.entrySet()) {
            String name = pair.getKey();
            List<Integer> clients = pair.getValue();
            String city = name + "\t" + clients.get(0) + "\t" + clients.get(1);
            writer.write(city);
            writer.newLine();
        }

        writer.flush();
        writer.close();

    }

    private void downloadAndRenameFile(Map<Integer, String> cityMap, LocalDate startDate, LocalDate endDate) throws InterruptedException, IOException {
        String linkSchema = "https://" + Selenium.getSiteName() + "/finances_reports/period_to_csv/%d?start_date=%s&end_date=%s";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        for (Map.Entry<Integer, String> pair : cityMap.entrySet()) {
            int id = pair.getKey();
            String name = pair.getValue();

            String fullLink = String.format(linkSchema, id, startDate.format(formatter), endDate.format(formatter));

            URL url = new URL(fullLink);
            URLConnection connection = url.openConnection();

            connection.addRequestProperty("Cookie", "auth=" + SESS);

            InputStream in = connection.getInputStream();

            File destFile = new File("C:/Подработка/Еженедельные/" + name + ".csv");

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


    private Map<String, List<Integer>> getClientsCount(Map<Integer, String> cityMap, LocalDate startDate, LocalDate endDate) throws InterruptedException, IOException {
        String linkSchema = "https://" + Selenium.getSiteName() + "/analytics/%d?start_date=%s&end_date=%s&master_id=0&user_id=0\n";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        Map<String, List<Integer>> result = new HashMap<>();

        for(Map.Entry<Integer, String> pair : cityMap.entrySet()) {
            int id = pair.getKey();
            String name = pair.getValue();

            String fullLink = String.format(linkSchema, id, startDate.format(formatter), endDate.format(formatter));

            Document document = Jsoup.connect(fullLink)
                    .cookie("auth", SESS)
                    .get();

            String clientS = document.getElementsContainingOwnText("Завершенных")
                    .first()
                    .parent()
                    .parent()
                    .getElementsByTag("h1")
                    .text();
            System.out.println(clientS);

            String repClientS = document.getElementsContainingOwnText("Повторные визиты")
                    .first()
                    .previousElementSibling()
                    .text();
            System.out.println(repClientS);

            int clients = Integer.parseInt(clientS);
            int newClients = clients - Integer.parseInt(repClientS);
            List<Integer> clientList = new ArrayList<>();
            clientList.add(clients);
            clientList.add(newClients);
            result.put(name, clientList);
        }
        return result;
    }
}

package com.bigbro.reports;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
public class Monthly {

    private final String SESS;

    public Monthly(String sess) {
        this.SESS = sess;
    }

    public void downloadDatas(Map<Integer, String> cityMap, LocalDate startDate, LocalDate endDate) throws InterruptedException, IOException {

        downloadRecordFiles(cityMap, startDate, endDate);

        downloadFinanceFiles(cityMap, startDate, endDate);

        downloadClientBase(cityMap);

        downloadServices(cityMap, startDate, endDate);

        //выгрузка из family.likebro
        //downloadGoogsSale(cityMap, startDate, endDate);
    }



    private void downloadRecordFiles(Map<Integer, String> cityMap, LocalDate startDate, LocalDate endDate) throws InterruptedException, IOException {
        //todo: загрузка записей по годам

        String createDateFromS = "01.03.2015";

        int yearsCount = endDate.getYear() - 2015; //except first year
        String createDateToS = "31.12.2015";
        String visitDateFromS = "01.03.2015";
        String visitDateToS = "31.12.2015";
        int year = 2015;
        String yearS = Integer.toString(year);

        downloadRecordFilesByYear(cityMap, visitDateFromS, visitDateToS, createDateFromS, createDateToS, yearS);

        for (int i = 1; i < yearsCount; i++) {
            yearS = String.valueOf(year + i);
            createDateToS = "31.12." + yearS;
            visitDateFromS = "01.01." + yearS;
            visitDateToS = createDateToS;
            downloadRecordFilesByYear(cityMap, visitDateFromS, visitDateToS, createDateFromS, createDateToS, yearS);
        }

        yearS = String.valueOf(endDate.getYear());
        createDateToS = endDate.getDayOfMonth() + "." + (endDate.getMonthValue() < 10 ? "0" : "") + endDate.getMonthValue() + "." + yearS;
        visitDateFromS = "01.01." + yearS;
        visitDateToS = createDateToS;
        downloadRecordFilesByYear(cityMap, visitDateFromS, visitDateToS, createDateFromS, createDateToS, yearS);
    }



    private void downloadRecordFilesByYear(Map<Integer, String> cityMap, String visitDateFromS, String visitDateToS, String createDateFromS, String createDateToS, String yearS) throws InterruptedException, IOException {
        String linkSchema = "https://" + Selenium.getSiteName() + "/dashboard_records/to_xls/%d?status=1&start_date=%s&end_date=%s&c_start_date=%s&c_end_date=%s";

        for (Map.Entry<Integer, String> pair : cityMap.entrySet()) {
            String s = String.format(linkSchema, pair.getKey(), visitDateFromS, visitDateToS, createDateFromS, createDateToS);
            String name = pair.getValue();

            System.out.println(s);

            URL url = new URL(s);
            URLConnection connection = url.openConnection();

            connection.addRequestProperty("Cookie", "auth=" + SESS);

            InputStream in = connection.getInputStream();

            File destFile = new File("C:/Подработка/Подработка/Ежемесяч/" + name + " " + yearS + ".xls");

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

    private void downloadFinanceFiles(Map<Integer, String> cityMap, LocalDate startDate, LocalDate endDate) throws InterruptedException, IOException {
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

            File destFile = new File("C:/Подработка/Подработка/Загрузки/" + name + ".csv");

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

    private void downloadClientBase(Map<Integer, String> cityMap) throws IOException {
        //todo: загрузка клиенских баз
        String linkSchema = "https://" + Selenium.getSiteName() + "/clients/excel/%d";

        for (Map.Entry<Integer, String> pair : cityMap.entrySet()) {
            int id = pair.getKey();
            String name = pair.getValue();

            String fullLink = String.format(linkSchema, id);

            URL url = new URL(fullLink);
            URLConnection connection = url.openConnection();

            connection.addRequestProperty("Cookie", "auth=" + SESS);

            InputStream in = connection.getInputStream();

            File destFile = new File("C:/Подработка/Подработка/Клиентская база/" + name + ".xls");

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

    private void downloadServices(Map<Integer, String> cityMap, LocalDate startDate, LocalDate endDate) throws IOException {
        //todo: загрузка расшифровки продаж услуг
        String linkSchema = "https://" + Selenium.getSiteName() + "/analytics_services/to_xls/%d?start_date=%s&end_date=%s\n";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        for (Map.Entry<Integer, String> pair : cityMap.entrySet()) {
            int id = pair.getKey();
            String name = pair.getValue();

            String fullLink = String.format(linkSchema, id, startDate.format(formatter), endDate.format(formatter));

            URL url = new URL(fullLink);
            URLConnection connection = url.openConnection();

            connection.addRequestProperty("Cookie", "auth=" + SESS);

            InputStream in = connection.getInputStream();

            File destFile = new File("C:/Подработка/Подработка/Услуги/" + name + ".xls");

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

    private void downloadGoogsSale(Map<Integer, String> cityMap, LocalDate startDate, LocalDate endDate) {
        //todo: загрузка продаж товаров
    }
}

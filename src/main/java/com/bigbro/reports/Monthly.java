package com.bigbro.reports;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

        downloadIncomeByMasters(cityMap, startDate, endDate);

        downloadServicesPrice(cityMap);

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

    private void downloadIncomeByMasters(Map<Integer, String> cityMap, LocalDate startDate, LocalDate endDate) throws IOException {
        String linkSchema = "https://yclients.com/analytics/%d?start_date=%s&end_date=%s&user_id=0&position_id=0&master_id=%d";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        ArrayList<String> resultList = new ArrayList<>();

        for (Map.Entry<Integer, String> pair : cityMap.entrySet()) {
            int id = pair.getKey();
            String name = pair.getValue();

            String fullLink = String.format(linkSchema, id, startDate.format(formatter), endDate.format(formatter), 0);

            Document document = Jsoup.connect(fullLink)
                    .cookie("auth", SESS)
                    .get();

            Element masterSelect = document.getElementsByAttributeValue("name", "master_id").first();
            Elements elements = masterSelect.getElementsByTag("option");

            for (Element element : elements) {
                int masterId = Integer.parseInt(element.attr("value"));
                String masterName = element.text();
                if (masterId == 0) continue;

                String masterLink = String.format(linkSchema, id, startDate.format(formatter), endDate.format(formatter), masterId);

                document = Jsoup.connect(masterLink)
                        .cookie("auth", SESS)
                        .get();

                Element incomeElem = document.getElementsByClass("col-lg-4").first();
                String incomeS = incomeElem.getElementsByTag("h1").text();
                incomeS = incomeS.substring(0, incomeS.length() - 2);
                incomeS = incomeS.replaceAll(" ", "");
                Double income = Double.parseDouble(incomeS);

                String masterResult = name + "\t" + masterName + "\t" + income;
                resultList.add(masterResult);
                System.out.println(masterResult);
            }
        }

        File masterFile = new File("C:/Подработка/mastersIncome.txt");
        if (masterFile.exists()) masterFile.delete();

        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(masterFile));
        for (String s : resultList) {
            bufferedWriter.write(s);
            bufferedWriter.newLine();
        }
        bufferedWriter.close();
    }

    private void downloadServicesPrice(Map<Integer, String> cityMap) throws IOException, InterruptedException {
        String linkSchema = "https://n10209.yclients.com/company:%d/idx:0/service?o=";

        ArrayList<String> resultList = new ArrayList<>();

        Selenium selenium = new Selenium();
        selenium.launchChromeDriver();

        WebDriver driver = selenium.getDriver();

        for (Map.Entry<Integer, String> pair : cityMap.entrySet()) {
            int id = pair.getKey();
            String name = pair.getValue();

            String fullLink = String.format(linkSchema, id);

            driver.get(fullLink);
            Thread.sleep(5000);
            selenium.waitForJSandJQueryToLoad();

            List<WebElement> webElements = driver.findElements(By.className("service-group-wrapper"));
            for (WebElement webElement : webElements) {
                Elements hidden = Jsoup.parse(webElement.getAttribute("outerHTML")).getElementsByClass("services__service-list_hided");
                if (hidden.size() > 0) {
                    webElement.findElement(By.className("fa-angle-down")).click();
                    Thread.sleep(2000);
                    selenium.waitForJSandJQueryToLoad();
                }
            }

            Document document = Jsoup.parse(driver.getPageSource());

            /*Document document = Jsoup.connect(fullLink)
                    .get();*/

            Elements categoryElements = document.getElementsByClass("service-group-wrapper");

            for (Element element : categoryElements) {
                Elements content = element.getElementsByTag("h3");
                if (content.size() == 0) continue;

                String categoryName = element.getElementsByTag("h3").first().text();

                Elements serviceElements;
                serviceElements = element.getElementsByTag("yclients-record-service-item");
                if (serviceElements.size() == 0)
                    serviceElements = element.getElementsByTag("yclients-record-service-item-serial");

                for(Element servElem : serviceElements) {
                    String servName = servElem.getElementsByClass("event-title-text").first().text();
                    String servPrice = servElem.getElementsByClass("price").first().text();
                    servPrice = servPrice.replace(" ", "");
                    int priceN = firstInt(servPrice);
                    String result = name
                            + "\t" + categoryName
                            + "\t" + servName
                            + "\t" + priceN;
                    System.out.println(result);
                    resultList.add(result);
                }
            }
        }

        selenium.quitChromeDriver();

        File servFile = new File("C:/Подработка/servicePrice.txt");
        if (servFile.exists()) servFile.delete();

        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(servFile));
        for (String s : resultList) {
            bufferedWriter.write(s);
            bufferedWriter.newLine();
        }
        bufferedWriter.close();
    }

    private int firstInt(String servPrice) {
        String str = servPrice.replaceAll("[^0-9]+", " ");
        String[] strs = str.trim().split(" ");
        return !strs[0].equals("") ? Integer.parseInt(strs[0]) : 0;
    }

    private void downloadGoogsSale(Map<Integer, String> cityMap, LocalDate startDate, LocalDate endDate) {
        //todo: загрузка продаж товаров
    }
}

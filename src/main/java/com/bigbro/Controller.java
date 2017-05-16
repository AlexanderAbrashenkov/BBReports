package com.bigbro;

import com.bigbro.reports.Monthly;
import com.bigbro.reports.Selenium;
import com.bigbro.reports.Weekly;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;

import java.io.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Controller {
    @FXML
    private TextArea cityListTextArea;

    @FXML
    private DatePicker weeklyStartDate;
    @FXML
    private DatePicker weeklyEndDate;

    @FXML
    private DatePicker monthlyStartDate;
    @FXML
    private DatePicker monthlyEndDate;

    @FXML
    private void initialize() {
        cityListTextArea.getParagraphs().addListener(new ListChangeListener<CharSequence>() {
            @Override
            public void onChanged(Change<? extends CharSequence> c) {
                writeCitiesToFile();
            }
        });

        fillCitiesTextArea();

        setDatesWeekly();

        setDatesMonthly();
    }

    private void writeCitiesToFile() {
        ObservableList<CharSequence> cities = cityListTextArea.getParagraphs();
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("cities.txt")))) {
            for (int i = 0; i < cities.size(); i++) {
                CharSequence charSequence = cities.get(i);
                String city = charSequence.toString();
                writer.write(city);
                writer.newLine();
            }
            writer.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fillCitiesTextArea() {
        File citiesFile = new File("cities.txt");
        if (citiesFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(citiesFile)))) {
                String city = "";
                while (reader.ready()) {
                    String readedCity = reader.readLine();
                    if (readedCity.length() > 0) {
                        city += readedCity;
                        city += "\n";
                    }
                }
                cityListTextArea.setText(city);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setDatesWeekly() {
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.minusDays(7);

        while (startDate.getDayOfWeek() != DayOfWeek.MONDAY) {
            startDate = startDate.minusDays(1);
        }

        LocalDate endDate = now.minusDays(1);
        while (endDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
            endDate = endDate.minusDays(1);
        }

        weeklyStartDate.setValue(startDate);
        weeklyEndDate.setValue(endDate);
    }

    private void setDatesMonthly() {
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.minusDays(28);

        while (startDate.getDayOfMonth() != 1) {
            startDate = startDate.minusDays(1);
        }

        LocalDate endDate = now.minusDays(1);
        while (endDate.getDayOfMonth() < 27) {
            endDate = endDate.minusDays(1);
        }

        monthlyStartDate.setValue(startDate);
        monthlyEndDate.setValue(endDate);
    }

    @FXML
    private void collectWeeklyDatas() {
        Map<Integer, String> cityMap = new HashMap<>();

        ObservableList<CharSequence> cityList = cityListTextArea.getParagraphs();
        cityMap = parseObservableList(cityList);

        Selenium selenium = new Selenium();
        String sess = null;
        while(sess == null) {
            sess = selenium.getSession();
        }

        Weekly weekly = new Weekly(sess);
        try {
            weekly.downloadDatas(cityMap, weeklyStartDate.getValue(), weeklyEndDate.getValue());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void collectMonthlyDatas() {
        Map<Integer, String> cityMap = new HashMap<>();

        ObservableList<CharSequence> cityList = cityListTextArea.getParagraphs();
        cityMap = parseObservableList(cityList);

        Selenium selenium = new Selenium();
        String sess = null;
        while(sess == null) {
            sess = selenium.getSession();
        }

        Monthly monthly = new Monthly(sess);
        try {
            monthly.downloadDatas(cityMap, monthlyStartDate.getValue(), monthlyEndDate.getValue());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<Integer, String> parseObservableList(ObservableList<CharSequence> cityList) {
        Map<Integer, String> result = new HashMap<>();
        for (int i = 0; i < cityList.size(); i++) {
            String cityFull = cityList.get(i).toString();
            if (cityFull.length() > 0) {
                int dotIndex = cityFull.indexOf(".");
                int commaIndex = cityFull.indexOf(",");
                String city = cityFull.substring(dotIndex + 2, commaIndex);
                int cityIndex = Integer.parseInt(cityFull.substring(commaIndex + 2, cityFull.length()));
                result.put(cityIndex, city);
            }
        }
        return result;
    }
}

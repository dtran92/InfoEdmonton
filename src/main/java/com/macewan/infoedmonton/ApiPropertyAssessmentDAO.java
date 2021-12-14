package com.macewan.infoedmonton;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ApiPropertyAssessmentDAO implements PropertyAssessmentDAO {
    private final Set<String> assessmentClassSet = new HashSet<>();
    private int min = 0;
    private int max = 0;

    public ApiPropertyAssessmentDAO() {
    }

    public Set<String> getAssessmentClassSet() {
        return assessmentClassSet;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int num) {
        this.min = num;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int num) {
        this.max = num;
    }

    @Override
    public PropertyAssessment getByAccountNumber(int accountNumber) throws IOException, InterruptedException {
        PropertyAssessment propertyAssessment;
        String[] tokens;

        String endpoint = "https://data.edmonton.ca/resource/q7d6-ambg.csv";
        String accQuery = "?account_number=" + accountNumber;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint + accQuery))
                .GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String[] str = response.body().split("\n");
        if (str.length == 1) return null;
        str[1] = str[1].replace("\"", "");
        tokens = str[1].split(",", -1);
        propertyAssessment = new PropertyAssessment(tokens[0],
                new PropertyAssessment.HouseInfo(tokens[1], tokens[2], tokens[3], tokens[4]),
                new PropertyAssessment.Neighborhood(tokens[5], tokens[6], tokens[7]), Integer.parseInt(tokens[8]),
                new PropertyAssessment.PointLocation(Double.parseDouble(tokens[9]), Double.parseDouble(tokens[10])),
                new ArrayList<>());
        if (!tokens[15].isEmpty()) {
            propertyAssessment.getAssessmentClassList().add(new PropertyAssessment.AssessmentClass(tokens[15], tokens[12]));
        }
        if (!tokens[16].isEmpty()) {
            propertyAssessment.getAssessmentClassList().add(new PropertyAssessment.AssessmentClass(tokens[16], tokens[13]));
        }
        if (!tokens[17].isEmpty()) {
            propertyAssessment.getAssessmentClassList().add(new PropertyAssessment.AssessmentClass(tokens[17], tokens[14]));
        }
        return propertyAssessment;
    }

    @Override
    public List<PropertyAssessment> getByNeighbourhood(String neighbourhood) throws IOException, InterruptedException {
        String endpoint = "https://data.edmonton.ca/resource/q7d6-ambg.csv";
        String neighborhoodQuery = "?$where=neighbourhood%20like%20'%25" + neighbourhood.replace(" ", "%20") + "%25'&$limit=1000000";

        return getListObj(endpoint + neighborhoodQuery);
    }

    @Override
    public List<PropertyAssessment> getByAssessmentClass(String assessmentClass) throws IOException, InterruptedException {
        assessmentClass = assessmentClass.replace(" ", "%20");

        String query = "https://data.edmonton.ca/resource/q7d6-ambg.csv?$where=mill_class_1=%27" + assessmentClass + "%27OR%20mill_class_2=%27" +
                assessmentClass + "%27OR%20mill_class_3=%27" + assessmentClass + "%27" + "&$limit=1000000";
        return getListObj(query);
    }

    @Override
    public List<PropertyAssessment> getAll(int limit, int offset) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        int i;
        String endpoint = "https://data.edmonton.ca/resource/q7d6-ambg.csv";

        // get min max
        if (this.max == this.min && this.max == 0) {
            String minQuery = "?$select=min(assessed_value)";
            String maxQuery = "?$select=max(assessed_value)";

            HttpRequest requestMin = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint + minQuery))
                    .GET().build();
            HttpResponse<String> responseMin = client.send(requestMin, HttpResponse.BodyHandlers.ofString());
            String[] strMin = responseMin.body().split("\n");

            HttpRequest requestMax = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint + maxQuery))
                    .GET().build();
            HttpResponse<String> responseMax = client.send(requestMax, HttpResponse.BodyHandlers.ofString());
            String[] strMax = responseMax.body().split("\n");

            setMin(Integer.parseInt(strMin[1].replace("\"", "")));
            setMax(Integer.parseInt(strMax[1].replace("\"", "")));
        }

        // get assessment classes
        if (this.assessmentClassSet.size() == 0) {
            for (i = 1; i < 4; i++) {
                String assessmentClassQuery = "?$select=distinct%20mill_class_" + i;
                HttpRequest requestAssesmentClass = HttpRequest.newBuilder()
                        .uri(URI.create(endpoint + assessmentClassQuery))
                        .GET().build();
                HttpResponse<String> responseAssesmentClass = client.send(requestAssesmentClass, HttpResponse.BodyHandlers.ofString());
                String[] str = responseAssesmentClass.body().split("\n");
                for (int j = 1; j < str.length; j++) {
                    this.assessmentClassSet.add(str[j].replace("\"", ""));
                }
            }
        }

        String query = "?$limit=" + limit + "&$offset=" + offset;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint + query))
                .GET().build();
        List<PropertyAssessment> propertyAssessmentList = new ArrayList<>();

        HttpResponse<String> responseAll = client.send(request, HttpResponse.BodyHandlers.ofString());
        String[] str = responseAll.body().split("\n");
        String[] tokens = null;
        if (str.length == 1) return propertyAssessmentList;

        for (i = 1; i < str.length; i++) {
            str[i] = str[i].replace("\"", "");
            for (int j = 0; j < str[i].length(); j++) {
                tokens = str[i].split(",", -1);
            }
            assert tokens != null;
            PropertyAssessment elem = new PropertyAssessment(tokens[0],
                    new PropertyAssessment.HouseInfo(tokens[1], tokens[2], tokens[3], tokens[4]),
                    new PropertyAssessment.Neighborhood(tokens[5], tokens[6], tokens[7]), Integer.parseInt(tokens[8]),
                    new PropertyAssessment.PointLocation(Double.parseDouble(tokens[9]), Double.parseDouble(tokens[10])),
                    new ArrayList<>());

            if (Integer.parseInt(tokens[8]) < getMin()) {
                setMin(Integer.parseInt(tokens[8]));
            }
            if (Integer.parseInt(tokens[8]) > getMax() || getMax() == 0) {
                setMax(Integer.parseInt(tokens[8]));
            }

            if (!tokens[15].isEmpty()) {
                assessmentClassSet.add(tokens[15]);
                elem.getAssessmentClassList().add(new PropertyAssessment.AssessmentClass(tokens[15], tokens[12]));
            }
            if (!tokens[16].isEmpty()) {
                assessmentClassSet.add(tokens[16]);
                elem.getAssessmentClassList().add(new PropertyAssessment.AssessmentClass(tokens[16], tokens[13]));
            }
            if (!tokens[17].isEmpty()) {
                assessmentClassSet.add(tokens[17]);
                elem.getAssessmentClassList().add(new PropertyAssessment.AssessmentClass(tokens[17], tokens[14]));
            }
            propertyAssessmentList.add(elem);
        }
        return propertyAssessmentList;
    }

    public List<PropertyAssessment> getByNeighbourhood(String neighborhood, String address, String assessmentClass, Integer min, Integer max) throws IOException, InterruptedException {
        String whereAddress = "";
        address = address.replace(" ", "%20");
        neighborhood = neighborhood.replace(" ", "%20").toUpperCase();

        if (!address.isEmpty()) {
            whereAddress = "AND%20%28house_number%20%7C%7C%20%27%20%27%20%7C%7C%20street_name%20like%20%27%25" + address.toUpperCase() + "%25%27%29";
        }

        String whereAssessmentClass = "";
        if (assessmentClass != null) {
            assessmentClass = assessmentClass.replace(" ", "%20");
            whereAssessmentClass = "AND%20%28mill_class_1=%27" + assessmentClass + "%27OR%20mill_class_2=%27" +
                    assessmentClass + "%27OR%20mill_class_3=%27" + assessmentClass + "%27%29";
        }

        if (min == null) {
            min = getMin();
        }
        String whereMinVal = "AND%20%28assessed_value%3E=" + min;

        if (max == null) {
            max = getMax();
        }
        String whereMaxVal = "AND%20assessed_value%3C=" + max + "%29";

        String query = "https://data.edmonton.ca/resource/q7d6-ambg.csv?$where=neighbourhood%20like%20%27%25" + neighborhood + "%25%27"
                + whereAddress + whereAssessmentClass + whereMinVal + whereMaxVal + "&$limit=1000000";
        return getListObj(query);
    }

    public List<PropertyAssessment> getByAssessmentClass(String assessmentClass, String address, Integer min, Integer max) throws IOException, InterruptedException {
        assessmentClass = assessmentClass.replace(" ", "%20");
        address = address.replace(" ", "%20");


        String whereAddress = "";
        if (!address.isEmpty()) {
            whereAddress = "AND%28%20house_number%20%7C%7C%20%27%20%27%20%7C%7C%20street_name%20like%20%27%25" + address.toUpperCase() + "%25%27%29";
        }

        if (min == null) {
            min = getMin();
        }
        String whereMinVal = "AND%20%28assessed_value%3E=" + min;

        if (max == null) {
            max = getMax();
        }
        String whereMaxVal = "AND%20assessed_value%3C=" + max + "%29";

        String query = "https://data.edmonton.ca/resource/q7d6-ambg.csv?$where=%28mill_class_1=%27" + assessmentClass + "%27OR%20mill_class_2=%27" +
                assessmentClass + "%27OR%20mill_class_3=%27" + assessmentClass + "%27%29"
                + whereAddress + whereMinVal + whereMaxVal + "&$limit=1000000";
        return getListObj(query);
    }

    public List<PropertyAssessment> getByAddress(String address, Integer min, Integer max) throws IOException, InterruptedException {

        address = address.toUpperCase().replace(" ", "%20");
        if (min == null) {
            min = getMin();
        }
        String whereMinVal = "AND%20%28assessed_value%3E=" + min;

        if (max == null) {
            max = getMax();
        }
        String whereMaxVal = "AND%20assessed_value%3C=" + max + "%29";

        String query = "https://data.edmonton.ca/resource/q7d6-ambg.csv?$where=%28house_number%20%7C%7C%20%27%20%27%20%7C%7C%20street_name%20like%20%27%25" +
                address + "%25%27%29" + whereMinVal + whereMaxVal + "&$limit=1000000";

        return getListObj(query);
    }

    public List<PropertyAssessment> getByMinMax(Integer min, Integer max) throws IOException, InterruptedException {
        if (min == null) {
            min = getMin();
        }
        if (max == null) {
            max = getMax();
        }

        String query = "https://data.edmonton.ca/resource/q7d6-ambg.csv?$where=%28assessed_value%3E=" +
                min + "AND%20assessed_value%3C=" + max + "%29" + "&$limit=1000000";
        return getListObj(query);
    }

    private List<PropertyAssessment> getListObj(String url) throws IOException, InterruptedException {
        List<PropertyAssessment> returnList = new ArrayList<>();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String[] str = response.body().split("\n");

        String[] tokens = null;
        if (str.length == 1) return returnList;

        for (int i = 1; i < str.length; i++) {
            str[i] = str[i].replace("\"", "");
            for (int j = 0; j < str[i].length(); j++) {
                tokens = str[i].split(",", -1);
            }
            assert tokens != null;
            PropertyAssessment elem = new PropertyAssessment(tokens[0],
                    new PropertyAssessment.HouseInfo(tokens[1], tokens[2], tokens[3], tokens[4]),
                    new PropertyAssessment.Neighborhood(tokens[5], tokens[6], tokens[7]), Integer.parseInt(tokens[8]),
                    new PropertyAssessment.PointLocation(Double.parseDouble(tokens[9]), Double.parseDouble(tokens[10])),
                    new ArrayList<>());

            if (!tokens[15].isEmpty()) {
                elem.getAssessmentClassList().add(new PropertyAssessment.AssessmentClass(tokens[15], tokens[12]));
            }
            if (!tokens[16].isEmpty()) {
                elem.getAssessmentClassList().add(new PropertyAssessment.AssessmentClass(tokens[16], tokens[13]));
            }
            if (!tokens[17].isEmpty()) {
                elem.getAssessmentClassList().add(new PropertyAssessment.AssessmentClass(tokens[17], tokens[14]));
            }
            returnList.add(elem);
        }
        return returnList;
    }
}

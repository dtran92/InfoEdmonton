package com.macewan.infoedmonton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class CsvPropertyAssessmentDAO implements PropertyAssessmentDAO {
    // <K, V> = Account number : PropertyAssessment object
    private final Map<String, PropertyAssessment> mapByAccountNum = new HashMap<>();
    // <K, V> = Neighborhood name : Neighborhood object
    private final Map<String, Neighborhood> mapByNeighborhood = new HashMap<>();
    // <K, V> = AssessmentClass : Property object
    private final Map<String, AssessmentClass> mapByAssessmentClass = new HashMap<>();
    private final List<PropertyAssessment> propertyList = new ArrayList<>();
    private int min = 0;
    private int max = 0;
    private long sum = 0;

    public CsvPropertyAssessmentDAO() throws IOException {
        // read csv file in the resource folder
        InputStream inputStream = getClass().getResourceAsStream("Property_Assessment_Data_2021.csv");
        InputStreamReader streamReader = new InputStreamReader(inputStream);
        BufferedReader reader = new BufferedReader(streamReader);

        Scanner scanner = new Scanner(reader);
        // read first line
        String input = scanner.nextLine();
        String[] tokens;

        while (scanner.hasNextLine()) {
            input = scanner.nextLine();
            tokens = input.split("\\s*,\\s*", -1);

            // create a property object
            PropertyAssessment elem = new PropertyAssessment(tokens[0],
                    new PropertyAssessment.HouseInfo(tokens[1], tokens[2], tokens[3], tokens[4]),
                    new PropertyAssessment.Neighborhood(tokens[5], tokens[6], tokens[7]), Integer.parseInt(tokens[8]),
                    new PropertyAssessment.PointLocation(Double.parseDouble(tokens[9]), Double.parseDouble(tokens[10])),
                    new ArrayList<>());

            /* General */
            addSum(elem.getAssessedValue());
            if (Integer.parseInt(tokens[8]) < getMin()) {
                setMin(Integer.parseInt(tokens[8]));
            }
            if (Integer.parseInt(tokens[8]) > getMax() || getMax() == 0) {
                setMax(Integer.parseInt(tokens[8]));
            }
            add(elem);

            /* Account number */
            getMapByAccountNum().put(tokens[0], elem);

            /* Assessment class */
            // residential
            if (!tokens[15].isEmpty()) {
                elem.getAssessmentClassList().add(new PropertyAssessment.AssessmentClass(tokens[15], tokens[12]));
                if (getMapByAssessmentClass().containsKey(tokens[15])) {
                    getMapByAssessmentClass().get(tokens[15]).getPropertyList().add(elem);
                    getMapByAssessmentClass().get(tokens[15]).addSum(Long.parseLong(tokens[8]));
                    if (getMapByAssessmentClass().get(tokens[15]).getAssessmentClassName().equals(tokens[15])) {
                        if (Integer.parseInt(tokens[8]) < getMapByAssessmentClass().get(tokens[15]).getMin()) {
                            getMapByAssessmentClass().get(tokens[15]).setMin(Integer.parseInt(tokens[8]));
                        }
                        if (Integer.parseInt(tokens[8]) > getMapByAssessmentClass().get(tokens[15]).getMax()
                                || getMapByAssessmentClass().get(tokens[15]).getMax() == 0) {
                        }
                    }
                } else if (!getMapByAssessmentClass().containsKey(tokens[15])) {
                    getMapByAssessmentClass().put(tokens[15], new AssessmentClass(tokens[15], new ArrayList<>()));
                    getMapByAssessmentClass().get(tokens[15]).getPropertyList().add(elem);
                    getMapByAssessmentClass().get(tokens[15]).addSum(Long.parseLong(tokens[8]));
                    getMapByAssessmentClass().get(tokens[15]).setMin(Integer.parseInt(tokens[8]));
                    getMapByAssessmentClass().get(tokens[15]).setMax(Integer.parseInt(tokens[8]));
                }
            }
            // commercial
            if (!tokens[16].isEmpty()) {
                elem.getAssessmentClassList().add(new PropertyAssessment.AssessmentClass(tokens[16], tokens[13]));
                if (getMapByAssessmentClass().containsKey(tokens[16])
                        && !tokens[16].equals(tokens[15])) {
                    getMapByAssessmentClass().get(tokens[16]).getPropertyList().add(elem);
                    getMapByAssessmentClass().get(tokens[16]).addSum(Long.parseLong(tokens[8]));
                    if (getMapByAssessmentClass().get(tokens[16]).getAssessmentClassName().equals(tokens[16])) {
                        if (Integer.parseInt(tokens[8]) < getMapByAssessmentClass().get(tokens[16]).getMin()) {
                            getMapByAssessmentClass().get(tokens[16]).setMin(Integer.parseInt(tokens[8]));
                        }
                        if (Integer.parseInt(tokens[8]) > getMapByAssessmentClass().get(tokens[16]).getMax()
                                || getMapByAssessmentClass().get(tokens[16]).getMax() == 0) {
                            getMapByAssessmentClass().get(tokens[16]).setMax(Integer.parseInt(tokens[8]));
                        }
                    }
                } else if (!getMapByAssessmentClass().containsKey(tokens[16])) {
                    getMapByAssessmentClass().put(tokens[16], new AssessmentClass(tokens[16], new ArrayList<>()));
                    getMapByAssessmentClass().get(tokens[16]).getPropertyList().add(elem);
                    getMapByAssessmentClass().get(tokens[16]).addSum(Long.parseLong(tokens[8]));
                    getMapByAssessmentClass().get(tokens[16]).setMin(Integer.parseInt(tokens[8]));
                    getMapByAssessmentClass().get(tokens[16]).setMax(Integer.parseInt(tokens[8]));
                }
            }
            //others
            if (!tokens[17].isEmpty()) {
                elem.getAssessmentClassList().add(new PropertyAssessment.AssessmentClass(tokens[17], tokens[14]));
                if (getMapByAssessmentClass().containsKey(tokens[17])
                        && !tokens[17].equals(tokens[16]) && !tokens[17].equals(tokens[15])) {
                    getMapByAssessmentClass().get(tokens[17]).getPropertyList().add(elem);
                    getMapByAssessmentClass().get(tokens[17]).addSum(Long.parseLong(tokens[8]));
                    if (getMapByAssessmentClass().get(tokens[17]).getAssessmentClassName().equals(tokens[17])) {
                        if (Integer.parseInt(tokens[8]) < getMapByAssessmentClass().get(tokens[17]).getMin()) {
                            getMapByAssessmentClass().get(tokens[17]).setMin(Integer.parseInt(tokens[8]));
                        }
                        if (Integer.parseInt(tokens[8]) > getMapByAssessmentClass().get(tokens[17]).getMax()
                                || getMapByAssessmentClass().get(tokens[17]).getMax() == 0) {
                            getMapByAssessmentClass().get(tokens[17]).setMax(Integer.parseInt(tokens[8]));
                        }
                    }
                } else if (!getMapByAssessmentClass().containsKey(tokens[17])) {
                    getMapByAssessmentClass().put(tokens[17], new AssessmentClass(tokens[17], new ArrayList<>()));
                    getMapByAssessmentClass().get(tokens[17]).getPropertyList().add(elem);
                    getMapByAssessmentClass().get(tokens[17]).addSum(Long.parseLong(tokens[8]));
                    getMapByAssessmentClass().get(tokens[17]).setMin(Integer.parseInt(tokens[8]));
                    getMapByAssessmentClass().get(tokens[17]).setMax(Integer.parseInt(tokens[8]));
                }
            }

            /* Neighborhood */
            // add property to corresponding neighborhood
            if (getMapByNeighborhood().containsKey(tokens[6])) {
                getMapByNeighborhood().get(tokens[6]).getPropertyList().add(elem);
                getMapByNeighborhood().get(tokens[6]).addSum(Long.parseLong(tokens[8]));
                if (getMapByNeighborhood().get(tokens[6]).getNeighborhoodName().equals(tokens[6])) {
                    if (Integer.parseInt(tokens[8]) < getMapByNeighborhood().get(tokens[6]).getMin()) {
                        getMapByNeighborhood().get(tokens[6]).setMin(Integer.parseInt(tokens[8]));
                    }
                    if (Integer.parseInt(tokens[8]) > getMapByNeighborhood().get(tokens[6]).getMax()) {
                        getMapByNeighborhood().get(tokens[6]).setMax(Integer.parseInt(tokens[8]));
                    }
                }
            } else if (!getMapByNeighborhood().containsKey(tokens[6]) && !tokens[6].isEmpty()) {
                getMapByNeighborhood().put(tokens[6], new Neighborhood(tokens[6], new ArrayList<>()));
                getMapByNeighborhood().get(tokens[6]).getPropertyList().add(elem);
                getMapByNeighborhood().get(tokens[6]).addSum(Long.parseLong(tokens[8]));
                getMapByNeighborhood().get(tokens[6]).setMin(Integer.parseInt(tokens[8]));
                getMapByNeighborhood().get(tokens[6]).setMax(Integer.parseInt(tokens[8]));
            }
        }

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

    public void addSum(long num) {
        this.sum += num;
    }

    public void add(PropertyAssessment property) {
        propertyList.add(property);
    }

    @Override
    public PropertyAssessment getByAccountNumber(int accountNumber) {
        return mapByAccountNum.get(String.valueOf(accountNumber));
    }

    @Override
    public List<PropertyAssessment> getByNeighbourhood(String neighbourhood) {
        return mapByNeighborhood.get(neighbourhood).getPropertyList();
    }

    @Override
    public List<PropertyAssessment> getByAssessmentClass(String assessmentClass) {
        return mapByAssessmentClass.get(assessmentClass).getPropertyList();
    }

    @Override
    public List<PropertyAssessment> getAll(int limit, int offset) {
        return propertyList;
    }

    public Map<String, PropertyAssessment> getMapByAccountNum() {
        return mapByAccountNum;
    }

    public Map<String, Neighborhood> getMapByNeighborhood() {
        return this.mapByNeighborhood;
    }

    public Map<String, AssessmentClass> getMapByAssessmentClass() {
        return mapByAssessmentClass;
    }
}

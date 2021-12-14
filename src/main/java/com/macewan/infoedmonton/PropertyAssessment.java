package com.macewan.infoedmonton;

import java.util.List;
import java.util.Objects;

public class PropertyAssessment implements Comparable<PropertyAssessment> {
    private final String accountNum;
    private final HouseInfo houseInfo;
    private final Neighborhood neighborhood;
    private final int assessedValue;
    private final PointLocation point;
    private final List<AssessmentClass> assessmentClassList;

    public PropertyAssessment(String accountNum, HouseInfo houseInfo, Neighborhood neighborhood, int assessedValue, PointLocation point, List<AssessmentClass> assessmentClassList) {
        this.accountNum = accountNum;
        this.houseInfo = houseInfo;
        this.neighborhood = neighborhood;
        this.assessedValue = assessedValue;
        this.point = point;
        this.assessmentClassList = assessmentClassList;
    }

    // getter methods for retrieving data
    public int getAssessedValue() {
        return this.assessedValue;
    }

    public String getAccountNum() {
        return this.accountNum;
    }

    public Neighborhood getNeighborhood() {
        return this.neighborhood;
    }

    public PointLocation getPoint() {
        return this.point;
    }

    public List<AssessmentClass> getAssessmentClassList() {
        return assessmentClassList;
    }

    public HouseInfo getHouseInfo() {
        return houseInfo;
    }

    @Override
    public int compareTo(PropertyAssessment o) {
        return this.assessedValue - o.assessedValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PropertyAssessment)) return false;
        PropertyAssessment that = (PropertyAssessment) o;
        return getAssessedValue() == that.getAssessedValue() && getAccountNum().equals(that.getAccountNum()) && getHouseInfo().equals(that.getHouseInfo()) && getNeighborhood().equals(that.getNeighborhood()) && getPoint().equals(that.getPoint()) && getAssessmentClassList().equals(that.getAssessmentClassList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAccountNum(), getHouseInfo(), getNeighborhood(), getAssessedValue(), getPoint(), getAssessmentClassList());
    }

    // AssessmentClass class
    static class AssessmentClass {
        String assessmentClass;
        String percentage;

        AssessmentClass(String assessmentClass, String percentage) {
            this.assessmentClass = assessmentClass;
            this.percentage = percentage;
        }

        public String getAssessmentClass() {
            return assessmentClass;
        }

        public String getPercentage() {
            return percentage;
        }

        @Override
        public String toString() {
            return assessmentClass + " " + percentage + "%";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AssessmentClass)) return false;
            AssessmentClass that = (AssessmentClass) o;
            return getAssessmentClass().equals(that.getAssessmentClass()) && getPercentage().equals(that.getPercentage());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getAssessmentClass(), getPercentage());
        }
    }

    // PointLocation class
    static class PointLocation {
        double latitude;
        double longitude;

        public PointLocation(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        @Override
        public String toString() {
            return "(" + latitude + ", " + longitude + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PointLocation)) return false;
            PointLocation that = (PointLocation) o;
            return Double.compare(that.getLatitude(), getLatitude()) == 0 && Double.compare(that.getLongitude(), getLongitude()) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(getLatitude(), getLongitude());
        }
    }

    // Neighborhood class
    static class Neighborhood {
        String neighborhood;
        String neighborhoodID;
        String ward;

        public Neighborhood(String neighborhoodID, String neighborhood, String ward) {
            this.neighborhoodID = neighborhoodID;
            this.neighborhood = neighborhood;
            this.ward = ward;
        }

        public String getNeighborhoodName() {
            return this.neighborhood;
        }

        @Override
        public String toString() {
            if (neighborhood.isEmpty() && neighborhoodID.isEmpty() && ward.isEmpty()) return null;
            return neighborhood + " (" + ward + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Neighborhood)) return false;
            Neighborhood that = (Neighborhood) o;
            return neighborhood.equals(that.neighborhood) && neighborhoodID.equals(that.neighborhoodID);
        }

        @Override
        public int hashCode() {
            return Objects.hash(neighborhood, neighborhoodID);
        }
    }

    // HouseInfo class
    static class HouseInfo {
        String suite;
        String houseNum;
        String streetName;
        String garage;

        public HouseInfo(String suite, String houseNum, String streetName, String garage) {
            this.suite = suite;
            this.houseNum = houseNum;
            this.streetName = streetName;
            this.garage = garage;
        }

        @Override
        public String toString() {
            return suite + " " + houseNum + " " + streetName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof HouseInfo)) return false;
            HouseInfo houseInfo = (HouseInfo) o;
            return suite.equals(houseInfo.suite) && houseNum.equals(houseInfo.houseNum) && streetName.equals(houseInfo.streetName) && garage.equals(houseInfo.garage);
        }

        @Override
        public int hashCode() {
            return Objects.hash(suite, houseNum, streetName, garage);
        }
    }
}

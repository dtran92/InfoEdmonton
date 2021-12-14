package com.macewan.infoedmonton;

import java.io.IOException;
import java.util.List;

public interface PropertyAssessmentDAO {
    PropertyAssessment getByAccountNumber(int accountNumber) throws IOException, InterruptedException;

    List<PropertyAssessment> getByNeighbourhood(String neighbourhood) throws IOException, InterruptedException;

    List<PropertyAssessment> getByAssessmentClass(String assessmentClass) throws IOException, InterruptedException;

    List<PropertyAssessment> getAll(int limit, int offset) throws IOException, InterruptedException;
}

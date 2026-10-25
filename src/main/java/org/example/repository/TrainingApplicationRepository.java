package org.example.repository;

import org.example.model.ApplicationStatus;
import org.example.model.EducationType;
import org.example.model.TrainingApplication;

import java.util.List;
import java.util.Optional;

public interface TrainingApplicationRepository {

    TrainingApplication save(TrainingApplication application);

    List<TrainingApplication> findAll();

    Optional<TrainingApplication> findById(int id);

    TrainingApplication update(TrainingApplication application);

    void deleteById(int id);

    List<TrainingApplication> searchByCourseName(String courseName);

    List<TrainingApplication> searchByUserName(String userName);

    List<TrainingApplication> findByUserId(int userId);

    List<TrainingApplication> findByStatus(ApplicationStatus status);

    List<TrainingApplication> findByEducationType(EducationType educationType);
}
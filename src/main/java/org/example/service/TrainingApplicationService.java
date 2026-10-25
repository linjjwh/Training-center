package org.example.service;

import org.example.exception.BusinessException;
import org.example.exception.EntityNotFoundException;
import org.example.model.ApplicationStatus;
import org.example.model.EducationType;
import org.example.model.TrainingApplication;
import org.example.model.User;
import org.example.repository.TrainingApplicationRepository;
import org.example.repository.UserRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TrainingApplicationService {

    private final TrainingApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    public TrainingApplicationService(
            TrainingApplicationRepository applicationRepository,
            UserRepository userRepository
    ) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
    }

    public TrainingApplication create(
            TrainingApplication application
    ) {

        validateApplication(application);

        User user = findUser(application.getUser().getId());
        application.setUser(user);

        return applicationRepository.save(application);
    }

    public List<TrainingApplication> findAll() {
        return applicationRepository.findAll();
    }

    public TrainingApplication findById(int id) {

        return applicationRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Заявка с ID " + id + " не найдена"
                        )
                );
    }

    public TrainingApplication update(
            TrainingApplication application
    ) {

        TrainingApplication existing =
                findById(application.getId());

        validateApplication(application);

        checkStatusTransition(
                existing.getStatus(),
                application.getStatus()
        );

        User user = findUser(application.getUser().getId());
        application.setUser(user);

        return applicationRepository.update(application);
    }

    public void delete(int id) {

        TrainingApplication application = findById(id);

        if (application.getStatus() == ApplicationStatus.APPROVED) {
            throw new BusinessException(
                    "Нельзя удалить одобренную заявку"
            );
        }

        applicationRepository.deleteById(id);
    }

    public List<TrainingApplication> searchByCourseName(
            String courseName
    ) {

        if (courseName == null || courseName.isBlank()) {
            throw new BusinessException(
                    "Название курса не может быть пустым"
            );
        }

        return applicationRepository.searchByCourseName(courseName);
    }

    public List<TrainingApplication> searchByUserName(
            String userName
    ) {

        if (userName == null || userName.isBlank()) {
            throw new BusinessException(
                    "Имя пользователя не может быть пустым"
            );
        }

        return applicationRepository.searchByUserName(userName);
    }

    public List<TrainingApplication> findByUserId(int userId) {
        return applicationRepository.findByUserId(userId);
    }

    public List<TrainingApplication> findByStatus(
            ApplicationStatus status
    ) {

        if (status == null) {
            throw new BusinessException(
                    "Статус не может быть пустым"
            );
        }

        return applicationRepository.findByStatus(status);
    }

    public List<TrainingApplication> findByEducationType(
            EducationType educationType
    ) {

        if (educationType == null) {
            throw new BusinessException(
                    "Тип обучения не может быть пустым"
            );
        }

        return applicationRepository.findByEducationType(
                educationType
        );
    }

    private User findUser(int userId) {

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Пользователь с ID " +
                                        userId +
                                        " не найден"
                        )
                );
    }

    private void validateApplication(
            TrainingApplication application
    ) {

        if (application == null) {
            throw new BusinessException(
                    "Заявка не может быть null"
            );
        }

        if (application.getUser() == null) {
            throw new BusinessException(
                    "У заявки должен быть пользователь"
            );
        }

        if (application.getCourseName() == null ||
                application.getCourseName().isBlank()) {

            throw new BusinessException(
                    "Название курса не может быть пустым"
            );
        }

        if (application.getApplicationDate() == null) {
            throw new BusinessException(
                    "Дата заявки не может быть пустой"
            );
        }

        if (application.getApplicationDate()
                .isAfter(LocalDate.now())) {

            throw new BusinessException(
                    "Дата заявки не может быть в будущем"
            );
        }

        if (application.getStatus() == null) {
            throw new BusinessException(
                    "Статус заявки не может быть пустым"
            );
        }

        if (application.getEducationType() == null) {
            throw new BusinessException(
                    "Тип обучения не может быть пустым"
            );
        }
    }

    private void checkStatusTransition(
            ApplicationStatus oldStatus,
            ApplicationStatus newStatus
    ) {

        if (oldStatus == newStatus) {
            return;
        }

        if (oldStatus == ApplicationStatus.CANCELLED) {
            throw new BusinessException(
                    "Отменённую заявку нельзя изменить"
            );
        }

        if (oldStatus == ApplicationStatus.REJECTED) {
            throw new BusinessException(
                    "Отклонённую заявку нельзя изменить"
            );
        }

        if (oldStatus == ApplicationStatus.APPROVED) {
            throw new BusinessException(
                    "Одобренную заявку нельзя изменить"
            );
        }

        if (oldStatus == ApplicationStatus.NEW) {

            if (newStatus != ApplicationStatus.UNDER_REVIEW &&
                    newStatus != ApplicationStatus.CANCELLED) {

                throw new BusinessException(
                        "Из NEW заявку можно перевести только " +
                                "в UNDER_REVIEW или CANCELLED"
                );
            }
        }

        if (oldStatus == ApplicationStatus.UNDER_REVIEW) {

            if (newStatus != ApplicationStatus.APPROVED &&
                    newStatus != ApplicationStatus.REJECTED &&
                    newStatus != ApplicationStatus.CANCELLED) {

                throw new BusinessException(
                        "Из UNDER_REVIEW заявку можно перевести " +
                                "в APPROVED, REJECTED или CANCELLED"
                );
            }
        }
    }

    public List<TrainingApplication> sortByDate(boolean ascending) {

        List<TrainingApplication> applications =
                new ArrayList<>(applicationRepository.findAll());

        Comparator<TrainingApplication> comparator =
                Comparator.comparing(
                        TrainingApplication::getApplicationDate
                );

        if (!ascending) {
            comparator = comparator.reversed();
        }

        applications.sort(comparator);

        return applications;
    }

    public List<TrainingApplication> sortByCourseName(
            boolean ascending
    ) {

        List<TrainingApplication> applications =
                new ArrayList<>(applicationRepository.findAll());

        Comparator<TrainingApplication> comparator =
                Comparator.comparing(
                        TrainingApplication::getCourseName,
                        String.CASE_INSENSITIVE_ORDER
                );

        if (!ascending) {
            comparator = comparator.reversed();
        }

        applications.sort(comparator);

        return applications;
    }
}
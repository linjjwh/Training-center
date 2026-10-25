package org.example.fx;

import org.example.repository.TrainingApplicationRepository;
import org.example.repository.TrainingApplicationRepositoryImpl;
import org.example.repository.UserRepository;
import org.example.repository.UserRepositoryImpl;
import org.example.service.StatisticsService;
import org.example.service.TrainingApplicationService;
import org.example.service.UserService;

public final class ServiceFactory {

    private static UserService userService;
    private static TrainingApplicationService applicationService;
    private static StatisticsService statisticsService;

    private ServiceFactory() {
    }

    public static synchronized UserService userService() {
        if (userService == null) {
            UserRepository repository = new UserRepositoryImpl();
            userService = new UserService(repository);
        }
        return userService;
    }

    public static synchronized TrainingApplicationService applicationService() {
        if (applicationService == null) {
            TrainingApplicationRepository repository =
                    new TrainingApplicationRepositoryImpl();

            applicationService = new TrainingApplicationService(
                    repository,
                    new UserRepositoryImpl()
            );
        }
        return applicationService;
    }

    public static synchronized StatisticsService statisticsService() {
        if (statisticsService == null) {
            statisticsService = new StatisticsService(
                    new TrainingApplicationRepositoryImpl()
            );
        }
        return statisticsService;
    }
}

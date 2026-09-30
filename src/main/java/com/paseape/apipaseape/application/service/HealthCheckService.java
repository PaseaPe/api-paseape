package com.paseape.apipaseape.application.service;

import com.paseape.apipaseape.application.dto.response.DatabaseHealthResponse;
import com.paseape.apipaseape.application.dto.response.HealthResponse;
import com.paseape.apipaseape.application.port.in.HealthCheckUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class HealthCheckService implements HealthCheckUseCase {

    private static final String ZONE_LIMA = "America/Lima";
    private static final String SERVICE_NAME = "api-paseape";
    private static final String ENV_PROD = "production";
    private static final String STATUS_UP = "UP";
    private static final String STATUS_DOWN = "DOWN";
    private static final String VALIDATION_QUERY = "SELECT 1";

    private final DataSource dataSource;

    @Override
    public HealthResponse execute() {
        DatabaseHealthResponse dbStatus = checkDatabaseConnection();
        String overallStatus = STATUS_UP.equals(dbStatus.getStatus()) ? STATUS_UP : STATUS_DOWN;

        return HealthResponse.builder()
                .status(overallStatus)
                .service(SERVICE_NAME)
                .environment(ENV_PROD)
                .timezone(ZONE_LIMA)
                .timestamp(ZonedDateTime.now(ZoneId.of(ZONE_LIMA)))
                .database(dbStatus)
                .build();
    }

    private DatabaseHealthResponse checkDatabaseConnection() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(VALIDATION_QUERY);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return DatabaseHealthResponse.builder()
                        .status(STATUS_UP)
                        .database("Aiven MySQL (TLS)")
                        .error(null)
                        .build();
            }
            return DatabaseHealthResponse.builder()
                    .status(STATUS_DOWN)
                    .database("Aiven MySQL (TLS)")
                    .error("Query did not return results")
                    .build();

        } catch (Exception ex) {
            return DatabaseHealthResponse.builder()
                    .status(STATUS_DOWN)
                    .database("Aiven MySQL (TLS)")
                    .error(ex.getMessage())
                    .build();
        }
    }
}

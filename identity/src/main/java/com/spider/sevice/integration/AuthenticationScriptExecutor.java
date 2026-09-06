package com.spider.sevice.integration;

import com.spider.exception.ShipmntsIntegrationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Executes the Shipmnts authentication script to generate a refresh token.
 * This component runs the npm script that uses Playwright to authenticate with Shipmnts.
 */
@Component
public class AuthenticationScriptExecutor {

    private static final long SCRIPT_TIMEOUT_MINUTES = 10;
    private static final String SCRIPT_PATH = "ui/react-auth-skeleton/scripts/shipmnts-authenticate.mjs";
    private static final String NODE_COMMAND = "node";
    private static final String ENV_FILENAME = ".env";

    /**
     * Executes the Shipmnts authentication script using credentials already present in the repository `.env`.
     */
    public void executeAuthentication() {
        executeAuthentication(null, null, false);
    }

    /**
     * Executes the Shipmnts authentication script. If email and password are provided, the method will
     * temporarily write them to the repository `.env` file before running the script. After the script
     * completes, the original `.env` will be restored but the new `SHIPMNTS_REFRESH_TOKEN` will be kept.
     *
     * @param email       optional Shipmnts email
     * @param password    optional Shipmnts password
     * @param interactive if true, the script is run with a visible browser so the user can manually complete CAPTCHA/MFA
     * @return the refresh token captured by the script (may be blank)
     */
    public String executeAuthentication(String email, String password, boolean interactive) {
        try {
            String projectRoot = findProjectRoot();
            Path envPath = Paths.get(projectRoot, ENV_FILENAME);
            String originalEnv = Files.exists(envPath) ? Files.readString(envPath, StandardCharsets.UTF_8) : "";

            // Prepare modified env if credentials provided
            String modifiedEnv = originalEnv;
            if (email != null || password != null) {
                modifiedEnv = replaceEnvValue(modifiedEnv, "SHIPMNTS_EMAIL", email == null ? "" : email);
                modifiedEnv = replaceEnvValue(modifiedEnv, "SHIPMNTS_PASSWORD", password == null ? "" : password);
                // write temporary env
                Files.writeString(envPath, modifiedEnv, StandardCharsets.UTF_8);
            }

            String scriptPath = Paths.get(projectRoot, SCRIPT_PATH).toString();
            if (!new File(scriptPath).exists()) {
                // restore if we modified
                if (!modifiedEnv.equals(originalEnv)) {
                    Files.writeString(envPath, originalEnv, StandardCharsets.UTF_8);
                }
                throw new ShipmntsIntegrationException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Shipmnts authentication script not found at: " + scriptPath);
            }

            ProcessBuilder processBuilder = new ProcessBuilder(NODE_COMMAND, scriptPath);
            processBuilder.directory(new File(projectRoot));
            processBuilder.redirectErrorStream(true);
            // set headless mode based on interactive flag
            if (interactive) {
                processBuilder.environment().put("SHIPMNTS_AUTH_HEADLESS", "false");
            } else {
                processBuilder.environment().put("SHIPMNTS_AUTH_HEADLESS", "true");
            }

            Process process = processBuilder.start();

            // Capture output for logging (non-blocking)
            // We will stream stdout lines so we can log progress while waiting
            Thread loggerThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("[shipmnts-auth] " + line);
                    }
                } catch (IOException ignored) {
                }
            }, "shipmnts-auth-logger");
            loggerThread.setDaemon(true);
            loggerThread.start();

            boolean completed = process.waitFor(SCRIPT_TIMEOUT_MINUTES, TimeUnit.MINUTES);
            if (!completed) {
                process.destroy();
                // restore original
                if (!modifiedEnv.equals(originalEnv)) {
                    Files.writeString(envPath, originalEnv, StandardCharsets.UTF_8);
                }
                throw new ShipmntsIntegrationException(HttpStatus.REQUEST_TIMEOUT,
                        "Shipmnts authentication script timed out after " + SCRIPT_TIMEOUT_MINUTES + " minutes");
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                // restore original
                if (!modifiedEnv.equals(originalEnv)) {
                    Files.writeString(envPath, originalEnv, StandardCharsets.UTF_8);
                }
                throw new ShipmntsIntegrationException(HttpStatus.BAD_GATEWAY,
                        "Shipmnts authentication script failed with exit code " + exitCode);
            }

            // Read updated env and extract refresh token
            String afterEnv = Files.exists(envPath) ? Files.readString(envPath, StandardCharsets.UTF_8) : "";
            String refreshToken = parseEnvValue(afterEnv, "SHIPMNTS_REFRESH_TOKEN");

            // Restore original env but keep refresh token line
            String restored = originalEnv;
            if (refreshToken != null && !refreshToken.isBlank()) {
                restored = replaceEnvValue(restored, "SHIPMNTS_REFRESH_TOKEN", refreshToken);
            }
            Files.writeString(envPath, restored, StandardCharsets.UTF_8);

            return refreshToken == null ? "" : refreshToken;

        } catch (ShipmntsIntegrationException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ShipmntsIntegrationException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Shipmnts authentication script was interrupted: " + e.getMessage());
        } catch (IOException e) {
            throw new ShipmntsIntegrationException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to execute Shipmnts authentication script: " + e.getMessage());
        }
    }

    private String parseEnvValue(String contents, String key) {
        for (String rawLine : contents.split("\r?\n")) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            int sep = line.indexOf('=');
            if (sep < 0) continue;
            String k = line.substring(0, sep).trim();
            String v = line.substring(sep + 1).trim();
            if (k.equals(key)) {
                if ((v.startsWith("\"") && v.endsWith("\"")) || (v.startsWith("'") && v.endsWith("'"))) {
                    v = v.substring(1, v.length() - 1);
                }
                return v;
            }
        }
        return null;
    }

    private String replaceEnvValue(String contents, String key, String value) {
        String val = value == null ? "" : value;
        if (val.contains("\n") || val.contains("\r")) {
            throw new IllegalArgumentException("Refusing to store an invalid " + key + " value.");
        }
        StringBuilder sb = new StringBuilder();
        boolean replaced = false;
        for (String rawLine : contents.split("\r?\n", -1)) {
            if (rawLine.startsWith(key + "=")) {
                sb.append(key).append("=").append(val).append(System.lineSeparator());
                replaced = true;
            } else {
                sb.append(rawLine).append(System.lineSeparator());
            }
        }
        if (!replaced) {
            if (!contents.endsWith("\n") && contents.length() > 0) sb.append(System.lineSeparator());
            sb.append(key).append("=").append(val).append(System.lineSeparator());
        }
        return sb.toString();
    }


    /**
     * Finds the project root directory by looking for key project files.
     */
    private String findProjectRoot() {
        File currentDir = new File(".").getAbsoluteFile();

        // Traverse up the directory tree to find pom.xml
        while (currentDir != null) {
            if (new File(currentDir, "pom.xml").exists() &&
                new File(currentDir, SCRIPT_PATH).exists()) {
                return currentDir.getAbsolutePath();
            }
            currentDir = currentDir.getParentFile();
        }

        // Fallback to current working directory
        return System.getProperty("user.dir");
    }
}

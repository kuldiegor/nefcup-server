package org.nefcup.server.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nefcup.server.entity.ProjectCleanRequest;
import org.nefcup.server.entity.ProjectCreateDirectoryRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ProjectServiceTest {

    @Test
    @DisplayName("Загрузку файла (ошибка, так как директории не созданы)")
    void uploadFile() throws IOException {
        Files.createDirectories(Path.of("temp"));
        ProjectService projectService = new ProjectService("temp");
        ByteArrayInputStream inputStream = new ByteArrayInputStream("test-text".getBytes(StandardCharsets.UTF_8));
        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () -> {
            projectService.uploadFile(inputStream, "test.txt", "test-project");
        });
        assertEquals(responseStatusException.getStatusCode(), HttpStatus.BAD_REQUEST);
        inputStream.close();

        Files.delete(Path.of("temp"));
    }

    @Test
    @DisplayName("Загрузку файла (успешно)")
    void uploadFile2() throws IOException {
        Path testProjectPath = Path.of("temp", "test-project");
        Files.createDirectories(testProjectPath);
        ProjectService projectService = new ProjectService("temp");
        ByteArrayInputStream inputStream = new ByteArrayInputStream("test-text".getBytes(StandardCharsets.UTF_8));
        projectService.uploadFile(inputStream,"test.txt","test-project");
        inputStream.close();

        Path testPath = Path.of("temp", "test-project", "test.txt");
        assertTrue(Files.exists(testPath));
        String temp = Files.readString(testPath, StandardCharsets.UTF_8);
        assertEquals("test-text",temp);

        Files.delete(testPath);
        Files.delete(testProjectPath);
        Files.delete(Path.of("temp"));
    }

    @Test
    @DisplayName("Очистка проекта (успешно)")
    void cleanProject() throws IOException {
        Path test2Directory = Path.of("temp", "project-temp", "test1", "test2");
        Files.createDirectories(test2Directory);
        Path testFile2Path = Path.of("temp", "project-temp", "test1", "test2", "test-file2");
        Files.writeString(testFile2Path,"test-text2", StandardCharsets.UTF_8);
        Path testFile1Path = Path.of("temp", "project-temp", "test1", "test-file1");
        Files.writeString(testFile1Path,"test-text1", StandardCharsets.UTF_8);
        Path testFilePath = Path.of("temp", "project-temp", "test-file");
        Files.writeString(testFilePath,"test-text", StandardCharsets.UTF_8);
        ProjectService projectService = new ProjectService("temp");
        projectService.cleanProject(new ProjectCleanRequest("project-temp"));

        assertFalse(Files.exists(test2Directory));
        assertFalse(Files.exists(testFile2Path));
        assertFalse(Files.exists(testFile1Path));
        assertFalse(Files.exists(testFilePath));
        assertFalse(Files.exists(Path.of("temp","project-temp")));

        Files.delete(Path.of("temp"));
    }

    @Test
    @DisplayName("Создание директории (успешно)")
    void createDirectory() throws IOException {
        ProjectService projectService = new ProjectService("temp");
        projectService.createDirectory(new ProjectCreateDirectoryRequest("project-temp","/test1/test2"));
        Path test2Directory = Path.of("temp", "project-temp", "test1", "test2");
        Path test1Directory = Path.of("temp", "project-temp", "test1");
        Path projectTempDirectory = Path.of("temp", "project-temp");
        Path tempDirectory = Path.of("temp");

        assertTrue(Files.exists(test2Directory));
        assertTrue(Files.exists(test1Directory));
        assertTrue(Files.exists(projectTempDirectory));
        assertTrue(Files.exists(tempDirectory));

        Files.delete(test2Directory);
        Files.delete(test1Directory);
        Files.delete(projectTempDirectory);
        Files.delete(tempDirectory);
    }

    @Test
    @DisplayName("Создание директории с переходом на уровень выше (успешно, но все переходы исключаются)")
    void createDirectory2() throws IOException {
        ProjectService projectService = new ProjectService("temp");
        projectService.createDirectory(new ProjectCreateDirectoryRequest("project-temp","../test1/test2"));
        Path test2Directory = Path.of("temp", "project-temp", "test1", "test2");
        Path test1Directory = Path.of("temp", "project-temp", "test1");
        Path projectTempDirectory = Path.of("temp", "project-temp");
        Path tempDirectory = Path.of("temp");

        assertTrue(Files.exists(test2Directory));
        assertTrue(Files.exists(test1Directory));
        assertTrue(Files.exists(projectTempDirectory));
        assertTrue(Files.exists(tempDirectory));

        Files.delete(test2Directory);
        Files.delete(test1Directory);
        Files.delete(projectTempDirectory);
        Files.delete(tempDirectory);
    }

    @Test
    @DisplayName("Создание директории с переходом на уровень выше (успешно, но все переходы исключаются)")
    void createDirectory3() throws IOException {
        ProjectService projectService = new ProjectService("temp");
        projectService.createDirectory(new ProjectCreateDirectoryRequest("project-temp","/../test1/test2"));
        Path test2Directory = Path.of("temp", "project-temp", "test1", "test2");
        Path test1Directory = Path.of("temp", "project-temp", "test1");
        Path projectTempDirectory = Path.of("temp", "project-temp");
        Path tempDirectory = Path.of("temp");

        assertTrue(Files.exists(test2Directory));
        assertTrue(Files.exists(test1Directory));
        assertTrue(Files.exists(projectTempDirectory));
        assertTrue(Files.exists(tempDirectory));

        Files.delete(test2Directory);
        Files.delete(test1Directory);
        Files.delete(projectTempDirectory);
        Files.delete(tempDirectory);
    }
}
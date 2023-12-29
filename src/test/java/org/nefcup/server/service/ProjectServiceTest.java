/*
    Copyright 2023 Dmitrij Kulabuhov

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package org.nefcup.server.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.nefcup.server.entity.FileDeleteRequest;
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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProjectServiceTest {

    private ProjectService projectService;

    @BeforeAll
    public void init() {
        projectService = new ProjectService("temp", "rwxr-xr-x","rwxr-xr-x");
    }

    @Test
    @DisplayName("Загрузка файла (ошибка, так как директории не созданы)")
    void uploadFile() throws IOException {
        Files.createDirectories(Path.of("temp"));

        ByteArrayInputStream inputStream = new ByteArrayInputStream("test-text".getBytes(StandardCharsets.UTF_8));
        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () -> {
            projectService.uploadFile(inputStream, "test.txt", "test-project", false);
        });
        assertEquals(responseStatusException.getStatusCode(), HttpStatus.BAD_REQUEST);
        inputStream.close();

        Files.delete(Path.of("temp"));
    }

    @Test
    @DisplayName("Загрузка файла (успешно)")
    void uploadFile2() throws IOException {
        Path testProjectPath = Path.of("temp", "test-project");
        Files.createDirectories(testProjectPath);

        ByteArrayInputStream inputStream = new ByteArrayInputStream("test-text".getBytes(StandardCharsets.UTF_8));
        projectService.uploadFile(inputStream,"test.txt","test-project", false);
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
    @DisplayName("Загрузка файла (ошибка так как файл уже есть)")
    void uploadFile3() throws IOException {
        Path testProjectPath = Path.of("temp", "test-project");
        Files.createDirectories(testProjectPath);
        Path testPath = Path.of("temp", "test-project", "test.txt");
        Files.writeString(testPath,"test-text-original",StandardCharsets.UTF_8);

        ByteArrayInputStream inputStream = new ByteArrayInputStream("test-text".getBytes(StandardCharsets.UTF_8));
        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () -> projectService.uploadFile(inputStream, "test.txt", "test-project", false));
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY,responseStatusException.getStatusCode());

        inputStream.close();


        assertTrue(Files.exists(testPath));
        String temp = Files.readString(testPath, StandardCharsets.UTF_8);
        assertEquals("test-text-original",temp);

        Files.delete(testPath);
        Files.delete(testProjectPath);
        Files.delete(Path.of("temp"));
    }

    @Test
    @DisplayName("Загрузка файла (успешно, файл существует, но включён флаг замены)")
    void uploadFile4() throws IOException {
        Path testProjectPath = Path.of("temp", "test-project");
        Files.createDirectories(testProjectPath);
        Path testPath = Path.of("temp", "test-project", "test.txt");
        Files.writeString(testPath,"test-text-original",StandardCharsets.UTF_8);

        ByteArrayInputStream inputStream = new ByteArrayInputStream("test-text".getBytes(StandardCharsets.UTF_8));
        projectService.uploadFile(inputStream, "test.txt", "test-project", true);

        inputStream.close();


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

        projectService.cleanProject(new ProjectCleanRequest("project-temp",null));

        assertFalse(Files.exists(test2Directory));
        assertFalse(Files.exists(testFile2Path));
        assertFalse(Files.exists(testFile1Path));
        assertFalse(Files.exists(testFilePath));
        assertFalse(Files.exists(Path.of("temp","project-temp")));

        Files.delete(Path.of("temp"));
    }

    @Test
    @DisplayName("Очистка проекта, каталога такого не существует (успешно)")
    void cleanProject2() throws IOException {
        Path tempDirectory = Path.of("temp");
        Files.createDirectories(tempDirectory);
        Path test2Directory = Path.of("temp", "project-temp", "test1", "test2");
        Path testFile2Path = Path.of("temp", "project-temp", "test1", "test2", "test-file2");
        Path testFile1Path = Path.of("temp", "project-temp", "test1", "test-file1");
        Path testFilePath = Path.of("temp", "project-temp", "test-file");

        projectService.cleanProject(new ProjectCleanRequest("project-temp",null));

        assertFalse(Files.exists(test2Directory));
        assertFalse(Files.exists(testFile2Path));
        assertFalse(Files.exists(testFile1Path));
        assertFalse(Files.exists(testFilePath));
        assertFalse(Files.exists(Path.of("temp","project-temp")));

        Files.delete(tempDirectory);
    }

    @Test
    @DisplayName("Очистка проекта (успешно). Проигнорированы файлы из входящего запроса.")
    void cleanProject3() throws IOException {
        Path test2Directory = Path.of("temp", "project-temp", "test1", "test2");
        Files.createDirectories(test2Directory);
        Path testFile2Path = Path.of("temp", "project-temp", "test1", "test2", "test-file2");
        Files.writeString(testFile2Path,"test-text2", StandardCharsets.UTF_8);
        Path testFile1Path = Path.of("temp", "project-temp", "test1", "test-file1");
        Files.writeString(testFile1Path,"test-text1", StandardCharsets.UTF_8);
        Path testFilePath = Path.of("temp", "project-temp", "test-file");
        Files.writeString(testFilePath,"test-text", StandardCharsets.UTF_8);

        projectService.cleanProject(
                new ProjectCleanRequest(
                        "project-temp",
                        """
                                test-file
                                test1/test-file1
                                """
                )
        );

        assertFalse(Files.exists(test2Directory));
        assertFalse(Files.exists(testFile2Path));
        assertTrue(Files.exists(testFile1Path));
        assertTrue(Files.exists(testFilePath));
        assertTrue(Files.exists(Path.of("temp","project-temp")));

        Files.delete(testFile1Path);
        Files.delete(Path.of("temp", "project-temp", "test1"));
        Files.delete(testFilePath);
        Files.delete(Path.of("temp", "project-temp"));
        Files.delete(Path.of("temp"));
    }

    @Test
    @DisplayName("Очистка проекта (успешно). Проигнорированы файлы из входящего запроса.")
    void cleanProject4() throws IOException {
        Path test2Directory = Path.of("temp", "project-temp", "test1", "test2");
        Files.createDirectories(test2Directory);
        Path testFile2Path = Path.of("temp", "project-temp", "test1", "test2", "test-file2");
        Files.writeString(testFile2Path,"test-text2", StandardCharsets.UTF_8);
        Path testFile1Path = Path.of("temp", "project-temp", "test1", "test-file1");
        Files.writeString(testFile1Path,"test-text1", StandardCharsets.UTF_8);
        Path testFilePath = Path.of("temp", "project-temp", "test-file");
        Files.writeString(testFilePath,"test-text", StandardCharsets.UTF_8);

        projectService.cleanProject(
                new ProjectCleanRequest(
                        "project-temp",
                        """
                                test1/test2/test-file2
                                test1/test-file1
                                """
                )
        );

        assertTrue(Files.exists(test2Directory));
        assertTrue(Files.exists(testFile2Path));
        assertTrue(Files.exists(testFile1Path));
        assertFalse(Files.exists(testFilePath));
        assertTrue(Files.exists(Path.of("temp","project-temp")));

        Files.delete(testFile1Path);
        Files.delete(testFile2Path);
        Files.delete(test2Directory);
        Files.delete(Path.of("temp", "project-temp", "test1"));
        Files.delete(Path.of("temp", "project-temp"));
        Files.delete(Path.of("temp"));
    }

    @Test
    @DisplayName("Создание директории (успешно)")
    void createDirectory() throws IOException {

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

    @Test
    @DisplayName("Создание директории, название одно и тоже (успешно)")
    void createDirectory4() throws IOException {
        Path test2Directory = Path.of("temp", "project-temp", "test1", "test2");
        Files.createDirectories(test2Directory);

        projectService.createDirectory(new ProjectCreateDirectoryRequest("project-temp","/test1/test2"));

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
    @DisplayName("Создание директории, название пустое (успешно)")
    void createDirectory5() throws IOException {
        Path tempDirectory = Path.of("temp");
        Files.createDirectories(tempDirectory);


        projectService.createDirectory(new ProjectCreateDirectoryRequest("project-temp",""));

        Path projectTempDirectory = Path.of("temp", "project-temp");

        assertTrue(Files.exists(projectTempDirectory));
        assertTrue(Files.exists(tempDirectory));

        Files.delete(projectTempDirectory);
        Files.delete(tempDirectory);
    }

    @Test
    @DisplayName("Удаление файла (успешно, файл существует)")
    void deleteFile1() throws IOException {
        Path testProjectPath = Path.of("temp", "test-project");
        Files.createDirectories(testProjectPath);
        Path testPath = Path.of("temp", "test-project", "test.txt");
        Files.writeString(testPath,"test-text-original",StandardCharsets.UTF_8);

        projectService.deleteFile(new FileDeleteRequest(
                "test-project",
                "test.txt",
                null
        ));

        assertFalse(Files.exists(testPath));

        Files.delete(testProjectPath);
        Files.delete(Path.of("temp"));
    }

    @Test
    @DisplayName("Удаление файла (успешно, файл отсутствует)")
    void deleteFile2() throws IOException {
        Path testProjectPath = Path.of("temp", "test-project");
        Files.createDirectories(testProjectPath);
        Path testPath = Path.of("temp", "test-project", "test.txt");

        projectService.deleteFile(new FileDeleteRequest(
                "test-project",
                "test.txt",
                null
        ));

        assertFalse(Files.exists(testPath));

        Files.delete(testProjectPath);
        Files.delete(Path.of("temp"));
    }

    @Test
    @DisplayName("Удаление файла вместе с каталогом (успешно, файл существует)")
    void deleteFile3() throws IOException {
        Path testProjectPath = Path.of("temp", "test-project");
        Files.createDirectories(testProjectPath);
        Path test1Directory = Path.of("temp", "test-project", "test1");
        Files.createDirectories(test1Directory);

        Path testPath = Path.of("temp", "test-project", "test1","test.txt");
        Files.writeString(testPath,"test-text-original",StandardCharsets.UTF_8);

        projectService.deleteFile(new FileDeleteRequest(
                "test-project",
                "test1",
                null
        ));

        assertFalse(Files.exists(testPath));
        assertFalse(Files.exists(test1Directory));

        Files.delete(testProjectPath);
        Files.delete(Path.of("temp"));
    }

    @Test
    @DisplayName("Удаление файла (успешно, файл существует)")
    void deleteFile4() throws IOException {
        Path testProjectPath = Path.of("temp", "test-project");
        Files.createDirectories(testProjectPath);
        Path test1Directory = Path.of("temp", "test-project", "test1");
        Files.createDirectories(test1Directory);

        Path testPath = Path.of("temp", "test-project", "test1","test.txt");

        projectService.deleteFile(new FileDeleteRequest(
                "test-project",
                "test1/test.txt",
                null
        ));

        assertFalse(Files.exists(testPath));
        assertTrue(Files.exists(test1Directory));

        Files.delete(test1Directory);
        Files.delete(testProjectPath);
        Files.delete(Path.of("temp"));
    }

    @Test
    @DisplayName("Удаление файла (успешно). Проигнорированы файлы из входящего запроса.")
    void deleteFile5() throws IOException {
        Path test2Directory = Path.of("temp", "project-temp", "test1", "test2");
        Files.createDirectories(test2Directory);
        Path testFile2Path = Path.of("temp", "project-temp", "test1", "test2", "test-file2");
        Files.writeString(testFile2Path,"test-text2", StandardCharsets.UTF_8);
        Path testFile1Path = Path.of("temp", "project-temp", "test1", "test-file1");
        Files.writeString(testFile1Path,"test-text1", StandardCharsets.UTF_8);
        Path testFilePath = Path.of("temp", "project-temp", "test-file");
        Files.writeString(testFilePath,"test-text", StandardCharsets.UTF_8);

        projectService.deleteFile(
                new FileDeleteRequest(
                        "project-temp",
                        "test1",
                        """
                                test-file1
                                """
                )
        );

        assertFalse(Files.exists(test2Directory));
        assertFalse(Files.exists(testFile2Path));
        assertTrue(Files.exists(testFile1Path));
        assertTrue(Files.exists(testFilePath));
        assertTrue(Files.exists(Path.of("temp","project-temp")));
        assertTrue(Files.exists(Path.of("temp","project-temp","test1")));

        Files.delete(testFile1Path);
        Files.delete(testFilePath);
        Files.delete(Path.of("temp", "project-temp", "test1"));
        Files.delete(Path.of("temp", "project-temp"));
        Files.delete(Path.of("temp"));
    }

}
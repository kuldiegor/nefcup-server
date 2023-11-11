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

import lombok.extern.slf4j.Slf4j;
import org.nefcup.server.entity.ProjectCleanRequest;
import org.nefcup.server.entity.ProjectCreateDirectoryRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class ProjectService {
    private final String rootDirectory;
    private final Set<PosixFilePermission> filePosixFilePermission;
    private final Set<PosixFilePermission> directoryPosixFilePermission;

    public ProjectService(
            @Value("${nefcup.root-directory}") String rootDirectory,
            @Value("${nefcup.file-permissions}") String filePermissionsStr,
            @Value("${nefcup.directory-permissions}") String directoryPermissionsStr
            ) {
        log.info("nefcup.root-directory = "+rootDirectory);
        this.rootDirectory = rootDirectory;
        filePosixFilePermission = PosixFilePermissions.fromString(filePermissionsStr);
        directoryPosixFilePermission = PosixFilePermissions.fromString(directoryPermissionsStr);
    }

    public void uploadFile(InputStream inputStream, String fileName, String projectName, Boolean isReplace) {
        Path projectPath = Path.of("/"+projectName).normalize();
        Path filePath = Path.of("/"+fileName).normalize();
        Path fullPathOfFile = Path.of(rootDirectory, projectPath.toString(), filePath.toString());
        if (!isReplace && Files.exists(fullPathOfFile)){
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY);
        }
        try {
            if (!Files.exists(fullPathOfFile)) {
                Files.createFile(fullPathOfFile);
                Files.setPosixFilePermissions(fullPathOfFile, filePosixFilePermission);
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        log.info("file = {}",fullPathOfFile);
        try (OutputStream outputStream = Files.newOutputStream(fullPathOfFile)) {
            inputStream.transferTo(outputStream);
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void cleanProject(ProjectCleanRequest request) {
        Path projectPath = Path.of("/"+request.getProjectName()).normalize();
        Path fullPathOfProject = Path.of(rootDirectory, projectPath.toString());
        if (!Files.exists(fullPathOfProject)){
            return;
        }
        try (Stream<Path> pathStream = Files.walk(fullPathOfProject)) {
            List<Path> pathList = pathStream.collect(Collectors.toList());
            for (int i= pathList.size()-1;i>=0;i--){
                Files.delete(pathList.get(i));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void createDirectory(ProjectCreateDirectoryRequest request) {
        Path projectPath = Path.of("/"+request.getProjectName()).normalize();
        Path directoryPath = Path.of("/"+request.getDirectoryName()).normalize();
        Path fullPath = Path.of(rootDirectory, projectPath.toString(),directoryPath.toString());

        try {
            Files.createDirectories(fullPath);
            Files.setPosixFilePermissions(fullPath,directoryPosixFilePermission);
            log.info("directory = {}",fullPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

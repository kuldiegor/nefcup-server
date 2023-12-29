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
package org.nefcup.server.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nefcup.server.entity.FileDeleteRequest;
import org.nefcup.server.entity.ProjectCleanRequest;
import org.nefcup.server.entity.ProjectCreateDirectoryRequest;
import org.nefcup.server.service.ProjectService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/project")
public class ProjectController {
    private final ProjectService projectService;

    @PostMapping(value = "/file/upload",consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void uploadFile(
            HttpServletRequest request,
            @RequestParam("file-name") String fileName,
            @RequestParam("project-name") String projectName,
            @RequestParam("is-replace") Boolean isReplace
            ) throws IOException {
        projectService.uploadFile(request.getInputStream(),fileName,projectName,isReplace);
    }

    @PostMapping(value = "/clean",consumes = MediaType.APPLICATION_JSON_VALUE)
    public void clean(@Valid @RequestBody ProjectCleanRequest request){
        projectService.cleanProject(request);
    }

    @PostMapping(value = "/directory/create",consumes = MediaType.APPLICATION_JSON_VALUE)
    public void createDirectory(@Valid @RequestBody ProjectCreateDirectoryRequest request){
        projectService.createDirectory(request);
    }

    @PostMapping(value = "/file/delete",consumes = MediaType.APPLICATION_JSON_VALUE)
    public void deleteFile(@Valid @RequestBody FileDeleteRequest request) {
        projectService.deleteFile(request);
    }
}

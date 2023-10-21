package org.nefcup.server.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
}

package pl.put.poznan.rulestudio.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.put.poznan.rulestudio.model.Project;
import pl.put.poznan.rulestudio.model.ProjectsContainer;

import java.io.IOException;
import java.io.ObjectInputStream;

@Service
public class ImportService {

    private static final Logger logger = LoggerFactory.getLogger(ImportService.class);

    @Autowired
    ProjectsContainer projectsContainer;

    public Project getImport(MultipartFile importFile) throws IOException, ClassNotFoundException {
        Project project = null;

        ObjectInputStream ois = new ObjectInputStream(importFile.getInputStream());
        project = (Project)ois.readObject();

        projectsContainer.addProject(project);
        return project;
    }
}

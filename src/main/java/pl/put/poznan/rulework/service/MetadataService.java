package pl.put.poznan.rulework.service;

import javafx.util.Pair;
import org.rulelearn.data.Attribute;
import org.rulelearn.data.InformationTable;
import org.rulelearn.data.json.AttributeParser;
import org.rulelearn.data.json.InformationTableWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.put.poznan.rulework.exception.NoDataException;
import pl.put.poznan.rulework.model.Project;
import pl.put.poznan.rulework.model.ProjectsContainer;

import java.io.*;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class MetadataService {

    private static final Logger logger = LoggerFactory.getLogger(MetadataService.class);

    @Autowired
    ProjectsContainer projectsContainer;

    private static Attribute[] attributesFromInputStreamMetadata(InputStream targetStream) throws IOException {
        Attribute[] attributes;
        AttributeParser attributeParser = new AttributeParser();
        Reader reader = new InputStreamReader(targetStream);
        attributes = attributeParser.parseAttributes(reader);
        for(int i = 0; i < attributes.length; i++) {
            logger.info(i + ":\t" + attributes[i]);
        }

        return attributes;
    }

    public static Attribute[] attributesFromMultipartFileMetadata(MultipartFile metadata) throws IOException {
        InputStream targetStream = metadata.getInputStream();
        return attributesFromInputStreamMetadata(targetStream);
    }

    public static Attribute[] attributesFromStringMetadata(String metadata) throws IOException {
        InputStream targetStream = new ByteArrayInputStream(metadata.getBytes());
        return attributesFromInputStreamMetadata(targetStream);
    }

    public Attribute[] getMetadata(UUID id) {
        logger.info("Id:\t" + id);

        Project project = ProjectService.getProjectFromProjectsContainer(projectsContainer, id);

        InformationTable informationTable = project.getInformationTable();
        if (informationTable == null) {
            NoDataException ex = new NoDataException("There is no metadata in project. Couldn't get it.");
            logger.error(ex.getMessage());
            throw ex;
        }

        return informationTable.getAttributes();
    }

    public Project putMetadata(UUID id, String metadata) throws IOException {
        logger.info("Id:\t" + id);
        logger.info("Metadata:\t" + metadata);


        Project project = ProjectService.getProjectFromProjectsContainer(projectsContainer, id);

        Attribute[] attributes = attributesFromStringMetadata(metadata);

        InformationTable informationTable = new InformationTable(attributes, new ArrayList<>());

        project.setInformationTable(informationTable);
        logger.info(project.toString());

        return project;
    }

    private InputStreamResource produceJsonResource(InformationTable informationTable) throws IOException {
        StringWriter sw = new StringWriter();

        InformationTableWriter itw = new InformationTableWriter();
        itw.writeAttributes(informationTable, sw);

        byte[] barray = sw.toString().getBytes();
        InputStream is = new ByteArrayInputStream(barray);

        return new InputStreamResource(is);
    }

    public Pair<String, Resource> getDownload(UUID id) throws IOException {
        logger.info("Id:\t" + id);

        Project project = ProjectService.getProjectFromProjectsContainer(projectsContainer, id);

        InformationTable informationTable = project.getInformationTable();
        if (informationTable == null) {
            NoDataException ex = new NoDataException("There is no metadata in project. Couldn't download it.");
            logger.error(ex.getMessage());
            throw ex;
        }

        InputStreamResource resource = produceJsonResource(informationTable);

        return new Pair<>(project.getName(), resource);
    }

    public Pair<String, Resource> putDownload(UUID id, String metadata) throws IOException {
        logger.info("Downloading metadata in json format");
        logger.info("Id:\t{}", id);
        logger.info("Metadata:\t{}", metadata);

        Project project = ProjectService.getProjectFromProjectsContainer(projectsContainer, id);

        // prepare attributes from metadata
        Attribute[] attributes = attributesFromStringMetadata(metadata);
        InformationTable informationTable = new InformationTable(attributes, new ArrayList<>());

        // serialize data from InformationTable object
        InputStreamResource resource = produceJsonResource(informationTable);

        return new Pair<>(project.getName(), resource);
    }
}

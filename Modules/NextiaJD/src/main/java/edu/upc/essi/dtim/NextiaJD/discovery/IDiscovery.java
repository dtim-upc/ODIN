package edu.upc.essi.dtim.NextiaJD.discovery;


import edu.upc.essi.dtim.NextiaCore.datasets.Dataset;
import edu.upc.essi.dtim.NextiaCore.discovery.Alignment;
import jakarta.xml.bind.JAXBException;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public interface IDiscovery {

    double calculateJoinQualityDiscreteFromCSV(String CSVPath1, String CSVPath2, String att1, String att2);

    double calculateJoinQualityContinuousFromCSV(String CSVPath1, String CSVPath2, String att1, String att2);

    LinkedList<Map<String, Object>> calculateQualitiesFromDatasets(String CSVPath1, String CSVPath2, String qualityType);

    JSONArray createProfile(String path, String pathToStoreProfile, String resultingProfileName) throws SQLException, ClassNotFoundException, IOException;

    double predictJoinQuality(String path1, String path2, String att1, String att2) throws SQLException, ClassNotFoundException, IOException, ParseException, SAXException, JAXBException;

    List<Alignment> getAlignments(Dataset d1, Dataset d2) throws Exception;
}

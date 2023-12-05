package edu.upc.essi.dtim.NextiaJD;


import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaCore.discovery.Alignment;
import jakarta.xml.bind.JAXBException;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public interface IDiscovery {

    double calculateJoinQualityDiscrete(String table1, String table2, String att1, String att2) throws SQLException, ClassNotFoundException, IOException, InterruptedException;

    double calculateJoinQualityContinuous(String table1, String table2, String att1, String att2) throws SQLException, ClassNotFoundException, IOException, InterruptedException;

    JSONArray createProfile(String path, String pathToStoreProfile, String resultingProfileName) throws SQLException, ClassNotFoundException, IOException;

    double predictJoinQuality(String path1, String path2, String att1, String att2) throws SQLException, ClassNotFoundException, IOException, ParseException, SAXException, JAXBException;

    List<Alignment> getAlignments(Dataset d1, Dataset d2) throws Exception;
}

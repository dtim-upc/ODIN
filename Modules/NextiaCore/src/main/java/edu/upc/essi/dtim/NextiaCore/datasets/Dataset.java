package edu.upc.essi.dtim.NextiaCore.datasets;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import edu.upc.essi.dtim.NextiaCore.graph.LocalGraph;
import edu.upc.essi.dtim.NextiaCore.repositories.DataRepository;
import edu.upc.essi.dtim.NextiaCore.graph.jena.LocalGraphJenaImpl;
import edu.upc.essi.dtim.NextiaCore.discovery.Attribute;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public class Dataset {
	private String id;
	private String datasetName;
	private String datasetDescription;
	private Date created_at;
	private List<Attribute> attributes;
	private String wrapper;
	@JsonIgnoreProperties({"datasets"})
	private DataRepository repository;
	private LocalGraph localGraph;
	// This is used as the name of the dataset in files and in the data layers, as it is more robust than a "normal" ID.
	private String UUID;

	public Dataset() {}
	public Dataset(String id, String name, String description) {
		this.id = id;
		this.datasetName = name;
		this.datasetDescription = description;
		this.created_at = new Date(System.currentTimeMillis());
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public String getDatasetName() {
		return datasetName;
	}
	public void setDatasetName(String name) {
		this.datasetName = name;
	}

	public String getDatasetDescription() {
		return datasetDescription;
	}
	public void setDatasetDescription(String description) {
		this.datasetDescription = description;
	}

	public Date getCreated_at() {
		return created_at;
	}
	public void setCreated_at(Timestamp created_at) {
		this.created_at = created_at;
	}

	public List<Attribute> getAttributes() {return attributes;}
	public void setAttributes(List<Attribute> attributes) {this.attributes = attributes;}

	public String getWrapper() {
		return wrapper;
	}
	public void setWrapper(String wrapper) {
		this.wrapper = wrapper;
	}

	public LocalGraph getLocalGraph() {
		return localGraph;
	}
	public void setLocalGraph(LocalGraph localGraph) {
		this.localGraph = localGraph;
	}

	public DataRepository getRepository() {
		return repository;
	}
	public void setRepository(DataRepository repository) {
		this.repository = repository;
	}

	public String getUUID() {
		return UUID;
	}
	public void setUUID(String UUID) {
		this.UUID = UUID;
	}
}
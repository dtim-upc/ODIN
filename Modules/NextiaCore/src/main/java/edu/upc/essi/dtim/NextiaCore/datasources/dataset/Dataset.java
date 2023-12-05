package edu.upc.essi.dtim.NextiaCore.datasources.dataset;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.DataRepository;
import edu.upc.essi.dtim.NextiaCore.graph.jena.LocalGraphJenaImpl;
import edu.upc.essi.dtim.NextiaCore.discovery.Attribute;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;


public class Dataset {
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	private String id;
	private String datasetName;
	private String datasetDescription;
	private Date created_at;

	private List<Attribute> attributes;

	public String getWrapper() {
		return wrapper;
	}

	public void setWrapper(String wrapper) {
		this.wrapper = wrapper;
	}

	private String wrapper;

	@JsonIgnoreProperties({"datasets"})
	private DataRepository repository;

	public LocalGraphJenaImpl getLocalGraph() {
		return localGraph;
	}

	public void setLocalGraph(LocalGraphJenaImpl localGraph) {
		this.localGraph = localGraph;
	}

	private LocalGraphJenaImpl localGraph;

	public String getUUID() {
		return UUID;
	}

	public void setUUID(String UUID) {
		this.UUID = UUID;
	}

	private String UUID;



	/**
	 * Constructor for the Dataset class.
	 *
	 * @param id          The ID of the dataset.
	 * @param name        The name of the dataset.
	 * @param description A description of the dataset.
	 */
	public Dataset(String id, String name, String description) {
		this.id = id;
		this.datasetName = name;
		this.datasetDescription = description;
		this.created_at = new Date(System.currentTimeMillis());
	}

	public Dataset() {
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

	public DataRepository getRepository() {
		return repository;
	}

	public void setRepository(DataRepository repository) {
		this.repository = repository;
	}

	public List<Attribute> getAttributes() {return attributes;}

	public void setAttributes(List<Attribute> attributes) {this.attributes = attributes;}
}
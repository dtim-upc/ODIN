package edu.upc.essi.dtim.NextiaCore.queries;

import java.util.LinkedList;
import java.util.List;

public class Query {
	private String queryID;
	private String CSVPath;
	private String queryName;
	private String UUID;
	private String label;
	private List<Workflow> workflows;

	public Query() {}

	public Query(String CSVPath, String queryName, String UUID, String label) {
		this.CSVPath = CSVPath;
		this.queryName = queryName;
		this.UUID = UUID;
		this.label = label;
		this.workflows = new LinkedList<>();
	}

	public String getQueryID() { return queryID; }
	public void setQueryID(String queryID) { this.queryID = queryID; }

	public String getCSVPath() { return CSVPath; }
	public void setCSVPath(String CSVPath) { this.CSVPath = CSVPath; }

	public String getQueryName() { return queryName; }
	public void setQueryName(String queryName) { this.queryName = queryName; }

	public String getUUID() { return UUID; }
	public void setUUID(String UUID) { this.UUID = UUID;}

	public String getLabel() { return label; }
	public void setLabel(String label) { this.label = label; }

	public List<Workflow> getWorkflows() { return workflows; }
	public void setWorkflows(List<Workflow> workflows) { this.workflows = workflows; }
	public void addWorkflow(Workflow workflow) { this.workflows.add(workflow); }
}
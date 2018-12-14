package org.ibp.api.rest.dataset;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.generationcp.middleware.pojos.dms.Phenotype;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.Map;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AutoProperty
public class ObservationUnitRow {

	private Integer observationUnitId;

	private Integer gid;

	private String designation;

	private String action;

	private Map<String, ObservationUnitData> variables;

	public ObservationUnitRow() {

	}

	public Integer getObservationUnitId() {
		return this.observationUnitId;
	}

	public void setObservationUnitId(final Integer observationUnitId) {
		this.observationUnitId = observationUnitId;
	}

	public Integer getGid() {
		return this.gid;
	}

	public void setGid(final Integer gid) {
		this.gid = gid;
	}

	public String getDesignation() {
		return this.designation;
	}

	public void setDesignation(final String designation) {
		this.designation = designation;
	}

	public String getAction() {
		return this.action;
	}

	public void setAction(final String action) {
		this.action = action;
	}

	public Map<String, ObservationUnitData> getVariables() {
		return this.variables;
	}

	public void setVariables(final Map<String, ObservationUnitData> variables) {
		this.variables = variables;
	}

	@Override
	public boolean equals(final Object o) {
		return Pojomatic.equals(this, o);
	}

	@Override
	public int hashCode() {
		return Pojomatic.hashCode(this);
	}

	@Override
	public String toString() {
		return Pojomatic.toString(this);
	}
}
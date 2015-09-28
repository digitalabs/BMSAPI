
package org.ibp.api.domain.study;

import java.util.ArrayList;
import java.util.List;

public class EnvironmentDetails {

	private Integer numberOfEnvironments = new Integer(1);
	private Integer numberOfReplications = new Integer(1);

	private DesignType designType;

	private List<EnvironmentLevelVariable> environmentLevelVariables = new ArrayList<>();

	private List<EnvironmentLevelObservation> environmentLevelObservations = new ArrayList<>();

	public Integer getNumberOfEnvironments() {
		return this.numberOfEnvironments;
	}

	public void setNumberOfEnvironments(final Integer numberOfEnvironments) {
		this.numberOfEnvironments = numberOfEnvironments;
	}

	public Integer getNumberOfReplications() {
		return this.numberOfReplications;
	}

	public void setNumberOfReplications(final Integer numberOfReplications) {
		this.numberOfReplications = numberOfReplications;
	}

	public DesignType getDesignType() {
		return this.designType;
	}

	public void setDesignType(final DesignType designType) {
		this.designType = designType;
	}

	public List<EnvironmentLevelVariable> getEnvironmentLevelVariables() {
		return this.environmentLevelVariables;
	}

	public void setEnvironmentLevelVariables(final List<EnvironmentLevelVariable> environmentLevelVariables) {
		this.environmentLevelVariables = environmentLevelVariables;
	}

	public List<EnvironmentLevelObservation> getEnvironmentLevelObservations() {
		return this.environmentLevelObservations;
	}

	public void setEnvironmentLevelObservations(final List<EnvironmentLevelObservation> environmentLevelObservations) {
		this.environmentLevelObservations = environmentLevelObservations;
	}

	public String findVariableName(final Integer variableId) {
		for (final EnvironmentLevelVariable var : this.getEnvironmentLevelVariables()) {
			if (var.getVariableId().equals(variableId)) {
				return var.getVariableName();
			}
		}
		return "";
	}

}

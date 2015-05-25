package org.ibp.api.domain.ontology;

import java.util.HashSet;
import java.util.Set;

/**
 * Contains basic data used for list, insert and update of variable
 * Extended from {@link TermSummary} for getting basic fields like id, name and description
 */
public class VariableSummary extends TermSummary {

	private String alias;
	private String programUuid;
	private TermSummary propertySummary = new TermSummary();
	private TermSummary methodSummary = new TermSummary();
	private TermSummary scaleSummary = new TermSummary();
	private DataType dataType = new DataType();
	private Set<VariableType> variableTypes = new HashSet<>();
	private boolean favourite;
	private final ExpectedRange expectedRange = new ExpectedRange();
	private MetadataSummary metadata = new MetadataSummary();

	public String getProgramUuid() {
		return programUuid;
	}

	public void setProgramUuid(String programUuid) {
		this.programUuid = programUuid;
	}

	public MetadataSummary getMetadata() {
		return metadata;
	}

	public void setMetadata(MetadataSummary metadata) {
		this.metadata = metadata;
	}

	public String getAlias() {
		return this.alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public TermSummary getPropertySummary() {
		return this.propertySummary;
	}

	public void setPropertySummary(TermSummary propertySummary) {
		this.propertySummary = propertySummary;
	}

	public TermSummary getMethodSummary() {
		return this.methodSummary;
	}

	public void setMethodSummary(TermSummary methodSummary) {
		this.methodSummary = methodSummary;
	}

	public TermSummary getScaleSummary() {
		return this.scaleSummary;
	}

	public void setScaleSummary(TermSummary scaleSummary) {
		this.scaleSummary = scaleSummary;
	}

	public DataType getDataType() {
		return dataType;
	}

	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}

	public Set<VariableType> getVariableTypes() {
		return this.variableTypes;
	}

	public void setVariableTypes(Set<VariableType> variables) {
		// Note: clear list if any exist
		this.variableTypes.clear();

		if (variables == null){
			return;
		}

		for (VariableType v : variables) {
			this.variableTypes.add(new VariableType(v.getId(), v.getName(), v.getDescription()));
		}
	}

	public boolean isFavourite() {
		return this.favourite;
	}

	public void setFavourite(boolean favourite) {
		this.favourite = favourite;
	}

	public ExpectedRange getExpectedRange() {
		return this.expectedRange;
	}

	public void setExpectedMin(String min) {
		this.expectedRange.setMin(min);
	}

	public void setExpectedMax(String max) {
		this.expectedRange.setMax(max);
	}

	@Override
	public String toString() {
		return "VariableSummary{" +
				"alias='" + alias + '\'' +
				", propertySummary=" + propertySummary +
				", methodSummary=" + methodSummary +
				", scaleSummary=" + scaleSummary +
				", variableTypes=" + variableTypes +
				", favourite=" + favourite +
				", expectedRange=" + expectedRange +
				"} " + super.toString();
	}
}

package org.ibp.api.domain.ontology;

import java.util.Set;

/**
 * Contains basic data used for list, insert and update of property
 * Extended from {@link TermSummary} for getting basic fields like id, name and description
 */

public class PropertySummary extends TermSummary {

	private String cropOntologyId;
	private Set<String> classes;
	private MetadataSummary metadata = new MetadataSummary();


	public String getCropOntologyId() {
		return this.cropOntologyId;
	}

	public void setCropOntologyId(String cropOntologyId) {
		this.cropOntologyId = cropOntologyId;
	}

	public Set<String> getClasses() {
		return this.classes;
	}

	public void setClasses(Set<String> classes) {
		this.classes = classes;
	}

	public MetadataSummary getMetadata() {
		return metadata;
	}

	public void setMetadata(MetadataSummary metadata) {
		this.metadata = metadata;
	}

	@Override
	public String toString() {
		return "PropertySummary{" +
				"cropOntologyId='" + cropOntologyId + '\'' +
				", classes=" + classes +
				", metadata=" + metadata +
				"} " + super.toString();
	}
}
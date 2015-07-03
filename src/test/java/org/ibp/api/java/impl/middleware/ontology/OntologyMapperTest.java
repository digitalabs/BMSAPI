
package org.ibp.api.java.impl.middleware.ontology;

import java.text.ParseException;
import java.util.Date;

import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermRelationship;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.domain.ontology.Method;
import org.generationcp.middleware.domain.ontology.Property;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.util.ISO8601DateParser;
import org.generationcp.middleware.util.StringUtil;
import org.ibp.api.domain.ontology.MethodDetails;
import org.ibp.api.domain.ontology.MethodSummary;
import org.ibp.api.domain.ontology.PropertyDetails;
import org.ibp.api.domain.ontology.PropertySummary;
import org.ibp.api.domain.ontology.ScaleDetails;
import org.ibp.api.domain.ontology.ScaleSummary;
import org.ibp.api.domain.ontology.TermSummary;
import org.ibp.api.domain.ontology.VariableDetails;
import org.junit.Assert;
import org.junit.Test;
import org.modelmapper.ModelMapper;

/**
 * Test to check mapping between middleware domain and bmsapi domain
 */
public class OntologyMapperTest {

	@Test
	public void methodSummaryMapperTest() throws ParseException {

		Method method = TestDataProvider.getTestMethod();

		ModelMapper mapper = OntologyMapper.getInstance();

		MethodSummary methodSummary = mapper.map(method, MethodSummary.class);

		Assert.assertEquals(String.valueOf(method.getId()), methodSummary.getId());
		Assert.assertEquals(method.getName(), methodSummary.getName());
		Assert.assertEquals(method.getDefinition(), methodSummary.getDescription());
		Assert.assertEquals(method.getDateCreated(), ISO8601DateParser.parse(methodSummary.getMetadata().getDateCreated()));
		Assert.assertEquals(method.getDateLastModified(), ISO8601DateParser.parse(methodSummary.getMetadata().getDateLastModified()));
	}

	@Test
	public void methodDetailsMapperTest() throws ParseException {
		Method method = TestDataProvider.getTestMethod();
		ModelMapper mapper = OntologyMapper.getInstance();

		MethodDetails methodDetails = mapper.map(method, MethodDetails.class);

		Assert.assertEquals(String.valueOf(method.getId()), methodDetails.getId());
		Assert.assertEquals(method.getName(), methodDetails.getName());
		Assert.assertEquals(method.getDefinition(), methodDetails.getDescription());
		Assert.assertEquals(method.getDateCreated(), ISO8601DateParser.parse(methodDetails.getMetadata().getDateCreated()));
		Assert.assertEquals(method.getDateLastModified(), ISO8601DateParser.parse(methodDetails.getMetadata().getDateLastModified()));
		Assert.assertTrue(methodDetails.getMetadata().getEditableFields().isEmpty());
		Assert.assertFalse(methodDetails.getMetadata().isDeletable());
		Assert.assertTrue(methodDetails.getMetadata().getUsage().getVariables().isEmpty());
	}

	@Test
	public void propertySummaryMapperTest() throws ParseException {
		Term term = new Term();

		term.setId(1);
		term.setName("name");
		term.setDefinition("definition");

		Property property = new Property(term);
		property.setDateCreated(new Date());
		property.setDateLastModified(new Date());

		ModelMapper mapper = OntologyMapper.getInstance();

		PropertySummary propertySummary = mapper.map(property, PropertySummary.class);

		Assert.assertEquals(String.valueOf(property.getId()), propertySummary.getId());
		Assert.assertEquals(property.getName(), propertySummary.getName());
		Assert.assertEquals(property.getDefinition(), propertySummary.getDescription());
		Assert.assertEquals(property.getDateCreated(), ISO8601DateParser.parse(propertySummary.getMetadata().getDateCreated()));
		Assert.assertEquals(property.getDateLastModified(), ISO8601DateParser.parse(propertySummary.getMetadata().getDateLastModified()));
	}

	@Test
	public void propertyDetailsMapperTest() throws ParseException {

		Property property = TestDataProvider.getTestProperty();
		ModelMapper mapper = OntologyMapper.getInstance();

		PropertyDetails propertyDetails = mapper.map(property, PropertyDetails.class);

		Assert.assertEquals(String.valueOf(property.getId()), propertyDetails.getId());
		Assert.assertEquals(property.getName(), propertyDetails.getName());
		Assert.assertEquals(property.getDefinition(), propertyDetails.getDescription());
		Assert.assertEquals(property.getCropOntologyId(), propertyDetails.getCropOntologyId());
		Assert.assertEquals(property.getDateCreated(), ISO8601DateParser.parse(propertyDetails.getMetadata().getDateCreated()));
		Assert.assertEquals(property.getDateLastModified(), ISO8601DateParser.parse(propertyDetails.getMetadata().getDateLastModified()));
		Assert.assertTrue(propertyDetails.getMetadata().getEditableFields().isEmpty());
		Assert.assertFalse(propertyDetails.getMetadata().isDeletable());
		Assert.assertTrue(propertyDetails.getMetadata().getUsage().getVariables().isEmpty());
	}

	@Test
	public void scaleSummaryMapperTest() throws ParseException {
		Term term = new Term();

		term.setId(1);
		term.setName("name");
		term.setDefinition("definition");

		Scale scale = new Scale(term);
		scale.setDateCreated(new Date());
		scale.setDateLastModified(new Date());

		ModelMapper mapper = OntologyMapper.getInstance();

		ScaleSummary scaleSummary = mapper.map(scale, ScaleSummary.class);

		Assert.assertEquals(String.valueOf(scale.getId()), scaleSummary.getId());
		Assert.assertEquals(scale.getName(), scaleSummary.getName());
		Assert.assertEquals(scale.getDefinition(), scaleSummary.getDescription());
		Assert.assertEquals(scale.getDateCreated(), ISO8601DateParser.parse(scaleSummary.getMetadata().getDateCreated()));
		Assert.assertEquals(scale.getDateLastModified(), ISO8601DateParser.parse(scaleSummary.getMetadata().getDateLastModified()));
	}

	@Test
	public void scaleDetailsMapperTest() throws ParseException {

		Scale scale = TestDataProvider.getTestScale();
		ModelMapper mapper = OntologyMapper.getInstance();

		ScaleDetails scaleDetails = mapper.map(scale, ScaleDetails.class);

		Assert.assertEquals(String.valueOf(scale.getId()), scaleDetails.getId());
		Assert.assertEquals(scale.getName(), scaleDetails.getName());
		Assert.assertEquals(scale.getDefinition(), scaleDetails.getDescription());
		Assert.assertEquals(scale.getMinValue(), String.valueOf(scaleDetails.getValidValues().getMin()));
		Assert.assertEquals(scale.getMaxValue(), String.valueOf(scaleDetails.getValidValues().getMax()));
		Assert.assertEquals(scale.getDateCreated(), ISO8601DateParser.parse(scaleDetails.getMetadata().getDateCreated()));
		Assert.assertEquals(scale.getDateLastModified(), ISO8601DateParser.parse(scaleDetails.getMetadata().getDateLastModified()));
		Assert.assertTrue(scaleDetails.getMetadata().getEditableFields().isEmpty());
		Assert.assertFalse(scaleDetails.getMetadata().isDeletable());
		Assert.assertTrue(scaleDetails.getMetadata().getUsage().getVariables().isEmpty());
	}

	@Test
	public void variableDetailsMapperTest() throws ParseException {

		Variable variable = TestDataProvider.getTestVariable();
		ModelMapper mapper = OntologyMapper.getInstance();

		VariableDetails variableDetails = mapper.map(variable, VariableDetails.class);

		Assert.assertEquals(String.valueOf(variable.getId()), variableDetails.getId());
		Assert.assertEquals(variable.getName(), variableDetails.getName());
		Assert.assertEquals(variable.getDefinition(), variableDetails.getDescription());
		Assert.assertEquals(String.valueOf(variable.getMethod().getId()), variableDetails.getMethodSummary().getId());
		Assert.assertEquals(variable.getMethod().getName(), variableDetails.getMethodSummary().getName());
		Assert.assertEquals(variable.getMethod().getDefinition(), variableDetails.getMethodSummary().getDescription());
		Assert.assertEquals(String.valueOf(variable.getProperty().getId()), variableDetails.getPropertySummary().getId());
		Assert.assertEquals(variable.getProperty().getName(), variableDetails.getPropertySummary().getName());
		Assert.assertEquals(variable.getProperty().getDefinition(), variableDetails.getPropertySummary().getDescription());
		Assert.assertEquals(String.valueOf(variable.getScale().getId()), variableDetails.getScale().getId());
		Assert.assertEquals(variable.getScale().getName(), variableDetails.getScale().getName());
		Assert.assertEquals(variable.getScale().getDefinition(), variableDetails.getScale().getDescription());
		Assert.assertEquals(variable.getObservations(), variableDetails.getMetadata().getUsage().getObservations());
		Assert.assertEquals(variable.getStudies(), variableDetails.getMetadata().getUsage().getStudies());
		Assert.assertEquals(variable.getMinValue(), variableDetails.getExpectedRange().getMin());
		Assert.assertEquals(variable.getMaxValue(), variableDetails.getExpectedRange().getMax());
		Assert.assertEquals(variable.getDateCreated(), ISO8601DateParser.parse(variableDetails.getMetadata().getDateCreated()));
		Assert.assertEquals(variable.getDateLastModified(), ISO8601DateParser.parse(variableDetails.getMetadata().getDateLastModified()));
		Assert.assertTrue(variableDetails.getMetadata().getEditableFields().isEmpty());
		Assert.assertFalse(variableDetails.getMetadata().isDeletable());
		Assert.assertTrue(variableDetails.getMetadata().getUsage().getVariables().isEmpty());
	}

	@Test
	public void termRelationShipMapperTest() {

		TermRelationship relationship = new TermRelationship();
		relationship.setId(100);
		relationship.setSubjectTerm(new Term(4040, "Name", "Description"));

		ModelMapper mapper = OntologyMapper.getInstance();

		TermSummary termSummary = mapper.map(relationship, TermSummary.class);

		Assert.assertEquals(termSummary.getId(), String.valueOf(relationship.getSubjectTerm().getId()));
		Assert.assertEquals(termSummary.getName(), relationship.getSubjectTerm().getName());
		Assert.assertEquals(termSummary.getDescription(), relationship.getSubjectTerm().getDefinition());
	}

	@Test
	public void variableTypeMapperTest() {

		VariableType variableType = VariableType.getById(1801);

		ModelMapper mapper = OntologyMapper.getInstance();

		org.ibp.api.domain.ontology.VariableType vType = mapper.map(variableType, org.ibp.api.domain.ontology.VariableType.class);

		Assert.assertEquals(vType.getId(), variableType.getId());
		Assert.assertEquals(vType.getName(), variableType.getName());
		Assert.assertEquals(vType.getDescription(), variableType.getDescription());
	}

	@Test
	public void dataTypeMapperTest() {

		DataType dataType = DataType.NUMERIC_VARIABLE;

		ModelMapper mapper = OntologyMapper.getInstance();

		org.ibp.api.domain.ontology.DataType dType = mapper.map(dataType, org.ibp.api.domain.ontology.DataType.class);

		Assert.assertEquals(StringUtil.parseInt(dType.getId(), null), dataType.getId());
		Assert.assertEquals(dType.getName(), dataType.getName());
	}
}

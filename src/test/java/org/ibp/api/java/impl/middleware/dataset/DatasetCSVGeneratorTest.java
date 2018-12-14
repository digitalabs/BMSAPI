package org.ibp.api.java.impl.middleware.dataset;

import au.com.bytecode.opencsv.CSVWriter;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.ibp.api.rest.dataset.ObservationUnitData;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class DatasetCSVGeneratorTest {

	public static final int RANDOM_STRING_LENGTH = 10;

	private List<ObservationUnitRow> observationUnitRows;
	private final String variableName1 = RandomStringUtils.randomAlphabetic(RANDOM_STRING_LENGTH);
	private final String variableName2 = RandomStringUtils.randomAlphabetic(RANDOM_STRING_LENGTH);
	private final String variableValue1 = RandomStringUtils.randomAlphabetic(RANDOM_STRING_LENGTH);
	private final String variableValue2 = RandomStringUtils.randomAlphabetic(RANDOM_STRING_LENGTH);
	private final List<String> headerNames = Arrays.asList(variableName1, variableName2);

	@InjectMocks
	private DatasetCSVGenerator datasetCSVGenerator;

	@Before
	public void setUp() {

		final ObservationUnitData observationUnitData1 = new ObservationUnitData();
		observationUnitData1.setValue(variableValue1);
		final ObservationUnitData observationUnitData2 = new ObservationUnitData();
		observationUnitData2.setValue(variableValue2);
		final ObservationUnitRow observationUnitRow = new ObservationUnitRow();
		final Map<String, ObservationUnitData> variables = new HashMap<>();
		variables.put(variableName1, observationUnitData1);
		variables.put(variableName2, observationUnitData2);
		observationUnitRow.setVariables(variables);
		this.observationUnitRows = Arrays.asList(observationUnitRow);

	}

	@Test
	public void testGenerateCSVFile() throws IOException {

		final CSVWriter csvWriter = Mockito.mock(CSVWriter.class);
		final String fileNameFullPath = RandomStringUtils.randomAlphabetic(RANDOM_STRING_LENGTH);

		datasetCSVGenerator.generateCSVFile(headerNames, observationUnitRows, fileNameFullPath, csvWriter);

		Mockito.verify(csvWriter).writeAll(Mockito.anyList());
		Mockito.verify(csvWriter).close();

	}

	@Test
	public void testGetColumnValues() {

		final String[] result = datasetCSVGenerator.getColumnValues(observationUnitRows.get(0), headerNames);
		assertEquals(result.length, headerNames.size());
		assertEquals(variableValue1, result[0]);
		assertEquals(variableValue2, result[1]);
	}

	@Test
	public void testGetHeaderNames() {

		final MeasurementVariable measurementVariable1 = new MeasurementVariable();
		measurementVariable1.setName(variableName1);
		final MeasurementVariable measurementVariable2 = new MeasurementVariable();
		measurementVariable2.setName(variableName2);

		final List<MeasurementVariable> measurementVariables = Arrays.asList(measurementVariable1, measurementVariable2);

		final List<String> result = datasetCSVGenerator.getHeaderNames(measurementVariables);
		assertEquals(measurementVariables.size(), result.size());
		assertTrue(result.contains(variableName1));
		assertTrue(result.contains(variableName2));

	}
}
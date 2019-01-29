package org.ibp.api.java.impl.middleware.dataset;

import au.com.bytecode.opencsv.CSVWriter;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Component
public class DatasetCSVGenerator {

	protected File generateCSVFileWithHeaders(
		final List<MeasurementVariable> columns, final String fileNameFullPath, final CSVWriter csvWriter) {
		final File newFile = new File(fileNameFullPath);
		csvWriter.writeNext(this.getHeaderNames(columns).toArray(new String[] {}));
		return newFile;
	}

	protected void writeInstanceObservationUnitRowsToCSVFile(
		final List<MeasurementVariable> columns, final List<ObservationUnitRow> observationUnitRows,
		final CSVWriter csvWriter) {
		// feed in your array (or convert your data to an array)
		final List<String[]> rowValues = new ArrayList<>();
		for (final ObservationUnitRow row : observationUnitRows) {
			rowValues.add(this.getColumnValues(row, columns));
		}
		csvWriter.writeAll(rowValues);
	}

	protected String[] getColumnValues(final ObservationUnitRow row, List<MeasurementVariable> subObservationSetColumns) {
		final List<String> values = new LinkedList<>();
		for (final MeasurementVariable column : subObservationSetColumns) {
			values.add(row.getVariables().get(column.getName()).getValue());
		}
		return values.toArray(new String[] {});
	}

	protected List<String> getHeaderNames(final List<MeasurementVariable> subObservationSetColumns) {
		final List<String> headerNames = new LinkedList<>();
		for (final MeasurementVariable measurementVariable : subObservationSetColumns) {
			headerNames.add(measurementVariable.getAlias());
		}
		return headerNames;
	}

}

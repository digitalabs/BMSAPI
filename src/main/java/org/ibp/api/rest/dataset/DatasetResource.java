package org.ibp.api.rest.dataset;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.domain.dataset.DatasetVariable;
import org.ibp.api.java.dataset.DatasetService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Api(value = "Dataset Services")
@Controller
@RequestMapping("/crops")
public class DatasetResource {

	@Autowired
	private DatasetService studyDatasetService;

	@ApiOperation(value = "Get Dataset Columns", notes = "Retrieves ALL MeasurementVariables (columns) associated to the dataset, "
		+ "that will be shown in the Observation Table")
	@RequestMapping(value = "/{crop}/studies/{studyId}/datasets/{datasetId}/observationUnits/table/columns", method = RequestMethod.GET)
	public ResponseEntity<List<MeasurementVariable>> getSubObservationSetColumns(@PathVariable final String crop,
		@PathVariable final Integer studyId,
		@PathVariable final Integer datasetId) {

		final List<MeasurementVariable> subObservationSetColumns = this.studyDatasetService.getSubObservationSetColumns(studyId, datasetId);

		return new ResponseEntity<>(subObservationSetColumns, HttpStatus.OK);
	}

	@ApiOperation(value = "Count Phenotypes", notes = "Returns count of phenotypes for variables")
	@RequestMapping(value = "/{crop}/studies/{studyId}/datasets/{datasetId}/variables/observations", method = RequestMethod.HEAD)
	public ResponseEntity<String> countPhenotypes(@PathVariable final String crop, @PathVariable final Integer studyId,
		@PathVariable final Integer datasetId, @RequestParam(value = "variableIds", required = true) final Integer[] variableIds) {

		final long count = this.studyDatasetService.countPhenotypes(studyId, datasetId, Arrays.asList(variableIds));
		final HttpHeaders respHeaders = new HttpHeaders();
		respHeaders.add("X-Total-Count", String.valueOf(count));

		return new ResponseEntity<>("", respHeaders, HttpStatus.OK);
	}

	@ApiOperation(value = "Add Dataset Variable", notes = "Add Dataset Variable")
	@RequestMapping(value = "/{crop}/studies/{studyId}/datasets/{datasetId}/variables", method = RequestMethod.PUT)
	@Transactional
	public ResponseEntity<MeasurementVariable> addTrait(@PathVariable final String crop, @PathVariable final Integer studyId,
			@PathVariable final Integer datasetId, @RequestBody final DatasetVariable datasetTrait) {

		final MeasurementVariable variable = this.studyDatasetService.addDatasetVariable(  studyId, datasetId, datasetTrait);
		return new ResponseEntity<>(variable, HttpStatus.OK);
	}
	
	@ApiOperation(value ="Remove dataset variables", notes = "Remove a set of variables from dataset")
	@RequestMapping(value = "/{crop}/studies/{studyId}/datasets/{datasetId}/variables", method = RequestMethod.DELETE)
	@Transactional
	public ResponseEntity<Void> removeVariables(@PathVariable final String crop, @PathVariable final Integer studyId,
			@PathVariable final Integer datasetId,  @RequestParam(value = "variableIds", required = true) final Integer[] variableIds) {
		this.studyDatasetService.removeVariables(studyId, datasetId, Arrays.asList(variableIds));
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Generate and save a sub-observation dataset", notes = "Returns the basic information for the generated dataset")
	@RequestMapping(value = "/{cropName}/studies/{studyId}/datasets/{parentId}/generation", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<DatasetDTO> generateDataset(@PathVariable
	final String cropName, @PathVariable final Integer studyId, @PathVariable final Integer parentId, @RequestBody final DatasetGeneratorInput datasetGeneratorInput) {
		return new ResponseEntity<>(this.studyDatasetService.generateSubObservationDataset(cropName, studyId, parentId, datasetGeneratorInput), HttpStatus.OK);
	}

	@ApiOperation(value = "It will retrieve all the observation units", notes = "It will retrieve all the observation units including observations and props values in a format that will be used by the Observations table.")
	@RequestMapping(value = "/{cropname}/studies/{studyId}/datasets/{datasetId}/instances/{instanceId}/observationUnits/table", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<ObservationUnitTable> getObservationUnitTable(@PathVariable final String cropname,
		@PathVariable final Integer studyId, @PathVariable final Integer datasetId,
		@PathVariable final Integer instanceId,
		@ApiParam(value = BrapiPagedResult.CURRENT_PAGE_DESCRIPTION, required = false) @RequestParam(value = "pageNumber",
			required = false) final Integer pageNumber,
		@ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION, required = false) @RequestParam(value = "pageSize",
			required = false) final Integer pageSize,
		@ApiParam(value = "Sort order. Name of the field to sorty by.", required = false) @RequestParam(value = "sortBy",
			required = false) final String sortBy,
		@ApiParam(value = "Sort order direction. asc/desc.", required = false) @RequestParam(value = "sortOrder",
			required = false) final String sortOrder,
		final HttpServletRequest req) {

		final PagedResult<ObservationUnitRow> pageResult =
			new PaginatedSearch().execute(pageNumber, pageSize, new SearchSpec<ObservationUnitRow>() {

				@Override
				public long getCount() {
					return DatasetResource.this.studyDatasetService.countTotalObservationUnitsForDataset (datasetId, instanceId);
				}

				@Override
				public List<ObservationUnitRow> getResults(final PagedResult<ObservationUnitRow> pagedResult) {
					// BRAPI services have zero-based indexing for pages but paging for Middleware method starts at 1
					final int pageNumber = pagedResult.getPageNumber() + 1;
					return DatasetResource.this.studyDatasetService.getObservationUnitRows(
						studyId,
						datasetId,
						instanceId,
						pagedResult.getPageNumber(),
						pagedResult.getPageSize(),
						pagedResult.getSortBy(),
						pagedResult.getSortOrder());
				}
			});

		final ObservationUnitTable observationUnitTable = new ObservationUnitTable();
		observationUnitTable.setData(pageResult.getPageResults());
		observationUnitTable.setDraw(req.getParameter("draw"));
		observationUnitTable.setRecordsTotal((int) pageResult.getTotalResults());
		observationUnitTable.setRecordsFiltered((int) pageResult.getTotalResults());
		return new ResponseEntity<>(observationUnitTable, HttpStatus.OK);
	}

	@ApiOperation(value = "It will retrieve a list of datasets", notes = "Retrieves the list of datasets for the specified study.")
	@RequestMapping(value = "/{crop}/studies/{studyId}/datasets", method = RequestMethod.GET)
	public ResponseEntity<List<DatasetDTO>> getDatasets(@PathVariable final String crop, @PathVariable final Integer studyId,
		@RequestParam(value = "datasetTypeIds", required = false) final Set<Integer> datasetTypeIds) {
		return new ResponseEntity<>(this.studyDatasetService.getDatasets(studyId, datasetTypeIds), HttpStatus.OK);
	}

	@ApiOperation(value = "It will retrieve a dataset given the id", notes = "Retrieves a dataset given the id")
	@RequestMapping(value = "/{crop}/studies/{studyId}/datasets/{datasetId}", method = RequestMethod.GET)
	public ResponseEntity<DatasetDTO> getDataset(@PathVariable final String crop,
		@PathVariable final Integer studyId,
		@PathVariable final Integer datasetId) {
		return new ResponseEntity<>(this.studyDatasetService.getDataset(crop, studyId, datasetId), HttpStatus.OK);
	}
}

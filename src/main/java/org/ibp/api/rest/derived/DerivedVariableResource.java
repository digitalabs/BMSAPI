package org.ibp.api.rest.derived;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.ibp.api.exception.OverwriteDataException;
import org.ibp.api.java.derived.DerivedVariableService;
import org.ibp.api.java.impl.middleware.derived.DerivedVariableServiceImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Api(value = "Derived Variable Services")
@Controller
@RequestMapping("/crops")
public class DerivedVariableResource {

	@Resource
	private DerivedVariableService derivedVariableService;

	@ApiOperation(value = "Execute Derived Variable", notes = "Execute the formula of a derived variable for each observation of specified instances.")
	@RequestMapping(value = "/{crop}/studies/{studyId}/datasets/{datasetId}/derived-variables/calculation", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Object>> calculate(
		@PathVariable final String crop,
		@PathVariable final Integer studyId,
		@PathVariable final Integer datasetId, @RequestBody final CalculateVariableRequest request) {

		try {
			final Map<String, Object> result =
				this.derivedVariableService
					.execute(studyId, datasetId, request.getVariableId(), request.getGeoLocationIds(), request.isOverwriteExistingData());
			return new ResponseEntity<>(result, HttpStatus.OK);
		} catch (final OverwriteDataException e2) {
			final Map<String, Object> result = new HashMap<>();
			result.put(DerivedVariableServiceImpl.HAS_DATA_OVERWRITE_RESULT_KEY, true);
			return new ResponseEntity<>(result, HttpStatus.OK);
		}

	}

	@ApiOperation(value = "Get Derived Traits Dependencies", notes =
		"Gets the list of all formula dependencies of all derived traits in a dataset."
			+ "This will only return the variables that are not yet added in the dataset.")
	@ResponseBody
	@RequestMapping(value = "/{crop}/studies/{studyId}/datasets/{datasetId}/derived-variables/missing-dependencies", method = RequestMethod.GET)
	public ResponseEntity<Set<String>> dependencyVariablesForAllDerivedTraitsInDataset(
		@PathVariable final String crop,
		@PathVariable final Integer studyId,
		@PathVariable final Integer datasetId) {
		return new ResponseEntity<>(this.derivedVariableService.getDependencyVariables(studyId, datasetId), HttpStatus.OK);
	}

	@ApiOperation(value = "Get Derived Traits Dependencies of a Specific Trait", notes =
		"Gets the list of all formula dependencies of a specific trait in a dataset."
			+ "This will only return the variables that are not yet added in the dataset.")
	@ResponseBody
	@RequestMapping(value = "/{crop}/studies/{studyId}/datasets/{datasetId}/derived-variables/{variableId}/missing-dependencies", method = RequestMethod.GET)
	public ResponseEntity<Set<String>> dependencyVariablesForSpecificDerivedTrait(
		@PathVariable final String crop,
		@PathVariable final Integer studyId,
		@PathVariable final Integer datasetId,
		@PathVariable final Integer variableId) {
		return new ResponseEntity<>(this.derivedVariableService.getDependencyVariables(studyId, datasetId, variableId), HttpStatus.OK);
	}

	@ApiOperation(value = "Count Calculated Traits", notes = "Count the calculated traits (derived traits) in a specified dataset(s)")
	@ResponseBody
	@RequestMapping(value = "/{crop}/studies/{studyId}/datasets/derived-variables", method = RequestMethod.HEAD)
	public ResponseEntity<String> countCalculatedVariables(
		@PathVariable final String crop,
		@PathVariable final Integer studyId, @RequestParam(value = "datasetIds") final Set<Integer> datasetIds) {

		final long count = this.derivedVariableService.countCalculatedVariablesInDatasets(studyId, datasetIds);
		final HttpHeaders respHeaders = new HttpHeaders();
		respHeaders.add("X-Total-Count", String.valueOf(count));

		return new ResponseEntity<>("", respHeaders, HttpStatus.OK);
	}

}
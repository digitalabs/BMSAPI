package org.ibp.api.rest.study;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.ibp.api.domain.study.StudyInstance;
import org.ibp.api.java.study.StudyInstanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

@Api(value = "Study Instance Services")
@Controller
@RequestMapping("/crops")
public class StudyInstanceResource {

	@Resource
	private StudyInstanceService studyInstanceService;

	@ApiOperation(value = "Create new study instance",
		notes = "Create new study instance")
	@RequestMapping(value = "/{cropname}/studies/{studyId}/instances/{instanceNumber}", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity<StudyInstance> createStudyInstance(final @PathVariable String cropname,
		@PathVariable final Integer studyId, @PathVariable final String instanceNumber) {
		return new ResponseEntity<>(this.studyInstanceService.createStudyInstance(cropname, studyId, instanceNumber),
			HttpStatus.OK);

	}

	@ApiOperation(value = "List all study instances with basic metadata.",
		notes = "Returns list of all study instances with basic metadata.")
	@RequestMapping(value = "/{cropname}/studies/{studyId}/instances", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<StudyInstance>> listStudyInstances(final @PathVariable String cropname,
		@PathVariable final Integer studyId) {
		final List<StudyInstance> studyInstances = this.studyInstanceService.getStudyInstances(studyId);

		if (studyInstances.isEmpty()) {
			return new ResponseEntity(HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<>(studyInstances, HttpStatus.OK);
	}

	@ApiOperation(value = "Remove study instance",
		notes = "Remove instance from study")
	@RequestMapping(value = "/{crop}/studies/{studyId}/instances/{instanceNumber}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity removeStudyInstance(final @PathVariable String cropname,
		@PathVariable final Integer studyId, @PathVariable final String instanceNumber) {

		this.studyInstanceService.removeStudyInstance(cropname, studyId, instanceNumber);

		return new ResponseEntity(HttpStatus.OK);

	}

}
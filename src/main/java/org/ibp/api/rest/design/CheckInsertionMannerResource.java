package org.ibp.api.rest.design;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.generationcp.middleware.domain.dms.InsertionMannerItem;
import org.ibp.api.domain.ontology.TermSummary;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Api(value = "Check Insertion Manner Service")
@Controller
@RequestMapping("/crops")
public class CheckInsertionMannerResource {

	@ApiOperation(value = "Gets insertion manners for checks")
	@RequestMapping(value= "/{crop}/check-insertion-manners", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<TermSummary>> retrieveCheckInsertionManners() {
		final List<TermSummary> terms = new ArrayList<>();
		final ModelMapper map = new ModelMapper();
		for (final InsertionMannerItem item : InsertionMannerItem.values()) {
			terms.add(map.map(item, TermSummary.class));
		}
		return new ResponseEntity<>(terms, HttpStatus.OK);
	}

}
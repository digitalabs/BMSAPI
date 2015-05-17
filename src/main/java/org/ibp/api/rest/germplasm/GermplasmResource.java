
package org.ibp.api.rest.germplasm;

import java.util.List;

import org.ibp.api.domain.germplasm.GermplasmSummary;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.germplasm.GermplasmService;
import org.ibp.api.rest.ResourceURLLinkProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Api(value = "Germplasm Services")
@RestController
@RequestMapping(GermplasmResource.URL)
public class GermplasmResource {

	public static final String URL = "/germplasm";

	@Autowired
	private GermplasmService germplasmService;
	
	@Autowired
	private ResourceURLLinkProvider resourceURLLinkProvider;

	@ApiOperation(value = "Search germplasm.", notes = "Search germplasm.")
	@RequestMapping(value = "/{cropname}/search", method = RequestMethod.GET)
	public ResponseEntity<List<GermplasmSummary>> searchGermplasm(@PathVariable String cropname, @RequestParam String q) {
		List<GermplasmSummary> searchResults = germplasmService.searchGermplasm(q);
		for(GermplasmSummary summary : searchResults) {
			populateParentLinkUrls(cropname, summary);
		}
		return new ResponseEntity<List<GermplasmSummary>>(searchResults, HttpStatus.OK);
	}
	
	private void populateParentLinkUrls(String cropname, GermplasmSummary summary) {
		if (summary.getParent1Id() != null && !summary.getParent1Id().equals("Unknown")) {
			summary.setParent1Url(resourceURLLinkProvider.getGermplasmByIDUrl(summary.getParent1Id(), cropname));
		} else {
			summary.setParent1Url("Not applicable as parent 1 is is unknown.");
		}

		if (summary.getParent2Id() != null && !summary.getParent2Id().equals("Unknown")) {
			summary.setParent2Url(resourceURLLinkProvider.getGermplasmByIDUrl(summary.getParent2Id(), cropname));
		} else {
			summary.setParent2Url("Not applicable as parent 2 is is unknown.");
		}
	}

	@ApiOperation(value = "Get germplasm by germplasm id.", notes = "Get germplasm by germplasm id.")
	@RequestMapping(value = "/{cropname}/{germplasmId}", method = RequestMethod.GET)
	public ResponseEntity<GermplasmSummary> getGermplasm(@PathVariable String cropname, @PathVariable String germplasmId) {
		GermplasmSummary summary = germplasmService.getGermplasm(germplasmId);
		if (summary != null) {
			populateParentLinkUrls(cropname, summary);
			return new ResponseEntity<GermplasmSummary>(summary, HttpStatus.OK);
		} else {
			throw new ApiRuntimeException("No germplasm record found for the supplied identifier: " + germplasmId);
		}
	}
}
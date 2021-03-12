package org.ibp.api.rest.germplasm;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.domain.germplasm.GermplasmNameRequestDto;
import org.ibp.api.brapi.v1.common.SingleEntityResponse;
import org.ibp.api.java.impl.middleware.germplasm.GermplasmNameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Api(value = "Germplasm name Services")
@Controller
public class GermplasmNameResource {

	@Autowired
	private GermplasmNameService germplasmNameService;

	@ApiOperation(value = "Create Germplasm name")
	@PreAuthorize("hasAnyAuthority('ADMIN')")
	@RequestMapping(value = "/crops/{cropName}/germplasm/{gid}/names", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Integer> CreateGermplasmName(@PathVariable final String cropName,
		@PathVariable final Integer gid, @RequestBody final GermplasmNameRequestDto germplasmNameRequestDto) {
		germplasmNameRequestDto.setGid(gid);
		return new ResponseEntity<>(this.germplasmNameService.createName(germplasmNameRequestDto), HttpStatus.OK);
	}

	@ApiOperation(value = "Update Germplasm name")
	@PreAuthorize("hasAnyAuthority('ADMIN')")
	@RequestMapping(value = "/crops/{cropName}/germplasm/{gid}/names/{nameId}", method = RequestMethod.PATCH)
	@ResponseBody
	public ResponseEntity<Void> updateGermplasmName(@PathVariable final String cropName,
		@PathVariable final Integer gid, @PathVariable final Integer nameId, @RequestBody final GermplasmNameRequestDto germplasmNameRequestDto) {
		germplasmNameRequestDto.setId(nameId);
		germplasmNameRequestDto.setGid(gid);
		this.germplasmNameService.updateName(germplasmNameRequestDto);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ApiOperation(value = "Delete Germplasm name")
	@PreAuthorize("hasAnyAuthority('ADMIN')")
	@RequestMapping(value = "/crops/{cropName}/germplasm/{gid}/names/{nameId}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity deleteGermplasmName(@PathVariable final String cropName,
		@PathVariable final Integer gid, @PathVariable final Integer nameId) {

		final GermplasmNameRequestDto germplasmNameRequestDto = new GermplasmNameRequestDto(nameId, gid);
		this.germplasmNameService.deleteName(germplasmNameRequestDto);

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
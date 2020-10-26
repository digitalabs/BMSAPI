package org.ibp.api.rest.germplasmlist;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.generationcp.commons.pojo.treeview.TreeNode;
import org.generationcp.middleware.api.germplasmlist.GermplasmListGeneratorDTO;
import org.generationcp.middleware.domain.germplasm.GermplasmListTypeDTO;
import org.generationcp.middleware.pojos.workbench.PermissionsEnum;
import org.ibp.api.java.germplasm.GermplamListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Api(value = "Germplasm List Services")
@Controller
public class GermplasmListResource {

	@Autowired
	public GermplamListService germplamListService;

	@ApiOperation(value = "Get germplasm lists given a tree parent node folder", notes = "Get germplasm lists given a tree parent node folder")
	@PreAuthorize("hasAnyAuthority('ADMIN', 'CROP_MANAGEMENT', 'GERMPLASM', 'MANAGE_GERMPLASM', 'SEARCH_GERMPLASM')" + PermissionsEnum.HAS_INVENTORY_VIEW)
	@RequestMapping(value = "/crops/{crop}/germplasm-lists/tree", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<TreeNode>> getGermplasmListByParentFolderId(
		@ApiParam(value = "The crop type", required = true) @PathVariable final String crop,
		@ApiParam("The program UUID") @RequestParam(required = false) final String programUUID,
		@ApiParam(value = "The id of the parent folder") @RequestParam(required = false) final String parentFolderId,
		@ApiParam(value = "Only folders") @RequestParam(required = true) final Boolean onlyFolders) {
		final List<TreeNode> children = this.germplamListService.getGermplasmListChildrenNodes(crop, programUUID, parentFolderId, onlyFolders);
		return new ResponseEntity<>(children, HttpStatus.OK);
	}

	@ApiOperation(value = "Create a new Germplasm list")
	@RequestMapping(value = "/crops/{crop}/germplasm-lists", method = RequestMethod.POST)
	@PreAuthorize("hasAnyAuthority('ADMIN', 'CROP_MANAGEMENT', 'GERMPLASM', 'MANAGE_GERMPLASM')")
	@ResponseBody
	public ResponseEntity<GermplasmListGeneratorDTO> create(
		@ApiParam(required = true) @PathVariable final String crop,
		@RequestParam(required = false) final String programUUID,
		@RequestBody final GermplasmListGeneratorDTO request
	) {
		return new ResponseEntity<>(this.germplamListService.create(request), HttpStatus.CREATED);
	}


	@ApiOperation(value = "Get germplasm lists types", notes = "Get germplasm lists types")
	@RequestMapping(value = "/crops/{crop}/germplasm-list-types", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<GermplasmListTypeDTO>> getGermplasmListTypes(
		@ApiParam(value = "The crop type", required = true) @PathVariable final String crop,
		@ApiParam("The program UUID") @RequestParam(required = false) final String programUUID) {
		final List<GermplasmListTypeDTO> germplasmListTypes = this.germplamListService.getGermplasmListTypes();
		return new ResponseEntity<>(germplasmListTypes, HttpStatus.OK);
	}


}

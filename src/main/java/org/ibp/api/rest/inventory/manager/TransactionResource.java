package org.ibp.api.rest.inventory.manager;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionsSearchDto;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.ibp.api.brapi.v1.common.SingleEntityResponse;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.domain.search.SearchDto;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.inventory.manager.TransactionService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@Api(value = "Transaction Services")
@RestController
@PreAuthorize("hasAnyAuthority('ADMIN','CROP_MANAGEMENT')")
public class TransactionResource {

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private SearchRequestService searchRequestService;

	@Autowired
	private SecurityService securityService;

	@ApiOperation(value = "Post transaction search", notes = "Post transaction search")
	@RequestMapping(value = "/crops/{cropName}/transactions/search", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<SingleEntityResponse<SearchDto>> postSearchTransactions(
		@PathVariable final String cropName, @RequestBody final TransactionsSearchDto transactionsSearchDto) {
		final String searchRequestId =
			this.searchRequestService.saveSearchRequest(transactionsSearchDto, TransactionsSearchDto.class).toString();

		final SearchDto searchDto = new SearchDto(searchRequestId);
		final SingleEntityResponse<SearchDto> singleGermplasmResponse = new SingleEntityResponse<SearchDto>(searchDto);

		return new ResponseEntity<>(singleGermplasmResponse, HttpStatus.OK);

	}

	@ApiOperation(value = "It will retrieve transactions that matches search conditions", notes = "It will retrieve transactions that "
		+ "matches search conditions")
	@RequestMapping(value = "/crops/{cropName}/transactions/search", method = RequestMethod.GET)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
			value = "Results page you want to retrieve (0..N)"),
		@ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
			value = "Number of records per page."),
		@ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
			value = "Sorting criteria in the format: property(,asc|desc). " +
				"Default sort order is ascending. " +
				"Multiple sort criteria are supported.")
	})
	@ResponseBody
	public ResponseEntity<List<TransactionDto>> getTransactions(
		@PathVariable final String cropName, //
		@RequestParam final Integer searchRequestId, @ApiIgnore
	final Pageable pageable) {

		final TransactionsSearchDto searchDTO = (TransactionsSearchDto) this.searchRequestService
			.getSearchRequest(searchRequestId, TransactionsSearchDto.class);

		final PagedResult<TransactionDto> resultPage =
			new PaginatedSearch().executeBrapiSearch(pageable.getPageNumber(), pageable.getPageSize(), new SearchSpec<TransactionDto>() {

				@Override
				public long getCount() {
					return TransactionResource.this.transactionService.countSearchTransactions(searchDTO);
				}

				@Override
				public List<TransactionDto> getResults(final PagedResult<TransactionDto> pagedResult) {
					return TransactionResource.this.transactionService.searchTransactions(searchDTO, pageable);
				}
			});

		final List<TransactionDto> transactionDtos = resultPage.getPageResults();

		final HttpHeaders headers = new HttpHeaders();
		headers.add("X-Total-Count", Long.toString(resultPage.getTotalResults()));

		return new ResponseEntity<>(transactionDtos, headers, HttpStatus.OK);

	}

	@ApiOperation(value = "Create Transaction", notes = "Create a new transaction")
	@RequestMapping(value = "/crops/{cropName}/lots/{lotId}/transactions", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Integer> createTransaction(
		@PathVariable final String cropName,
		@PathVariable final String lotId,
		@ApiParam("Transaction to be created")
		@RequestBody final TransactionDto transactionDto) {

		final WorkbenchUser user = this.securityService.getCurrentlyLoggedInUser();
		transactionDto.setCreatedByUsername(user.getUserid().toString());
		if (transactionDto.getLot() == null) {
			transactionDto.setLot(new ExtendedLotDto());
		}
		transactionDto.getLot().setLotId(Integer.valueOf(lotId));
		return new ResponseEntity<>(this.transactionService.saveTransaction(transactionDto), HttpStatus.CREATED);
	}
}
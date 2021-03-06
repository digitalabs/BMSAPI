package org.ibp.api.brapi.v2.germplasm;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.brapi.v1.germplasm.GermplasmDTO;
import org.generationcp.middleware.api.brapi.v2.germplasm.GermplasmImportRequest;
import org.generationcp.middleware.domain.search_request.brapi.v1.GermplasmSearchRequestDto;
import org.hamcrest.Matchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.java.germplasm.GermplasmService;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.validation.ObjectError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang.math.RandomUtils.nextInt;
import static org.mockito.Mockito.doReturn;

public class GermplasmResourceBrapiTest extends ApiUnitTestBase {

	private static final String  NAMETYPE = "ACCNO";
	private static final String  ATTRIBUTETYPE = "PLOTCODE";

	@Autowired
	private GermplasmService germplasmService;

	@Test
	public void testGetGermplasm() throws Exception {
		final int gid = nextInt();
		final String germplasmDbId = String.valueOf(gid);
		final GermplasmSearchRequestDto germplasmSearchRequestDTO = new GermplasmSearchRequestDto();
		germplasmSearchRequestDTO.setGermplasmDbIds(Lists.newArrayList(germplasmDbId));

		final List<GermplasmDTO> list = this.getTestGermplasmDTOList(germplasmDbId);
		doReturn(list).when(this.germplasmService)
			.searchGermplasmDTO(Mockito.any(GermplasmSearchRequestDto.class), Mockito
				.eq(new PageRequest(BrapiPagedResult.DEFAULT_PAGE_NUMBER, BrapiPagedResult.DEFAULT_PAGE_SIZE)));

		final GermplasmDTO germplasmDTO = list.get(0);
		this.mockMvc.perform(MockMvcRequestBuilders.get("/maize/brapi/v2/germplasm")
			.contentType(this.contentType))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data", IsCollectionWithSize.hasSize(list.size())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].germplasmDbId",
				Matchers.is(germplasmDbId)))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].accessionNumber",
				Matchers.is(germplasmDTO.getAccessionNumber())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].breedingMethodDbId",
				Matchers.is(germplasmDTO.getBreedingMethodDbId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].countryOfOriginCode",
				Matchers.is(germplasmDTO.getCountryOfOriginCode())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].commonCropName",
				Matchers.is(germplasmDTO.getCommonCropName())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].defaultDisplayName",
				Matchers.is(germplasmDTO.getDefaultDisplayName())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].genus",
				Matchers.is(germplasmDTO.getGenus())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].germplasmName",
				Matchers.is(germplasmDTO.getGermplasmName())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].germplasmOrigin",
				Matchers.is(germplasmDTO.getGermplasmOrigin())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].countryOfOriginCode",
				Matchers.is(germplasmDTO.getCountryOfOriginCode())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].additionalInfo",
				Matchers.hasKey(ATTRIBUTETYPE)))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].additionalInfo",
				Matchers.hasValue(germplasmDTO.getAdditionalInfo().get(ATTRIBUTETYPE))))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].synonyms",
				Matchers.hasKey(NAMETYPE)))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].synonyms",
				Matchers.hasValue(germplasmDTO.getSynonyms().get(NAMETYPE))));

	}


	@Test
	public void testCreateGermplasm_AllCreated() throws Exception {
		final int gid = nextInt();
		final String germplasmDbId = String.valueOf(gid);
		final List<GermplasmDTO> list = this.getTestGermplasmDTOList(germplasmDbId);
		final GermplasmImportRequest importRequest = new GermplasmImportRequest();
		importRequest.setBreedingMethodDbId("13");
		importRequest.setDefaultDisplayName("CB2");
		importRequest.setSeedSource("BC07A-412-201");
		final List<GermplasmImportRequest> requestList = Lists.newArrayList(importRequest);
		final String cropName = "maize";
		final GermplasmImportResponse response = new GermplasmImportResponse();
		response.setGermplasmList(list);
		response.setStatus(RandomStringUtils.randomAlphabetic(30));
		doReturn(response).when(this.germplasmService).createGermplasm(cropName, requestList);


		final GermplasmDTO germplasmDTO = list.get(0);
		this.mockMvc.perform(MockMvcRequestBuilders.post("/{crop}/brapi/v2/germplasm", cropName)
			.content(this.convertObjectToByte(requestList)).contentType(this.contentType))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.status", IsCollectionWithSize.hasSize(1)))
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.status[0]", Matchers.hasKey("INFO")))
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.status[0]", Matchers.hasValue(response.getStatus())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data", IsCollectionWithSize.hasSize(list.size())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].germplasmDbId",
				Matchers.is(germplasmDbId)))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].accessionNumber",
				Matchers.is(germplasmDTO.getAccessionNumber())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].breedingMethodDbId",
				Matchers.is(germplasmDTO.getBreedingMethodDbId())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].countryOfOriginCode",
				Matchers.is(germplasmDTO.getCountryOfOriginCode())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].commonCropName",
				Matchers.is(germplasmDTO.getCommonCropName())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].defaultDisplayName",
				Matchers.is(germplasmDTO.getDefaultDisplayName())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].genus",
				Matchers.is(germplasmDTO.getGenus())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].germplasmName",
				Matchers.is(germplasmDTO.getGermplasmName())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].germplasmOrigin",
				Matchers.is(germplasmDTO.getGermplasmOrigin())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].countryOfOriginCode",
				Matchers.is(germplasmDTO.getCountryOfOriginCode())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].additionalInfo",
				Matchers.hasKey(ATTRIBUTETYPE)))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].additionalInfo",
				Matchers.hasValue(germplasmDTO.getAdditionalInfo().get(ATTRIBUTETYPE))))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].synonyms",
				Matchers.hasKey(NAMETYPE)))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data[0].synonyms",
				Matchers.hasValue(germplasmDTO.getSynonyms().get(NAMETYPE))));
	}

	@Test
	public void testCreateGermplasm_InvalidGermplasm() throws Exception {
		final int gid = nextInt();
		final String germplasmDbId = String.valueOf(gid);
		final GermplasmImportRequest importRequest = new GermplasmImportRequest();
		importRequest.setBreedingMethodDbId("13");
		importRequest.setSeedSource("BC07A-412-201");
		final List<GermplasmImportRequest> requestList = Lists.newArrayList(importRequest);
		final String cropName = "maize";
		final GermplasmImportResponse response = new GermplasmImportResponse();
		response.setStatus(RandomStringUtils.randomAlphabetic(30));
		final ObjectError error = new ObjectError("defaultDisplayName",  new String[] {"germplasm.create.null.name.types"}, new String[] {"1"}, "");
		response.setErrors(Lists.newArrayList(error));
		doReturn(response).when(this.germplasmService).createGermplasm(cropName, requestList);


		this.mockMvc.perform(MockMvcRequestBuilders.post("/{crop}/brapi/v2/germplasm", cropName)
			.content(this.convertObjectToByte(requestList)).contentType(this.contentType))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.status", IsCollectionWithSize.hasSize(1)))
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.status[0]", Matchers.hasKey("INFO")))
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.status[0]", Matchers.hasValue(response.getStatus())))
			.andExpect(MockMvcResultMatchers.jsonPath("$.metadata.status[0]", Matchers.hasKey("ERROR1")))
			.andExpect(MockMvcResultMatchers.jsonPath("$.result.data", IsCollectionWithSize.hasSize(0)));

	}



	private List<GermplasmDTO> getTestGermplasmDTOList(final String germplasmDbId) {
		final GermplasmDTO germplasmDTO = new GermplasmDTO();
		germplasmDTO.setGermplasmDbId(germplasmDbId);
		germplasmDTO.setAccessionNumber(RandomStringUtils.randomAlphabetic(20));
		germplasmDTO.setBreedingMethodDbId(RandomStringUtils.randomAlphabetic(20));
		germplasmDTO.setCommonCropName(RandomStringUtils.randomAlphabetic(20));
		germplasmDTO.setCountryOfOriginCode(RandomStringUtils.randomAlphabetic(20));
		germplasmDTO.setDefaultDisplayName(RandomStringUtils.randomAlphabetic(20));
		germplasmDTO.setGenus(RandomStringUtils.randomAlphabetic(20));
		germplasmDTO.setGermplasmName(RandomStringUtils.randomAlphabetic(20));
		germplasmDTO.setGermplasmOrigin(RandomStringUtils.randomAlphabetic(20));
		germplasmDTO.setSeedSource(RandomStringUtils.randomAlphabetic(20));
		germplasmDTO.setSynonyms(Collections.singletonMap(NAMETYPE, RandomStringUtils.randomAlphabetic(20)));
		germplasmDTO.setAdditionalInfo(Collections.singletonMap(ATTRIBUTETYPE, RandomStringUtils.randomAlphabetic(20)));
		return Lists.newArrayList(germplasmDTO);
	}

}

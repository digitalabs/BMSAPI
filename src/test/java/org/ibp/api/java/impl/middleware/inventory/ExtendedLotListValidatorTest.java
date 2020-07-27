package org.ibp.api.java.impl.middleware.inventory;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.pojos.ims.LotStatus;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.ExtendedLotListValidator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ExtendedLotListValidatorTest {

	@InjectMocks
	private ExtendedLotListValidator extendedLotListValidator;

	@Test
	public void testValidateListIsEmptyOrNull() {
		try {
			this.extendedLotListValidator.validateEmptyList(null);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("no.lots.selected"));
		}
	}

	@Test
	public void validateEmptyUnitsToDeposit() {
		try {
			final ExtendedLotDto extendedLotDto = new ExtendedLotDto();
			final List<ExtendedLotDto> lotDtoList = Arrays.asList(extendedLotDto);
			this.extendedLotListValidator.validateEmptyUnitsToDeposit(lotDtoList);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("selected.lots.with.no.unit"));
		}
	}

	@Test
	public void validateEmptyUnitsToWithdrawal() {
		try {
			final ExtendedLotDto extendedLotDto = new ExtendedLotDto();
			final List<ExtendedLotDto> lotDtoList = Arrays.asList(extendedLotDto);
			this.extendedLotListValidator.validateEmptyUnitsToWithdrawal(lotDtoList);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("selected.lots.with.no.unit"));
		}
	}

	@Test
	public void testValidateClosedLots() {
		try {
			final ExtendedLotDto extendedLotDto = new ExtendedLotDto();
			extendedLotDto.setStatus(LotStatus.CLOSED.name());
			final List<ExtendedLotDto> lotDtoList = Arrays.asList(extendedLotDto);
			this.extendedLotListValidator.validateClosedLots(lotDtoList);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("selected.lots.closed"));
		}
	}

	@Test
	public void testValidateAllProvidedLotIdsExist() {
		try {
			final ExtendedLotDto extendedLotDto = new ExtendedLotDto();
			extendedLotDto.setLotId(1);
			final List<ExtendedLotDto> lotDtoList = Arrays.asList(extendedLotDto);
			final Set<String> lotUUIDs =
				new HashSet<>(Arrays.asList(RandomStringUtils.randomAlphabetic(38), RandomStringUtils.randomAlphabetic(38)));
			this.extendedLotListValidator.validateAllProvidedLotUUIDsExist(lotDtoList, lotUUIDs);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("lots.does.not.exist"));
		}
	}

}

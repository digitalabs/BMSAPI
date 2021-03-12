package org.ibp.api.java.impl.middleware.germplasm;

import org.generationcp.middleware.domain.germplasm.GermplasmAttributeDto;
import org.generationcp.middleware.domain.germplasm.GermplasmAttributeRequestDto;
import org.ibp.api.java.germplasm.GermplasmAttributeService;
import org.ibp.api.java.impl.middleware.common.validator.AttributeValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.ibp.api.java.impl.middleware.common.validator.LocationValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Service
@Transactional
public class GermplasmAttributeServiceImpl implements GermplasmAttributeService {

	@Autowired
	private org.generationcp.middleware.api.germplasm.GermplasmAttributeService germplasmAttributeService;

	@Autowired
	private AttributeValidator attributeValidator;

	@Autowired
	private GermplasmValidator germplasmValidator;

	@Autowired
	private LocationValidator locationValidator;

	@Override
	public List<GermplasmAttributeDto> getGermplasmAttributeDtos(final Integer gid, final String attributeType) {
		BindingResult errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.germplasmValidator.validateGids(errors, Collections.singletonList(gid));
		this.attributeValidator.validateAttributeType(errors, attributeType);
		return this.germplasmAttributeService.getGermplasmAttributeDtos(gid, attributeType);
	}

	@Override
	public GermplasmAttributeRequestDto createGermplasmAttribute(final Integer gid, final GermplasmAttributeRequestDto dto) {
		BindingResult errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.attributeValidator.validateAttribute(errors, gid, dto);
		this.locationValidator.validateLocation(errors, dto.getLocationId());
		return  dto;
	}
}

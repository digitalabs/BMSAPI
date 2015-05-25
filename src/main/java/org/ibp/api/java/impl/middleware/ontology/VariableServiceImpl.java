package org.ibp.api.java.impl.middleware.ontology;

import com.google.common.base.Strings;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.OntologyVariableInfo;
import org.generationcp.middleware.domain.oms.OntologyVariableSummary;
import org.generationcp.middleware.domain.oms.VariableType;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.domain.ontology.VariableSummary;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.impl.middleware.ServiceBaseImpl;
import org.ibp.api.java.impl.middleware.common.CommonUtil;
import org.ibp.api.java.impl.middleware.common.validator.ProgramValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.VariableValidator;
import org.ibp.api.java.ontology.VariableService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Validate data of API Services and pass data to middleware services
 */

@Service
public class VariableServiceImpl extends ServiceBaseImpl implements VariableService{

	@Autowired
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Autowired
	private VariableValidator variableValidator;

	@Autowired
	private ProgramValidator programValidator;

	@Override
	public List<VariableSummary> getAllVariablesByFilter(String programId, String propertyId, Boolean favourite) {

		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Variable");
		this.programValidator.validate(programId, bindingResult);

		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}

		if (!Strings.isNullOrEmpty(propertyId)) {
			validateId(propertyId, "Variable");
		}

		try {
			List<OntologyVariableSummary> variableSummaries = this.ontologyVariableDataManager.getWithFilter(programId, favourite, null, CommonUtil.tryParseSafe(propertyId), null);
			List<VariableSummary> variableSummaryList = new ArrayList<>();

			ModelMapper mapper = OntologyMapper.getInstance();

			for (OntologyVariableSummary variable : variableSummaries) {
				VariableSummary variableSummary = mapper.map(variable, VariableSummary.class);
				variableSummaryList.add(variableSummary);
			}
			return variableSummaryList;
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

    @Override
    public VariableDetails getVariableById(String programId, String variableId) {

		validateId(variableId, "Variable");
		BindingResult errors = new MapBindingResult(new HashMap<String, String>(), "Variable");
		TermRequest term = new TermRequest(variableId, "variable", CvId.VARIABLES.getId());
		this.termValidator.validate(term, errors);
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

        try {
			Integer id = CommonUtil.tryParseSafe(variableId);

			Variable ontologyVariable = this.ontologyVariableDataManager.getVariable(programId, id);

			if (ontologyVariable == null) {
			    return null;
			}

			boolean deletable = true;
			if (this.termDataManager.isTermReferred(id)) {
			    deletable = false;
			}

			ModelMapper mapper = OntologyMapper.getInstance();
			VariableDetails response = mapper.map(ontologyVariable, VariableDetails.class);

			if (!deletable) {
			    response.getMetadata().setEditableFields(new ArrayList<>(Collections.singletonList("description")));
			} else {
			    response.getMetadata().setEditableFields(new ArrayList<>(Arrays.asList("name", "description", "alias",
						"cropOntologyId", "variableTypeIds", "propertySummary", "methodSummary", "scale", "expectedRange")));
			}
			response.getMetadata().setDeletable(deletable);
			return response;
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error!", e);
		}
    }

    @Override
    public GenericResponse addVariable(String programId, VariableSummary variable) {

		variable.setId(null);
		variable.setProgramUuid(programId);

		BindingResult errors = new MapBindingResult(new HashMap<String, String>(), "Variable");
		this.variableValidator.validate(variable, errors);
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

        try {
			Integer methodId = CommonUtil.tryParseSafe(variable.getMethodSummary().getId());
			Integer propertyId = CommonUtil.tryParseSafe(variable.getPropertySummary().getId());
			Integer scaleId = CommonUtil.tryParseSafe(variable.getScaleSummary().getId());

			OntologyVariableInfo variableInfo = new OntologyVariableInfo();
			variableInfo.setName(variable.getName());
			variableInfo.setDescription(variable.getDescription());
			variableInfo.setMethodId(methodId);
			variableInfo.setPropertyId(propertyId);
			variableInfo.setScaleId(scaleId);

			if (!Strings.isNullOrEmpty(variable.getExpectedRange().getMin()) && !Strings.isNullOrEmpty(variable.getExpectedRange().getMax())) {
			    variableInfo.setMinValue(variable.getExpectedRange().getMin());
			    variableInfo.setMaxValue(variable.getExpectedRange().getMax());
			}

			for (String i : variable.getVariableTypeIds()) {
			    variableInfo.addVariableType(VariableType.getById(CommonUtil.tryParseSafe(i)));
			}

			this.ontologyVariableDataManager.addVariable(variableInfo);
			return new GenericResponse(variableInfo.getId());
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error!", e);
		}
    }

    @Override
    public void updateVariable(String programId, String variableId, VariableSummary variable) {

		validateId(variableId, "Variable");
		BindingResult errors = new MapBindingResult(new HashMap<String, String>(), "Variable");
		TermRequest term = new TermRequest(variableId, "variable", CvId.VARIABLES.getId());
		this.termValidator.validate(term, errors);
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		variable.setId(variableId);
		variable.setProgramUuid(programId);

		this.variableValidator.validate(variable, errors);
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

        try {
			Integer id = CommonUtil.tryParseSafe(variable.getId());

			Integer methodId = CommonUtil.tryParseSafe(variable.getMethodSummary().getId());
			Integer propertyId = CommonUtil.tryParseSafe(variable.getPropertySummary().getId());
			Integer scaleId = CommonUtil.tryParseSafe(variable.getScaleSummary().getId());

			OntologyVariableInfo variableInfo = new OntologyVariableInfo();
			variableInfo.setId(id);
			variableInfo.setProgramUuid(variable.getProgramUuid());
			variableInfo.setName(variable.getName());
			variableInfo.setAlias(variable.getAlias());
			variableInfo.setDescription(variable.getDescription());
			variableInfo.setMethodId(methodId);
			variableInfo.setPropertyId(propertyId);
			variableInfo.setScaleId(scaleId);
			variableInfo.setIsFavorite(variable.isFavourite());

			if (!Strings.isNullOrEmpty(variable.getExpectedRange().getMin()) && !Strings.isNullOrEmpty(variable.getExpectedRange().getMax())) {
			    variableInfo.setMinValue(variable.getExpectedRange().getMin());
			    variableInfo.setMaxValue(variable.getExpectedRange().getMax());
			}

			for (String i : variable.getVariableTypeIds()) {
			    variableInfo.addVariableType(VariableType.getById(CommonUtil.tryParseSafe(i)));
			}

			this.ontologyVariableDataManager.updateVariable(variableInfo);
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error!", e);
		}
    }

	@Override
	public void deleteVariable(String id) {

		// Note: Validate Id for valid format and check if variable exists or not
		validateId(id, "Variable");
		BindingResult errors = new MapBindingResult(new HashMap<String, String>(), "Variable");

		// Note: Check if variable is deletable or not by checking its usage in variable
		this.termDeletableValidator.validate(new TermRequest(String.valueOf(id), "Variable", CvId.VARIABLES.getId()), errors);
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		try{
			ontologyVariableDataManager.deleteVariable(CommonUtil.tryParseSafe(id));
		}catch (MiddlewareException e){
			throw new ApiRuntimeException("Error!", e);
		}
	}

}
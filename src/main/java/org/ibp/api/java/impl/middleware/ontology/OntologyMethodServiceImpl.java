package org.ibp.api.java.impl.middleware.ontology;

import org.generationcp.middleware.domain.oms.Method;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.service.api.OntologyManagerService;
import org.ibp.api.CommonUtil;
import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.domain.ontology.MethodRequest;
import org.ibp.api.domain.ontology.MethodResponse;
import org.ibp.api.domain.ontology.MethodSummary;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.ontology.OntologyMethodService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class OntologyMethodServiceImpl implements OntologyMethodService{

	@Autowired
	private OntologyManagerService ontologyManagerService;

  @Override
	public List<MethodSummary> getAllMethods() {
		try {
			List<Method> methodList = this.ontologyManagerService.getAllMethods();
			List<MethodSummary> methods = new ArrayList<>();

			ModelMapper mapper = OntologyMapper.methodMapper();

			for (Method method : methodList) {
			  	MethodSummary methodSummary = mapper.map(method, MethodSummary.class);
			  	methods.add(methodSummary);
			}
			return methods;
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

	@Override
	public MethodResponse getMethod(Integer id) {
		try {
			Method method = this.ontologyManagerService.getMethod(id);
			if (method == null) {
			  	return null;
			}
			boolean deletable = true;
			if (this.ontologyManagerService.isTermReferred(id)) {
			  	deletable = false;
			}
			ModelMapper mapper = OntologyMapper.methodMapper();
			MethodResponse response = mapper.map(method, MethodResponse.class);
			String FIELD_TO_BE_EDITABLE_IF_TERM_REFERRED = "description";
			if (!deletable) {
			  response.setEditableFields(new ArrayList<>(Collections
					  .singletonList(FIELD_TO_BE_EDITABLE_IF_TERM_REFERRED)));
			} else {
			  	response.setEditableFields(new ArrayList<>(Arrays.asList("name", FIELD_TO_BE_EDITABLE_IF_TERM_REFERRED)));
			}
			response.setDeletable(deletable);
			return response;
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

	@Override
	public GenericResponse addMethod(MethodRequest request) {
		try {
			Method method = new Method();
			method.setName(request.getName());
			method.setDefinition(request.getDescription());
			this.ontologyManagerService.addMethod(method);
			return new GenericResponse(method.getId());
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

	@Override
	public void updateMethod(Integer id, MethodRequest request) {
		try {
			Method method = new Method();
			method.setId(CommonUtil.tryParseSafe(request.getId()));
			method.setName(request.getName());
			method.setDefinition(request.getDescription());
			this.ontologyManagerService.updateMethod(method);
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

	@Override
	public void deleteMethod(Integer id) {
	  	try {
			this.ontologyManagerService.deleteMethod(id);
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}
}

package org.ibp.api.java.ontology;

import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.ibp.api.domain.ontology.*;

import java.util.List;

public interface OntologyModelService {

	/**
	 * get all data types
	 *
	 * @return list of data types
	 * @throws MiddlewareQueryException
	 */
	List<IdName> getAllDataTypes() throws MiddlewareQueryException;

	/**
	 * get all classes
	 *
	 * @return list of classes
	 * @throws MiddlewareQueryException
	 */
	List<String> getAllClasses() throws MiddlewareQueryException;

	/**
	 * get all scales with details
	 *
	 * @return list of scales
	 * @throws MiddlewareQueryException
	 */
	List<ScaleSummary> getAllScales() throws MiddlewareQueryException;

	/**
	 * get scale using given id
	 *
	 * @param id
	 *            scale id
	 * @return scale that matches id
	 * @throws MiddlewareQueryException
	 */
	ScaleResponse getScaleById(Integer id) throws MiddlewareQueryException;

	/**
	 * Adding new scale
	 *
	 * @param request
	 *            ScaleRequest
	 * @throws MiddlewareQueryException
	 * @throws MiddlewareException
	 */
	GenericResponse addScale(ScaleRequest request) throws MiddlewareQueryException,
	MiddlewareException;

	/**
	 * update scale with new request data
	 *
	 * @param request
	 *            ScaleRequest instance that have new data
	 * @throws MiddlewareQueryException
	 * @throws MiddlewareException
	 */
	void updateScale(ScaleRequest request) throws MiddlewareQueryException, MiddlewareException;

	/**
	 * Delete a scale using given id
	 *
	 * @param id
	 *            scale to be deleted
	 * @throws MiddlewareQueryException
	 * @throws MiddlewareException
	 */
	void deleteScale(Integer id) throws MiddlewareQueryException, MiddlewareException;

	/**
	 * Get List of all variable types
	 *
	 * @return List of Variable Types
	 */
	List<VariableTypeResponse> getAllVariableTypes();

	/**
	 * Get List of variables
	 *
	 * @param programId
	 *            id of program
	 * @param propertyId
	 *            id of property
	 * @param favourite
	 *            favourite variable
	 * @return list of variables
	 * @throws MiddlewareQueryException
	 */
	List<VariableSummary> getAllVariablesByFilter(Integer programId, Integer propertyId,
			Boolean favourite) throws MiddlewareQueryException;

	/**
	 * Get variable using given id
	 *
	 * @param programId
	 *            id of program
	 * @param variableId
	 *            id of the variable
	 * @return variable that matches id
	 * @throws MiddlewareQueryException
	 * @throws MiddlewareException
	 */
	VariableResponse getVariableById(Integer programId, Integer variableId)
			throws MiddlewareQueryException, MiddlewareException;

	/**
	 * Add variable using given data
	 *
	 * @param request
	 *            data to be added
	 * @return newly created variable id
	 * @throws MiddlewareQueryException
	 * @throws MiddlewareException
	 */
	GenericResponse addVariable(VariableRequest request) throws MiddlewareQueryException,
	MiddlewareException;
}

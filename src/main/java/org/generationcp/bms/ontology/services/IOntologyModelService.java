package org.generationcp.bms.ontology.services;


import org.generationcp.bms.ontology.dto.incoming.AddMethodRequest;
import org.generationcp.bms.ontology.dto.outgoing.*;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;

import java.util.List;

public interface IOntologyModelService {

    public List<MethodSummary> getAllMethods() throws MiddlewareQueryException;
    public MethodResponse getMethod(Integer id) throws MiddlewareQueryException;
    public GenericAddResponse addMethod(AddMethodRequest method) throws MiddlewareQueryException;

    public List<PropertySummary> getAllProperties() throws MiddlewareQueryException;

    public List<DataTypeSummary> getAllDataTypes() throws MiddlewareQueryException;
}

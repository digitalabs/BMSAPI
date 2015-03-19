package org.generationcp.bms.ontology.validator;

import org.generationcp.bms.util.I18nUtil;
import org.generationcp.middleware.domain.oms.DataType;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.OntologyManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.Errors;

import java.util.Objects;

import static org.generationcp.bms.util.I18nUtil.formatErrorMessage;

public abstract class OntologyValidator extends BaseValidator {

    protected static final String DOES_NOT_EXIST = "does.not.exist";
    protected static final String SHOULD_BE_NUMERIC = "should.be.numeric";
    protected static final String SHOULD_NOT_NULL_OR_EMPTY = "should.not.be.null";
    protected static final String SHOULD_BE_UNIQUE = "should.be.unique";
    protected static final String ENUM_TYPE_NOT_VALID = "enum.type.not.valid";

    @Autowired
    protected OntologyManagerService ontologyManagerService;

    @Autowired
    ResourceBundleMessageSource messageSource;

    protected void checkNumberField(String fieldName, String value, Errors errors){
        if(value.matches("^[0-9]+$")) return;
        log.error("field should be numeric");
        errors.rejectValue(fieldName, I18nUtil.formatErrorMessage(messageSource, SHOULD_BE_NUMERIC, null));
    }

    protected void shouldNotNullOrEmpty(String fieldName, Object value, Errors errors){
        if(!isNullOrEmpty(value)) return;
        errors.rejectValue(fieldName, I18nUtil.formatErrorMessage(messageSource, SHOULD_NOT_NULL_OR_EMPTY, null));
    }

    protected void checkTermExist(Integer id, Integer cvId, Errors errors){
        try {
            Term term = ontologyManagerService.getTermById(id);
            if(Objects.equals(term, null) || !Objects.equals(term.getVocabularyId(), cvId) ){
                errors.rejectValue("id", formatErrorMessage(messageSource, DOES_NOT_EXIST, new Object[]{id.toString()}));
            }
        } catch (Exception e) {
            log.error("Error while validating object", e);
        }
    }

    protected void checkTermUniqueness(Integer id, String name, Integer cvId, Errors errors) {

        try {
            Term term = ontologyManagerService.getTermByNameAndCvId(name, cvId);
            if (term == null) return;

            if (Objects.isNull(id) || !Objects.equals(id, term.getId())) {
                errors.rejectValue("name", I18nUtil.formatErrorMessage(messageSource, SHOULD_BE_UNIQUE, null));
            }
        }
        catch (MiddlewareQueryException e) {
            log.error("Error checking uniqueness of term name", e);
        }
    }

    protected void shouldHaveValidDataType(String fieldName, Integer dataTypeId, Errors errors){
        if(Objects.isNull(DataType.getById(dataTypeId))){
            errors.rejectValue(fieldName, I18nUtil.formatErrorMessage(messageSource, ENUM_TYPE_NOT_VALID, null));
        }
    }
}

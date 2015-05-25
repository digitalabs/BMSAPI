package org.ibp.api.java.impl.middleware.ontology.validator;

import com.google.common.base.Strings;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.DataType;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.ibp.api.domain.ontology.ScaleSummary;
import org.ibp.api.domain.ontology.ValidValues;
import org.ibp.api.domain.ontology.VariableCategory;
import org.ibp.api.java.impl.middleware.common.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Add Scale/Update Scale Validation rules for Scale request Refer:
 * http://confluence.leafnode.io/display/CD/Services+Validation
 * 1. Name is required
 * 2. The name must be unique
 * 3. Data type is required
 * 4. The data type ID must correspond to the ID of one of the supported data types (Numeric, Categorical, Character, DateTime, Person, Location or any other special data type that we add)
 * 5. If the data type is categorical, at least one category must be submitted
 * 6. Categories are only stored if the data type is categorical
 * 7. If there are categories, all labels and values within the set of categories must be unique
 * 8. The min and max valid values are only stored if the data type is numeric
 * 9. If the data type is numeric and minimum and maximum valid values are provided (they are not mandatory), they must be numeric values
 * 10. If present, the minimum valid value must be less than or equal to the maximum valid value, and the maximum valid value must be greater than or equal to the minimum valid value
 * 11. The name, data type and valid values cannot be changed if the scale is already in use
 * 12. Name is no more than 200 characters
 * 13. Description is no more than 255 characters
 */

/**
 * Extended from {@link OntologyValidator} for basic validation functions and error messages
 */

@Component
public class ScaleValidator extends OntologyValidator implements
org.springframework.validation.Validator {

	private static final Integer NAME_TEXT_LIMIT = 200;
	private static final Integer DESCRIPTION_TEXT_LIMIT = 255;

	private static final String SCALE_CATEGORIES_NAME_DUPLICATE = "scale.category.name.duplicate";
	private static final String SCALE_CATEGORIES_DESCRIPTION_DUPLICATE = "scale.category.description.duplicate";
	private static final String SCALE_CATEGORY_DESCRIPTION_REQUIRED = "scale.category.description.required";
	private static final String SCALE_NAME_DESCRIPTION_REQUIRED = "scale.category.name.required";

	private static final Logger LOGGER = LoggerFactory.getLogger(ScaleValidator.class);
	
	@Override
	public boolean supports(Class<?> aClass) {
		return ScaleSummary.class.equals(aClass);
	}

	@Override
	public void validate(Object target, Errors errors) {

		ScaleSummary scaleSummary = (ScaleSummary) target;

		boolean nameValidationResult = nameValidationProcessor(scaleSummary, errors);

		descriptionValidationProcessor(scaleSummary, errors);

		boolean dataTypeValidationResult = dataTypeValidationProcessor(scaleSummary, errors);

		if(dataTypeValidationResult){

			Integer dataTypeId = scaleSummary.getDataType().getId();

			if (Objects.equals(dataTypeId, DataType.CATEGORICAL_VARIABLE.getId())) {
				categoricalDataTypeValidationProcessor(scaleSummary, errors);
			}
			if (Objects.equals(dataTypeId, DataType.NUMERIC_VARIABLE.getId())) {
				numericDataTypeValidationProcessor(scaleSummary, errors);
			}
		}

		if(nameValidationResult){
			scaleShouldBeEditable(scaleSummary, errors);
		}
	}

	private void validateCategoriesForUniqueness(List<VariableCategory> categories, DataType dataType, Errors errors) {
		if (categories != null && Objects.equals(dataType, DataType.CATEGORICAL_VARIABLE)) {
			Set<String> labels = new HashSet<>();
			Set<String> values = new HashSet<>();
			for (int i = 1; i <= categories.size(); i++) {
				VariableCategory category = categories.get(i-1);
				String name = category.getName().trim();
				String value = category.getDescription().trim();

				if(isNullOrEmpty(value)){
					this.addCustomError(errors, "validValues.categories[" + i + "].description", SCALE_CATEGORY_DESCRIPTION_REQUIRED, null);
				}

				if(isNullOrEmpty(name)){
					this.addCustomError(errors, "validValues.categories[" + i + "].name", SCALE_NAME_DESCRIPTION_REQUIRED, null);
				}

				if (errors.hasErrors()) {
					return;
				}

				if (labels.contains(name)) {
					this.addCustomError(errors, "validValues.categories[" + i + "].name", SCALE_CATEGORIES_NAME_DUPLICATE, null);
				} else {
					labels.add(category.getName().trim());
				}

				if (values.contains(value)) {
					this.addCustomError(errors, "validValues.categories[" + i + "].description", SCALE_CATEGORIES_DESCRIPTION_DUPLICATE, null);
				} else {
					values.add(category.getDescription().trim());
				}
			}
		}
	}

	private void scaleShouldBeEditable(ScaleSummary scaleSummary, Errors errors) {
		if (scaleSummary.getId() == null) {
			return;
		}

		try {
			Scale oldScale = this.ontologyScaleDataManager.getScaleById(CommonUtil.tryParseSafe(scaleSummary.getId()));

			// that method should exist with requestId
			if (Objects.equals(oldScale, null)) {
				this.addCustomError(errors, ID_DOES_NOT_EXIST, new Object[] {"Scale", scaleSummary.getId()});
				return;
			}

			boolean isEditable = !this.termDataManager.isTermReferred(CommonUtil.tryParseSafe(scaleSummary.getId()));
			if (isEditable) {
				return;
			}

			boolean isNameSame = Objects.equals(scaleSummary.getName(), oldScale.getName());
			if (!isNameSame) {
				this.addCustomError(errors, "name", RECORD_IS_NOT_EDITABLE,new Object[] { "scale", "Name" });
			}

			boolean isDataTypeSame = Objects.equals(scaleSummary.getDataType().getId(), this.getDataTypeIdSafe(oldScale.getDataType()));
			if (!isDataTypeSame) {
				this.addCustomError(errors, "dataTypeId", RECORD_IS_NOT_EDITABLE, new Object[] { "scale", "DataTypeId" });
			}

			ValidValues validValues = scaleSummary.getValidValues() == null ? new ValidValues() : scaleSummary.getValidValues();
			boolean minValuesAreEqual = Objects.equals(validValues.getMin(), CommonUtil.tryParseSafe(oldScale.getMinValue()));
			boolean maxValuesAreEqual = Objects.equals(validValues.getMax(), CommonUtil.tryParseSafe(oldScale.getMaxValue()));
			List<VariableCategory> categories = validValues.getCategories() == null ? new ArrayList<VariableCategory>() : validValues.getCategories();
			boolean categoriesEqualSize = Objects.equals(categories.size(), oldScale .getCategories().size());
			boolean categoriesValuesAreSame = true;
			if (categoriesEqualSize) {
				for (VariableCategory l : categories) {
					if (oldScale.getCategories().containsKey(l.getName()) && Objects.equals(oldScale.getCategories().get(l.getName()), l.getDescription())) {
						continue;
					}
					categoriesValuesAreSame = false;
					break;
				}
			}
			if (!minValuesAreEqual || !maxValuesAreEqual || !categoriesEqualSize || !categoriesValuesAreSame) {
				this.addCustomError(errors, "validValues", RECORD_IS_NOT_EDITABLE, new Object[] { "scale", "ValidValues" });
			}

		} catch (MiddlewareException e) {
			LOGGER.error("Error while executing scaleShouldBeEditable", e);
			this.addDefaultError(errors);
		}
	}

	private Integer getDataTypeIdSafe(DataType dataType) {
		return dataType == null ? null : dataType.getId();
	}

	private boolean nameValidationProcessor(ScaleSummary scaleSummary, Errors errors){

		Integer initialCount = errors.getErrorCount();

		// 1. Name is required
		this.shouldNotNullOrEmpty("Name", "name", scaleSummary.getName(), errors);

		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		// 12. Name is no more than 200 characters
		this.fieldShouldNotOverflow("name", scaleSummary.getName(), NAME_TEXT_LIMIT, errors);

		// 2. The name must be unique
		this.checkTermUniqueness("Scale", CommonUtil.tryParseSafe(scaleSummary.getId()), scaleSummary.getName(), CvId.SCALES.getId(), errors);

		return errors.getErrorCount() == initialCount;
	}

	// 13. Description is no more than 255 characters
	private boolean descriptionValidationProcessor(ScaleSummary scaleSummary, Errors errors){

		Integer initialCount = errors.getErrorCount();

		if(Strings.isNullOrEmpty(scaleSummary.getDescription())) {
			scaleSummary.setDescription("");
		} else {
			scaleSummary.setDescription(scaleSummary.getDescription().trim());
		}

		this.fieldShouldNotOverflow("description", scaleSummary.getDescription(), DESCRIPTION_TEXT_LIMIT, errors);

		return errors.getErrorCount() == initialCount;
	}

	private boolean dataTypeValidationProcessor(ScaleSummary scaleSummary, Errors errors){

		Integer initialCount = errors.getErrorCount();

		// 3. Data type is required
		this.shouldNotNullOrEmpty("Data Type", "dataTypeId", scaleSummary.getDataType(), errors);

		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		if(!this.isNonNullValidNumericString(scaleSummary.getDataType().getId())){
			addCustomError(errors, "dataTypeId", INVALID_TYPE_ID, new Object[]{"Data Type"});
		}

		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		// 4. The data type ID must correspond to the ID of one of the supported
		// data types (Numeric, Categorical, Character, DateTime, Person,
		// Location or any other special data type that we add)
		if (DataType.getById(scaleSummary.getDataType().getId()) == null) {
			this.addCustomError(errors, "dataTypeId", INVALID_TYPE_ID, new Object[] {"Data Type"});
		}

		return errors.getErrorCount() == initialCount;
	}

	private boolean categoricalDataTypeValidationProcessor(ScaleSummary scaleSummary, Errors errors){

		Integer initialCount = errors.getErrorCount();

		DataType dataType = DataType.getById(scaleSummary.getDataType().getId());

		ValidValues validValues = scaleSummary.getValidValues() == null ? new ValidValues() : scaleSummary.getValidValues();

		List<VariableCategory> categories = validValues.getCategories();

		// 5. If the data type is categorical, at least one category must be
		// submitted
		if (Objects.equals(dataType, DataType.CATEGORICAL_VARIABLE)) {
			if (categories == null || categories.isEmpty()) {
				this.addCustomError(errors, "validValues.categories", LIST_SHOULD_NOT_BE_EMPTY, new Object[]{"category"});
			}
		}

		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		// 7. If there are categories, all labels and values within the set of
		// categories must be unique
		this.validateCategoriesForUniqueness(categories, dataType, errors);

		return errors.getErrorCount() == initialCount;
	}

	private boolean numericDataTypeValidationProcessor(ScaleSummary scaleSummary, Errors errors){

		Integer initialCount = errors.getErrorCount();

		DataType dataType = DataType.getById(scaleSummary.getDataType().getId());

		ValidValues validValues = scaleSummary.getValidValues() == null ? new ValidValues() : scaleSummary.getValidValues();

		String minValue = validValues.getMin() == null ? null : validValues.getMin().toString();
		String maxValue = validValues.getMax() == null ? null : validValues.getMax().toString();

		// 9. If the data type is numeric and minimum and maximum valid values
		// are provided (they are not mandatory), they must be numeric values
		if (Objects.equals(dataType, DataType.NUMERIC_VARIABLE)) {
			if (!this.isNonNullValidNumericString(minValue)) {
				this.addCustomError(errors, "validValues.min", FIELD_SHOULD_BE_NUMERIC, null);
			}

			if (!this.isNonNullValidNumericString(maxValue)) {
				this.addCustomError(errors, "validValues.max", FIELD_SHOULD_BE_NUMERIC, null);
			}
		}

		if (errors.getErrorCount() > initialCount) {
			return false;
		}

		// 10. If present, the minimum valid value must be less than or equal to
		// the maximum valid value, and the maximum valid value must be greater
		// than or equal to the minimum valid value
		if (this.isNonNullValidNumericString(minValue) && this.isNonNullValidNumericString(maxValue) && this.getIntegerValueSafe(minValue, 0) > this.getIntegerValueSafe(maxValue, 0)) {
			this.addCustomError(errors, "validValues.min", OntologyValidator.MIN_MAX_NOT_VALID, null);
		}

		return errors.getErrorCount() == initialCount;
	}
}

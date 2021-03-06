package org.ibp.api.java.impl.middleware.preset;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.constant.ToolSection;
import org.generationcp.middleware.ContextHolder;
import org.generationcp.middleware.manager.api.PresetService;
import org.generationcp.middleware.pojos.presets.ProgramPreset;
import org.ibp.api.domain.common.LabelPrintingStaticField;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ConflictException;
import org.ibp.api.exception.NotSupportedException;
import org.ibp.api.java.ontology.VariableService;
import org.ibp.api.rest.common.FileType;
import org.ibp.api.rest.preset.domain.LabelPrintingPresetDTO;
import org.ibp.api.rest.preset.domain.PresetDTO;
import org.ibp.api.rest.preset.domain.PresetType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.List;

/**
 * Created by clarysabel on 2/19/19.
 */
@Component
public class PresetDTOValidator {

	private static final Integer FIELDBOOK_TOOL_ID = 23;
	private static final int NAME_MAX_LENGTH = 50;

	@Autowired
	private PresetService presetService;

	@Autowired
	private VariableService variableService;

	private BindingResult errors;

	public void validate(final String crop, final PresetDTO presetDTO) {

		this.errors = new MapBindingResult(new HashMap<String, String>(), PresetDTO.class.getName());

		if (presetDTO.getToolId() == null || presetDTO.getToolId()!= FIELDBOOK_TOOL_ID) {
			this.errors.reject("preset.invalid.tool", "");
		}

		if (!isValidToolSection(presetDTO.getToolSection())) {
			this.errors.reject("preset.invalid.tool.section", "");
		}

		//Validate type
		if (StringUtils.isEmpty(presetDTO.getType()) || PresetType.getEnum(presetDTO.getType()) == null) {
			this.errors.reject("preset.invalid.type", "");
		}

		final String programUUID = presetDTO.getProgramUUID();
		if (StringUtils.isEmpty(programUUID)) {
			this.errors.reject("preset.program.uuid.required", "");
		// It is assumed that programUUID was set in ContextHolder. Check that it is the same specified in DTO
		} else if (!programUUID.equals(ContextHolder.getCurrentProgram())) {
			this.errors.reject("preset.invalid.program.uuid", "");
		}

		final String presetName = presetDTO.getName();
		if (StringUtils.isEmpty(presetName)) {
			this.errors.reject("preset.name.required", "");
		} else if (presetName.length() > NAME_MAX_LENGTH) {
			this.errors.reject("preset.name.length.invalid", new String[] {String.valueOf(NAME_MAX_LENGTH)}, "");
		}

		if (this.errors.hasErrors()) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		final List<ProgramPreset> presets = this.presetService
				.getProgramPresetFromProgramAndToolByName(presetName, programUUID, presetDTO.getToolId(),
						presetDTO.getToolSection());
		if (!presets.isEmpty()) {
			this.errors.reject("preset.name.invalid", "");
			throw new ConflictException(this.errors.getAllErrors());
		}

		if (!PresetType.LABEL_PRINTING_PRESET.equals(PresetType.getEnum(presetDTO.getType()))) {
			this.errors.reject("preset.type.not.supported", "");
			throw new NotSupportedException(this.errors.getAllErrors().get(0));
		}

		//Cast according type and call specific validations
		if (presetDTO instanceof LabelPrintingPresetDTO) {
			final LabelPrintingPresetDTO labelPrintingPresetDTO = (LabelPrintingPresetDTO) presetDTO;
			this.validateLabelPrintingPreset(crop, labelPrintingPresetDTO);
		}
	}

	/**
	 * Validates supported tool sections
	 */
	private static boolean isValidToolSection(final String toolSection) {
		return ToolSection.DATASET_LABEL_PRINTING_PRESET.name().equals(toolSection)
			|| ToolSection.LOT_LABEL_PRINTING_PRESET.name().equals(toolSection);
	}

	private void validateLabelPrintingPreset(final String crop, final LabelPrintingPresetDTO labelPrintingPresetDTO) {
		if (labelPrintingPresetDTO.getFileConfiguration() == null) {
			this.errors.reject("label.printing.preset.file.configuration.required", "");
		}

		final LabelPrintingPresetDTO.BarcodeSetting barcodeSetting = labelPrintingPresetDTO.getBarcodeSetting();

		if (barcodeSetting == null) {
			this.errors.reject("label.printing.preset.barcode.setting.required", "");
		}
		if (labelPrintingPresetDTO.getSelectedFields() == null) {
			this.errors.reject("label.printing.preset.selected.fields.required", "");
		}
		if (this.errors.hasErrors()) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		labelPrintingPresetDTO.getSelectedFields().forEach(list -> {
			list.forEach(fieldId -> {
				if (this.isInvalidField(crop, labelPrintingPresetDTO, fieldId)) {
					this.errors.reject("label.printing.preset.invalid.field.in.selected.field", "");
					throw new ApiRequestValidationException(this.errors.getAllErrors());
				}
			});
		});

		final List<Integer> barcodeFields = barcodeSetting.getBarcodeFields();

		if (barcodeSetting.isBarcodeNeeded()) {
			if (barcodeSetting.isAutomaticBarcode()) {
				if (barcodeFields != null && !barcodeFields.isEmpty()) {
					this.errors.reject("label.printing.preset.inconsistent.barcode.setting", "");
					throw new ApiRequestValidationException(this.errors.getAllErrors());
				}
			} else {
				if (barcodeFields == null || barcodeFields.isEmpty()) {
					this.errors.reject("label.printing.preset.inconsistent.barcode.setting", "");
					throw new ApiRequestValidationException(this.errors.getAllErrors());
				}
				barcodeFields.forEach(fieldId -> {
					if (this.isInvalidField(crop, labelPrintingPresetDTO, fieldId)) {
						this.errors.reject("label.printing.preset.invalid.field.in.barcode.field", "");
						throw new ApiRequestValidationException(this.errors.getAllErrors());
					}
				});
			}
		} else {
			if (barcodeSetting.isAutomaticBarcode()
				|| (barcodeFields != null && !barcodeFields.isEmpty())) {
				this.errors.reject("label.printing.preset.inconsistent.barcode.setting", "");
				throw new ApiRequestValidationException(this.errors.getAllErrors());
			}
		}

		final FileType fileType = FileType.getEnum(labelPrintingPresetDTO.getFileConfiguration().getOutputType());
		if (fileType == null) {
			this.errors.reject("preset.invalid.file.type", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private boolean isInvalidField(final String crop, final LabelPrintingPresetDTO labelPrintingPresetDTO, final Integer fieldId) {
		return this.isValidateFieldId(labelPrintingPresetDTO)
			&& !LabelPrintingStaticField.getAvailableStaticFields().contains(fieldId)
			&& this.variableService.getVariableById(crop, labelPrintingPresetDTO.getProgramUUID(), String.valueOf(fieldId))
			== null;
	}

	private boolean isValidateFieldId(final LabelPrintingPresetDTO labelPrintingPresetDTO) {
		return !ToolSection.LOT_LABEL_PRINTING_PRESET.name().equals(labelPrintingPresetDTO.getToolSection());
	}

}

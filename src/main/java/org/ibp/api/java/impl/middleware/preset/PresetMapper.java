package org.ibp.api.java.impl.middleware.preset;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.generationcp.middleware.pojos.presets.ProgramPreset;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.rest.preset.domain.PresetDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

/**
 * Created by clarysabel on 2/19/19.
 */
@Component
public class PresetMapper {

	private ObjectMapper jacksonMapper;

	@Autowired
	private ResourceBundleMessageSource messageSource;

	public PresetMapper() {
		jacksonMapper = new ObjectMapper();
		jacksonMapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
	}

	ProgramPreset map (final PresetDTO presetDTO) {
		final ProgramPreset programPreset = new ProgramPreset();
		programPreset.setName(presetDTO.getName());
		programPreset.setToolId(presetDTO.getToolId());
		programPreset.setToolSection(presetDTO.getToolSection());
		programPreset.setProgramUuid(presetDTO.getProgramUUID());
		try {
			programPreset.setConfiguration(jacksonMapper.writerWithView(PresetDTO.View.Configuration.class).writeValueAsString(presetDTO));
		} catch (final Exception e) {
			throw new ApiRuntimeException(messageSource.getMessage("preset.mapping.internal.error", null, LocaleContextHolder.getLocale()));
		}
		return programPreset;
	}

	PresetDTO map (final ProgramPreset programPreset) {
		final PresetDTO presetDTO;
		try {
			presetDTO = jacksonMapper.readValue(programPreset.getConfiguration(), PresetDTO.class);
		} catch (final Exception e) {
			throw new ApiRuntimeException(messageSource.getMessage("preset.mapping.internal.error", null, LocaleContextHolder.getLocale()));
		}
		presetDTO.setToolId(programPreset.getToolId());
		presetDTO.setProgramUUID(programPreset.getProgramUuid());
		presetDTO.setToolSection(programPreset.getToolSection());
		presetDTO.setName(programPreset.getName());
		presetDTO.setId(programPreset.getProgramPresetId());
		return presetDTO;
	}

}

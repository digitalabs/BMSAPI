
package org.ibp.api.java.impl.middleware.study;

import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.generationcp.middleware.service.api.study.StudyEntryPropertyData;
import org.ibp.api.domain.study.StudyGermplasm;
import org.ibp.api.mapper.ApiMapper;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.spi.MappingContext;

import java.util.HashMap;
import java.util.Map;

public class StudyEntryMapper {

	private static final ModelMapper applicationWideModelMapper = ApiMapper.getInstance();
	private static final String DEFAULT_ENTRY_TYPE = String.valueOf(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId());

	/**
	 * We do not want public constructor of this class as all methods are static
	 */
	private StudyEntryMapper() {

	}

	/**
	 * Configuring the application wide {@link ModelMapper} with ontology related configuration.
	 */
	static {
		StudyEntryMapper.addStudyEntryDtoMapping(StudyEntryMapper.applicationWideModelMapper);
		StudyEntryMapper.addGermplasmListDataMapping(StudyEntryMapper.applicationWideModelMapper);
	}

	public static ModelMapper getInstance() {
		return StudyEntryMapper.applicationWideModelMapper;
	}

	private static void addStudyEntryDtoMapping(final ModelMapper mapper) {
		mapper.addMappings(new PropertyMap<StudyEntryDto, StudyGermplasm>() {

			@Override
			protected void configure() {
				this.map().setEntryNumber(this.source.getEntryNumber());
				this.using(new OptionalConverter(TermId.ENTRY_TYPE.getId())).map(this.source).setEntryType(null);

				this.map().getGermplasmListEntrySummary().setGid(this.source.getGid());
				this.using(new OptionalConverter(TermId.CROSS.getId())).map(this.source).getGermplasmListEntrySummary().setCross(null);
				this.map().getGermplasmListEntrySummary().setDesignation(this.source.getDesignation());
				this.map().getGermplasmListEntrySummary().setEntryCode(this.source.getEntryCode());
				this.using(new OptionalConverter(TermId.SEED_SOURCE.getId())).map(this.source).getGermplasmListEntrySummary().setSeedSource(null);
			}
		});
	}

	private static class OptionalConverter implements Converter<StudyEntryDto, String> {

		private Integer termId;

		public OptionalConverter(final Integer termId) {
			this.termId = termId;
		}

		@Override
		public String convert(MappingContext<StudyEntryDto, String> mappingContext) {
			return mappingContext.getSource().getStudyEntryPropertyValue(this.termId).orElse("");
		}
	}


	private static class PropertiesConverter implements Converter<GermplasmListData, Map<Integer, StudyEntryPropertyData>> {

		@Override
		public Map<Integer, StudyEntryPropertyData> convert(final MappingContext<GermplasmListData, Map<Integer, StudyEntryPropertyData>> context) {
			final Map<Integer, StudyEntryPropertyData> stringStudyEntryPropertyDataMap = new HashMap<>();
			final GermplasmListData listData = context.getSource();
			stringStudyEntryPropertyDataMap
					.put(TermId.ENTRY_TYPE.getId(), new StudyEntryPropertyData(null, TermId.ENTRY_TYPE.getId(), DEFAULT_ENTRY_TYPE));
			stringStudyEntryPropertyDataMap
					.put(TermId.SEED_SOURCE.getId(), new StudyEntryPropertyData(null, TermId.SEED_SOURCE.getId(), listData.getSeedSource()));
			stringStudyEntryPropertyDataMap
					.put(TermId.GROUPGID.getId(),
							new StudyEntryPropertyData(null, TermId.GROUPGID.getId(), String.valueOf(listData.getGermplasm().getMgid())));


			return context.getMappingEngine().map(context.create(stringStudyEntryPropertyDataMap, context.getDestinationType()));
		}

	}

	private static void addGermplasmListDataMapping(final ModelMapper mapper) {
		mapper.addMappings(new PropertyMap<GermplasmListData, StudyEntryDto>() {

			@Override
			protected void configure() {
				this.map().setEntryNumber(this.source.getEntryId());
				this.map().setEntryId(this.source.getEntryId());
				this.map().setGid(this.source.getGermplasmId());
				this.map().setDesignation(this.source.getDesignation());
				this.map().setEntryCode(this.source.getEntryCode());
				this.using(new PropertiesConverter()).map(this.source).setProperties(null);
			}
		});
	}

}

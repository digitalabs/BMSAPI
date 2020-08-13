package org.ibp.api.rest.labelprinting;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.api.inventory.study.StudyTransactionsDto;
import org.generationcp.middleware.api.inventory.study.StudyTransactionsRequest;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.inventory.manager.TransactionsSearchDto;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.manager.Season;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.api.dataset.DatasetTypeService;
import org.generationcp.middleware.service.api.dataset.InstanceDetailsDTO;
import org.generationcp.middleware.service.api.dataset.ObservationUnitData;
import org.generationcp.middleware.service.api.dataset.ObservationUnitRow;
import org.ibp.api.domain.common.LabelPrintingStaticField;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.inventory.study.StudyTransactionsService;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.ibp.api.rest.common.FileType;
import org.ibp.api.rest.labelprinting.domain.Field;
import org.ibp.api.rest.labelprinting.domain.LabelType;
import org.ibp.api.rest.labelprinting.domain.LabelsData;
import org.ibp.api.rest.labelprinting.domain.LabelsGeneratorInput;
import org.ibp.api.rest.labelprinting.domain.LabelsInfoInput;
import org.ibp.api.rest.labelprinting.domain.LabelsNeededSummary;
import org.ibp.api.rest.labelprinting.domain.LabelsNeededSummaryResponse;
import org.ibp.api.rest.labelprinting.domain.OriginResourceMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Component
@Transactional
public class ObservationDatasetLabelPrinting extends LabelPrintingStrategy {

	@Autowired
	private ResourceBundleMessageSource messageSource;

	@Autowired
	private DatasetService middlewareDatasetService;

	@Autowired
	private DatasetValidator datasetValidator;

	@Autowired
	private StudyDataManager studyDataManager;

	@Autowired
	private StudyValidator studyValidator;

	@Autowired
	private DatasetTypeService datasetTypeService;

	@Autowired
	private StudyTransactionsService studyTransactionsService;

	private static Field STUDY_NAME_FIELD;
	private static Field YEAR_FIELD;
	private static Field PARENTAGE_FIELD;
	private static Field SEASON_FIELD;
	private static List<Field> DEFAULT_STUDY_DETAILS_FIELDS;
	private static List<Field> DEFAULT_LOT_DETAILS_FIELDS;
	private static List<Field> DEFAULT_TRANSACTION_DETAILS_FIELDS;

	static String PLOT = "PLOT";
	private static final String OBS_UNIT_ID = "OBS_UNIT_ID";
	private static final String LOCATION_ID = "LOCATION_ID";
	private static final String GID = "GID";
	private static final String TRIAL_INSTANCE = "TRIAL_INSTANCE";
	private static final String PLOT_NO = "PLOT_NO";
	private static final String ENTRY_NO = "ENTRY_NO";

	public static List<FileType> SUPPORTED_FILE_TYPES = Arrays.asList(FileType.CSV, FileType.PDF,  FileType.XLS);

	//Variable ids of PI_NAME_ID and COOPERATOR_ID
	static List<Integer> PAIR_ID_VARIABLES = Arrays.asList(TermId.PI_ID.getId(), TermId.COOPERATOOR_ID.getId());

	private static List<Integer> STATIC_FIELD_IDS;
	private static List<Integer> STATIC_LOT_FIELD_IDS;
	private static List<Integer> STATIC_TRANSACTION_FIELD_IDS;

	@PostConstruct
	void initStaticFields() {
		final String studyNamePropValue = this.getMessage("label.printing.field.study.name");
		final String yearPropValue = this.getMessage("label.printing.field.year");
		final String parentagePropValue = this.getMessage("label.printing.field.parentage");
		final String seasonPropValue = this.getMessage("label.printing.field.season");

		STUDY_NAME_FIELD = new Field(LabelPrintingStaticField.STUDY_NAME.getFieldId(), studyNamePropValue);
		YEAR_FIELD = new Field(LabelPrintingStaticField.YEAR.getFieldId(), yearPropValue);
		PARENTAGE_FIELD = new Field(LabelPrintingStaticField.PARENTAGE.getFieldId(), parentagePropValue);
		SEASON_FIELD = new Field(TermId.SEASON_VAR.getId(),seasonPropValue);
		DEFAULT_STUDY_DETAILS_FIELDS = Arrays.asList(STUDY_NAME_FIELD, YEAR_FIELD);

		DEFAULT_TRANSACTION_DETAILS_FIELDS = ImmutableList.<Field>builder()
			.add(new Field(LabelPrintingStaticField.TRN_ID.getFieldId(), "Trn ID"))
			.add(new Field(LabelPrintingStaticField.STATUS.getFieldId(), "Status"))
			.add(new Field(LabelPrintingStaticField.TYPE.getFieldId(), "Type"))
			.add(new Field(LabelPrintingStaticField.CREATED.getFieldId(), "Creation date"))
			.add(new Field(LabelPrintingStaticField.NOTES.getFieldId(), "Notes"))
			.add(new Field(LabelPrintingStaticField.USERNAME.getFieldId(), "Username")).build();

		DEFAULT_LOT_DETAILS_FIELDS = ImmutableList.<Field>builder()
			.add(new Field(LabelPrintingStaticField.LOT_ID.getFieldId(), "Lot ID"))
			.add(new Field(LabelPrintingStaticField.LOT_UID.getFieldId(), "Lot UID"))
			.add(new Field(LabelPrintingStaticField.STOCK_ID.getFieldId(), "Stock id"))
			.add(new Field(LabelPrintingStaticField.AVAILABLE_BALANCE.getFieldId(), "Available balance"))
			.add(new Field(LabelPrintingStaticField.UNITS.getFieldId(), "Units"))
			.add(new Field(LabelPrintingStaticField.STORAGE_LOCATION_ABBR.getFieldId(), "Storage location"))
			.add(new Field(LabelPrintingStaticField.STORAGE_LOCATION.getFieldId(), "Storage location"))
			.add(new Field(LabelPrintingStaticField.TRN_NOTES.getFieldId(), "Notes")).build();

		STATIC_FIELD_IDS = Arrays.asList(LabelPrintingStaticField.STUDY_NAME.getFieldId(), LabelPrintingStaticField.YEAR.getFieldId(),
			LabelPrintingStaticField.PARENTAGE.getFieldId(),
			LabelPrintingStaticField.LOT_ID.getFieldId(),
			LabelPrintingStaticField.LOT_UID.getFieldId(),
			LabelPrintingStaticField.STOCK_ID.getFieldId(),
			LabelPrintingStaticField.AVAILABLE_BALANCE.getFieldId(),
			LabelPrintingStaticField.UNITS.getFieldId(),
			LabelPrintingStaticField.STORAGE_LOCATION_ABBR.getFieldId(),
			LabelPrintingStaticField.STORAGE_LOCATION.getFieldId(),
			LabelPrintingStaticField.NOTES.getFieldId(),
			LabelPrintingStaticField.TRN_ID.getFieldId(),
			LabelPrintingStaticField.STATUS.getFieldId(),
			LabelPrintingStaticField.TYPE.getFieldId(),
			LabelPrintingStaticField.CREATED.getFieldId(),
			LabelPrintingStaticField.TRN_NOTES.getFieldId(),
			LabelPrintingStaticField.USERNAME.getFieldId()
		);

		STATIC_LOT_FIELD_IDS = Arrays.asList(LabelPrintingStaticField.LOT_ID.getFieldId(),
			LabelPrintingStaticField.LOT_UID.getFieldId(),
			LabelPrintingStaticField.STOCK_ID.getFieldId(),
			LabelPrintingStaticField.AVAILABLE_BALANCE.getFieldId(),
			LabelPrintingStaticField.UNITS.getFieldId(),
			LabelPrintingStaticField.STORAGE_LOCATION_ABBR.getFieldId(),
			LabelPrintingStaticField.STORAGE_LOCATION.getFieldId(),
			LabelPrintingStaticField.NOTES.getFieldId()
		);

		STATIC_TRANSACTION_FIELD_IDS = Arrays.asList(
			LabelPrintingStaticField.TRN_ID.getFieldId(),
			LabelPrintingStaticField.STATUS.getFieldId(),
			LabelPrintingStaticField.TYPE.getFieldId(),
			LabelPrintingStaticField.CREATED.getFieldId(),
			LabelPrintingStaticField.TRN_NOTES.getFieldId(),
			LabelPrintingStaticField.USERNAME.getFieldId()
		);
	}

	@Override
	public void validateLabelsInfoInputData(final LabelsInfoInput labelsInfoInput) {
		this.studyValidator.validate(labelsInfoInput.getStudyId(), false);
		this.datasetValidator.validateDataset(labelsInfoInput.getStudyId(), labelsInfoInput.getDatasetId());
		this.datasetValidator.validateObservationDatasetType(labelsInfoInput.getDatasetId());
	}

	@Override
	public void validateLabelsGeneratorInputData(final LabelsGeneratorInput labelsGeneratorInput) {
		super.validateLabelsGeneratorInputData(labelsGeneratorInput);
	}

	@Override
	public LabelsNeededSummary getSummaryOfLabelsNeeded(final LabelsInfoInput labelsInfoInput) {
		final LabelsNeededSummary labelsNeededSummary = new LabelsNeededSummary();
		final List<InstanceDetailsDTO>  InstanceDetailsDTOs =
			this.middlewareDatasetService.getInstanceDetails(labelsInfoInput.getDatasetId(), labelsInfoInput.getStudyId());
		long totalNumberOfLabelsNeeded = 0;
		for (InstanceDetailsDTO instanceDetailsDTO :InstanceDetailsDTOs) {
			final LabelsNeededSummary.Row row =
				new LabelsNeededSummary.Row(instanceDetailsDTO.getEnvironment().toString(), instanceDetailsDTO.getnOfObservations(),
					instanceDetailsDTO.getnOfObservations(),
					instanceDetailsDTO.getnOfReps(),
					instanceDetailsDTO.getnOfEntries());
			labelsNeededSummary.addRow(row);
			totalNumberOfLabelsNeeded += row.getLabelsNeeded();
		}
		labelsNeededSummary.setTotalNumberOfLabelsNeeded(totalNumberOfLabelsNeeded);
		return labelsNeededSummary;
	}

	@Override
	public LabelsNeededSummaryResponse transformLabelsNeededSummary(final LabelsNeededSummary labelsNeededSummary) {
		final String labelsNeededText = this.getMessage("label.printing.labels.needed");
		final String environmentText = this.getMessage("label.printing.environment");
		final String numberOfEntriesText = this.getMessage("label.printing.number.of.entries.needed");
		final String numberOfRepsText = this.getMessage("label.printing.number.of.reps.needed");
		final List<String> headers = new LinkedList<>();
		headers.add(environmentText);
		headers.add(numberOfEntriesText);
		headers.add(numberOfRepsText);
		headers.add(labelsNeededText);
		final List<Map<String, String>> values = new LinkedList<>();
		for (final LabelsNeededSummary.Row row : labelsNeededSummary.getRows()) {
			final Map<String, String> valuesMap = new LinkedHashMap<>();
			valuesMap.put(environmentText, row.getInstanceNumber());
			valuesMap.put(numberOfEntriesText, String.valueOf(row.getEntries()));
			valuesMap.put(numberOfRepsText, String.valueOf(row.getReps()));
			valuesMap.put(labelsNeededText, String.valueOf(row.getLabelsNeeded()));
			values.add(valuesMap);
		}
		return new LabelsNeededSummaryResponse(headers, values, labelsNeededSummary.getTotalNumberOfLabelsNeeded());
	}

	@Override
	public OriginResourceMetadata getOriginResourceMetadata(final LabelsInfoInput labelsInfoInput) {
		final StudyDetails study = this.studyDataManager.getStudyDetails(labelsInfoInput.getStudyId());
		final DatasetDTO datasetDTO = this.middlewareDatasetService.getDataset(labelsInfoInput.getDatasetId());

		final String defaultFileName = this.getDefaultFileName(study, datasetDTO);

		final Map<String, String> resultsMap = new LinkedHashMap<>();
		resultsMap.put(this.getMessage("label.printing.name"), study.getStudyName());
		resultsMap.put(this.getMessage("label.printing.title"), study.getDescription());
		resultsMap.put(this.getMessage("label.printing.objective"), (study.getObjective() == null) ? StringUtils.EMPTY : study.getObjective());
		resultsMap.put(this.getMessage("label.printing.selected.dataset"), datasetDTO.getName());
		resultsMap.put(
			this.getMessage("label.printing.number.of.environments.in.dataset"),
			String.valueOf(datasetDTO.getInstances().size()));

		return new OriginResourceMetadata(defaultFileName, resultsMap);
	}

	@Override
	public List<LabelType> getAvailableLabelTypes(final LabelsInfoInput labelsInfoInput) {
		final List<LabelType> labelTypes = new LinkedList<>();

		final String studyDetailsPropValue = this.getMessage("label.printing.study.details");
		final String datasetDetailsPropValue = this.getMessage("label.printing.dataset.details");

		final String lotDetailsPropValue = this.getMessage("label.printing.study.lot.list.details");
		final String transactionDetailsPropValue = this.getMessage("label.printing.study.transaction.list.details");

		final DatasetDTO dataSetDTO = this.middlewareDatasetService.getDataset(labelsInfoInput.getDatasetId());

		final int environmentDatasetId =
			this.studyDataManager.getDataSetsByType(labelsInfoInput.getStudyId(), DatasetTypeEnum.SUMMARY_DATA.getId()).get(0).getId();

		final int plotDatasetId = dataSetDTO.getDatasetId();

		final List<MeasurementVariable> studyDetailsVariables = this.middlewareDatasetService
			.getObservationSetVariables(labelsInfoInput.getStudyId(), Arrays.asList(VariableType.STUDY_DETAIL.getId()));

		final List<MeasurementVariable> environmentVariables = this.middlewareDatasetService.getObservationSetVariables(environmentDatasetId,
			Arrays.asList(VariableType.ENVIRONMENT_DETAIL.getId(), VariableType.EXPERIMENTAL_DESIGN.getId(),
				VariableType.STUDY_CONDITION.getId()));

		final List<MeasurementVariable> treatmentFactors =
			this.middlewareDatasetService.getObservationSetVariables(plotDatasetId, Arrays.asList(VariableType.TREATMENT_FACTOR.getId()));

		final List<MeasurementVariable> plotVariables = this.middlewareDatasetService.getObservationSetVariables(plotDatasetId,
			Arrays.asList(VariableType.EXPERIMENTAL_DESIGN.getId(), VariableType.GERMPLASM_DESCRIPTOR.getId()));

		final List<MeasurementVariable> datasetVariables = this.middlewareDatasetService
			.getObservationSetVariables(labelsInfoInput.getDatasetId(), Arrays.asList(VariableType.OBSERVATION_UNIT.getId(),VariableType.SELECTION_METHOD
				.getId(), VariableType.TRAIT.getId()));

		final LabelType studyDetailsLabelType = new LabelType(studyDetailsPropValue, studyDetailsPropValue);
		final LabelType lotDetailsLabelType = new LabelType(lotDetailsPropValue, lotDetailsPropValue);
		final LabelType transactionDetailsLabelType = new LabelType(transactionDetailsPropValue, transactionDetailsPropValue);

		lotDetailsLabelType.setFields(DEFAULT_LOT_DETAILS_FIELDS);
		transactionDetailsLabelType.setFields(DEFAULT_TRANSACTION_DETAILS_FIELDS);

		final List<Field> studyDetailsFields = new LinkedList<>();
		//Requirement to add Study Name as an available label when in fact it is not a variable.
		studyDetailsFields.addAll(DEFAULT_STUDY_DETAILS_FIELDS);
		studyDetailsFields.addAll(this.transform(studyDetailsVariables));
		studyDetailsFields.addAll(this.transform(environmentVariables));
		studyDetailsFields.addAll(this.transform(treatmentFactors));
		studyDetailsLabelType.setFields(studyDetailsFields);

		final LabelType datasetDetailsLabelType = new LabelType(datasetDetailsPropValue, datasetDetailsPropValue);
		final List<Field> datasetDetailsFields = new LinkedList<>();

		datasetDetailsFields.addAll(this.transform(plotVariables));
		datasetDetailsFields.addAll(this.transform(datasetVariables));
		datasetDetailsFields.add(PARENTAGE_FIELD);

		if(studyDetailsFields.indexOf(SEASON_FIELD)== -1){
			studyDetailsFields.add(SEASON_FIELD);
		}
		datasetDetailsLabelType.setFields(datasetDetailsFields);

		labelTypes.add(studyDetailsLabelType);
		labelTypes.add(datasetDetailsLabelType);
		labelTypes.add(lotDetailsLabelType);
		labelTypes.add(transactionDetailsLabelType);
		this.removePairIdVariables(labelTypes);
		return labelTypes;
	}

	@Override
	public LabelsData getLabelsData(final LabelsGeneratorInput labelsGeneratorInput) {
		final StudyDetails study = this.studyDataManager.getStudyDetails(labelsGeneratorInput.getStudyId());

		final Integer ObsDatasetUnitIdFieldKey = TermId.OBS_UNIT_ID.getId();

		final StudyTransactionsRequest studyTransactionsRequest = new StudyTransactionsRequest();
		final TransactionsSearchDto transactionsSearch = new TransactionsSearchDto();

		transactionsSearch.setPlantingStudyIds(Arrays.asList(labelsGeneratorInput.getStudyId()));
		studyTransactionsRequest.setTransactionsSearch(transactionsSearch);

		final List<StudyTransactionsDto> studyTransactionsDtos =
			studyTransactionsService.searchStudyTransactions(labelsGeneratorInput.getStudyId(), studyTransactionsRequest);

		final Map<String, StudyTransactionsDto> observationUnitDtoTransactionDtoMap = new HashMap<>();
		studyTransactionsDtos.forEach(studyTransactionsDto -> observationUnitDtoTransactionDtoMap
			.put(studyTransactionsDto.getObservationUnits().get(0).getObsUnitId(), studyTransactionsDto));

		final Map<Integer, Field> termIdFieldMap = Maps.uniqueIndex(labelsGeneratorInput.getAllAvailablefields(), Field::getId);

		final Set<Integer> allRequiredKeys = new HashSet<>();
		if (labelsGeneratorInput.isBarcodeRequired()) {
			if (labelsGeneratorInput.isAutomaticBarcode()) {
				allRequiredKeys.add(ObsDatasetUnitIdFieldKey);
			} else {
				allRequiredKeys.addAll(labelsGeneratorInput.getBarcodeFields());
			}
		}
		labelsGeneratorInput.getFields().forEach(f -> allRequiredKeys.addAll(f));

		final Map<String, String> gidPedigreeMap = new HashMap<>();

		final List<ObservationUnitRow> observationUnitRows =
			this.middlewareDatasetService.getAllObservationUnitRows(labelsGeneratorInput.getStudyId(), labelsGeneratorInput.getDatasetId());

		Collections.sort(
			observationUnitRows,
			Comparator.comparing((ObservationUnitRow o) -> Integer.valueOf(o.getVariables().get(TRIAL_INSTANCE).getValue()))
				.thenComparing(o -> Integer.valueOf(o.getVariables().get(PLOT_NO).getValue()))
				.thenComparing(o -> Integer.valueOf(o.getVariables().get(ENTRY_NO).getValue())));

		// Data to be exported
		final List<Map<Integer, String>> results = new LinkedList<>();

		for (final ObservationUnitRow observationUnitRow : observationUnitRows) {
			final Map<Integer, String> row = new HashMap<>();
			for (final Integer requiredField : allRequiredKeys) {
				final Field field = termIdFieldMap.get(requiredField);
				if (!STATIC_FIELD_IDS.contains(field.getId())) {
					// Special cases: LOCATION_NAME, PLOT OBS_UNIT_ID, CROP_SEASON_CODE
					final Integer termId = requiredField;
					if (TermId.getById(termId).equals(TermId.LOCATION_ID)) {
						row.put(requiredField, observationUnitRow.getVariables().get(LOCATION_ID).getValue());
						continue;
					}
					if (TermId.getById(termId).equals(TermId.OBS_UNIT_ID)) {
						row.put(requiredField, observationUnitRow.getVariables().get(OBS_UNIT_ID).getValue());
						continue;
					}
					if (TermId.getById(termId).equals(TermId.SEASON_VAR)) {
						final ObservationUnitData observationUnitData =
							observationUnitRow.getEnvironmentVariables().get("Crop_season_Code");
						row.put(requiredField, this.getSeason(observationUnitData != null ? observationUnitData.getValue() : null));
						continue;
					}
					if (observationUnitRow.getVariables().containsKey(field.getName())) {
						row.put(requiredField, observationUnitRow.getVariables().get(field.getName()).getValue());
					}
					if (observationUnitRow.getEnvironmentVariables().containsKey(field.getName())) {
						row.put(requiredField, observationUnitRow.getEnvironmentVariables().get(field.getName()).getValue());
					}

				} else {
					if (STATIC_LOT_FIELD_IDS.contains(field.getId()) || STATIC_TRANSACTION_FIELD_IDS.contains(field.getId())) {
						final StudyTransactionsDto studyTransactionsDto = observationUnitDtoTransactionDtoMap.get(observationUnitRow.getObsUnitId());

						if (studyTransactionsDto == null) {
							continue;
						}

						if (STATIC_LOT_FIELD_IDS.contains(requiredField)) {
							if(LabelPrintingStaticField.LOT_UID.getFieldId().equals(requiredField)){
								row.put(requiredField, studyTransactionsDto.getLot().getLotUUID());
								continue;

							}
							if(LabelPrintingStaticField.LOT_ID.getFieldId().equals(requiredField)){
								row.put(requiredField, Objects.toString(studyTransactionsDto.getLot().getLotId(), ""));
								continue;

							}
							if(LabelPrintingStaticField.STOCK_ID.getFieldId().equals(requiredField)){
								row.put(requiredField, studyTransactionsDto.getLot().getStockId());
								continue;

							}
							if(LabelPrintingStaticField.STORAGE_LOCATION_ABBR.getFieldId().equals(requiredField)){
								row.put(requiredField, studyTransactionsDto.getLot().getLocationAbbr());
								continue;

							}
							if(LabelPrintingStaticField.STORAGE_LOCATION.getFieldId().equals(requiredField)){
								row.put(requiredField, studyTransactionsDto.getLot().getLocationName());
								continue;

							}
							if(LabelPrintingStaticField.UNITS.getFieldId().equals(requiredField)){
								row.put(requiredField, studyTransactionsDto.getLot().getUnitName());
								continue;

							}
							if(LabelPrintingStaticField.AVAILABLE_BALANCE.getFieldId().equals(requiredField)){
								row.put(requiredField, Objects.toString(studyTransactionsDto.getLot().getAvailableBalance(), ""));
								continue;

							}
							if(LabelPrintingStaticField.NOTES.getFieldId().equals(requiredField)){
								row.put(requiredField, studyTransactionsDto.getLot().getNotes());
								continue;

							}

						} else if (STATIC_TRANSACTION_FIELD_IDS.contains(requiredField)) {
							if(LabelPrintingStaticField.TRN_ID.getFieldId().equals(requiredField)){
								row.put(requiredField, studyTransactionsDto.getTransactionId().toString());
								continue;

							}
							if(LabelPrintingStaticField.STATUS.getFieldId().equals(requiredField)){
								row.put(requiredField, Objects.toString(studyTransactionsDto.getTransactionStatus(), ""));
								continue;

							}
							if(LabelPrintingStaticField.TYPE.getFieldId().equals(requiredField)){
								row.put(requiredField, Objects.toString(studyTransactionsDto.getTransactionType(), ""));
								continue;

							}
							if(LabelPrintingStaticField.CREATED.getFieldId().equals(requiredField)){
								row.put(requiredField, studyTransactionsDto.getCreatedDate().toString());
								continue;

							}
							if(LabelPrintingStaticField.TRN_NOTES.getFieldId().equals(requiredField)){
								row.put(requiredField, studyTransactionsDto.getNotes());
								continue;

							}
							if(LabelPrintingStaticField.USERNAME.getFieldId().equals(requiredField)){
								row.put(requiredField, studyTransactionsDto.getCreatedByUsername());
								continue;

							}
						}
					}

					// If it is not a number it is a hardcoded field
					// Year, Study Name, Parentage, ObsDatasetUnitIdFieldKey
					if (requiredField.equals(YEAR_FIELD.getId())) {
						row.put(
							requiredField,
							(StringUtils.isNotEmpty(study.getStartDate())) ? study.getStartDate().substring(0, 4) : StringUtils.EMPTY);
						continue;
					}
					if (requiredField.equals(STUDY_NAME_FIELD.getId())) {
						row.put(requiredField, study.getStudyName());
						continue;
					}
					if (requiredField.equals(PARENTAGE_FIELD.getId())) {
						final String gid = observationUnitRow.getVariables().get(GID).getValue();
						row.put(requiredField, this.getPedigree(gid, gidPedigreeMap));
						continue;
					}
					if (requiredField.equals(ObsDatasetUnitIdFieldKey)) {
						row.put(ObsDatasetUnitIdFieldKey, observationUnitRow.getVariables().get(OBS_UNIT_ID).getValue());
						continue;
					}
				}
			}
			results.add(row);
		}

		return new LabelsData(ObsDatasetUnitIdFieldKey, results);
	}

	@Override
	public List<FileType> getSupportedFileTypes() {
		return SUPPORTED_FILE_TYPES;
	}

	List<Field> transform(final List<MeasurementVariable> measurementVariables) {
		final List<Field> fields = new LinkedList<>();
		for (final MeasurementVariable measurementVariable : measurementVariables) {
			final Field field = new Field(measurementVariable);
			//Requirement to show PLOT OBS_UNIT_ID label when variable = OBS_UNIT_ID in Plot Dataset
			//Which is in fact the only dataset that cointains this variable.
			if (field.getId() == TermId.OBS_UNIT_ID.getId()) {
				field.setName(PLOT.concat(" ").concat(field.getName()));
			}
			if (field.getId() == TermId.SEASON_VAR.getId()) {
				field.setName(this.getMessage("label.printing.field.season"));
			}
			fields.add(field);
		}
		return fields;
	}

	private String getDefaultFileName(final StudyDetails studyDetails, final DatasetDTO datasetDTO) {

		final String fileName = "Labels-for-".concat(studyDetails.getStudyName()).concat("-").concat(datasetDTO.getName())
			.concat("-").concat(DateUtil.getCurrentDateAsStringValue());
		return FileUtils.cleanFileName(fileName);
	}

	String getMessage(final String code) {
		return this.messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
	}

	private String getSeason(final String seasonStr) {
		final String value;
		if (seasonStr != null && Integer.parseInt(seasonStr.trim()) == TermId.SEASON_DRY.getId()) {
			value = Season.DRY.getLabel().toUpperCase();
		} else if (seasonStr != null && Integer.parseInt(seasonStr.trim()) == TermId.SEASON_WET.getId()) {
			value = Season.WET.getLabel().toUpperCase();
		} else {
			value = Season.GENERAL.getLabel().toUpperCase();
		}
		return value;
	}

	void removePairIdVariables(final List<LabelType> labelTypes) {
		for (final LabelType labelType : labelTypes) {
			final Iterator<Field> fieldIterator = labelType.getFields().iterator();
			while (fieldIterator.hasNext()) {
				if (PAIR_ID_VARIABLES.contains(fieldIterator.next().getId())) {
					fieldIterator.remove();
				}
			}
		}
	}

	void setMessageSource(final ResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}
}

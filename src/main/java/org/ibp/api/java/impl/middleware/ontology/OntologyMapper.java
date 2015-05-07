package org.ibp.api.java.impl.middleware.ontology;

import org.generationcp.middleware.domain.oms.OntologyVariableSummary;
import org.generationcp.middleware.domain.ontology.Method;
import org.generationcp.middleware.domain.ontology.Property;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.Variable;
import org.ibp.api.domain.ontology.*;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.convention.MatchingStrategies;

import java.util.ArrayList;

public class OntologyMapper {

	private static ModelMapper SINGLETON = null;

	/**
	 * We do not want public constructor of this class as all methods are static
	 */
	private OntologyMapper() {

	}

	/**
	 * Eager Initialization of ModelMapper Instance Used when Simple Class to
	 * Class Mapping is Required without Custom Mapping
	 *
	 * @return ModelMapper Instance
	 */
	public static ModelMapper getInstance() {

		if (SINGLETON == null) {
			// Thread Safe. Might be costly operation in some case
			synchronized (OntologyMapper.class) {
				if (SINGLETON == null) {
					SINGLETON = new ModelMapper();
					applyMapperConfiguration(SINGLETON);
					addMethodMappers(SINGLETON);
					addPropertyMappers(SINGLETON);
					addScaleMappers(SINGLETON);
					addVariableMappers(SINGLETON);
				}
			}
		}

		return SINGLETON;
	}

	private static void applyMapperConfiguration(ModelMapper mapper) {
		mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
	}

	/**
	 * Get ModelMapper instance and add Method related mapping to it
	 */
	private static void addMethodMappers(ModelMapper mapper) {

		mapper.addMappings(new PropertyMap<Method, MethodSummary>() {
			@Override
			protected void configure() {
				this.map().setId(String.valueOf(this.source.getId()));
				this.map().setName(this.source.getName());
				this.map().setDescription(this.source.getDefinition());
				this.map().setDateCreated(this.source.getDateCreated());
				this.map().setDateLastModified(this.source.getDateLastModified());
			}
		});

		mapper.addMappings(new PropertyMap<Method, MethodDetails>() {
			@Override
			protected void configure() {
				this.map().setId(String.valueOf(this.source.getId()));
				this.map().setName(this.source.getName());
				this.map().setDescription(this.source.getDefinition());
				this.map().setEditableFields(new ArrayList<String>());
				this.map().setDeletable(false);
			}
		});
	}

	/**
	 * Get ModelMapper instance and add Property related mapping to it
	 */
	public static void addPropertyMappers(ModelMapper mapper) {

		mapper.addMappings(new PropertyMap<Property, PropertySummary>() {
			@Override
			protected void configure() {
				this.map().setId(String.valueOf(this.source.getId()));
				this.map().setName(this.source.getName());
				this.map().setDescription(this.source.getDefinition());
				this.map().setCropOntologyId(this.source.getCropOntologyId());
				this.map().setClasses(this.source.getClasses());
				this.map().setDateCreated(this.source.getDateCreated());
				this.map().setDateLastModified(this.source.getDateLastModified());
			}
		});

		mapper.addMappings(new PropertyMap<Property, PropertyResponse>() {
			@Override
			protected void configure() {
				this.map().setId(String.valueOf(this.source.getId()));
				this.map().setName(this.source.getName());
				this.map().setDescription(this.source.getDefinition());
				this.map().setCropOntologyId(this.source.getCropOntologyId());
				this.map().setClasses(this.source.getClasses());
				this.map().setEditableFields(new ArrayList<String>());
				this.map().setDeletable(false);
			}
		});
	}

	/**
	 *  Get ModelMapper instance and add Scale related mapping to it
	 */
	public static void addScaleMappers(ModelMapper mapper) {

		mapper.addMappings(new PropertyMap<Scale, ScaleSummary>() {
			@Override
			protected void configure() {
				this.map().setId(String.valueOf(this.source.getId()));
				this.map().setName(this.source.getName());
				this.map().setDescription(this.source.getDefinition());
				this.map().setMinValue(this.source.getMinValue());
				this.map().setMaxValue(this.source.getMaxValue());
				this.map().setCategories(this.source.getCategories());
				this.map().setDateCreated(this.source.getDateCreated());
				this.map().setDateLastModified(this.source.getDateLastModified());
			}
		});

		mapper.addMappings(new PropertyMap<Scale, ScaleResponse>() {
			@Override
			protected void configure() {
				this.map().setId(String.valueOf(this.source.getId()));
				this.map().setName(this.source.getName());
				this.map().setDescription(this.source.getDefinition());
				this.map().setMinValue(this.source.getMinValue());
				this.map().setMaxValue(this.source.getMaxValue());
				this.map().setCategories(this.source.getCategories());
				this.map().setEditableFields(new ArrayList<String>());
				this.map().setDeletable(false);
			}
		});
	}

	/**
	 * Get ModelMapper instance and add Property related mapping to it
	 */
	public static void addVariableMappers(ModelMapper mapper) {

		mapper.addMappings(new PropertyMap<OntologyVariableSummary, VariableSummary>() {
			@Override
			protected void configure() {
				this.map().setId(String.valueOf(this.source.getId()));
				this.map().setName(this.source.getName());
				this.map().setDescription(this.source.getDescription());
				this.map().setFavourite(this.source.getIsFavorite());
				this.map().setAlias(this.source.getAlias());
				this.map().setVariableTypes(this.source.getVariableTypes());
				this.map().setDateCreated(this.source.getDateCreated());
				this.map().setDateLastModified(this.source.getDateLastModified());
				this.map().setExpectedMin(this.source.getMinValue());
				this.map().setExpectedMax(this.source.getMaxValue());
			}
		});

		mapper.addMappings(new PropertyMap<Variable, VariableResponse>() {
			@Override
			protected void configure() {
				this.map().setName(this.source.getName());
				this.map().setDescription(this.source.getDefinition());
				this.map().setFavourite(this.source.getIsFavorite());
				this.map().setAlias(this.source.getAlias());
				this.map().setVariableTypes(this.source.getVariableTypes());
				this.map().setObservations(this.source.getObservations());
				this.map().setExpectedMin(this.source.getMinValue());
				this.map().setExpectedMax(this.source.getMaxValue());
				this.map().setEditableFields(new ArrayList<String>());
				this.map().setDeletable(false);
			}
		});
	}

}

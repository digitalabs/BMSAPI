
package org.ibp.api.java.impl.middleware.ontology;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.generationcp.middleware.util.Util;
import org.ibp.api.domain.ontology.DataType;
import org.ibp.api.domain.ontology.VariableType;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.ontology.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;

@Service
public class ModelServiceImpl implements ModelService {

	@Autowired
	private TermDataManager termDataManager;

	@Override
	public List<DataType> getAllDataTypes() {
		return Util.convertAll(Arrays.asList(org.generationcp.middleware.domain.oms.DataType.values()),
				new Function<org.generationcp.middleware.domain.oms.DataType, DataType>() {

					@Override
					public DataType apply(org.generationcp.middleware.domain.oms.DataType dataType) {
						return new DataType(dataType.getId(), dataType.getName());
					}
				});
	}

	@Override
	public List<String> getAllClasses() {
		try {
			List<Term> classes = this.termDataManager.getTermByCvId(CvId.TRAIT_CLASS.getId());
			List<String> classList = new ArrayList<>();

			for (Term term : classes) {
				classList.add(term.getName());
			}

			Comparator<String> ALPHABETICAL_ORDER = new Comparator<String>() {

				@Override
				public int compare(String str1, String str2) {
					int res = String.CASE_INSENSITIVE_ORDER.compare(str1, str2);
					if (res == 0) {
						res = str1.compareTo(str2);
					}
					return res;
				}
			};

			Collections.sort(classList, ALPHABETICAL_ORDER);

			return classList;
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

	@Override
	public List<VariableType> getAllVariableTypes() {

		List<VariableType> variableTypes =
				Util.convertAll(Arrays.asList(org.generationcp.middleware.domain.oms.VariableType.values()),
						new Function<org.generationcp.middleware.domain.oms.VariableType, VariableType>() {

							@Override
							public VariableType apply(org.generationcp.middleware.domain.oms.VariableType variableType) {
								return new VariableType(variableType.getId(), variableType.getName(), variableType.getDescription());
							}
						});

		Collections.sort(variableTypes, new Comparator<VariableType>() {

			@Override
			public int compare(VariableType o1, VariableType o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		return variableTypes;
	}
}

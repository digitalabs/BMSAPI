package org.ibp.api.java.ontology;

import org.generationcp.middleware.domain.ontology.FormulaDto;

public interface FormulaService {

	FormulaDto save(FormulaDto formulaDto);

	void delete(Integer formulaId);
}
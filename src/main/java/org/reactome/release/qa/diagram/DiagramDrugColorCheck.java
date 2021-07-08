package org.reactome.release.qa.diagram;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.gk.model.GKInstance;
import org.gk.model.InstanceUtilities;
import org.gk.model.ReactomeJavaConstants;
import org.gk.persistence.DiagramGKBReader;
import org.gk.render.DefaultRenderConstants;
import org.gk.render.Node;
import org.gk.render.Renderable;
import org.gk.render.RenderableChemicalDrug;
import org.gk.render.RenderableComplex;
import org.gk.render.RenderableEntitySet;
import org.gk.render.RenderablePathway;
import org.gk.render.RenderableProteinDrug;
import org.gk.render.RenderableRNADrug;
import org.reactome.release.qa.annotations.DiagramQACheck;
import org.reactome.release.qa.common.QACheckerHelper;
import org.reactome.release.qa.common.QAReport;

/**
 * QA check to flag any oddities in drug coloring.
 */
@DiagramQACheck
public class DiagramDrugColorCheck extends AbstractDiagramQACheck {

    public DiagramDrugColorCheck() {
    }

    @Override
    public QAReport executeQACheck() throws Exception {
        QAReport report = new QAReport();
        Collection<GKInstance> pathwayDiagrams = getPathwayDiagrams();
        DiagramGKBReader reader = new DiagramGKBReader();
        for (GKInstance diagram : pathwayDiagrams) {
            checkPathwayDiagram(diagram, reader, report);
        }
        report.setColumnHeaders("PathwayDiagram_DBID",
                                "Pathway_DisplayName",
                                "Pathway_DBID",
                                "Renderable_DisplayName",
                                "Renderable_DBID",
                                "MostRecentAuthor");
        return report;
    }

    private void checkPathwayDiagram(GKInstance diagram, DiagramGKBReader reader, QAReport report) throws Exception {
        RenderablePathway pathway = reader.openDiagram(diagram);
        GKInstance pathwayInst = (GKInstance) diagram.getAttributeValue(ReactomeJavaConstants.representedPathway);
        String modDate = QACheckerHelper.getLastModificationAuthor(diagram);
        List<Renderable> components = pathway.getComponents();
        if (components == null || components.size() == 0)
            return;
        // For all rendered components.
        for (Renderable component : components) {
            // Check correctness of drug background.
            List<Class<? extends Node>> classesToCheck = Arrays.asList(RenderableEntitySet.class,
                                                                       RenderableComplex.class,
                                                                       RenderableChemicalDrug.class,
                                                                       RenderableProteinDrug.class,
                                                                       RenderableRNADrug.class);
            Class<Renderable> cls = (Class<Renderable>) component.getClass();
            if (classesToCheck.contains(cls) && !hasCorrectColors(component)) {
                // Add line to report.
                report.addLine(diagram.getDBID().toString(),
                               pathway.getDisplayName(),
                               pathwayInst.getDBID().toString(),
                               component.getDisplayName(),
                               component.getReactomeId().toString(),
                               modDate);
            }
        }
    }

    /**
     * Check if a component has the appropriate background color for its drug status.
     *
     * Returns true is color matches the status.
     * Returns false if there is a mismatch error.
     *
     * @param component
     * @return boolean
     * @throws Exception
     */
    private boolean hasCorrectColors(Renderable component) throws Exception {
        GKInstance instance = dba.fetchInstance(component.getReactomeId());
        if (component.getReactomeId() == null || instance == null)
            return true;

        // If instance is not a drug nor has a drug, skip further color tests.
        if (!instance.getSchemClass().isa(ReactomeJavaConstants.Drug) ||
            !InstanceUtilities.hasDrug(instance))
            return true;

        // Drug background and foreground check.
        Color background = component.getBackgroundColor();
        Color foreground = component.getForegroundColor();

        // If background or foreground is null, default values will be used.
        if (background == null || foreground == null)
            return false;

        boolean correctBackground = background.equals(DefaultRenderConstants.DEFAULT_DRUG_BACKGROUND);
        boolean correctForeground = foreground.equals(DefaultRenderConstants.DEFAULT_DRUG_FOREGROUND);

        return correctBackground && correctForeground;
    }

}

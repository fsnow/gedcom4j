package com.mattharrah.gedcom4j.validate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.mattharrah.gedcom4j.Gedcom;
import com.mattharrah.gedcom4j.Individual;
import com.mattharrah.gedcom4j.Note;
import com.mattharrah.gedcom4j.Repository;
import com.mattharrah.gedcom4j.Source;
import com.mattharrah.gedcom4j.Submission;

/**
 * <p>
 * A class to validate the contents of a {@link Gedcom} structure.
 * </p>
 * <p>
 * General usage is as follows:
 * <ol>
 * <li>Instantiate a {@link GedcomValidator}, passing the {@link Gedcom}
 * structure to be validated as the argument to the constructor</li>
 * <li>If desired, turn off automatic repairs during validation by setting
 * {@link GedcomValidator#autorepair} to <tt>false</tt>.
 * <li>Call the {@link GedcomValidator#validate()} method.</li>
 * <li>Inspect the {@link GedcomValidator#findings} list, which contains
 * {@link GedcomValidationFinding} objects describing the problems that were
 * found.</li>
 * </ol>
 * </p>
 * 
 * @author frizbog1
 */
public class GedcomValidator extends AbstractValidator {

    /**
     * Will the most simple, obvious, non-destructive errors be automatically
     * fixed? This includes things like creating empty collections where one is
     * expected but only a null reference exists.
     */
    public boolean autorepair = true;

    /**
     * The findings from validation
     */
    public List<GedcomValidationFinding> findings = new ArrayList<GedcomValidationFinding>();

    /**
     * The gedcom structure being validated
     */
    protected Gedcom gedcom = null;

    /**
     * Constructor
     * 
     * @param gedcom
     *            the gedcom structure being validated
     */
    public GedcomValidator(Gedcom gedcom) {
        this.gedcom = gedcom;
        rootValidator = this;
    }

    /**
     * Validate the gedcom file
     */
    @Override
    public void validate() {
        if (gedcom == null) {
            addError("gedcom structure is null");
            return;
        }
        // TODO - validate header
        validateIndividuals();
        // TODO - validate families
        validateRepositories();
        // TODO - validate media
        validateSources();
        // TODO - validate submitters
        // TODO - validate trailer
        validateSubmission();
        checkNotes(new ArrayList<Note>(gedcom.notes.values()), gedcom);
    }

    /**
     * Validate the {@link Gedcom#individuals} collection
     */
    void validateIndividuals() {
        if (gedcom.individuals == null) {
            if (autorepair) {
                gedcom.individuals = new HashMap<String, Individual>();
                addInfo("Individuals collection was null - autorepaired",
                        gedcom);
            } else {
                addError("Individuals collection is null", gedcom);
                return;
            }
        }
        for (Entry<String, Individual> e : gedcom.individuals.entrySet()) {
            if (e.getKey() == null) {
                addError("Entry in individuals collection has null key", e);
                return;
            }
            if (e.getValue() == null) {
                addError("Entry in individuals collection has null value", e);
                return;
            }
            if (!e.getKey().equals(e.getValue().xref)) {
                addError(
                        "Entry in individuals collection is not keyed by the individual's xref",
                        e);
                return;
            }
            new IndividualValidator(this, e.getValue()).validate();
        }
    }

    /**
     * Validate the repositories collection
     */
    private void validateRepositories() {
        if (gedcom.repositories == null) {
            if (autorepair) {
                gedcom.repositories = new HashMap<String, Repository>();
                addInfo("Repositories collection on root gedcom was null - autorepaired",
                        gedcom);
                return;
            }
            addError("Repositories collection on root gedcom is null", gedcom);
            return;
        }
        for (Entry<String, Repository> e : gedcom.repositories.entrySet()) {
            if (e.getKey() == null) {
                addError("Entry in repositories collection has null key", e);
                return;
            }
            if (e.getValue() == null) {
                addError("Entry in repositories collection has null value", e);
                return;
            }
            if (!e.getKey().equals(e.getValue().xref)) {
                addError(
                        "Entry in repositories collection is not keyed by the Repository's xref",
                        e);
                return;
            }
            new RepositoryValidator(this, e.getValue()).validate();
        }

    }

    /**
     * Validate the {@link Gedcom#sources} collection
     */
    private void validateSources() {
        if (gedcom.sources == null) {
            if (autorepair) {
                gedcom.sources = new HashMap<String, Source>();
                addInfo("Sources collection was null - autorepaired", gedcom);
            } else {
                addError("Sources collection is null", gedcom);
                return;
            }
        }
        for (Entry<String, Source> e : gedcom.sources.entrySet()) {
            if (e.getKey() == null) {
                addError("Entry in sources collection has null key", e);
                return;
            }
            if (e.getValue() == null) {
                addError("Entry in sources collection has null value", e);
                return;
            }
            if (!e.getKey().equals(e.getValue().xref)) {
                addError(
                        "Entry in sources collection is not keyed by the individual's xref",
                        e);
                return;
            }
            new SourceValidator(this, e.getValue()).validate();
        }
    }

    /**
     * Validate the submission substructure under the root gedcom
     */
    private void validateSubmission() {
        Submission s = gedcom.submission;
        if (s == null) {
            addError("Submission record on root gedcom is null", gedcom);
            return;
        }
        checkXref(s);
        checkOptionalString(s.ancestorsCount, "Ancestor count", s);
        checkOptionalString(s.descendantsCount, "Descendant count", s);
        checkOptionalString(s.nameOfFamilyFile, "Name of family file", s);
        checkOptionalString(s.ordinanceProcessFlag, "Ordinance process flag", s);
        checkOptionalString(s.recIdNumber, "Automated record id", s);
        checkOptionalString(s.templeCode, "Temple code", s);
    }

}

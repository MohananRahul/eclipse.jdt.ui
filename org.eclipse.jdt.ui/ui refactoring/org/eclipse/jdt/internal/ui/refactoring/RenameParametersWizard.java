/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

package org.eclipse.jdt.internal.ui.refactoring;import org.eclipse.jdt.core.JavaModelException;import org.eclipse.jdt.core.refactoring.RefactoringStatus;import org.eclipse.jdt.internal.ui.refactoring.RefactoringWizard;import org.eclipse.jdt.internal.ui.refactoring.RenameInputWizardPage;import org.eclipse.jdt.internal.ui.refactoring.TextInputWizardPage;import org.eclipse.jdt.core.refactoring.methods.RenameParametersRefactoring;

public class RenameParametersWizard extends RefactoringWizard {
	
	private static final String RESOURCE_KEY_PREFIX= "Refactoring.RenameParameters";
	private static final String INPUTPAGE_TITLE_SUFFIX= ".wizard.inputpage.title";
	private static final String INPUTPAGE_MESSAGE_SUFFIX= ".wizard.inputpage.message";
	
	public RenameParametersWizard(){
		super(getTitle());
	}

	/**
	 * @see RefactoringWizard#addUserInputPages
	 */ 
	protected void addUserInputPages(){
		RenameParametersWizardPage page= new RenameParametersWizardPage(true);
		page.setMessage(getMessage());
		addPage(page);
	}
	
	/**
	 * @see RefactoringWizard#addPreviewPage
	 */ 
	protected void addPreviewPage(){
		PreviewWizardPage page= new PreviewWizardPage();
		page.setExpandFirstNode(true);
		addPage(page);
	}
	
	private static String getTitle(){
		return RefactoringResources.getResourceString(RESOURCE_KEY_PREFIX + INPUTPAGE_TITLE_SUFFIX);
	}

	private static String getMessage(){
		return RefactoringResources.getResourceString(RESOURCE_KEY_PREFIX + INPUTPAGE_MESSAGE_SUFFIX);
	}
	
}
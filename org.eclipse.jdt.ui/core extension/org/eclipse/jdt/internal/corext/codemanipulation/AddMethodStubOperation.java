/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.corext.codemanipulation;

import java.util.ArrayList;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;

import org.eclipse.jdt.internal.corext.util.JavaModelUtil;

/**
 * Add method stubs to a type (the parent type)
 * Methods are added without checking if they already exist (will result in duplicated methods)
 * If the parent type is open in an editor, be sure to pass over its working copy.
 */
public class AddMethodStubOperation implements IWorkspaceRunnable {
		
	private IType fType;
	private IMethod[] fMethods;
	private IMethod[] fCreatedMethods;
	private boolean fDoSave;
		
	private IRequestQuery fOverrideQuery;
	private IRequestQuery fReplaceQuery;
	
	private boolean fOverrideAll;
	private boolean fReplaceAll;
	
	private CodeGenerationSettings fSettings;
	
	public AddMethodStubOperation(IType type, IMethod[] methods, CodeGenerationSettings settings, IRequestQuery overrideQuery, IRequestQuery replaceQuery, boolean save) {
		super();
		fType= type;
		fMethods= methods;
		fCreatedMethods= null;
		fDoSave= save;
		fOverrideQuery= overrideQuery;
		fReplaceQuery= replaceQuery;
		fSettings= settings;
	}
	
	private boolean queryOverrideFinalMethods(IMethod inheritedMethod) throws OperationCanceledException {
		if (!fOverrideAll) {
			switch (fOverrideQuery.doQuery(inheritedMethod)) {
				case IRequestQuery.CANCEL:
					throw new OperationCanceledException();
				case IRequestQuery.NO:
					return false;
				case IRequestQuery.YES_ALL:
					fOverrideAll= true;
			}
		}
		return true;
	}
	
	private boolean queryReplaceMethods(IMethod method) throws OperationCanceledException {
		if (!fReplaceAll) {
			switch (fReplaceQuery.doQuery(method)) {
				case IRequestQuery.CANCEL:
					throw new OperationCanceledException();
				case IRequestQuery.NO:
					return false;
				case IRequestQuery.YES_ALL:
					fReplaceAll= true;
			}
		}
		return true;
	}	

	/**
	 * Runs the operation.
	 * @throws OperationCanceledException Runtime error thrown when operation is cancelled.
	 */	
	public void run(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
		try {
			if (monitor == null) {
				monitor= new NullProgressMonitor();
			}			
			
			monitor.beginTask(CodeGenerationMessages.getString("AddMethodStubOperation.description"), fMethods.length + 2); //$NON-NLS-1$

			fOverrideAll= (fOverrideQuery == null);
			fReplaceAll= (fReplaceQuery == null);

			IMethod[] existingMethods= fType.getMethods();
			
			ArrayList createdMethods= new ArrayList();
			
			ImportsStructure imports= new ImportsStructure(fType.getCompilationUnit(), fSettings.importOrder, fSettings.importThreshold, true);
			
			String lineDelim= StubUtility.getLineDelimiterUsed(fType);
			int indent= StubUtility.getIndentUsed(fType) + 1;
			
			StubUtility.GenStubSettings genStubSetting= new StubUtility.GenStubSettings(fSettings);
			
			ITypeHierarchy typeHierarchy= fType.newSupertypeHierarchy(new SubProgressMonitor(monitor, 1));
			
			for (int i= 0; i < fMethods.length; i++) {
				try {
					String content;
					IMethod curr= fMethods[i];
					for (int k= 0; k < createdMethods.size(); k++) {
						IMethod meth= (IMethod) createdMethods.get(k);
						if (JavaModelUtil.isSameMethodSignature(meth.getElementName(), meth.getParameterTypes(), meth.isConstructor(), curr)) {
							// ignore duplicated methods
							continue;
						}
					}			 	
					
					IMethod overwrittenMethod= JavaModelUtil.findMethodImplementationInHierarchy(typeHierarchy, fType, curr.getElementName(), curr.getParameterTypes(), curr.isConstructor());
					if (overwrittenMethod == null) {
						// create method without super call, no overwrite
						genStubSetting.callSuper= false;
						genStubSetting.methodOverwrites= false;
						content= StubUtility.genStub(fType.getElementName(), curr, genStubSetting, imports);
					} else {
						int flags= overwrittenMethod.getFlags();
						if (Flags.isFinal(flags) || Flags.isPrivate(flags)) {
							// ask before overwriting final methods
							if (!queryOverrideFinalMethods(overwrittenMethod)) {
								continue;
							}
						}
						genStubSetting.callSuper= !Flags.isAbstract(overwrittenMethod.getFlags());
						genStubSetting.methodOverwrites= true;
					
						IMethod declaration= JavaModelUtil.findMethodDeclarationInHierarchy(typeHierarchy, fType, curr.getElementName(), curr.getParameterTypes(), curr.isConstructor());
						content= StubUtility.genStub(fType.getElementName(), declaration, genStubSetting, imports);	
					}
					IJavaElement sibling= null;
					IMethod existing= JavaModelUtil.findMethod(curr.getElementName(), curr.getParameterTypes(), curr.isConstructor(), existingMethods);
					if (existing != null) {
						// ask before replacing a method
						if (!queryReplaceMethods(existing)) {
							continue;
						}	
						sibling= StubUtility.findNextSibling(existing);
						existing.delete(false, null);
					} else if (curr.isConstructor() && existingMethods.length > 0) {
						// add constructors at the beginning
						sibling= existingMethods[0];
					}
						
					String formattedContent= StubUtility.codeFormat(content, indent, lineDelim) + lineDelim;				
					IMethod newMethod= fType.createMethod(formattedContent, sibling, true, null);
					createdMethods.add(newMethod);
				} finally {
					monitor.worked(1);
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
				}
			}
			
			int nCreated= createdMethods.size();
			if (nCreated > 0) {
				imports.create(fDoSave, null);
				monitor.worked(1);
				fCreatedMethods= (IMethod[]) createdMethods.toArray(new IMethod[nCreated]);
			}
		} finally {
			monitor.done();
		}		
	}
	
	
	public IMethod[] getCreatedMethods() {
		return fCreatedMethods;
	}
	
		
}

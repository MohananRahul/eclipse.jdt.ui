/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
package org.eclipse.jdt.refactoring.tests.infra;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.jdt.internal.ui.util.IDocumentManager;

public class FileDocumentManager implements IDocumentManager{

	private ICompilationUnit fCU;
	private IDocument fDocument;
	
	public FileDocumentManager(ICompilationUnit cu) throws JavaModelException {
		fCU= cu;
	}
	
	public void save(IProgressMonitor pm) throws CoreException {
		if (fDocument == null)
			return;
		String newSource= fDocument.get();
		try{
		IPackageFragment pack= (IPackageFragment)fCU.getParent();
		String name= fCU.getElementName();
		fCU.delete(true, pm);
		pack.createCompilationUnit(name, newSource, true, pm);
		} catch (JavaModelException e){
			e.printStackTrace();
		}
	}

	public void connect(){
		if (fDocument != null)
			return;
		try{
			fDocument= new Document(fCU.getSource());
		} catch (JavaModelException e){
			e.printStackTrace();
		}	
	}
	
	public void disconnect() {
		fDocument= null;
	}

	public IDocument getDocument() {
		return fDocument;
	}
	
	public void changed(){
		//??
	}

	public void aboutToChange(){
		//??
	}
}
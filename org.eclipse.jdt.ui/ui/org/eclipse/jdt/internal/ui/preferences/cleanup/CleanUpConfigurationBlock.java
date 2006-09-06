/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Aaron Luchko, aluchko@redhat.com - 105926 [Formatter] Exporting Unnamed profile fails silently
 *******************************************************************************/
package org.eclipse.jdt.internal.ui.preferences.cleanup;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.preferences.IScopeContext;

import org.eclipse.core.resources.IProject;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jdt.ui.JavaUI;

import org.eclipse.jdt.internal.ui.fix.CodeFormatCleanUp;
import org.eclipse.jdt.internal.ui.fix.CodeStyleCleanUp;
import org.eclipse.jdt.internal.ui.fix.CommentFormatCleanUp;
import org.eclipse.jdt.internal.ui.fix.ControlStatementsCleanUp;
import org.eclipse.jdt.internal.ui.fix.ExpressionsCleanUp;
import org.eclipse.jdt.internal.ui.fix.ICleanUp;
import org.eclipse.jdt.internal.ui.fix.Java50CleanUp;
import org.eclipse.jdt.internal.ui.fix.PotentialProgrammingProblemsCleanUp;
import org.eclipse.jdt.internal.ui.fix.StringCleanUp;
import org.eclipse.jdt.internal.ui.fix.UnnecessaryCodeCleanUp;
import org.eclipse.jdt.internal.ui.fix.UnusedCodeCleanUp;
import org.eclipse.jdt.internal.ui.fix.VariableDeclarationCleanUp;
import org.eclipse.jdt.internal.ui.preferences.PreferencesAccess;
import org.eclipse.jdt.internal.ui.preferences.formatter.IProfileVersioner;
import org.eclipse.jdt.internal.ui.preferences.formatter.JavaPreview;
import org.eclipse.jdt.internal.ui.preferences.formatter.ProfileConfigurationBlock;
import org.eclipse.jdt.internal.ui.preferences.formatter.ProfileManager;
import org.eclipse.jdt.internal.ui.preferences.formatter.ModifyDialog;
import org.eclipse.jdt.internal.ui.preferences.formatter.ProfileStore;
import org.eclipse.jdt.internal.ui.preferences.formatter.ProfileManager.Profile;


/**
 * The clean up configuration block for the clean up preference page. 
 */
public class CleanUpConfigurationBlock extends ProfileConfigurationBlock {
	
	private static final String CLEANUP_PAGE_SETTINGS_KEY= "cleanup_page"; //$NON-NLS-1$

	private static final String CLEANUP_PROFILES_PREFERENCE_KEY= "org.eclipse.jdt.ui.cleanupprofiles"; //$NON-NLS-1$
    
	private static final String DIALOGSTORE_LASTSAVELOADPATH= JavaUI.ID_PLUGIN + ".cleanup"; //$NON-NLS-1$
    
    public CleanUpConfigurationBlock(IProject project, PreferencesAccess access) {
	    super(project, access, DIALOGSTORE_LASTSAVELOADPATH);
    }

	protected IProfileVersioner createProfileVersioner() {
	    return new CleanUpProfileVersioner();
    }
	
	protected ProfileStore createProfileStore(IProfileVersioner versioner) {
	    return new ProfileStore(CLEANUP_PROFILES_PREFERENCE_KEY, versioner);
    }
	
	protected ProfileManager createProfileManager(List profiles, IScopeContext context, PreferencesAccess access, IProfileVersioner profileVersioner) {
	    return new CleanUpProfileManager(profiles, context, access, profileVersioner);
    }

	protected JavaPreview createJavaPreview(Composite composite, int numColumns, Profile profile) {
		Map settings= profile.getSettings();
		final Map sharedSettings= new Hashtable();
		fill(settings, sharedSettings);
		
		ICleanUp[] cleanUps1= {
        		new ControlStatementsCleanUp(sharedSettings),
        		new ExpressionsCleanUp(sharedSettings),
        		new VariableDeclarationCleanUp(sharedSettings),
        		new CodeStyleCleanUp(sharedSettings),
        		new UnusedCodeCleanUp(sharedSettings),
        		new UnnecessaryCodeCleanUp(sharedSettings),
        		new StringCleanUp(sharedSettings),
        		new Java50CleanUp(sharedSettings),
        		new PotentialProgrammingProblemsCleanUp(sharedSettings),
        		new CodeFormatCleanUp(sharedSettings),
        		new CommentFormatCleanUp(sharedSettings)
        };
		
		ICleanUp[] cleanUps= cleanUps1;
    	CleanUpPreview result= new CleanUpPreview(composite, cleanUps, false) {
    		public void setWorkingValues(Map workingValues) {
    			fill(workingValues, sharedSettings);
    		}

    	};
		return result;
    }

	private void fill(Map settings, Map sharedSettings) {
		sharedSettings.clear();
		for (Iterator iterator= settings.keySet().iterator(); iterator.hasNext();) {
	        String key= (String)iterator.next();
	        sharedSettings.put(key, settings.get(key));
        }
    }

	protected ModifyDialog createModifyDialog(Shell shell, Profile profile, ProfileManager profileManager, boolean newProfile) {
        return new CleanUpModifyDialog(shell, profile, profileManager, newProfile, CLEANUP_PAGE_SETTINGS_KEY);
    }

}

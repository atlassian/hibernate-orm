/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2012, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.build.gradle.testing.database;

import java.io.File;
import java.util.Collections;

import groovy.lang.Closure;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;

/**
 * Database profile as defined by a {@code matrix.gradle} file
 *
 * @author Steve Ebersole
 * @author Strong Liu
 */
public class MatrixDotGradleProfile extends AbstractDatabaseProfileImpl {
	private static final String MATRIX_NODE_CONVENTION_KEY = "matrixNode";

	private final Configuration jdbcDependencies;

	protected MatrixDotGradleProfile(File matrixDotGradleFile, Project project) {
		super( matrixDotGradleFile.getParentFile(), project );
		jdbcDependencies = prepareConfiguration( getName() );
        final ConventionImpl convention = new ConventionImpl( jdbcDependencies, project );
        // In Gradle 4.x extensions can't be removed, so use a unique name per profile to avoid conflicts
        final String extensionKey = MATRIX_NODE_CONVENTION_KEY + "_" + getName();
        project.getExtensions().add( extensionKey, convention );
        // Expose jdbcDependency() as top-level project ext closures so matrix.gradle scripts
        // can call it without the "matrixNode." prefix. This replaces the Gradle 1.x Convention
        // API which is no longer supported in Gradle 4.x.
        project.getExtensions().getExtraProperties().set( "jdbcDependency", new groovy.lang.Closure<Void>( convention ) {
            @Override
            public Void call(Object... args) {
                if ( args.length == 2 ) {
                    convention.jdbcDependency( args[0], (groovy.lang.Closure) args[1] );
                }
                else {
                    convention.jdbcDependency( args[0] );
                }
                return null;
            }
        } );
        try {
            project.apply( Collections.singletonMap( "from", matrixDotGradleFile ) );
        }
        finally {
            // Clean up the ext property after apply so it doesn't leak
            project.getExtensions().getExtraProperties().set( "jdbcDependency", null );
        }
	}

	@Override
	public Configuration getTestingRuntimeConfiguration() {
		return jdbcDependencies;
	}

	private class ConventionImpl {
        private final Configuration jdbcDependencies;
		private final Project project;

        private ConventionImpl(Configuration jdbcDependencies, Project project) {
            this.jdbcDependencies = jdbcDependencies;
			this.project = project;
		}

		@SuppressWarnings( {"UnusedDeclaration"})
        public void jdbcDependency(Object dependencyNotation, Closure closure) {
            project.getDependencies().add( jdbcDependencies.getName(), dependencyNotation, closure );
        }

		@SuppressWarnings( {"UnusedDeclaration"})
        public void jdbcDependency(Object dependencyNotation) {
            project.getDependencies().add( jdbcDependencies.getName(), dependencyNotation );
        }
	}

}

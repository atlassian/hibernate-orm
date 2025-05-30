/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.orm.test.embeddable;

import java.util.Objects;
import java.util.Set;

import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.JiraKey;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DomainModel(
		annotatedClasses = {
				ElementCollectionAllPrimitivePropertyUpdateTest.MyEntity.class
		}
)
@SessionFactory
@JiraKey( "HHH-17257" )
public class ElementCollectionAllPrimitivePropertyUpdateTest {

	private static final Long ENTITY_ID = 1l;

	@BeforeAll
	public void setUp(SessionFactoryScope scope) {
		scope.inTransaction(
				session -> {
					MyEntity myEntity = new MyEntity( ENTITY_ID, Set.of(
							new MyEmbeddable( 1, false ),
							new MyEmbeddable( 2, false )
					) );
					session.persist( myEntity );

				}
		);
	}

	@Test
	public void testUpdateElement(SessionFactoryScope scope) {

		scope.inTransaction(
				session -> {
					MyEntity myEntity = session.find( MyEntity.class, ENTITY_ID );
					Set<MyEmbeddable> elementCollection = myEntity.getElementCollection();
					assertThat( elementCollection ).hasSize( 2 );

					elementCollection.stream().filter( it -> it.getIntField() == 1 ).forEach(
							it -> it.setBooleanField( true ) );
				}
		);

		scope.inTransaction(
				session -> {
					MyEntity myEntity = session.find( MyEntity.class, ENTITY_ID );
					assertThat( myEntity.getElementCollection() ).hasSize( 2 );
					assertThat( myEntity.getElementCollection() ).extracting( MyEmbeddable::isBooleanField )
							.containsExactlyInAnyOrder( true, false );
				}
		);
	}


	@Entity(name = "MyEntity")
	public static class MyEntity {
		@Id
		private Long id;

		private String name;

		@ElementCollection(fetch = FetchType.EAGER)
		private Set<MyEmbeddable> elementCollection;

		public MyEntity() {
		}

		public MyEntity(Long id, Set<MyEmbeddable> elementCollection) {
			this.id = id;
			this.elementCollection = elementCollection;
		}

		public Long getId() {
			return id;
		}

		public Set<MyEmbeddable> getElementCollection() {
			return elementCollection;
		}

		public void setElementCollection(Set<MyEmbeddable> elementCollection) {
			this.elementCollection = elementCollection;
		}
	}

	@Embeddable
	public static class MyEmbeddable {
		@Column
		// the field is considered non-optional because of its primitive type
		private int intField;

		@Column
		// the field is considered non-optional because of its primitive type
		private boolean booleanField;

		public MyEmbeddable() {
		}

		public MyEmbeddable(int intField, boolean booleanField) {
			this.intField = intField;
			this.booleanField = booleanField;
		}

		public int getIntField() {
			return intField;
		}

		public void setIntField(int intField) {
			this.intField = intField;
		}

		public boolean isBooleanField() {
			return booleanField;
		}

		public void setBooleanField(boolean booleanField) {
			this.booleanField = booleanField;
		}

		@Override
		public boolean equals(Object o) {
			if ( this == o ) {
				return true;
			}
			if ( o == null || getClass() != o.getClass() ) {
				return false;
			}
			MyEmbeddable that = (MyEmbeddable) o;
			return booleanField == that.booleanField && Objects.equals( intField, that.intField );
		}

		@Override
		public int hashCode() {
			return Objects.hash( intField, booleanField );
		}
	}

}

/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.orm.test.tenantlongid;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import org.hibernate.annotations.TenantId;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Client {
	@Id
	@GeneratedValue
	Long id;

	String name;

	@TenantId
	Long tenantId;

	@OneToMany(mappedBy = "client")
	Set<Account> accounts = new HashSet<>();

	public Client(String name) {
		this.name = name;
	}

	Client() {}
}

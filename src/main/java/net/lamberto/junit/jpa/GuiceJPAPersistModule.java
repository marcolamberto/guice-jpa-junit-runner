package net.lamberto.junit.jpa;

import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.Properties;
import java.util.Set;

import javax.persistence.Entity;

import org.hibernate.jpa.AvailableSettings;
import org.reflections.Reflections;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.persist.jpa.JpaPersistModule;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class GuiceJPAPersistModule extends AbstractModule {
	private final Collection<Package> entitiesPackages = Lists.newArrayList();

	public GuiceJPAPersistModule(final Package ... entitiesPackages) {
		this(asList(entitiesPackages));
	}

	public GuiceJPAPersistModule(final Collection<Package> entitiesPackages) {
		this.entitiesPackages.addAll(entitiesPackages);
	}

	@Override
	public final void configure() {
		preConfigure();

		final Properties properties = new Properties();

		properties.put("hibernate.ejb.entitymanager_factory_name", "EF." + System.currentTimeMillis());

		final Set<Class<?>> entitiesClasses = Sets.newHashSet();
		for (final Package entitiesPackage : entitiesPackages) {
			entitiesClasses.addAll(new Reflections(entitiesPackage.getName()).getTypesAnnotatedWith(Entity.class));
		}

		if (entitiesPackages.isEmpty()) {
			entitiesClasses.addAll(new Reflections("").getTypesAnnotatedWith(Entity.class));
		}
		System.err.println(entitiesClasses.size());

		properties.put(AvailableSettings.LOADED_CLASSES, Lists.newArrayList(entitiesClasses));

		install(new JpaPersistModule("GuicePersistTest").properties(properties));

		postConfigure();
	}

	protected void preConfigure() {
		// extension hook
	}

	protected void postConfigure() {
		// extension hook
	}
}

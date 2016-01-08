package net.lamberto.junit.jpa;

import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;

import javax.persistence.Entity;

import org.hibernate.jpa.AvailableSettings;
import org.reflections.Reflections;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.persist.jpa.JpaPersistModule;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

public class GuiceJPAPersistModule extends AbstractModule {
	public static class Debugging extends GuiceJPAPersistModule {
		public Debugging() {
			super(Collections.<Package>emptyList(), true);
		}

		public Debugging(final Package ... entitiesPackages) {
			super(asList(entitiesPackages), true);
		}

		public Debugging(final Collection<Package> entitiesPackages) {
			super(entitiesPackages, true);
		}
	}


	private final Collection<Package> entitiesPackages = Lists.newArrayList();
	private final boolean debug;

	public GuiceJPAPersistModule() {
		this(Collections.<Package>emptyList(), false);
	}

	public GuiceJPAPersistModule(final Package ... entitiesPackages) {
		this(asList(entitiesPackages), false);
	}

	public GuiceJPAPersistModule(final Collection<Package> entitiesPackages) {
		this(entitiesPackages, false);
	}

	private GuiceJPAPersistModule(final Collection<Package> entitiesPackages, boolean debug) {
		this.entitiesPackages.addAll(entitiesPackages);
		this.debug = debug;
	}

	public static GuiceJPAPersistModule newDebuggingInstance(final Package ... entitiesPackages) {
		return newDebuggingInstance(asList(entitiesPackages));
	}

	public static GuiceJPAPersistModule newDebuggingInstance(final Collection<Package> entitiesPackages) {
		return new GuiceJPAPersistModule(entitiesPackages, true);
	}

	@Override
	public final void configure() {
		preConfigure();

		final Properties properties = new Properties();

		final long now = System.currentTimeMillis();
		properties.setProperty("javax.persistence.jdbc.url", String.format("jdbc:hsqldb:mem:testdb-%d;shutdown=true", now));
		properties.setProperty("hibernate.ejb.entitymanager_factory_name", String.format("EF-%d", now));

		configureDebugOptions(properties);

		final Set<Class<?>> entitiesClasses = Sets.newHashSet();
		for (final Package entitiesPackage : entitiesPackages) {
			entitiesClasses.addAll(new Reflections(entitiesPackage.getName()).getTypesAnnotatedWith(Entity.class));
		}

		if (entitiesPackages.isEmpty()) {
			entitiesClasses.addAll(new Reflections("").getTypesAnnotatedWith(Entity.class));
		}

		properties.put(AvailableSettings.LOADED_CLASSES, Lists.newArrayList(entitiesClasses));

		install(new JpaPersistModule("GuicePersistTest").properties(properties));

		postConfigure();
	}

	private void configureDebugOptions(Properties properties) {
		if (!debug) {
			return;
		}

		properties.setProperty("hibernate.show_sql", Boolean.TRUE.toString());
		properties.setProperty("hibernate.format_sql", Boolean.TRUE.toString());

		final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

		context.getLogger("org.hibernate.type").setLevel(Level.TRACE);
		context.getLogger("org.hibernate.sql").setLevel(Level.DEBUG);
	}

	protected void preConfigure() {
		// extension hook
	}

	protected void postConfigure() {
		// extension hook
	}
}

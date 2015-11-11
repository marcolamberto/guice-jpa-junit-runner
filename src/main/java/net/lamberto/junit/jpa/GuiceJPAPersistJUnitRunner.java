package net.lamberto.junit.jpa;

import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.Collections;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Collections2;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.persist.PersistService;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.lamberto.junit.GuiceJUnitRunner.GuiceModules;

@Slf4j
public class GuiceJPAPersistJUnitRunner extends BlockJUnit4ClassRunner {
	private PersistService persistService;
	private Provider<EntityManager> entityManager;
	private Collection<Class<? extends Module>> modules;


	public GuiceJPAPersistJUnitRunner(final Class<?> testClass) throws InitializationError {
		super(testClass);
    }

    private Injector createInjectorFor(final Collection<Class<? extends Module>> classes) throws InitializationError {
    	return Guice.createInjector(Collections2.transform(classes, new Function<Class<? extends Module>, Module>() {
			@Override
			@SneakyThrows
			public Module apply(final Class<? extends Module> module) {
                return module.newInstance();
			}
		}));
    }

	private Collection<Class<? extends Module>> getModulesFor(final Class<?> module) throws InitializationError {
        final GuiceModules annotation = module.getAnnotation(GuiceModules.class);

        return annotation == null ?
    		null :
			asList(annotation.value());
    }

	private Collection<Class<? extends Module>> getModulesFor(final FrameworkMethod method) throws InitializationError {
        final GuiceModules annotation = method.getAnnotation(GuiceModules.class);

        return annotation == null ?
    		null :
			asList(annotation.value());
    }

	private Collection<Class<? extends Module>> getModulesFor(final FrameworkMethod method, final Class<?> module) throws InitializationError {
		return Optional.fromNullable(getModulesFor(method))
			.or(Optional.fromNullable(getModulesFor(module))
				.or(Collections.<Class<? extends Module>>singleton(GuiceJPAPersistModule.class)));
	}

	@Override
	protected Object createTest() throws Exception {
        final Injector injector = createInjectorFor(modules);

        persistService = injector.getInstance(PersistService.class);
		persistService.start();

		entityManager = injector.getProvider(EntityManager.class);

		return injector.getInstance(getTestClass().getJavaClass());
	}

	@Override
	protected void runChild(final FrameworkMethod method, final RunNotifier notifier) {
		try {
			modules = getModulesFor(method, method.getDeclaringClass());

			super.runChild(method, notifier);
		} catch (final InitializationError e) {
			throw new IllegalArgumentException(e);
		} finally {
			if (persistService != null) {
				try {
					entityManager.get().clear();

					final EntityTransaction transaction = entityManager.get().getTransaction();
					if (transaction.isActive()) {
						transaction.rollback();
					}
				} catch (final Exception e) {
					log.debug(e.getMessage(), e);
				}

				try {
					persistService.stop();
				} finally {
					persistService = null;
				}
			}
		}
	}
}

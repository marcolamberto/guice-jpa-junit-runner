package net.lamberto.junit.jpa;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import com.google.common.base.Function;
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
    private final List<Class<? extends Module>> classes;
	private PersistService persistService;
	private Provider<EntityManager> entityManager;


	public GuiceJPAPersistJUnitRunner(final Class<? extends Module> testClass) throws InitializationError {
		super(testClass);

	    classes = getModulesFor(testClass);
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

	private List<Class<? extends Module>> getModulesFor(final Class<?> testClass) throws InitializationError {
        final GuiceModules annotation = testClass.getAnnotation(GuiceModules.class);

        return annotation == null ?
    		Collections.<Class<? extends Module>>singletonList(GuiceJPAPersistModule.class) :
			Arrays.asList(annotation.value());
    }

	@Override
	protected Object createTest() throws Exception {
        final Injector injector = createInjectorFor(classes);

        persistService = injector.getInstance(PersistService.class);
		persistService.start();

		entityManager = injector.getProvider(EntityManager.class);

		return injector.getInstance(getTestClass().getJavaClass());
	}

	@Override
	protected void runChild(final FrameworkMethod method, final RunNotifier notifier) {
		try {
			super.runChild(method, notifier);
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

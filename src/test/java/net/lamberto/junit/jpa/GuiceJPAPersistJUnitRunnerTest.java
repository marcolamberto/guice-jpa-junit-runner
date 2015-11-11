package net.lamberto.junit.jpa;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

import net.lamberto.junit.GuiceJUnitRunner.GuiceModules;

@RunWith(GuiceJPAPersistJUnitRunner.class)
public class GuiceJPAPersistJUnitRunnerTest {
	@Inject
	private Provider<EntityManager> emProvider;


	@Test
	@Transactional
	public void basicUsage() {
		assertThat(findAll(), hasSize(0));

		final SampleEntity entity = new SampleEntity();
		persist(entity);

		assertThat(findAll(), hasSize(1));
	}


	public static class TestModule extends GuiceJPAPersistModule {
		public TestModule() {
			super(SampleEntity.class.getPackage());
		}
	}

	@Test
	@Transactional
	@GuiceModules(TestModule.class)
	public void itShouldAllowSpecificPackagesForEntitiesAutodiscovery() {
		assertThat(findAll(), hasSize(0));

		final SampleEntity entity = new SampleEntity();
		persist(entity);

		assertThat(findAll(), hasSize(1));
	}


	@Test(expected=OutOfMemoryError.class)
	@Transactional
	public void itShouldHandleStaleTransactions() {
		throw new OutOfMemoryError();
	}


	private void persist(final SampleEntity entity) {
		emProvider.get().persist(entity);
	}

	private List<SampleEntity> findAll() {
		final EntityManager em = emProvider.get();
		final CriteriaBuilder cb = em.getCriteriaBuilder();
		final CriteriaQuery<SampleEntity> query = cb.createQuery(SampleEntity.class);
		query.from(SampleEntity.class);

		return em.createQuery(query).getResultList();
	}
}
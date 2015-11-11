guice-jpa-junit-runner
====

**Continuous Integration:** [![Build Status](https://api.travis-ci.org/marcolamberto/guice-jpa-junit-runner.png?branch=master)](https://travis-ci.org/marcolamberto/guice-jpa-junit-runner) <br/>

A Guice JPA JUnit Runner allowing Guice-based testing with JPA Hibernate4-based.

## What is guice-jpa-junit-runner?

guice-jpa-junit-runner is a JUnit Runner allowing Guice-based testing with JPA entities.
Each test method is running with a clean Injector instance.

## Basic usage

```java
@RunWith(GuiceJPAPersistJUnitRunner.class)
public class GuiceJPAPersistJUnitRunnerTest {
	@Inject
	public MyService service;

	@Test
	public void test() {
		// ...
	}
}
```

## Using custom Guice modules

You can easily add one more modules by using the @GuiceModules annotation.

```java
@RunWith(GuiceJPAPersistJUnitRunner.class)
@GuiceModules(TestModule.class)
public class GuiceJUnitRunnerTest {

	public static class TestModule extends AbstractModule {
		@Override
		protected void configure() {
			bind(MyService.class).to(MyServiceImpl.class);
			// ...
		}
	}


	@Inject
	public MyService service;

	@Test
	@GuiceModules(TestModule2.class)
	public void perTestSpecificModule() {
		// ...
	}

	@Test
	public void test() {
		// ...
	}
}
```

## Configuring entities specific autodiscovery

```java
public class TestModule extends GuiceJPAPersistModule {
	public TestModule() {
		super(
			SampleEntity.class.getPackage(),
			AnotherEntityWithDifferentPackage.class.getPackage()
		);
	}

	@Override
	protected void preConfigure() {
		// ...
	}

	@Override
	protected void postConfigure() {
		bind(MyService.class).to(MyServiceImpl.class);
		// ...
	}
}
```
# avans-freemarker

freemarker wrapper for avans.

## SYNOPSIS

In your base controller class:

	public static abstract class MyControllerBase extends ControllerBase {
		// Configuration is thread safe.
		private static final Configuration configuration = buildConfiguration();

		public <T> WebResponse render(String templateName, Object dataModel)
				throws IOException, TemplateException {
			return new FreeMarkerView(configuration).render(this, templateName,
					dataModel);
		}

		private static Configuration buildConfiguration() {
			final Configuration cfg = new Configuration();
			try {
				cfg.setDirectoryForTemplateLoading(new File(
						"src/test/resources/"));
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
			cfg.setDefaultEncoding("UTF-8");

			// cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
			cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
			cfg.setIncompatibleImprovements(new Version(2, 3, 20));
			return cfg;
		}
	}

In your controller:

	public static class MyController extends MyControllerBase {

		@GET("/")
		public Object root() throws IOException, TemplateException {
			final Map<String, String> map = new HashMap<>();
			map.put("name", "John");
			return this.render("hello.fttl", map);
		}
	}

## LICENSE

Same as avans core.

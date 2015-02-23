#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.view;

import com.google.common.io.CharSource;
import freemarker.cache.TemplateLoader;

import java.io.IOException;
import java.io.Reader;

public class HtmlTemplateLoader implements TemplateLoader {

	private static final String PROLOGUE = "<${symbol_pound}escape x as x?html>";
	private static final String EPILOGUE = "</${symbol_pound}escape>";

	private final TemplateLoader delegate;

	public HtmlTemplateLoader(final TemplateLoader delegate) {
		this.delegate = delegate;
	}

	@Override
	public Object findTemplateSource(final String name) throws IOException {
		return delegate.findTemplateSource(name);
	}

	@Override
	public long getLastModified(final Object templateSource) {
		return delegate.getLastModified(templateSource);
	}

	@Override
	public Reader getReader(final Object templateSource, final String encoding) throws IOException {
		return CharSource.concat(
				CharSource.wrap(PROLOGUE),
				new CharSource() {
					@Override
					public Reader openStream() throws IOException {
						return delegate.getReader(templateSource, encoding);
					}
				},
				CharSource.wrap(EPILOGUE)
		).openBufferedStream();
	}

	@Override
	public void closeTemplateSource(final Object templateSource) throws IOException {
		delegate.closeTemplateSource(templateSource);
	}
}

# avans

[![Build Status](https://travis-ci.org/tokuhirom/avans.svg?branch=master)](https://travis-ci.org/tokuhirom/avans)

Tiny and thin web application framework for Java 8.

## SYNOPSIS

	public static class MyController extends ControllerBase {
		@BeforeDispatchTrigger
		public Optional<WebResponse> beforeDispatch() {
			return Optional.empty();
		}

		@GET("/")
		public WebResponse index() {
			return new ByteArrayResponse(200,
					"Hello world".getBytes(StandardCharsets.UTF_8));
		}
	}

## Motivation

I need tiny, thin, and simple web application framework for Java 8.
I need the web application framework like Sledge(Popular web application framework for Perl5).


## Architecture

You can build web application based on servlet API.
That's all.

## INSTALLATION

avans was uploaded on maven central.
Please look maven central site: http://search.maven.org/#search%7Cga%7C1%7Cavans

## Create skeleton site

avans supports maven archetype. You can create a simple site by this skeleton generator.

    mvn archetype:generate \
      -DarchetypeGroupId=me.geso.avans \
      -DarchetypeArtifactId=avans-setup \
      -DarchetypeVersion=LATEST_VERSION \
      -DgroupId=com.example.myapp \
      -DartifactId=MyWebApp

## web.xml

You can initialize servlets by web.xml.

    <?xml version="1.0" encoding="UTF-8"?>
    <web-app version="3.0" xmlns="http://java.sun.com/xml/ns/javaee" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd">
      <servlet>
        <servlet-name>hello</servlet-name>
        <servlet-class>me.geso.avans.AvansServlet</servlet-class>
        <init-param>
          <param-name>class</param-name>
          <param-value>com.example.helloworld.Main$Foo</param-value>
        </init-param>
        <load-on-startup>1</load-on-startup>
      </servlet>
      <servlet-mapping>
        <servlet-name>hello</servlet-name>
        <url-pattern>/*</url-pattern>
      </servlet-mapping>
    </web-app>

Pass the controller class names in CSV.

You can pass the controller class list by packages.

        <init-param>
          <param-name>package</param-name>
          <param-value>my.project.sp,my.project.pc</param-value>
        </init-param>

## Controller hooks

### @BeforeDispatchTrigger

    public class MyController extends ControllerBase {
      @BeforeDispatchTrigger
      public Optional<WebResponse> beforeDispatch() {
        return Optional.empty();
      }
    }

The callback methods will execute before dispatching controller methods.
You can prepare the controller states at here.

If the return value contains instance of WebResponse, ControllerBase use the response as the response. It'll skip the controller method.

### @ResponseFilter

You can modify every response by this hook point.

    public class MyController extends ControllerBase {
      @ResponseFilter
      public void beforeDispatch(WebResponse resp) {
        resp.addHeader("X-Content-Type-Options", "nosniff");
      }
    }

### @HTMLFilter

You can rewrite HTML in hook point. It supported by avans-mustache and avans-freemarker.

	public static class MyController extends ControllerBase {
		@HTMLFilter
		public String htmlFilter(String src) {
			return src.toUpperCase();
		}

		@GET("/")
		public WebResponse foo() {
			return this.renderText(this.filterHTML("Hige"));
		}
	}

### @ResponseConverter

You can convert return value from controller method at this hook point.

	@Data
	public static class MyValue {
		private final int foo = 3;
	}

	public static class MyController extends ControllerBase {
		@ResponseConverter(MyValue.class)
		public Optional<WebResponse> responseFilter(MyValue o) {
			return Optional.of(this.renderJSON(o));
		}

		@GET("/")
		public MyValue call() {
			return new MyValue();
		}
	}

## @ParamProcessor

You can implement your own controller parameter converter.

	public static class MyController extends ControllerBase {
		@ParamProcessor(targetClass = String.class)
		public ParameterProcessorResult paramUpperQ(Parameter parameter) {
			final Optional<String> q = this.getRequest().getQueryParams()
					.getFirst("q");
			if (q.isPresent()) {
				return ParameterProcessorResult.fromData(q.get().toUpperCase());
			} else {
				final WebResponse response = this.renderError(400, "Missing Q");
				return ParameterProcessorResult.fromWebResponse(response);
			}
		}

		@GET("/")
		public WebResponse index(String q) {
			return this.renderText(q);
		}
	}

You can filter the injection target by annotation.

	@Slf4j
	public static class MyController extends ControllerBase {
		@ParamProcessor(targetAnnotation = MyAnnotation.class)
		public ParameterProcessorResult paramAnnotation(Parameter parameter) {
			log.info("paramAnnotation");
			return ParameterProcessorResult.fromData(3.14);
		}

		@GET("/annotation")
		public WebResponse annotation(@MyAnnotation Double pi) {
			return this.renderText("" + pi);
		}
	}

Concrete usecase: Inject member object deflated from `X-MY-TOKEN` header.

## FAQ

### Is there a HTML::FillInForm support?

No there isn't. You should do it with JavaScript.

## Incompatible changes

### 0.35.0

 * Removed `ControllerBase#getBaseDirectory()`
 * AvansUtil was gone.
 * `me.geso.APIResponse` was gone.
 * `me.geso.APIResponse` no longer converts `APIResponse` by default.
   * You need to add your own response converter by `@ResponseConverter`
   * Or, you can use BasicAPIResponse instead.
 * Removed `ControllerBase#BEFORE_INIT` hook point.
 * Removed `ControllerBase#AFTER_INIT` hook point.
 * Removed `ControllerBase#getServletResponse` method.
 * Removed `ControllerBase#BEFORE_DISPATCH` method.
   * Use `@BeforeDispatchTrigger` method instead.
 * ControllerBase no longer execute tinyvalidator by default.
   * You need to implement avans-tinyvalidator on your controller class
 * Removed `ControllerBase#GET_PARAMETER` method.
   * Use `@ParamProcessor` method instead.
 * Added JsonParamReader interface and JacksonJsonParamReader.
   * It ignores unknown parameters by default.
   * It throws IOException
 * avans-session package was splitted from core

## LICENSE

  The MIT License (MIT)
  Copyright © 2014 Tokuhiro Matsuno, http://64p.org/ <tokuhirom@gmail.com>

  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the “Software”), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:

  The above copyright notice and this permission notice shall be included in
  all copies or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  THE SOFTWARE.

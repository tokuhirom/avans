package me.geso.avans.session;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalLong;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;

import me.geso.avans.AvansServlet;
import me.geso.avans.ControllerBase;
import me.geso.avans.annotation.GET;
import me.geso.avans.annotation.POST;
import me.geso.avans.trigger.BeforeDispatchTrigger;
import me.geso.mech.MechJettyServlet;
import me.geso.mech.PrintRequestListener;
import me.geso.mech2.Mech2;
import me.geso.mech2.Mech2Result;
import me.geso.mech2.Mech2WithBase;
import me.geso.webscrew.response.WebResponse;

public class SessionMixinTest {
	public static class MyController extends ControllerBase implements
			SessionMixin {
		@POST("/counter")
		public WebResponse counter() {
			final OptionalLong currentCounter = this.getSession().getLong(
				"counter");
			long counter = currentCounter.orElse(0);
			counter++;
			this.getSession().setLong("counter", counter);
			return this.renderText("counter:" + counter);
		}

		@GET("/getSessionID")
		public WebResponse getSessionID() {
			return this.renderText(getSession().getSessionId());
		}

		@GET("/getXSRFToken")
		public WebResponse getXSRFToken() {
			return this.renderText(getSession().getXSRFToken());
		}

		@GET("/expire")
		public WebResponse expire() {
			this.getSession().expire();
			return this.renderText("expired.");
		}

		// HashMapSesssionStore as a default session store.
		// It's on memory volatile storage. If you want to persistent
		// the
		// data, you should override this method.
		static final WebSessionStore store = new HashMapSessionStore();

		@Override
		public WebSessionManager buildSessionManager() {
			try {
				// Generate HMAC key at runtime...
				final SecretKeySpec signingKey = new SecretKeySpec(
					"My Secret".getBytes(), "HmacSHA1");

				final Mac mac = Mac.getInstance("HmacSHA1");
				mac.init(signingKey);

				final DefaultSessionCookieFactory sessionCookieFactory = DefaultSessionCookieFactory
					.builder()
					.name("avans_session_id")
					.build();
				final DefaultXSRFTokenCookieFactory xsrfTokenCookieFactory = DefaultXSRFTokenCookieFactory
					.builder()
					.mac(mac)
					.build();
				final SessionIDGenerator sessionIDGenerator = new SecureRandomSessionIDGenerator(
					new SecureRandom(),
					32);

				return new DefaultWebSessionManager(
					this.getServletRequest(),
					MyController.store,
					sessionIDGenerator,
					sessionCookieFactory,
					xsrfTokenCookieFactory);
			} catch (final InvalidKeyException | NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}
		}

		@BeforeDispatchTrigger
		public Optional<WebResponse> xsrfDetection() {
			final String method = getServletRequest().getMethod();
			if (!(method.equals("GET") || method.equals("HEAD"))) {
				final String xsrfToken = getServletRequest().getHeader("X-XSRF-Token");
				if (!getSession().validateXSRFToken(xsrfToken)) {
					return Optional.of(this.renderError(403, "XSRF Detected"));
				}
			}
			return Optional.empty();
		}
	}

	@Test
	public void test() throws Exception {
		final AvansServlet servlet = new AvansServlet();
		servlet.registerClass(MyController.class);
		try (MechJettyServlet mechJettyServlet = new MechJettyServlet(servlet)) {
			final CookieStore cookieStore = new BasicCookieStore();
			final HttpClientBuilder httpClientBuilder = HttpClientBuilder.create()
				.setDefaultCookieStore(cookieStore);
			final Mech2 rawMech2 = Mech2.builder()
				.setHttpClientBuilder(httpClientBuilder)
				.build();
			final Mech2WithBase mech2 = new Mech2WithBase(rawMech2, new URI(mechJettyServlet.getBaseURL()));
			mechJettyServlet.addRequestListener(new PrintRequestListener(System.out));
			String xsrfToken;
			{
				final Mech2Result result = mech2.post("/counter")
					.execute();
				final HttpResponse res = result.getResponse();
				assertEquals(200, result.getStatusCode());
				Arrays.stream(result.getResponse().getHeaders(
					"Cookie")).forEach(System.out::println);
				assertThat(result.getResponse().getHeaders("Set-Cookie").length,
					is(2));
				System.out.println(result.getResponse().getHeaders("Set-Cookie")[0]);
				assertThat(
					res.getHeaders("Set-Cookie")[0].getValue()
						.startsWith("avans_session_id="),
					is(true));
				final String xsrfCookie = res.getHeaders("Set-Cookie")[1].getValue();
				assertThat(
					xsrfCookie.startsWith("XSRF-TOKEN="),
					is(true));
				xsrfToken = xsrfCookie.substring("XSRF-TOKEN=".length(), "XSRF-TOKEN=".length() + 28);
				assertThat(result.getResponseBodyAsString(),
					is("counter:1"));
			}
			{
				final Mech2Result res = mech2.post("/counter")
					.setHeader("X-XSRF-TOKEN", xsrfToken)
					.execute();
				assertThat(res.getResponseBodyAsString(),
					is("counter:2"));
			}
			String sessionId1;
			{
				final Mech2Result res = mech2.get("/getSessionID").execute();
				sessionId1 = res.getResponseBodyAsString();
			}
			{
				final Mech2Result res = mech2.post("/counter")
					.setHeader("X-XSRF-TOKEN", xsrfToken)
					.execute();
				assertThat(res.getResponseBodyAsString(),
					is("counter:3"));
			}
			String sessionId2;
			{
				final Mech2Result res = mech2.get("/getSessionID").execute();
				sessionId2 = res.getResponseBodyAsString();
			}
			assertEquals(sessionId1, sessionId2);
			String xsrfToken1;
			{
				final Mech2Result res = mech2.get("/getXSRFToken").execute();
				xsrfToken1 = res.getResponseBodyAsString();
				assertThat(xsrfToken1.length(), is(28));
			}
			String xsrfToken2;
			{
				Mech2Result res = mech2.get("/getXSRFToken").execute();
				xsrfToken2 = res.getResponseBodyAsString();
				assertThat(xsrfToken1, is(xsrfToken2));
			}
			{
				Mech2Result res = mech2.get("/expire").execute();
				assertThat(res.getResponseBodyAsString(),
					is("expired."));
			}
			{
				Mech2Result res = mech2.post("/counter").execute();
				assertThat(res.getResponseBodyAsString(),
					is("counter:1"));
			}
			{
				String xsrfToken3;
				{
					Mech2Result res = mech2.get("/getXSRFToken").execute();
					xsrfToken3 = res.getResponseBodyAsString();
					assertThat(xsrfToken1, is(xsrfToken2));
				}
				{
					Mech2Result res = mech2.post("/counter")
						.setHeader("X-XSRF-Token", xsrfToken3)
						.execute();
					assertThat(res.getResponseBodyAsString(),
						is("counter:2"));
				}
			}
		}
	}

}

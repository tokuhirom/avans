package me.geso.avans.session;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.OptionalLong;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.Test;

import me.geso.avans.AvansServlet;
import me.geso.avans.ControllerBase;
import me.geso.avans.annotation.GET;
import me.geso.mech.MechJettyServlet;
import me.geso.mech.MechResponse;
import me.geso.mech.PrintRequestListener;
import me.geso.webscrew.response.WebResponse;

public class SessionMixinTest {
	public static class MyController extends ControllerBase implements
			SessionMixin {
		@GET("/")
		public WebResponse root() {
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
	}

	@Test
	public void test() throws Exception {
		final AvansServlet servlet = new AvansServlet();
		servlet.registerClass(MyController.class);
		try (MechJettyServlet mech = new MechJettyServlet(servlet)) {
			mech.addRequestListener(new PrintRequestListener(System.out));
			try (MechResponse res = mech.get("/").execute()) {
				Arrays.stream(res.getResponse().getHeaders(
						"Cookie")).forEach(it -> System.out.println(it));
				assertThat(res.getHeaders("Set-Cookie").size(),
						is(2));
				System.out.println(res.getHeaders("Set-Cookie").get(0));
				assertThat(
						res.getHeaders("Set-Cookie").get(0)
								.startsWith("avans_session_id="),
						is(true));
				assertThat(
						res.getHeaders("Set-Cookie").get(1)
								.startsWith("XSRF-TOKEN="),
						is(true));
				assertThat(res.getContentString(),
						is("counter:1"));
			}
			mech.getCookieStore().getCookies().stream()
					.forEach(cookie -> System.out.println(cookie));
			try (MechResponse res = mech.get("/").execute()) {
				assertThat(res.getContentString(),
						is("counter:2"));
			}
			String sessionId1;
			try (MechResponse res = mech.get("/getSessionID").execute()) {
				sessionId1 = res.getContentString();
			}
			try (MechResponse res = mech.get("/").execute()) {
				assertThat(res.getContentString(),
						is("counter:3"));
			}
			String sessionId2;
			try (MechResponse res = mech.get("/getSessionID").execute()) {
				sessionId2 = res.getContentString();
			}
			assertEquals(sessionId1, sessionId2);
			String xsrfToken1;
			try (MechResponse res = mech.get("/getXSRFToken").execute()) {
				xsrfToken1 = res.getContentString();
				assertThat(xsrfToken1.length(), is(28));
			}
			String xsrfToken2;
			try (MechResponse res = mech.get("/getXSRFToken").execute()) {
				xsrfToken2 = res.getContentString();
				assertThat(xsrfToken1, is(xsrfToken2));
			}
			try (MechResponse res = mech.get("/expire").execute()) {
				assertThat(res.getContentString(),
						is("expired."));
			}
			try (MechResponse res = mech.get("/").execute()) {
				assertThat(res.getContentString(),
						is("counter:1"));
			}
			try (MechResponse res = mech.get("/").execute()) {
				assertThat(res.getContentString(),
						is("counter:2"));
			}
		}
	}

}

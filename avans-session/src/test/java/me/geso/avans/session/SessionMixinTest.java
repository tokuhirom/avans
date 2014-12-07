package me.geso.avans.session;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.OptionalLong;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;

import me.geso.avans.AvansServlet;
import me.geso.avans.ControllerBase;
import me.geso.avans.annotation.GET;
import me.geso.mech.MechJettyServlet;
import me.geso.mech.MechResponse;
import me.geso.mech.PrintRequestListener;
import me.geso.webscrew.response.WebResponse;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

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
				final KeyGenerator kg = KeyGenerator.getInstance("HmacSHA1");
				final SecretKey sk = kg.generateKey();

				final Mac mac = Mac.getInstance("HmacSHA1");
				mac.init(sk);

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
				Assert.assertThat(res.getHeaders("Set-Cookie").size(),
						CoreMatchers.is(2));
				System.out.println(res.getHeaders("Set-Cookie").get(0));
				Assert.assertThat(
						res.getHeaders("Set-Cookie").get(0)
								.startsWith("avans_session_id="),
						CoreMatchers.is(true));
				Assert.assertThat(
						res.getHeaders("Set-Cookie").get(1)
								.startsWith("XSRF-TOKEN="),
						CoreMatchers.is(true));
				Assert.assertThat(res.getContentString(),
						CoreMatchers.is("counter:1"));
			}
			mech.getCookieStore().getCookies().stream()
					.forEach(cookie -> System.out.println(cookie));
			try (MechResponse res = mech.get("/").execute()) {
				Assert.assertThat(res.getContentString(),
						CoreMatchers.is("counter:2"));
			}
			try (MechResponse res = mech.get("/").execute()) {
				Assert.assertThat(res.getContentString(),
						CoreMatchers.is("counter:3"));
			}
			try (MechResponse res = mech.get("/expire").execute()) {
				Assert.assertThat(res.getContentString(),
						CoreMatchers.is("expired."));
			}
			try (MechResponse res = mech.get("/").execute()) {
				Assert.assertThat(res.getContentString(),
						CoreMatchers.is("counter:1"));
			}
			try (MechResponse res = mech.get("/").execute()) {
				Assert.assertThat(res.getContentString(),
						CoreMatchers.is("counter:2"));
			}
		}
	}

}

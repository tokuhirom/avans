package me.geso.avans.session.spymemcached;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import me.geso.avans.AvansServlet;
import me.geso.avans.ControllerBase;
import me.geso.avans.annotation.GET;
import me.geso.avans.session.DefaultWebSessionManager;
import me.geso.avans.session.SessionMixin;
import me.geso.avans.session.WebSessionManager;
import me.geso.mech.MechJettyServlet;
import me.geso.mech.MechResponse;
import me.geso.webscrew.response.WebResponse;
import net.spy.memcached.MemcachedClient;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.thimbleware.jmemcached.CacheImpl;
import com.thimbleware.jmemcached.Key;
import com.thimbleware.jmemcached.LocalCacheElement;
import com.thimbleware.jmemcached.MemCacheDaemon;
import com.thimbleware.jmemcached.storage.CacheStorage;
import com.thimbleware.jmemcached.storage.hash.ConcurrentLinkedHashMap;

public class SpyMemcachedSessionStoreTest {
	private static MemCacheDaemon<LocalCacheElement> daemon;
	private static MemcachedClient client;

	@BeforeClass
	public static void beforeClass() throws IOException {
		daemon = new MemCacheDaemon<LocalCacheElement>();

		final int maxItems = 1000;
		final long maxBytes = 1000;
		final CacheStorage<Key, LocalCacheElement> storage = ConcurrentLinkedHashMap
				.create(ConcurrentLinkedHashMap.EvictionPolicy.FIFO, maxItems,
						maxBytes);
		daemon.setCache(new CacheImpl(storage));
		daemon.setBinary(false);
		final InetSocketAddress addr = new InetSocketAddress("127.0.0.1", 11211);
		daemon.setAddr(addr);
		final int idle = 0;
		daemon.setIdleTime(idle);
		final boolean verbose = true;
		daemon.setVerbose(verbose);
		daemon.start();
		client = new MemcachedClient(addr);
		client.set("iyaiya", 0, K.map("a", "b"));
	}

	@AfterClass
	public static void afterClass() {
		daemon.stop();
	}

	@Test
	public void test() throws IOException {

		final SpyMemcachedSessionStore spyMemcachedSessionStore = new SpyMemcachedSessionStore(
				client, 1024);
		final String sessionId1 = "hogehoge";
		final Map<String, Object> data1 = new HashMap<>();
		data1.put("hoge", "fuga");
		spyMemcachedSessionStore.save(sessionId1, data1);
		final Optional<Map<String, Object>> loaded = spyMemcachedSessionStore
				.load(sessionId1);
		assertThat(loaded.isPresent(), is(true));
		assertThat(loaded.get(), is(data1));
	}

	@Test
	public void testNodata() throws IOException {

		final SpyMemcachedSessionStore spyMemcachedSessionStore = new SpyMemcachedSessionStore(
				client, 1024);
		final String sessionId1 = "noooooooooooooodata";
		final Optional<Map<String, Object>> loaded = spyMemcachedSessionStore
				.load(sessionId1);
		assertThat(loaded.isPresent(), is(false));
	}

	@Test
	public void testRemove() throws IOException {
		final SpyMemcachedSessionStore store = new SpyMemcachedSessionStore(
				client, 1024);
		final String sessionId1 = "hogehoge";
		final Map<String, Object> map = K.map("hoge", "fuga");
		store.save(sessionId1, map);
		assertThat(store.load(sessionId1).isPresent(), is(true));
		assertThat(store.load(sessionId1).get(), is(map));
		store.remove(sessionId1);
		assertThat(store.load(sessionId1).isPresent(), is(false));
	}

	public static class MyController extends ControllerBase implements
			SessionMixin {
		private static final SecretKeySpec signingKey = new SecretKeySpec(
				"My Secret".getBytes(), "HmacSHA1");
		private static MemcachedClient memcachedClient = buildMemcachedClient();

		@GET("/")
		public WebResponse root() {
			final OptionalLong currentCounter = this.getSession().getLong(
					"counter");
			long counter = currentCounter.orElse(0);
			counter++;
			this.getSession().setLong("counter", counter);
			return this.renderText("counter:" + counter);
		}

		public static MemcachedClient buildMemcachedClient() {
			try {
				final InetSocketAddress addr = new InetSocketAddress(
						"127.0.0.1",
						11211);
				return new MemcachedClient(addr);
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public WebSessionManager buildSessionManager() {
			try {
				final SpyMemcachedSessionStore sessionStore = new SpyMemcachedSessionStore(
						memcachedClient, 1024);
				final Mac xsrfTokenMac = Mac.getInstance("HmacSHA1");
				xsrfTokenMac.init(signingKey);
				return new DefaultWebSessionManager(
						"avans_session_id",
						this.getRequest(),
						sessionStore,
						xsrfTokenMac);
			} catch (final NoSuchAlgorithmException | InvalidKeyException e) {
				throw new RuntimeException(e);
			}
		}

	}

	@Test
	public void testIntegration() throws Exception {
		final AvansServlet servlet = new AvansServlet();
		servlet.registerClass(MyController.class);
		try (MechJettyServlet mech = new MechJettyServlet(servlet)) {
			try (MechResponse resp = mech.get("/").execute()) {
				assertThat(resp.getStatusCode(), is(200));
				assertThat(resp.getContentString(), is("counter:1"));
			}

			try (MechResponse resp = mech.get("/").execute()) {
				assertThat(resp.getStatusCode(), is(200));
				assertThat(resp.getContentString(), is("counter:2"));
			}
		}
	}
}

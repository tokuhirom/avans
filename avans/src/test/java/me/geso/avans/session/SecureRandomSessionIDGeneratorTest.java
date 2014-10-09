package me.geso.avans.session;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.security.SecureRandom;
import java.util.stream.IntStream;

import org.junit.Test;

public class SecureRandomSessionIDGeneratorTest {

	@Test
	public void test() {
		final SecureRandom secureRandom = new SecureRandom();
		IntStream
				.range(0, 100)
				.forEach(
						it -> {
							final SecureRandomSessionIDGenerator secureRandomSessionIDGenerator = new SecureRandomSessionIDGenerator(
									secureRandom, 32);
							final String id = secureRandomSessionIDGenerator
									.generate();
							System.out.println(id);
							assertThat(id.length(), is(32));
						});
	}

}

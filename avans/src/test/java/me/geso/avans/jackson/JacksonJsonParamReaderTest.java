package me.geso.avans.jackson;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.FileInputStream;

import org.junit.Test;

public class JacksonJsonParamReaderTest {

    @Test
    public void test0() throws Exception {

        JacksonJsonParamReader sut = new JacksonJsonParamReader() {};

        {
            File testData = new File(getClass().getResource("data1.json").getFile());

            try (FileInputStream path = new FileInputStream(testData)) {

                JacksonJsonViewTest.JsonTest0 actual =
                        (JacksonJsonViewTest.JsonTest0) sut.readJsonParam(path,
                                                                          JacksonJsonViewTest.JsonTest0.class);

                assertThat(actual.getList0().size(), is(2));
                assertThat(actual.getList0().get(0), is("a1"));
                assertThat(actual.getList0().get(1), is("b1"));

                assertThat(actual.getList1().size(), is(3));
                assertThat(actual.getList1().get(0), is(1));
                assertThat(actual.getList1().get(1), is(2));
                assertThat(actual.getList1().get(2), is(3));

                assertThat(actual.getList2().size(), is(2));
                assertThat(actual.getList2().get(0).getName(), is("aa"));
                assertThat(actual.getList2().get(0).getValue(), is("bb"));
                assertThat(actual.getList2().get(1).getName(), is("cc"));
                assertThat(actual.getList2().get(1).getValue(), is("dd"));

                assertThat(actual.getMap0().size(), is(2));
                assertThat(actual.getMap0().get("ee"), is("ff"));
                assertThat(actual.getMap0().get("gg"), is("hh"));

                assertThat(actual.getMap1().size(), is(2));
                assertThat(actual.getMap1().get("ii"), is(10));
                assertThat(actual.getMap1().get("jj"), is(20));

                assertThat(actual.getMap2().size(), is(2));
                assertThat(actual.getMap2().get("x").getName(), is("x1"));
                assertThat(actual.getMap2().get("x").getValue(), is("x2"));
                assertThat(actual.getMap2().get("y").getName(), is("y1"));
                assertThat(actual.getMap2().get("y").getValue(), is("y2"));
            }
        }

        {
            File testData = new File(getClass().getResource("data2.json").getFile());

            try (FileInputStream path = new FileInputStream(testData)) {

                JacksonJsonViewTest.JsonTest1 actual =
                        (JacksonJsonViewTest.JsonTest1) sut.readJsonParam(path,
                                                                          JacksonJsonViewTest.JsonTest1.class);

                assertThat(actual.getMap0().size(), is(2));
                assertThat(actual.getMap0().get("xxx").size(), is(1));
                assertThat(actual.getMap0().get("xxx").get("xxx1"), is("xxx2"));
                assertThat(actual.getMap0().get("yyy").size(), is(1));
                assertThat(actual.getMap0().get("yyy").get("yyy1"), is("yyy2"));
            }
        }
    }
}

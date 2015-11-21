package alex.mojaki.boxes.test.collections;

import alex.mojaki.boxes.BoxFamily;
import alex.mojaki.boxes.collections.MapBox;
import com.google.common.collect.testing.MapTestSuiteBuilder;
import com.google.common.collect.testing.TestStringMapGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.MapFeature;
import junit.framework.TestSuite;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.util.Map;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        MapBoxTest.MapBoxSuite.class
})
public class MapBoxTest {

    public static class MapBoxSuite {

        @Test
        public void stub() {
            // I don't understand JUnit suites. I'm just making it shut up about 'No runnable methods'.
        }

        public static TestSuite suite() {
            return MapTestSuiteBuilder
                    .using(new TestStringMapGenerator() {

                        @Override
                        protected Map<String, String> create(Map.Entry<String, String>[] entries) {
                            MapBox<String, String> inner = new MapBox<String, String>(MapBoxTest.class, "map").init();
                            for (Map.Entry<String, String> entry : entries) {
                                if (entry != null) {
                                    inner.put(entry.getKey(), entry.getValue());
                                }
                            }
                            MapBox<String, String> outer = new MapBox<String, String>(
                                    BoxFamily.getInstance(MapBoxTest.class, "map"));
                            outer.set(inner);
                            return outer;
                        }
                    })
                    .named("MapBox")
                    .withFeatures(
                            CollectionFeature.ALLOWS_NULL_QUERIES,
                            CollectionFeature.DESCENDING_VIEW,
                            CollectionFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION,
                            CollectionFeature.SUBSET_VIEW,
                            CollectionFeature.SUPPORTS_ITERATOR_REMOVE,
                            CollectionFeature.SUPPORTS_REMOVE,
                            CollectionSize.ANY,
                            MapFeature.ALLOWS_ANY_NULL_QUERIES,
                            MapFeature.ALLOWS_NULL_KEYS,
                            MapFeature.ALLOWS_NULL_VALUES,
                            MapFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION,
                            MapFeature.GENERAL_PURPOSE,
                            MapFeature.SUPPORTS_PUT,
                            MapFeature.SUPPORTS_REMOVE
                    ).createTestSuite();
        }
    }
}
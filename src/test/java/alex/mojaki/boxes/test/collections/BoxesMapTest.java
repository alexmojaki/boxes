package alex.mojaki.boxes.test.collections;

import alex.mojaki.boxes.collections.BoxesMap;
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
        BoxesMapTest.BoxesMapSuite.class
})
public class BoxesMapTest {

    public static class BoxesMapSuite {

        @Test
        public void stub() {
            // I don't understand JUnit suites. I'm just making it shut up about 'No runnable methods'.
        }

        public static TestSuite suite() {
            return MapTestSuiteBuilder
                    .using(new TestStringMapGenerator() {

                        @Override
                        protected Map<String, String> create(Map.Entry<String, String>[] entries) {
                            BoxesMap<String, String> map = new BoxesMap<String, String>().allowBoxlessKeys();
                            for (Map.Entry<String, String> entry : entries) {
                                if (entry != null) {
                                    map.put(entry.getKey(), entry.getValue());
                                }
                            }
                            return map;
                        }
                    })
                    .named("BoxesMap")
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

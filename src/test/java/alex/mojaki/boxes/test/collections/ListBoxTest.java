package alex.mojaki.boxes.test.collections;

import alex.mojaki.boxes.BoxFamily;
import alex.mojaki.boxes.collections.ListBox;
import com.google.common.collect.testing.ListTestSuiteBuilder;
import com.google.common.collect.testing.TestStringListGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.ListFeature;
import junit.framework.TestSuite;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        ListBoxTest.ListBoxSuite.class
})
public class ListBoxTest {

    public static class ListBoxSuite {

        @Test
        public void stub() {
            // I don't understand JUnit suites. I'm just making it shut up about 'No runnable methods'.
        }

        public static TestSuite suite() {
            return ListTestSuiteBuilder
                    .using(new TestStringListGenerator() {

                        @Override
                        protected List<String> create(String[] elements) {
                            ListBox<String> inner = new ListBox<String>(ListBoxTest.class, "list");
                            inner.set(new ArrayList<String>(Arrays.asList(elements)));
                            ListBox<String> outer = new ListBox<String>(BoxFamily.getInstance(ListBoxTest.class, "list"));
                            outer.set(inner);
                            return outer;
                        }

                    })
                    .named("ListBox")
                    .withFeatures(
                            CollectionFeature.ALLOWS_NULL_QUERIES,
                            CollectionFeature.ALLOWS_NULL_VALUES,
                            CollectionFeature.DESCENDING_VIEW,
                            CollectionFeature.GENERAL_PURPOSE,
                            CollectionFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION,
                            CollectionFeature.KNOWN_ORDER,
                            CollectionFeature.SUBSET_VIEW,
                            CollectionFeature.SUPPORTS_ADD,
                            CollectionFeature.SUPPORTS_ITERATOR_REMOVE,
                            CollectionFeature.SUPPORTS_REMOVE,
                            CollectionSize.ANY,
                            ListFeature.GENERAL_PURPOSE,
                            ListFeature.SUPPORTS_ADD_WITH_INDEX,
                            ListFeature.SUPPORTS_REMOVE_WITH_INDEX,
                            ListFeature.SUPPORTS_SET
                    ).createTestSuite();
        }
    }
}
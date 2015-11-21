package alex.mojaki.boxes.test.collections;

import alex.mojaki.boxes.BoxFamily;
import alex.mojaki.boxes.collections.SetBox;
import com.google.common.collect.testing.SetTestSuiteBuilder;
import com.google.common.collect.testing.TestStringSetGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.SetFeature;
import junit.framework.TestSuite;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        SetBoxTest.SetBoxSuite.class
})
public class SetBoxTest {

    public static class SetBoxSuite {

        @Test
        public void stub() {
            // I don't understand JUnit suites. I'm just making it shut up about 'No runnable methods'.
        }

        public static TestSuite suite() {
            return SetTestSuiteBuilder
                    .using(new TestStringSetGenerator() {

                        @Override
                        protected Set<String> create(String[] elements) {
                            SetBox<String> inner = new SetBox<String>(SetBoxTest.class, "list");
                            inner.set(new HashSet<String>(Arrays.asList(elements)));
                            SetBox<String> outer = new SetBox<String>(BoxFamily.getInstance(SetBoxTest.class, "list"));
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
                            CollectionFeature.SUBSET_VIEW,
                            CollectionFeature.SUPPORTS_ADD,
                            CollectionFeature.SUPPORTS_ITERATOR_REMOVE,
                            CollectionFeature.SUPPORTS_REMOVE,
                            CollectionSize.ANY,
                            SetFeature.GENERAL_PURPOSE
                    ).createTestSuite();
        }
    }
}
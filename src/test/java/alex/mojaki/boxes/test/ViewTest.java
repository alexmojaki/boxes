package alex.mojaki.boxes.test;

import alex.mojaki.boxes.BoxFamily;
import alex.mojaki.boxes.CommonBox;
import alex.mojaki.boxes.PowerBox;
import alex.mojaki.boxes.middleware.get.GetMiddleware;
import alex.mojaki.boxes.observers.change.ChangeObserver;
import alex.mojaki.boxes.View;
import alex.mojaki.boxes.observers.get.GetObserver;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.*;

import static alex.mojaki.boxes.Boxes.box;
import static junit.framework.TestCase.assertSame;
import static org.junit.Assert.assertEquals;

public class ViewTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private class Product extends View<Integer> {

        private final PowerBox<Integer> x;
        private final PowerBox<Integer> y;

        public Product(PowerBox<Integer> x, PowerBox<Integer> y) {
            super(ViewTest.class, "product", x, y);
            this.x = x;
            this.y = y;
        }

        @Override
        public Integer calculate() {
            Integer x = this.x.get();
            Integer y = this.y.get();
            if (x != null && y != null) {
                return x * y;
            }
            return null;
        }
    }

    @Test
    public void testCaching() {
        final PowerBox<List<Integer>> list = new CommonBox<List<Integer>>(ViewTest.class, "list");
        final PowerBox<Integer> i = new CommonBox<Integer>(ViewTest.class, "i");
        View<String> stringView = new View<String>(BoxFamily.getInstance(ViewTest.class, "cachedView"),
                list, i) {
            @Override
            public String calculate() {
                return String.format("list = %s, i = %s", list, i);
            }
        };

        list.set(new ArrayList<Integer>(Arrays.asList(1, 2, 3)));
        i.set(0);
        String result = "list = [1, 2, 3], i = 0";
        assertEquals(result, stringView.get());

        // Changes that the view can't see don't affect its value
        list.get().add(4);
        list.get().add(5);
        assertEquals(result, stringView.get());

        // But directly setting the box still updates everything
        i.set(9);
        result = "list = [1, 2, 3, 4, 5], i = 9";
        assertEquals(result, stringView.get());
    }

    @Test
    public void testMultipleViewsForBox() {
        List<PowerBox<Integer>> boxes = new ArrayList<PowerBox<Integer>>();
        boxes.add(new CommonBox<Integer>(ViewTest.class, "x"));
        boxes.add(new CommonBox<Integer>(ViewTest.class, "y"));
        boxes.add(new CommonBox<Integer>(ViewTest.class, "x"));

        final Set<Integer> result = new HashSet<Integer>();
        ChangeObserver<Integer> viewObserver = new ChangeObserver<Integer>() {
            @Override
            public void onChange(PowerBox<Integer> box, Integer originalValue, Integer finalValue, Integer requestedValue) {
                if (finalValue != null) {
                    result.add(finalValue);
                }
            }
        };

        for (PowerBox<Integer> x : boxes) {
            for (PowerBox<Integer> y : boxes) {
                new Product(x, y).addChangeObserver(viewObserver);
            }
        }

        boxes.get(0).set(2);
        boxes.get(1).set(3);
        boxes.get(2).set(5);

        assertEquals(new HashSet<Integer>(Arrays.asList(4, 6, 10, 9, 15, 25)), result);
    }

    @Test
    public void setUnsupported() {
        View<String> view = new View<String>(ViewTest.class, "set") {
            @Override
            public String calculate() {
                return null;
            }
        };
        exception.expect(UnsupportedOperationException.class);
        view.set("");
    }

    @Test
    public void remembersPreviousValue() {
        final List<Integer> diffs = new ArrayList<Integer>();
        final PowerBox<Integer> x = box(BoxFamily.getInstance(ViewTest.class, "x"));
        new View<Integer>(ViewTest.class, "previous", x) {
            @Override
            public Integer calculate() {
                return x.get() * x.get();
            }
        }.addChangeObserver(new ChangeObserver<Integer>() {
            @Override
            public void onChange(PowerBox<Integer> box, Integer originalValue, Integer finalValue, Integer requestedValue) {
                assertSame(finalValue, requestedValue);
                if (originalValue != null) {
                    diffs.add(finalValue - originalValue);
                } else {
                    assertEquals(1, (int) finalValue);
                }
            }
        });
        x.set(1);
        x.set(2);
        x.set(3);
        x.set(4);
        assertEquals(Arrays.asList(3, 5, 7), diffs);
    }

    @Test
    public void testChain() {
        final List<Integer> getList = new ArrayList<Integer>();
        final PowerBox<Integer> x = box(BoxFamily.getInstance(ViewTest.class, "x"));
        final View<Integer> view1 = new View<Integer>(ViewTest.class, "chain1", x) {
            @Override
            public Integer calculate() {
                return x.get() + 1;
            }
        }.addGetMiddleware(new GetMiddleware<Integer>() {

            @Override
            public Integer onGet(PowerBox<Integer> box, Integer originalValue, Integer currentValue) {
                return currentValue + 2;
            }
        }).addGetObserver(new GetObserver() {
            @Override
            public void onGet(PowerBox box, Object originalValue, Object finalValue) {
                getList.add(1);
            }
        });
        final View<Integer> view2 = new View<Integer>(ViewTest.class, "chain2", view1) {
            @Override
            public Integer calculate() {
                return view1.get() + 3;
            }
        };
        x.set(4);
        assertEquals(0, getList.size());
        assertEquals(10, (int) view2.get());
        assertEquals(1, getList.size());
    }

}

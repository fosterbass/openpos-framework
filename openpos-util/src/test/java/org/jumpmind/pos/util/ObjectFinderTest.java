package org.jumpmind.pos.util;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.Assert.*;

public class ObjectFinderTest {
    interface Container {
    }

    static class MemberContainer implements Container {
        Target target;

        MemberContainer(Target target) {
            this.target = target;
        }
    }

    static class CollectionContainer implements Container {
        Collection<Target> targets;

        CollectionContainer(Collection<Target> targets) {
            this.targets = targets;
        }
    }

    static class ArrayContainer implements Container {
        Target[] targets;

        ArrayContainer(Target[] targets) {
            this.targets = targets;
        }
    }

    static class MapContainer implements Container {
        Map<String, Target> targets;

        MapContainer(Map<String, Target> targets) {
            this.targets = targets;
        }
    }

    static class NestedContainer implements Container {
        Container nested;

        NestedContainer(Container nested) {
            this.nested = nested;
        }
    }

    static class ComplexContainer extends NestedContainer {
        Target target;

        ComplexContainer(Target target, Container nested) {
            super(nested);
            this.target = target;
        }
    }

    static class CyclicalContainer implements Container {
        Target target;

        Container container;

        CyclicalContainer(Target target) {
            this.target = target;
            container = this; // self-referent
        }
    }

    static class Target {
        String name;

        Target(String name) {
            this.name = name;
        }
    }

    static class DerivedTarget extends Target {
        DerivedTarget(String name) {
            super(name);
        }
    }

    static class Visited implements ObjectSearchVisitor {
        Object parent;
        Object found;
        Field field;

        @Override
        public void visit(Object parentObject, Object object, Field location) {
            parent = parentObject;
            found = object;
            field = location;
        }

        void assertMatchesInChildCollection(Container container, String collectionFieldName, Object target) throws NoSuchFieldException, IllegalAccessException {
            Field f = container.getClass().getDeclaredField(collectionFieldName);
            f.setAccessible(true);
            Object o = f.get(container);

            assertSame(o, parent);
            assertEquals(f, field);
            assertEquals(target, found);
        }

        void assertMatches(Container container, String fieldName) throws NoSuchFieldException, IllegalAccessException {
            Field f = container.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            Object o = f.get(container);

            assertSame(container, parent);
            assertEquals(f, field);
            assertEquals(o, found);
        }
    }

    @Test
    public void findsTargetInImmediateMember() {
        Target expectedTarget = new Target("target");
        Container root = new MemberContainer(expectedTarget);

        ObjectFinder<Target> finder = new ObjectFinder<>(Target.class);
        finder.searchRecursive(root);

        List<Target> results = finder.getResults();
        assertEquals(1, results.size());
        assertEquals(expectedTarget, results.get(0));
    }

    @Test
    public void visitsTargetInImmediateMember() throws NoSuchFieldException, IllegalAccessException {
        Target expectedTarget = new Target("target");
        Container root = new MemberContainer(expectedTarget);

        Visited visitor = new Visited();

        ObjectFinder<Target> finder = new ObjectFinder<>(Target.class);
        finder.searchRecursive(root, visitor);

        visitor.assertMatches(root, "target");
    }

    @Test
    public void findsTargetInNestedMember() {
        Target expectedTarget = new Target("target");
        Container container = new MemberContainer(expectedTarget);
        Container root = new NestedContainer(container);

        ObjectFinder<Target> finder = new ObjectFinder<>(Target.class);
        finder.searchRecursive(root);

        List<Target> results = finder.getResults();
        assertEquals(1, results.size());
        assertEquals(expectedTarget, results.get(0));
    }

    @Test
    public void visitsTargetInNestedMember() throws NoSuchFieldException, IllegalAccessException {
        Target expectedTarget = new Target("target");
        Container container = new MemberContainer(expectedTarget);
        Container root = new NestedContainer(container);

        Visited visitor = new Visited();

        ObjectFinder<Target> finder = new ObjectFinder<>(Target.class);
        finder.searchRecursive(root, visitor);

        visitor.assertMatches(container, "target");
    }

    @Test
    public void findsTargetInListMember() {
        Target expectedTarget = new Target("target");
        Container root = new CollectionContainer(Collections.singletonList(expectedTarget));

        ObjectFinder<Target> finder = new ObjectFinder<>(Target.class);
        finder.searchRecursive(root);

        List<Target> results = finder.getResults();
        assertEquals(1, results.size());
        assertEquals(expectedTarget, results.get(0));
    }

    @Test
    public void visitsTargetInListMember() throws NoSuchFieldException, IllegalAccessException {
        Target target = new Target("target");
        List<Target> expectedList = Collections.singletonList(target);
        Container root = new CollectionContainer(expectedList);

        Visited visitor = new Visited();

        ObjectFinder<Target> finder = new ObjectFinder<>(Target.class);
        finder.searchRecursive(root, visitor);

        visitor.assertMatchesInChildCollection(root, "targets", target);
    }

    @Test
    public void findsTargetInNonListCollectionMember() {
        Target expectedTarget = new Target("target");
        Container root = new CollectionContainer(Collections.singleton(expectedTarget));

        ObjectFinder<Target> finder = new ObjectFinder<>(Target.class);
        finder.searchRecursive(root);

        List<Target> results = finder.getResults();
        assertEquals(1, results.size());
        assertEquals(expectedTarget, results.get(0));
    }

    @Test
    public void visitsTargetInNonListCollectionMember() throws NoSuchFieldException, IllegalAccessException {
        Target target = new Target("target");
        Collection<Target> expectedCollection = Collections.singleton(target);
        Container root = new CollectionContainer(expectedCollection);

        Visited visitor = new Visited();

        ObjectFinder<Target> finder = new ObjectFinder<>(Target.class);
        finder.searchRecursive(root, visitor);
        visitor.assertMatchesInChildCollection(root, "targets", target);
    }

    @Test
    public void findsTargetInArrayMember() {
        Target expectedTarget = new Target("target");
        Container root = new ArrayContainer(new Target[]{expectedTarget});

        ObjectFinder<Target> finder = new ObjectFinder<>(Target.class);
        finder.searchRecursive(root);

        List<Target> results = finder.getResults();
        assertEquals(1, results.size());
        assertEquals(expectedTarget, results.get(0));
    }

    @Test
    public void visitsTargetInArrayMember() throws NoSuchFieldException, IllegalAccessException {
        Target target = new Target("target");
        Target[] expectedArray = new Target[]{target};
        Container root = new ArrayContainer(expectedArray);

        Visited visitor = new Visited();

        ObjectFinder<Target> finder = new ObjectFinder<>(Target.class);
        finder.searchRecursive(root, visitor);

        visitor.assertMatchesInChildCollection(root, "targets", target);
    }

    @Test
    public void findsTargetInMapMember() {
        Target expectedTarget = new Target("target");
        Container root = new MapContainer(Collections.singletonMap("test", expectedTarget));

        ObjectFinder<Target> finder = new ObjectFinder<>(Target.class);
        finder.searchRecursive(root);

        List<Target> results = finder.getResults();
        assertEquals(1, results.size());
        assertEquals(expectedTarget, results.get(0));
    }

    @Test
    public void visitsTargetInMapMember() throws NoSuchFieldException, IllegalAccessException {
        Target target = new Target("target");
        Map<String, Target> expectedMap = Collections.singletonMap("test", target);
        Container root = new MapContainer(expectedMap);

        Visited visitor = new Visited();

        ObjectFinder<Target> finder = new ObjectFinder<>(Target.class);
        finder.searchRecursive(root, visitor);

        visitor.assertMatchesInChildCollection(root, "targets", target);
    }

    @Test
    public void findsDeeplyNestedCollectionItemTarget() {
        Target expectedTarget = new Target("target");
        Container container = new CollectionContainer(Collections.singleton(expectedTarget));
        Container root = new NestedContainer(container);

        ObjectFinder<Target> finder = new ObjectFinder<>(Target.class);
        finder.searchRecursive(root);

        List<Target> results = finder.getResults();
        assertEquals(1, results.size());
        assertEquals(expectedTarget, results.get(0));
    }

    @Test
    public void visitsDeeplyNestedCollectionItemTarget() throws NoSuchFieldException, IllegalAccessException {
        Target target = new Target("target");
        Set<Target> expectedSet = Collections.singleton(target);
        Container container = new CollectionContainer(expectedSet);
        Container root = new NestedContainer(container);

        Visited visitor = new Visited();

        ObjectFinder<Target> finder = new ObjectFinder<>(Target.class);
        finder.searchRecursive(root, visitor);

        visitor.assertMatchesInChildCollection(container, "targets", target);
    }

    @Test
    public void findsDistinctResultsByDefault() {
        Target target = new Target("target");
        List<Target> duplicateTargets = new ArrayList<>();
        duplicateTargets.add(target);
        duplicateTargets.add(target);
        Container root = new CollectionContainer(duplicateTargets);

        ObjectFinder<Target> finder = new ObjectFinder<>(Target.class);
        finder.searchRecursive(root);

        List<Target> results = finder.getResults();
        assertEquals(1, results.size());
        assertEquals(target, results.get(0));
    }

    @Test
    public void canFindDerivedTargetType() {
        Target expectedTarget = new DerivedTarget("derived-target");
        Container root = new MemberContainer(expectedTarget);

        ObjectFinder<Target> finder = new ObjectFinder<>(Target.class);
        finder.searchRecursive(root);

        List<Target> results = finder.getResults();
        assertEquals(1, results.size());
        assertEquals(expectedTarget, results.get(0));
    }

    @Test
    public void canFindDuplicateResultsByConfiguration() {
        Target target = new Target("target");
        List<Target> duplicateTargets = new ArrayList<>();
        duplicateTargets.add(target);
        duplicateTargets.add(target);
        Container root = new CollectionContainer(duplicateTargets);

        ObjectFinder<Target> finder = new ObjectFinder<>(Target.class, false);
        finder.searchRecursive(root);

        List<Target> results = finder.getResults();
        assertEquals(2, results.size());
        assertEquals(target, results.get(0));
        assertEquals(target, results.get(1));
    }

    @Test
    public void canFindTargetsAtMultipleLevels() {
        Target target1 = new Target("target");
        Target target2 = new DerivedTarget("derived-target");
        Container root = new ComplexContainer(target1, new CollectionContainer(Collections.singleton(target2)));

        ObjectFinder<Target> finder = new ObjectFinder<>(Target.class);
        finder.searchRecursive(root);

        List<Target> results = finder.getResults();
        assertEquals(2, results.size());
        assertTrue(results.contains(target1));
        assertTrue(results.contains(target2));
    }

    /* WARNING: If this test fails, the JVM will likely die with StackOverflowError */
    @Test
    public void avoidsInfiniteRecursionForCyclicalObjectGraphs() {
        Target expectedTarget = new Target("target");
        Container root = new CyclicalContainer(expectedTarget);

        ObjectFinder<Target> finder = new ObjectFinder<>(Target.class);
        finder.searchRecursive(root);

        List<Target> results = finder.getResults();
        assertEquals(1, results.size());
        assertEquals(expectedTarget, results.get(0));
    }
}
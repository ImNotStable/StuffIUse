package me.jeremiah.data;

import me.jeremiah.data.storage.CompleteTestDatabaseObject;
import me.jeremiah.data.storage.TestDatabaseObject;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TestData {

  public static final int ENTRY_COUNT = 10_000;

  public static final Collection<TestDatabaseObject> TEST_OBJECTS = IntStream.range(0, TestData.ENTRY_COUNT)
    .mapToObj(TestDatabaseObject::new)
    .collect(Collectors.toUnmodifiableSet());

  public static final Collection<CompleteTestDatabaseObject> COMPLETE_TEST_OBJECTS = IntStream.range(0, TestData.ENTRY_COUNT)
    .mapToObj(CompleteTestDatabaseObject::new)
    .collect(Collectors.toUnmodifiableSet());

}

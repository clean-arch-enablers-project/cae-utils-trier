# ☕ cae-utils-trier
Welcome to the repository for the open source CAE Trier library!

### ▶️ The artifact
```xml
<dependency>
  <groupId>com.clean-arch-enablers</groupId>
  <artifactId>trier</artifactId>
  <version>${version}</version>
</dependency>
```

### 💡 The concept

The Trier component does the work of a try-catch with some specifics:

![image](https://github.com/clean-arch-enablers-project/cae-utils-trier/assets/60593328/0c942aae-c57c-4707-b3d2-c33390a2861b)

If the exception being thrown during a Trier execution extends the type MappedException, the Trier will consider it as part of the expected flow that you've designed, so it will let it go. On the other hand, if the exception does not extend the MappedException type, the Trier will consider it a breach and catch it, passing it to the parameterized handler specified at the Trier instantiation phase.

The Trier can wrap 4 types of Action:

- FunctionAction: has Input and Output
- ConsumerAction: has only Input
- SupplierAction: has only Output
- RunnableAction: has no Input nor Output

```java
//Trier on a Function Action (has both Input and Output)
var output = Trier.of(TrierDemo::someFunctionAction, "the input here")
        .setHandlerForUnexpectedException(TrierDemo::handleUnexpectedException)
        .finishAndExecuteAction();
```

```java
//Trier on a Consumer Action (has only Input)
Trier.of(TrierDemo::someConsumerAction, "the input here")
        .setHandlerForUnexpectedException(TrierDemo::handleUnexpectedException)
        .finishAndExecuteAction();
```

```java
//Trier on a Supplier Action (has only Output)
var output = Trier.of(TrierDemo::someSupplierAction)
        .setHandlerForUnexpectedException(TrierDemo::handleUnexpectedException)
        .finishAndExecuteAction();
```

```java
//Trier on a Runnable Action (no I/O)
Trier.of(TrierDemo::someRunnableAction)
        .setHandlerForUnexpectedException(TrierDemo::handleUnexpectedException)
        .finishAndExecuteAction();
```

The current contract for the ```UnexpectedExceptionHandler``` is to receive an instance of ```Exception``` and to return an instance of ```MappedException```.

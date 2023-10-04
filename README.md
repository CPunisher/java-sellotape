# java-sellotape

> CLI with utils for java projects

## Usage

```shell
java -jar java-sellotape-1.0-jar-with-dependencies.jar

Usage: todolify [-o=<output>] [-m=<methodNames>[,<methodNames>...]]... PROJECT
      PROJECT
  -m, --methods=<methodNames>[,<methodNames>...]

  -o, --output=<output>
```

### Todolify

For example, let's search for the method with the full qualified name `pkg.Test.f` and `Test.g`, replacing the method body with `// TODO` in the **TestProject**

The default modified output project will be **TestProject-TODOLIFY**

```shell
java -jar java-sellotape-1.0-jar-with-dependencies.jar \
  TestProject \
  -m pkg.Test.f,Test.g
```

### Hydrate

For example, let's remove all method bodies of all classes in the **TestProject**, reserving their JavaDocs.

The default modified output project will be **TestProject-HYDRATE**

```shell
java -jar java-sellotape-1.0-jar-with-dependencies.jar \
  TestProject \
  --doc
```

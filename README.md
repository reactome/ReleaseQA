# ReleaseQA
A collection of tools to perform QA checks during the Release process.

### QA checks to run against the slice database

* ReactionlikeEvent_Species_Chimeric: Check the usage of isChimeric and species assignment
* FailedReaction_NormalReaction_Output: FailedReaction should have normalReaction filled and no output
* New_Event_QA: Check newly released Events for several related issues, which should output if any.
* PhysicalEntity_Container_Species: Make sure the usage of species is correct between container PE (e.g. Complex or EntitySet) and its contained PEs.
* ReactionlikeEventDiseaseCheck: Non-FailedReaction RLEs with normalReaction value but missing a disease value.
* SimpleEntity_With_Species: Get a list of SimpleEntity having no-null species.
* Species_in_Preceding_Event: Make sure two Events linked by preceding/following relationship have shared species in species or relatedSpecis slot.
* StableIdentifier_Identifier: Make sure StableIdentifiers don't have empty or duplicated identifier values, and a StableIdentifier instance is not used by more than one Instance. 
* OrphanEvents: Make sure human events are contained in the pathway hierarchy.

### QA checks to run against the release database

* CompareSpeciesbyclasses: This check will compare the current release database to the previous release database (for example: `test_reactome_65` and `test_reactome_64`) and compare the number of instances of each class type. When the percent difference is > 10% (where the actual difference is > 1000), the difference will be highlighted as `*** Difference is N% ***` in the output report.
This QA check requires additional config settings in `auth.properties` to connect to the previous release database. These properties are: `altDbHost`, `altDbName`, `altDbUser`, `altDbPwd`.
* FindMismatchedStableIdentifierVersions: This check will find Stable Identifiers that are present in two databases, but have mismatched version numbers.
This QA check requires additional config settings in `auth.properties` to connect to the previous release database. These properties are: `altDbHost`, `altDbName`, `altDbUser`, `altDbPwd`. 

### Running the code
You can run the checks like this:
```
java -Xmx8G -jar release-qa-0.1.0-jar-with-dependencies.jar
```
This will run a set of QAs for release. Before running the above, make sure you have edited
`resources/auth.properties` to provide required database connection information. All outputs
will be generated in the output folder.

You can run a specific check like this:
```
java -Xmx8G -jar release-qa-0.1.0-jar-with-dependencies.jar [CHECK]
```
where `CHECK` is the QA check simple class name without file extension.

To use a skip list for a QA check, place a file containing the DB ids of the instances to omit
in a `resources` folder file named _NAME_`.txt`, where _NAME_ is the check display name.
The display name is the generated report file name without directory or extension.
This display name is the report file heading with each space replaced by an underscore.

The QA checks to run can be filtered by two `resources` files:

* `IncludedQAChecks.txt`: the checks to include
* `ExcludedQAChecks.txt`: the checks to exclude

The includes and excludes consist of the QA class simple file name without extension.
The QA class can be the instantiable class or an annotation. For example, the following
lists:
```
IncludedQAChecks.txt:
SliceQACheck
GraphQACheck

ExcludedQAChecks.txt:
DatabaseObjectWithoutCreatedCheck
PhysicalEntitiesWithMoreThanOneCompartmentCheck
```
performs all QA checks annotated by `@SliceQACheck` or `@GraphQACheck` except the two
excluded checks. Adding `ReleaseQACheck` to the included list will run the QA checks with
the annotation `@ReleaseQACheck` as well. By default, all QA checks are included.

### Additional notes added by Guanming:

* Contributors check has been removed. It should be regarded as release report.
* unreleased_instances_with_released_stable_id: This should be regarded as release report.
  It has not been ported into Java.

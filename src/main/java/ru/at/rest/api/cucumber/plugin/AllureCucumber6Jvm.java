package ru.at.rest.api.cucumber.plugin;

import io.cucumber.messages.Messages.GherkinDocument.Feature;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario.Examples;
import io.cucumber.messages.Messages.GherkinDocument.Feature.TableRow;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.*;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.Step;
import io.qameta.allure.cucumber6jvm.testsourcemodel.TestSourcesModelProxy;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.*;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static io.qameta.allure.util.ResultsUtils.*;
import static ru.at.rest.api.cucumber.ScopedVariables.resolveVars;
import static ru.at.rest.api.utils.PropertyLoader.loadProperty;
import static ru.at.rest.api.utils.PropertyLoader.loadValuePropertyOrVariableOrDefault;

@SuppressWarnings({
        "ClassDataAbstractionCoupling",
        "ClassFanOutComplexity",
        "PMD.ExcessiveImports",
        "PMD.GodClass",
})
public class AllureCucumber6Jvm implements ConcurrentEventListener {

    private final AllureLifecycle lifecycle;

    private final ConcurrentHashMap<String, String> scenarioUuids = new ConcurrentHashMap<>();
    private final TestSourcesModelProxy testSources = new TestSourcesModelProxy();

    private final ThreadLocal<Feature> currentFeature = new InheritableThreadLocal<>();
    private final ThreadLocal<URI> currentFeatureFile = new InheritableThreadLocal<>();
    private final ThreadLocal<TestCase> currentTestCase = new InheritableThreadLocal<>();
    private final ThreadLocal<String> currentContainer = new InheritableThreadLocal<>();
    private final ThreadLocal<Boolean> forbidTestCaseStatusChange = new InheritableThreadLocal<>();

    private final EventHandler<TestSourceRead> featureStartedHandler = this::handleFeatureStartedHandler;
    private final EventHandler<TestCaseStarted> caseStartedHandler = this::handleTestCaseStarted;
    private final EventHandler<TestCaseFinished> caseFinishedHandler = this::handleTestCaseFinished;
    private final EventHandler<TestStepStarted> stepStartedHandler = this::handleTestStepStarted;
    private final EventHandler<TestStepFinished> stepFinishedHandler = this::handleTestStepFinished;
    private final EventHandler<WriteEvent> writeEventHandler = this::handleWriteEvent;
    private final EventHandler<EmbedEvent> embedEventHandler = this::handleEmbedEvent;

    private static final String TXT_EXTENSION = ".txt";
    private static final String TEXT_PLAIN = "text/plain";

    @SuppressWarnings("unused")
    public AllureCucumber6Jvm() {
        this(Allure.getLifecycle());
    }

    public AllureCucumber6Jvm(final AllureLifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }

    /*
    ########################################################################################
    #################################### Event Handlers ####################################
    ########################################################################################
     */
    @Override
    public void setEventPublisher(final EventPublisher publisher) {
        publisher.registerHandlerFor(TestSourceRead.class, featureStartedHandler);

        publisher.registerHandlerFor(TestCaseStarted.class, caseStartedHandler);
        publisher.registerHandlerFor(TestCaseFinished.class, caseFinishedHandler);

        publisher.registerHandlerFor(TestStepStarted.class, stepStartedHandler);
        publisher.registerHandlerFor(TestStepFinished.class, stepFinishedHandler);

        publisher.registerHandlerFor(WriteEvent.class, writeEventHandler);
        publisher.registerHandlerFor(EmbedEvent.class, embedEventHandler);
    }

    private void handleFeatureStartedHandler(final TestSourceRead event) {
        testSources.addTestSourceReadEvent(event.getUri(), event);
    }

    private void handleTestCaseStarted(final TestCaseStarted event) {
        currentFeatureFile.set(event.getTestCase().getUri());
        currentFeature.set(testSources.getFeature(currentFeatureFile.get()));
        currentTestCase.set(event.getTestCase());
        currentContainer.set(UUID.randomUUID().toString());
        forbidTestCaseStatusChange.set(false);

        final Deque<String> tags = new LinkedList<>(currentTestCase.get().getTags());

        final Feature feature = currentFeature.get();
        final LabelBuilder labelBuilder = new LabelBuilder(feature, currentTestCase.get(), tags);

        final String name = currentTestCase.get().getName();
        final String featureName = feature.getName();

        final TestResult result = new TestResult()
                .setUuid(getTestCaseUuid(currentTestCase.get()))
                .setHistoryId(getHistoryId(currentTestCase.get()))
                .setFullName(featureName + ": " + name)
                .setName(name)
                .setLabels(labelBuilder.getScenarioLabels())
                .setLinks(labelBuilder.getScenarioLinks());

        final Scenario scenarioDefinition =
                testSources.getScenarioDefinition(currentFeatureFile.get(), currentTestCase.get().getLine());

        if (scenarioDefinition.getExamplesCount() > 0) {
            result.setParameters(
                    getExamplesAsParameters(scenarioDefinition, currentTestCase.get())
            );
        }

        final String description = Stream.of(feature.getDescription(), scenarioDefinition.getDescription())
                .filter(Objects::nonNull)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining("\n"));

        if (!description.isEmpty()) {
            result.setDescription(description);
        }

        final TestResultContainer resultContainer = new TestResultContainer()
                .setName(String.format("%s: %s", scenarioDefinition.getKeyword(), scenarioDefinition.getName()))
                .setUuid(getTestContainerUuid())
                .setChildren(Collections.singletonList(getTestCaseUuid(currentTestCase.get())));

        lifecycle.scheduleTestCase(result);
        lifecycle.startTestContainer(getTestContainerUuid(), resultContainer);
        lifecycle.startTestCase(getTestCaseUuid(currentTestCase.get()));
    }

    private void handleTestCaseFinished(final TestCaseFinished event) {

        final String uuid = getTestCaseUuid(event.getTestCase());
        final Optional<StatusDetails> details = getStatusDetails(event.getResult().getError());
        details.ifPresent(statusDetails -> lifecycle.updateTestCase(
                uuid,
                testResult -> testResult.setStatusDetails(statusDetails)
        ));
        lifecycle.stopTestCase(uuid);
        lifecycle.stopTestContainer(getTestContainerUuid());
        lifecycle.writeTestCase(uuid);
        lifecycle.writeTestContainer(getTestContainerUuid());
    }

    private void handleTestStepStarted(final TestStepStarted event) {
        if (event.getTestStep() instanceof PickleStepTestStep) {
            final PickleStepTestStep pickleStep = (PickleStepTestStep) event.getTestStep();
            final String stepKeyword = Optional.ofNullable(
                    testSources.getKeywordFromSource(currentFeatureFile.get(), pickleStep.getStep().getLine())
            ).orElse("UNDEFINED");

            final StepResult stepResult = new StepResult()
                    .setName(String.format("%s %s", stepKeyword, updateName(pickleStep.getStep().getText(), pickleStep.getDefinitionArgument())))
                    .setStart(System.currentTimeMillis());

            lifecycle.startStep(getTestCaseUuid(currentTestCase.get()), getStepUuid(pickleStep), stepResult);

            final StepArgument stepArgument = pickleStep.getStep().getArgument();
            if (stepArgument instanceof DataTableArgument) {
                final DataTableArgument dataTableArgument = (DataTableArgument) stepArgument;
                createDataTableAttachment(lifecycle, "Data table", dataTableArgument);
            }
        } else if (event.getTestStep() instanceof HookTestStep) {
            initHook((HookTestStep) event.getTestStep());
        }
    }

    private String updateName(String name, List<Argument> arguments) {
        name = name.replaceAll("\"[^\"]*\"", "PARAMETER");
        String[] split = name.split("PARAMETER");
        StringBuilder updateName = new StringBuilder();
        List<String> stringArgs = arguments.stream()
                .filter(arg-> arg.getParameterTypeName().equals("string"))
                .map(arg -> arg.getValue().replaceAll("\"",""))
                .collect(Collectors.toList());

        for (int i = 0; i < split.length; i++) {
            updateName.append(split[i]);
            if (i < stringArgs.size()) {
                updateName.append("\"")
                        .append(resolveVars(loadProperty(stringArgs.get(i), stringArgs.get(i))))
                        .append("\"");
            }
        }
        return updateName.toString();
    }

    private void initHook(final HookTestStep hook) {
        String name = getStepAnnotation(hook.getCodeLocation());
        final FixtureResult hookResult = new FixtureResult()
                .setName(name)
                .setStart(System.currentTimeMillis());

        if (hook.getHookType() == HookType.BEFORE) {
            lifecycle.startPrepareFixture(getTestContainerUuid(), getHookStepUuid(hook), hookResult);
        } else {
            lifecycle.startTearDownFixture(getTestContainerUuid(), getHookStepUuid(hook), hookResult);
        }
    }

    private String getStepAnnotation(String hookLocation) {
        String result = null;
        try {
            Method[] methods = Class.forName(getCMFromHookLocation(hookLocation, true)).getMethods();
            Method method = null;
            String methodName = getCMFromHookLocation(hookLocation, false);
            for (Method m:methods) {
                if(m.getName().equals(methodName)) {
                    method = m;
                    break;
                }
            }
            Step a = Objects.requireNonNull(method).getAnnotation(Step.class);
            result = a.value();
        } catch (ClassNotFoundException|NullPointerException ignored) {}
        return result == null ? hookLocation : result;
    }

    private String getCMFromHookLocation(String hookLocation, boolean getClass) {
        String path = hookLocation.replaceAll("\\(.*\\)", "");
        int lastDot = path.lastIndexOf('.');
        return getClass ? path.substring(0, lastDot) : path.substring(lastDot + 1);
    }

    private void handleTestStepFinished(final TestStepFinished event) {
        if (event.getTestStep() instanceof HookTestStep) {
            handleHookStep(event);
        } else {
            handlePickleStep(event);
        }
    }

    private void handleWriteEvent(final WriteEvent event) {
        lifecycle.addAttachment(
                "Text output",
                TEXT_PLAIN,
                TXT_EXTENSION,
                Objects.toString(event.getText()).getBytes(StandardCharsets.UTF_8)
        );
    }

    private void handleEmbedEvent(final EmbedEvent event) {
        lifecycle.addAttachment(event.name, event.getMediaType(), null, new ByteArrayInputStream(event.getData()));
    }

    /*
    ########################################################################################
    ################################### Utility Methods ####################################
    ########################################################################################
     */

    private String getTestContainerUuid() {
        return currentContainer.get();
    }

    private String getTestCaseUuid(final TestCase testCase) {
        return scenarioUuids.computeIfAbsent(getHistoryId(testCase), it -> UUID.randomUUID().toString());
    }

    private String getStepUuid(final PickleStepTestStep step) {
        return currentFeature.get().getName() + getTestCaseUuid(currentTestCase.get())
                + step.getStep().getText() + step.getStep().getLine();
    }

    private String getHookStepUuid(final HookTestStep step) {
        return currentFeature.get().getName() + getTestCaseUuid(currentTestCase.get())
                + step.getHookType().toString() + step.getCodeLocation();
    }

    private String getHistoryId(final TestCase testCase) {
        final String testCaseLocation = testCase.getUri().toString()
                .substring(testCase.getUri().toString().lastIndexOf('/') + 1)
                + ":" + testCase.getLine();
        return md5(testCaseLocation);
    }

    private Status translateTestCaseStatus(final Result testCaseResult) {
        switch (testCaseResult.getStatus()) {
            case FAILED:
                return getStatus(testCaseResult.getError())
                        .orElse(Status.FAILED);
            case PASSED:
                return Status.PASSED;
            case SKIPPED:
            case PENDING:
                return Status.SKIPPED;
            case AMBIGUOUS:
            case UNDEFINED:
            default:
                return null;
        }
    }

    private List<Parameter> getExamplesAsParameters(
            final Scenario scenario, final TestCase localCurrentTestCase
    ) {
        final Optional<Examples> examplesBlock =
                scenario.getExamplesList().stream()
                        .filter(example -> example.getTableBodyList().stream()
                                .anyMatch(row -> row.getLocation().getLine() == localCurrentTestCase.getLine())
                        ).findFirst();

        if (examplesBlock.isPresent()) {
            final TableRow row = examplesBlock.get().getTableBodyList().stream()
                    .filter(example -> example.getLocation().getLine() == localCurrentTestCase.getLine())
                    .findFirst().get();
            return IntStream.range(0, examplesBlock.get().getTableHeader().getCellsList().size()).mapToObj(index -> {
                final String name = examplesBlock.get().getTableHeader().getCellsList().get(index).getValue();
                final String value = row.getCellsList().get(index).getValue();
                return createParameter(name, value);
            }).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    public static void createDataTableAttachment(AllureLifecycle lifecycle, String name, final DataTableArgument dataTableArgument) {
        final List<List<String>> rowsInTable = dataTableArgument.cells();
        final StringBuilder dataTableCsv = new StringBuilder();
        for (List<String> columns : rowsInTable) {
            if (!columns.isEmpty()) {
                for (int i = 0; i < columns.size(); i++) {
                    if (i == columns.size() - 1) {
                        dataTableCsv.append(resolveVars(loadValuePropertyOrVariableOrDefault(columns.get(i))));
                    } else {
                        dataTableCsv.append(columns.get(i));
                        dataTableCsv.append('\t');
                    }
                }
                dataTableCsv.append('\n');
            }
        }
        final String attachmentSource = lifecycle
                .prepareAttachment(name, "text/tab-separated-values", "csv");
        lifecycle.writeAttachment(attachmentSource,
                new ByteArrayInputStream(dataTableCsv.toString().getBytes(StandardCharsets.UTF_8)));
    }

    private void handleHookStep(final TestStepFinished event) {
        final HookTestStep hookStep = (HookTestStep) event.getTestStep();
        final String uuid = getHookStepUuid(hookStep);
        final FixtureResult fixtureResult = new FixtureResult().setStatus(translateTestCaseStatus(event.getResult()));

        if (!Status.PASSED.equals(fixtureResult.getStatus())) {
            final TestResult testResult = new TestResult().setStatus(translateTestCaseStatus(event.getResult()));
            final StatusDetails statusDetails = getStatusDetails(event.getResult().getError())
                    .orElseGet(StatusDetails::new);

            final String errorMessage = event.getResult().getError() == null ? hookStep.getHookType()
                    .name() + " is failed." : hookStep.getHookType()
                    .name() + " is failed: " + event.getResult().getError().getLocalizedMessage();
            statusDetails.setMessage(errorMessage);

            if (hookStep.getHookType() == HookType.BEFORE) {
                final TagParser tagParser = new TagParser(currentFeature.get(), currentTestCase.get());
                statusDetails
                        .setFlaky(tagParser.isFlaky())
                        .setMuted(tagParser.isMuted())
                        .setKnown(tagParser.isKnown());
                testResult.setStatus(Status.SKIPPED);
                updateTestCaseStatus(testResult.getStatus());
                forbidTestCaseStatusChange.set(true);
            } else {
                testResult.setStatus(Status.BROKEN);
                updateTestCaseStatus(testResult.getStatus());
            }
            fixtureResult.setStatusDetails(statusDetails);
        }

        lifecycle.updateFixture(uuid, result -> result.setStatus(fixtureResult.getStatus())
                .setStatusDetails(fixtureResult.getStatusDetails()));
        lifecycle.stopFixture(uuid);
    }

    private void handlePickleStep(final TestStepFinished event) {

        final Status stepStatus = translateTestCaseStatus(event.getResult());
        final StatusDetails statusDetails;
        if (event.getResult().getStatus() == io.cucumber.plugin.event.Status.UNDEFINED) {
            updateTestCaseStatus(Status.PASSED);

            statusDetails =
                    getStatusDetails(new IllegalStateException("Undefined Step. Please add step definition"))
                            .orElse(new StatusDetails());
            lifecycle.updateTestCase(getTestCaseUuid(currentTestCase.get()), scenarioResult ->
                    scenarioResult
                            .setStatusDetails(statusDetails));
        } else {
            statusDetails =
                    getStatusDetails(event.getResult().getError())
                            .orElse(new StatusDetails());
            updateTestCaseStatus(stepStatus);
        }

        if (!Status.PASSED.equals(stepStatus) && stepStatus != null) {
            forbidTestCaseStatusChange.set(true);
        }

        final TagParser tagParser = new TagParser(currentFeature.get(), currentTestCase.get());
        statusDetails
                .setFlaky(tagParser.isFlaky())
                .setMuted(tagParser.isMuted())
                .setKnown(tagParser.isKnown());

        lifecycle.updateStep(getStepUuid((PickleStepTestStep) event.getTestStep()),
                stepResult -> stepResult.setStatus(stepStatus).setStatusDetails(statusDetails));
        lifecycle.stopStep(getStepUuid((PickleStepTestStep) event.getTestStep()));
    }

    private void updateTestCaseStatus(final Status status) {
        if (!forbidTestCaseStatusChange.get()) {
            lifecycle.updateTestCase(getTestCaseUuid(currentTestCase.get()),
                    result -> result.setStatus(status));
        }
    }
}
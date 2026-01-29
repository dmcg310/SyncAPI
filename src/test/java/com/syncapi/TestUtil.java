package com.syncapi;

import com.syncapi.entity.environment.Environment;
import com.syncapi.entity.environment.variable.EnvironmentVariable;
import com.syncapi.entity.folder.Folder;
import com.syncapi.entity.request.Request;
import com.syncapi.entity.user.User;
import com.syncapi.entity.workspace.Workspace;
import com.syncapi.util.RequestMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.springframework.test.util.ReflectionTestUtils.setField;

/**
 * Utility class for generating random test data and creating test entities.
 */
public final class TestUtil {
    private TestUtil() {
    }

    /**
     * Generates a random Long ID.
     */
    public static Long generateRandomId() {
        return ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
    }

    /**
     * Generates a random integer.
     */
    public static int generateRandomInt() {
        return ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);
    }

    /**
     * Generates a random long.
     */
    public static long generateRandomLong() {
        return ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
    }

    /**
     * Generates a random email address.
     */
    public static String generateRandomEmail() {
        return "user-" + UUID.randomUUID() + "@test.com";
    }

    /**
     * Generates a random name.
     */
    public static String generateRandomName() {
        return "name-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Generates a random description.
     */
    public static String generateRandomDescription() {
        return "description-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Generates a random URL/link.
     */
    public static String generateRandomUrl() {
        return "https://api.example.com/" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Generates a random JWT-like token.
     */
    public static String generateRandomToken() {
        return "eyJ" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Generates a random secret key.
     */
    public static String generateRandomSecretKey() {
        return UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Generates a random password hash.
     */
    public static String generateRandomPasswordHash() {
        return "$2a$10$" + UUID.randomUUID().toString().replace("-", "").substring(0, 22);
    }

    /**
     * Generates a random password.
     */
    public static String generateRandomPassword() {
        return "Pass!" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Generates a random key for key-value pairs.
     */
    public static String generateRandomKey() {
        return "KEY_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Generates a random value for key-value pairs.
     */
    public static String generateRandomValue() {
        return "value-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Generates a random error message.
     */
    public static String generateRandomErrorMessage() {
        return "Error: " + UUID.randomUUID().toString().substring(0, 12);
    }

    /**
     * Generates a random RequestMethod enum value.
     */
    public static RequestMethod generateRandomRequestMethod() {
        RequestMethod[] methods = RequestMethod.values();
        int randomIndex = ThreadLocalRandom.current().nextInt(methods.length);

        return methods[randomIndex];
    }

    /**
     * Creates a Map with random key-value pairs.
     */
    public static Map<String, String> createRandomMap(int count) {
        return IntStream.range(0, count)
                .boxed()
                .collect(Collectors.toMap(i -> generateRandomKey(), i -> generateRandomValue()));
    }

    /**
     * Creates a User entity with the given parameters.
     */
    public static User createUser(Long id, String email, String name) {
        User user = new User(email, generateRandomPasswordHash(), name);
        setField(user, "id", id);

        return user;
    }

    /**
     * Creates a User entity with random values.
     */
    public static User createRandomUser() {
        return createUser(generateRandomId(), generateRandomEmail(), generateRandomName());
    }

    /**
     * Creates a Workspace entity with the given parameters.
     */
    public static Workspace createWorkspace(Long id, String name, User member) {
        Workspace workspace = new Workspace(name);
        setField(workspace, "id", id);
        workspace.setMembers(new ArrayList<>(List.of(member)));

        return workspace;
    }

    /**
     * Creates a Workspace entity with multiple members.
     */
    public static Workspace createWorkspaceWithMembers(Long id, String name, User... members) {
        Workspace workspace = new Workspace(name);
        setField(workspace, "id", id);
        workspace.setMembers(new ArrayList<>(List.of(members)));

        return workspace;
    }

    /**
     * Creates a Workspace entity with random values for a given member.
     */
    public static Workspace createRandomWorkspace(User member) {
        return createWorkspace(
                generateRandomId(),
                generateRandomName(),
                member
        );
    }

    /**
     * Creates a Workspace entity with random values.
     */
    public static Workspace createRandomWorkspace() {
        return createWorkspace(
                generateRandomId(),
                generateRandomName(),
                createRandomUser()
        );
    }

    /**
     * Creates a Folder entity with the given parameters.
     */
    public static Folder createFolder(Long id, String name, Workspace workspace) {
        Folder folder = new Folder(name, workspace);
        setField(folder, "id", id);
        folder.setRequests(new ArrayList<>());

        return folder;
    }

    /**
     * Creates a Folder entity with random values for a given workspace.
     */
    public static Folder createRandomFolder(Workspace workspace) {
        return createFolder(
                generateRandomId(),
                generateRandomName(),
                workspace
        );
    }

    /**
     * Creates an Environment entity with the given parameters.
     */
    public static Environment createEnvironment(Long id, String name, Workspace workspace) {
        Environment environment = new Environment(name, workspace);
        setField(environment, "id", id);

        return environment;
    }

    /**
     * Creates an Environment entity with random values for a given workspace.
     */
    public static Environment createRandomEnvironment(Workspace workspace) {
        return createEnvironment(
                generateRandomId(),
                generateRandomName(),
                workspace
        );
    }

    /**
     * Creates an EnvironmentVariable entity with the given parameters.
     */
    public static EnvironmentVariable createEnvironmentVariable(Long id, String key, String value,
                                                                Environment environment) {
        EnvironmentVariable variable = new EnvironmentVariable(key, value, environment);
        setField(variable, "id", id);

        return variable;
    }

    /**
     * Creates an EnvironmentVariable entity with random values for a given environment.
     */
    public static EnvironmentVariable createRandomEnvironmentVariable(Environment environment) {
        return createEnvironmentVariable(
                generateRandomId(),
                generateRandomKey(),
                generateRandomValue(),
                environment
        );
    }

    /**
     * Creates a Request entity with the given parameters.
     */
    public static Request createRequest(Long id, String name, RequestMethod method, String url, Folder folder) {
        Request request = new Request(name, method, url, folder);
        setField(request, "id", id);

        return request;
    }

    /**
     * Creates a Request entity with random values for a given folder.
     */
    public static Request createRandomRequest(Folder folder) {
        return createRequest(
                generateRandomId(),
                generateRandomName(),
                RequestMethod.GET,
                generateRandomUrl(),
                folder
        );
    }
}

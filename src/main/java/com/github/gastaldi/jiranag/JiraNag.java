package com.github.gastaldi.jiranag;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import io.quarkus.logging.Log;
import io.quarkus.mailer.MailTemplate.MailTemplateInstance;
import io.quarkus.qute.CheckedTemplate;
import picocli.CommandLine.Command;

import javax.inject.Inject;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Command(name = "jira-nag", mixinStandardHelpOptions = true)
public class JiraNag implements Runnable {
    @Inject
    JiraNagConfig config;

    @Override
    public void run() {
        JiraNagConfig.Jira jira = config.jira();
        final JiraRestClient restClient = new AsynchronousJiraRestClientFactory().create(
                jira.url(), new BearerHttpAuthenticationHandler(jira.token()));
        Set<User> users = getUsersWithIssues(restClient);
        // Email each user with their list of issues
        for (User user : users) {
            String jiraQueryPerUser = "assignee = '" + user.getName() + "' AND " + jira.query();
            Log.infof("Running: %s", jiraQueryPerUser);

            SearchResult searchResultsPerUser = restClient.getSearchClient().searchJql(jiraQueryPerUser).claim();
            Iterable<Issue> issues = searchResultsPerUser.getIssues();
            sendEmail(user, issues);
        }
    }

    private Set<User> getUsersWithIssues(JiraRestClient restClient) {
        JiraNagConfig.Jira jira = config.jira();
        Log.infof("Running: %s", jira.query());
        SearchResult searchResultsAll = restClient.getSearchClient().searchJql(jira.query()).claim();
        return StreamSupport.stream(searchResultsAll.getIssues().spliterator(), false)
                .map(Issue::getAssignee)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private void sendEmail(User user, Iterable<Issue> issues) {
        JiraNagConfig.Email email = config.email();
        MailTemplateInstance instance;
        switch (email.template()) {
            case REVIEW_ISSUES -> instance = Emails.reviewIssues(user, issues);
            default -> throw new IllegalArgumentException("Unsupported template: " + email.template());
        }
        instance
                .to(email.to().orElse(user.getEmailAddress()))
                .replyTo(email.replyTo())
                .subject(email.subject())
                .setAttribute("X-Mailer", "JIRA-Nag")
                .send().await().indefinitely();
    }

    @CheckedTemplate(basePath = "emails")
    static class Emails {
        public static native MailTemplateInstance reviewIssues(User user, Iterable<Issue> issues);
    }
}

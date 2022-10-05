package com.github.gastaldi.jiranag;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.github.gastaldi.jiranag.JiraNagConfig.Jira;
import io.quarkus.logging.Log;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.qute.Engine;
import io.quarkus.qute.TemplateInstance;
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

    @Inject
    Engine engine;

    @Inject
    Mailer mailer;

    @Override
    public void run() {
        Jira jira = config.jira();
        String jiraQuery = config.currentJiraQuery();
        if (jiraQuery == null) {
            throw new IllegalStateException("Profile " + config.profile() + " not found");
        }
        final JiraRestClient restClient = new AsynchronousJiraRestClientFactory().create(
                jira.url(), new BearerHttpAuthenticationHandler(jira.token()));
        Set<User> users = getUsersWithIssues(restClient);
        // Email each user with their list of issues
        for (User user : users) {
            String jiraQueryPerUser = "assignee = '" + user.getName() + "' AND " + jiraQuery;
            Log.infof("Running: %s", jiraQueryPerUser);

            SearchResult searchResultsPerUser = restClient.getSearchClient().searchJql(jiraQueryPerUser).claim();
            Iterable<Issue> issues = searchResultsPerUser.getIssues();
            sendEmail(user, issues);
        }
    }

    private Set<User> getUsersWithIssues(JiraRestClient restClient) {
        String jiraQuery = config.currentJiraQuery();
        Log.infof("Running: %s", jiraQuery);
        SearchResult searchResultsAll = restClient.getSearchClient().searchJql(jiraQuery).claim();
        return StreamSupport.stream(searchResultsAll.getIssues().spliterator(), false)
                .map(Issue::getAssignee)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private void sendEmail(User user, Iterable<Issue> issues) {
        JiraNagConfig.Email email = config.email();
        String profile = config.profile();
        TemplateInstance data = engine.getTemplate(profile).data("user", user).data("issues", issues);
        Mail mail = Mail.withHtml(email.to().orElse(user.getEmailAddress()), email.subject(), data.render());
        mail.addHeader("X-Mailer", "JIRA-Nag");
        mailer.send(mail);
    }

}

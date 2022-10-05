# Jira Nag

A CLI application that will nag your developers about their Jira tickets.


## Setup

1. Generate a Personal Access Token in JIRA (https://issues.redhat.com/secure/ViewProfile.jspa?selectedTab=com.atlassian.pats.pats-plugin:jira-user-personal-access-tokens)
2. Create an `.env` file in the root of this project with the following variables:
```dotenv
APP_JIRA_TOKEN=your-jira-token
```


## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw clean compile quarkus:dev
```


## Creating a new profile

A profile is a JIRA Query mapped to an email template.

To create a new profile:

1. Create the Qute template in the `emails/` directory. (eg. `reviewIssues.html`)
2. Include the JIRA query in the application.properties file.
```properties
app.profiles.reviewIssues=project = Quarkus AND fixVersion = 2.13-Fireball.GA AND status in ("To Do", "Analysis in Progress", "Ready For Dev") AND component in ("team/eng") AND assignee is not EMPTY ORDER BY key ASC
```

3. Run the application using the `app.profile` that is set to the name of the query (eg. `-Dapp.profile=reviewIssues`)